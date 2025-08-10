package com.eth.tx.exporter.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TransactionCategorizedResult {
    private List<EthTransaction> externalTransfers = new ArrayList<>();
    private List<EthTransaction> internalTransfers = new ArrayList<>();
    private List<EthTransaction> erc20Transfers = new ArrayList<>();
    private List<EthTransaction> erc721Transfers = new ArrayList<>();
    private List<EthTransaction> erc1155Transfers = new ArrayList<>();
}

