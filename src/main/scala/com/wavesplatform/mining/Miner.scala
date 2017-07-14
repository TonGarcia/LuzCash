package com.wavesplatform.mining

import java.time.{Duration, Instant}
import java.util.concurrent.atomic.AtomicBoolean

import com.wavesplatform.network._
import com.wavesplatform.settings.WavesSettings
import com.wavesplatform.state2.ByteStr
import com.wavesplatform.state2.reader.StateReader
import com.wavesplatform.{Coordinator, UtxPool}
import io.netty.channel.group.ChannelGroup
import monix.eval.Task
import monix.execution._
import scorex.account.PrivateKeyAccount
import scorex.block.Block
import scorex.consensus.nxt.NxtLikeConsensusBlockData
import scorex.transaction.PoSCalc._
import scorex.transaction.{BlockchainUpdater, CheckpointService, History}
import scorex.utils.{ScorexLogging, Time}
import scorex.wallet.Wallet

import scala.collection.concurrent.TrieMap
import scala.concurrent.duration._
import scala.math.Ordering.Implicits._

class Miner(
    allChannels: ChannelGroup,
    blockchainReadiness: AtomicBoolean,
    blockchainUpdater: BlockchainUpdater,
    checkpoint: CheckpointService,
    history: History,
    stateReader: StateReader,
    settings: WavesSettings,
    timeService: Time,
    utx: UtxPool,
    wallet: Wallet,
    startingLastBlock: Block) extends ScorexLogging {

  import Miner._

  private implicit val scheduler = Scheduler.fixedPool(name = "miner-pool", poolSize = 2)

  private def peerCount = allChannels.size()

  private val minerSettings = settings.minerSettings
  private val blockchainSettings = settings.blockchainSettings

  private val scheduledAttempts = TrieMap.empty[ByteStr, Cancelable]

  private def checkAge(parentHeight: Int, parent: Block): Either[String, Unit] =
    Either
      .cond(parentHeight == 1, (), Duration.between(Instant.ofEpochMilli(parent.timestamp), Instant.ofEpochMilli(timeService.correctedTime())))
      .left.flatMap(blockAge => Either.cond(blockAge <= minerSettings.intervalAfterLastBlockThenGenerationIsAllowed, (),
      s"BlockChain is too old (last block ${parent.uniqueId} generated $blockAge ago)"
    ))

  private def generateOneBlockTask(account: PrivateKeyAccount, parentHeight: Int, parent: Block,
                                   greatGrandParent: Option[Block], balance: Long)(delay: FiniteDuration): Task[Either[String, Block]] = Task {
    val pc = peerCount
    lazy val lastBlockKernelData = parent.consensusData
    lazy val currentTime = timeService.correctedTime()
    log.debug(s"${System.currentTimeMillis()}: Corrected time: $currentTime")
    lazy val h = calcHit(lastBlockKernelData, account)
    lazy val t = calcTarget(parent, currentTime, balance)
    for {
      _ <- Either.cond(pc >= minerSettings.quorum, (), s"Quorum not available ($pc/${minerSettings.quorum}, not forging block with ${account.address}")
      _ <- Either.cond(h < t, (), s"${System.currentTimeMillis()}: Hit $h was NOT less than target $t, not forging block with ${account.address}")
      _ = log.debug(s"Forging with ${account.address}, H $h < T $t, balance $balance, prev block ${parent.uniqueId}")
      _ = log.debug(s"Previous block ID ${parent.uniqueId} at $parentHeight with target ${lastBlockKernelData.baseTarget}")
      avgBlockDelay = blockchainSettings.genesisSettings.averageBlockDelay
      btg = calcBaseTarget(avgBlockDelay, parentHeight, parent, greatGrandParent, currentTime)
      gs = calcGeneratorSignature(lastBlockKernelData, account)
      consensusData = NxtLikeConsensusBlockData(btg, gs)
      unconfirmed = utx.packUnconfirmed()
      _ = log.debug(s"Adding ${unconfirmed.size} unconfirmed transaction(s) to new block")
      block = Block.buildAndSign(Version, currentTime, parent.uniqueId, consensusData, unconfirmed, account)
    } yield block
  }.delayExecution(delay)

  private def generateBlockTask(account: PrivateKeyAccount): Task[Unit] = Task {
    val height = history.height()
    val lastBlock = history.lastBlock.get
    val grandParent = history.parent(lastBlock, 2)
    (for {
      _ <- checkAge(height, lastBlock)
      ts <- nextBlockGenerationTime(height, stateReader, blockchainSettings.functionalitySettings, lastBlock, account)
      offset = calcOffset(timeService, ts)
      key = ByteStr(account.publicKey)
      _ = scheduledAttempts.get(key) match {
        case Some(_) => log.debug(s"Block generation already scheduled for $key")
        case None =>
          log.debug(s"${System.currentTimeMillis()}: Next attempt for acc=$account in $offset")
          val balance = generatingBalance(stateReader, blockchainSettings.functionalitySettings, account, height)
          val blockGenTask = generateOneBlockTask(account, height, lastBlock, grandParent, balance)(offset).flatMap {
            case Right(block) => Task {
              Coordinator.processBlock(checkpoint, history, blockchainUpdater, timeService, stateReader,
                utx, blockchainReadiness, Miner.this, settings)(block, local = true) match {
                case Left(err) => log.warn(err.toString)
                case Right(score) =>
                  allChannels.broadcast(LocalScoreChanged(score))
                  allChannels.broadcast(BlockForged(block))
              }
            }
            case Left(err) =>
              scheduledAttempts.remove(key).foreach(_.cancel())
              log.debug(s"No block generated because $err, retrying")
              generateBlockTask(account)
          }
          scheduledAttempts.put(key, blockGenTask.runAsync)
      }
    } yield ()).left.map(err => log.debug(s"NOT scheduling block generation: $err"))
  }

  def lastBlockChanged(): Unit = {
    log.debug(s"Miner notified of new block")
    scheduledAttempts.values.foreach(_.cancel)
    scheduledAttempts.clear()
    wallet.privateKeyAccounts().foreach(generateBlockTask(_).runAsync)
  }

  def shutdown(): Unit = ()
}

object Miner extends ScorexLogging {

  val Version: Byte = 2
  val MinimalGenerationOffsetMillis: Long = 1001

  def calcOffset(timeService: Time, calculatedTimestamp: Long): FiniteDuration = {
    val calculatedGenerationTimestamp = (Math.ceil(calculatedTimestamp / 1000.0) * 1000).toLong
    log.debug(s"CalculatedTS $calculatedTimestamp: CalculatedGenerationTS: $calculatedGenerationTimestamp")
    val calculatedOffset = calculatedGenerationTimestamp - timeService.correctedTime()
    Math.max(MinimalGenerationOffsetMillis, calculatedOffset).millis
  }
}