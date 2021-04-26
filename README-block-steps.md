1. Build Pack (generate .jar)
    ```bash
        sbt packageAll                   # Mainnet
        sbt -Dnetwork=testnet packageAll # Testnet
    ```
1. Complete run:
   ```bash
      sbt -Dwaves.it.max-parallel-suites=1 node-it/test
   ```
1. **docker**: runs a .jar already built
1. **node**: is the main (blockchain), "waves-sample.conf" is necessary
1. **node-it**: is the network
1. **node-generator**: generate new node
1. --> devnet.conf = config of devnet
1. --> Account.scala = sample of accounts (wallets)
1. fees: devnet.conf
1. conf: settings (com.wavesplatform.settings) , can use node-generator
1. docker: use already built image from dockerhub & only change the .jar 