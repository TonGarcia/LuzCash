# Docker (docker hypothesis)


# Study (fork steps)

## PART 1
1. Git Fork
1. Rename Repo name
1. Replace original crypto name to new name (ex.: replace litecoin to learncoin):
   ```shell 
        find ./ -type f -readable -writable -exec sed -i "s/Litecoin/Learncoin/g" {} \; 
        find ./ -type f -readable -writable -exec sed -i "s/litecoin/learncoin/g" {} \;
        find ./ -type f -readable -writable -exec sed -i "s/litecoind/learncoind/g" {} \;
        find ./ -type f -readable -writable -exec sed -i "s/LTC/LERN/g" {} \;
   ``` 
1. Change genesis block (for testnet & mainnet):
    1. change pchMessageStart after 0x to be another protocol
1. Location of genesis block:
    1. Litecoin: genesisblock is at main.cpp
    1. Bitcoin: genesisblock is at chainparams
1. Change pubkey: scriptpubkey at main.cpp (for litecoin), for bitcoin it is a function, nothing to be done
1. Change genesis block message:
   ```c++
      const char* pszTimestamp = "In genesis god created the universe and gave free will to humanity, but the Government reoke it, lets get it back!";
   ```
1. Change epochtime to present time block.nTime = unix time stamp (easy to get it at www.epochconverter.com) and block.nNonce = 0 for mainnet & testnet
    1. Location Bitcoin: chainparams.cpp file at CreateGenesisBlock function call (CMainParams & CTestNetParams & SigNetParams & CRegTestParams)
1. Change consensus:
    1. consensus.nPowTargetSpacing at chainparams.cpp to how much time the algorithm mine the next block: 1*30 = 30seconds
    1. consensus.nPowTargetTimespan = 10 * 30; 5 minutes split

## PART 2
1. Open chainparams.cpp and remove records from checkpointData list (line 147), let just one record (testnet & mainnet): { 0, uint256S("0x")}
1. comment vSeeds.emplace_back records, let just one: vSeeds.emplace_back("localhost"); // localhost
1. Compile:
   ```shell
      cd src/qt
      make -f Makefile USE_UPNP=-
   ```

COMPILE BITCOIN: https://technology.finra.org/code/compile-run-customize-your-own-bitcoin-client.html
Copy depends folder from Tag Release
Preare files: ``` sudo chmod +x config.guess && sudo chmod +x config.site.in && sudo chmod +x config.sub ```
Command to compile: ``` sudo make -j4 HOST=x86_64-w64-mingw32 ```