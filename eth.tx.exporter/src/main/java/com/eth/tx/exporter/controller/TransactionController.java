package com.eth.tx.exporter.controller;

import com.eth.tx.exporter.client.BlockchainExplorerClient;
import com.eth.tx.exporter.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    @Autowired
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping("/export")
    public ResponseEntity<String> exportTransactions(@RequestParam String walletAddress,
                                                     @RequestParam(defaultValue = "ETHEREUM") BlockchainExplorerClient.Blockchain blockchain) {
        String filename = transactionService.exportTransactionsToCsv(walletAddress, blockchain);
        return ResponseEntity.ok("Transactions exported to file: " + filename);
    }
}

