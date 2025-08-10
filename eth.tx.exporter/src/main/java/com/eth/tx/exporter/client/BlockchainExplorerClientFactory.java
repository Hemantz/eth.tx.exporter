package com.eth.tx.exporter.client;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BlockchainExplorerClientFactory {

    private final List<BlockchainExplorerClient> clients;

    public BlockchainExplorerClientFactory(List<BlockchainExplorerClient> clients) {
        this.clients = clients;
    }

    public BlockchainExplorerClient getClient(BlockchainExplorerClient.Blockchain blockchain) {
        return clients.stream()
                .filter(client -> client.getBlockchain() == blockchain)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No client found for blockchain " + blockchain));
    }
}
