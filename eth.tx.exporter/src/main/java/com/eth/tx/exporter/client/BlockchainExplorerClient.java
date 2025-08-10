package com.eth.tx.exporter.client;

import com.eth.tx.exporter.model.EthTransaction;
import com.eth.tx.exporter.model.TransactionCategorizedResult;

import java.util.List;

public interface BlockchainExplorerClient {

    /**
     * Fetches normal ETH/ERC20/ERC721/ERC1155 transactions for a wallet address.
     * @param address wallet address
     * @return list of transactions
     */
    public TransactionCategorizedResult fetchAndCategorizeTransactions(String address);



    /**
     * Returns the supported blockchain (eg. ETHEREUM, BSC, POLYGON)
     */
    Blockchain getBlockchain();

    enum Blockchain {
        ETHEREUM, BSC, POLYGON
    }
}