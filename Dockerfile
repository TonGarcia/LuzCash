FROM wavesplatform/node

ADD node/target/waves-testnet_1.2.20-1-g93aa062-DIRTY_all.deb /tmp/waves.deb
COPY docker/build-scripts/setup-node.sh /tmp/setup-node.sh
RUN /bin/bash /tmp/setup-node.sh