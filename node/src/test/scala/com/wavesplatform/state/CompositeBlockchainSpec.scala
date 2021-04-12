package com.wavesplatform.state

import com.wavesplatform.state.reader.CompositeBlockchain
import com.wavesplatform.utils.EmptyBlockchain
import com.wavesplatform.BlockGen
import com.wavesplatform.test.FreeSpec

class CompositeBlockchainSpec extends FreeSpec with BlockGen {
  "blockHeaderAndSize at current height is last block" in forAll(randomSignerBlockGen) { block =>
    val comp = CompositeBlockchain(EmptyBlockchain, newBlock = Some(block))

    comp.blockHeader(comp.height).map(_.id()) should contain(block.id())
  }
}
