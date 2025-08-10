package com.eth.tx.exporter.model;

import lombok.Data;

import java.time.Instant;

@Data
public class EthTransaction {
    private String txHash;
    private Instant timeStamp;
    private String from;
    private String to;
    private String type; // ETH_TRANSFER, INTERNAL, ERC20, ERC721
    private String assetContractAddress;
    private String assetSymbol;
    private String tokenId;
    private String value;
    private String gasFeeEth;
}