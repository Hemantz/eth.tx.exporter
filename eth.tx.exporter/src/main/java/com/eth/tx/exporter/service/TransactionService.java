package com.eth.tx.exporter.service;

import com.eth.tx.exporter.client.BlockchainExplorerClient;
import com.eth.tx.exporter.client.BlockchainExplorerClientFactory;
import com.eth.tx.exporter.model.EthTransaction;
import com.eth.tx.exporter.model.TransactionCategorizedResult;
import com.eth.tx.exporter.util.CsvExporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class TransactionService {

    private final BlockchainExplorerClientFactory clientFactory;
    private final CsvExporter csvExporter;

    @Autowired
    public TransactionService(BlockchainExplorerClientFactory clientFactory, CsvExporter csvExporter) {
        this.clientFactory = clientFactory;
        this.csvExporter = csvExporter;
    }

    public String exportTransactionsToCsv(String ethAddress, BlockchainExplorerClient.Blockchain blockchain) {
        BlockchainExplorerClient client = clientFactory.getClient(blockchain);
        TransactionCategorizedResult transactions = client.fetchAndCategorizeTransactions(ethAddress);

        String filename = ethAddress + "_" + blockchain.name().toLowerCase() + "_transactions.csv";
        csvExporter.exportToCsv(transactions, filename);
        return filename;
    }
}
