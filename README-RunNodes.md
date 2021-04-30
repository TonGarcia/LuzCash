# Running Node (application.conf)
--> typenet = devnet, testnet, mainnet
1. Formas de Subir a rede:
    1. Wallet Setup for Node --> Gerar Wallet: ``` java -jar walletgenerator.jar -t -p -c ```
    1. Copiar a "seed_hash" e jogar em: ```application{wallet{seed{}}}```
    1. no próprio computador:
        1. alterar a porta (port) --> waves{network {port}} = significa a porta do nó corrente
        1. alterar o directory: para cada instância a ser subida alterar o waves{directory}
        1. rodar o ```sbt -Dnetwork=testnet packageAll``` (compilou/buildar) --> WSL
        1. copiar o waves-typenet_all.jar para outra pasta fora do projeto: ``` cp node/target/waves-all-1.3.2-5-g35f423b-DIRTY.jar ../nodes-tests/ ```
        1. rodar esse jar para ele virar um node ativo: ```java -jar waves-typenet_all.jar``` --> aqui o node1 está ativo
        1. alterar o directory e alterar o port do network (manter o known-peers)
        1. rodar o ```sbt -Dnetwork=typenet packageAll``` (compilou/buildar)
        1. copiar o waves-typenet_all.jar para outra pasta fora do projeto: ``` cp node/target/waves-all-1.3.2-5-g35f423b-DIRTY.jar ../nodes-tests/ ```
        1. rodar esse jar para ele virar um node ativo: ```java -jar waves-typenet_all.jar``` --> aqui o node2 está ativo
    1. em mais de um computador:
        1. colocar ao menos 1 IP em todas as máquinas. Exemplo: rodando em 6 máquinas, pega IP de 2 e coloca no known-peers das 6, as outras 4 vão se achar.
           1. pegar o endereço local da máquina na rede para jogar ela no known-peers. [IPFind Windows](https://www.pcwdld.com/how-to-scan-network-for-ip-addresses) ipconfig para pegar IP também ou nmap (install via brew).
        1. rodar o ```sbt -Dnetwork=testnet packageAll``` (buildar)
        1. rodar esse jar para ele virar um node ativo: ```java -jar waves-typenet_all.jar``` --> aqui o node2 está ativo
    1. rodar na internet:
        1. 
1. Configuração de Taxas: fee settings (tem configuração de como é feita a marcação das taxas)


Exemplo de execução DevNet:
1. Known-Peers (network-defaults.conf)
    ```conf
      known-peers = [
        "0.0.0.0:6863"
        #"159.69.126.149:6863"
        #"94.130.105.239:6863"
        #"159.69.126.153:6863"
        #"94.130.172.201:6863"
      ]
    ```

1. directory deixa quieto(application.confg)
1. copiar o waves-typenet_all.jar para outra pasta fora do projeto (esse é o release): ``` cp node/target/waves-all-1.3.2-5-g35f423b-DIRTY.jar ../nodes-tests/ ```
1. rodar esse jar para ele virar um node ativo: ```java -jar waves-typenet_all.jar``` --> aqui o node1 está ativo
1. ao acessar localhost:6869 abrirá o swagger de forma interativa. Para poder usar 100% a API precisa colocar algo na ```api-key-hash = ""``` (usar isso só em dev)
    1. colocar DNS e colocar nginx ou apache

# Wallet Setup for Node 
1. Wallet Generator: tem um projeto separado para gerar a wallet aí cada nó tem que subir sua seed para fazer o load da carteira para receber os dividendos (forging):
    1. account = wallet = endereços da rede
    1. Gerador de Wallets: https://github.com/LuzCash/wallet-generator
        1. compilar (na raiz desse projeto): ``` sbt package ```
        1. gerar a wallet (neste exemplo está gerando wallet de testnet(-t) com senha(-p)): ```java -jar walletgenerator.jar -t -p -c 3```
    1. Copiar a "seed_hash" e jogar este hash do seed em: ```application{wallet{seed{}}}```
1. No application.conf terá de colocar no application.conf o seed e a password da wallet
1. No caso de ambiente de produção:
    1. no comando de rodar o .jar pode designar qual será o arquivo.conf a ser usado