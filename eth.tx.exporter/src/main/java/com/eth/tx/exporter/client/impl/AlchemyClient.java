package com.eth.tx.exporter.client.impl;

import com.eth.tx.exporter.client.BlockchainExplorerClient;
import com.eth.tx.exporter.config.AlchemyProperties;
import com.eth.tx.exporter.model.EthTransaction;
import com.eth.tx.exporter.model.TransactionCategorizedResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigInteger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class AlchemyClient implements BlockchainExplorerClient {

    private final WebClient webClient;
    private final AlchemyProperties properties;

    @Autowired
    public AlchemyClient(AlchemyProperties properties) {
        this.properties = properties;

        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer ->
                        configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024)  // 16 MB buffer size
                )
                .build();

        this.webClient = WebClient.builder()
                .baseUrl(properties.getBaseUrl())
                .exchangeStrategies(strategies)
                .build();
    }

    @Override
    public Blockchain getBlockchain() {
        return Blockchain.ETHEREUM;
    }


    public TransactionCategorizedResult fetchAndCategorizeTransactions(String address) {
        TransactionCategorizedResult result = new TransactionCategorizedResult();
        String pageKey = null;

        do {
            Map<String, Object> requestBody = buildRequestBody(address, pageKey);
            Map<String, Object> response = webClient.post()
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            log.info("Response from Alchemy: {}", response);

            if (response == null) break;

            Map<String, Object> resultMap = (Map<String, Object>) response.get("result");
            if (resultMap == null) break;

            List<Map<String, Object>> transfers = (List<Map<String, Object>>) resultMap.get("transfers");
            if (transfers == null || transfers.isEmpty()) break;

            for (Map<String, Object> transfer : transfers) {
                EthTransaction tx = parseTransaction(transfer);
                categorizeTransaction(tx, result);
            }

            pageKey = (String) resultMap.get("pageKey");
        } while (pageKey != null);

        return result;
    }

    private Map<String, Object> buildRequestBody(String address, String pageKey) {
        Map<String, Object> params = new HashMap<>();
        params.put("fromAddress", address);
        params.put("category", List.of("external", "erc20", "erc721", "erc1155", "internal"));
        params.put("withMetadata", true);
        params.put("maxCount", "0x3e8"); // 1000
        if (pageKey != null) {
            params.put("pageKey", pageKey);
        }

        return Map.of(
                "jsonrpc", "2.0",
                "id", 1,
                "method", "alchemy_getAssetTransfers",
                "params", List.of(params)
        );
    }

    private EthTransaction parseTransaction(Map<String, Object> transfer) {
        EthTransaction tx = new EthTransaction();

        tx.setTxHash((String) transfer.get("hash"));
        tx.setTimeStamp(parseTimestamp(transfer));
        tx.setFrom((String) transfer.get("from"));
        tx.setTo((String) transfer.get("to"));
        tx.setType((String) transfer.get("category"));
        tx.setValue(String.valueOf(transfer.getOrDefault("value", "0")));
        tx.setGasFeeEth("0"); // Not provided by Alchemy API

        // Prefer asset symbol, fallback contractName, fallback "UNKNOWN"
        String assetSymbol = (String) transfer.getOrDefault("asset",
                transfer.getOrDefault("contractName", "UNKNOWN"));
        tx.setAssetSymbol(assetSymbol);

        Map<String, Object> rawContract = (Map<String, Object>) transfer.get("rawContract");
        if (rawContract != null) {
            tx.setAssetContractAddress((String) rawContract.get("address"));
        }

        // Set tokenId using a prioritized approach
        String tokenId = extractTokenId(transfer);
        tx.setTokenId(tokenId);

        return tx;
    }

    private Instant parseTimestamp(Map<String, Object> transfer) {
        Map<String, Object> metadata = (Map<String, Object>) transfer.get("metadata");
        if (metadata != null) {
            Object blockTimestamp = metadata.get("blockTimestamp");
            if (blockTimestamp instanceof String) {
                return Instant.parse((String) blockTimestamp);
            }
        }
        return null;
    }

    private String extractTokenId(Map<String, Object> transfer) {
        if (transfer.containsKey("erc721TokenId") && transfer.get("erc721TokenId") != null) {
            return parseTokenId(transfer.get("erc721TokenId"));
        }
        if (transfer.containsKey("erc1155TokenId") && transfer.get("erc1155TokenId") != null) {
            return parseTokenId(transfer.get("erc1155TokenId"));
        }
        if (transfer.containsKey("tokenId") && transfer.get("tokenId") != null) {
            return parseTokenId(transfer.get("tokenId"));
        }
        return null;
    }

    private void categorizeTransaction(EthTransaction tx, TransactionCategorizedResult result) {
        switch (tx.getType()) {
            case "external" -> result.getExternalTransfers().add(tx);
            case "internal" -> result.getInternalTransfers().add(tx);
            case "erc20" -> result.getErc20Transfers().add(tx);
            case "erc721" -> result.getErc721Transfers().add(tx);
            case "erc1155" -> result.getErc1155Transfers().add(tx);
            default -> log.warn("Unknown transfer category: {}", tx.getType());
        }
    }

    // Assume you have this method implemented elsewhere as per previous discussion:
    private String parseTokenId(Object tokenIdObj) {
        if (tokenIdObj == null) return null;
        String tokenIdStr = tokenIdObj.toString();
        if (tokenIdStr.startsWith("0x") || tokenIdStr.startsWith("0X")) {
            try {
                BigInteger tokenIdBigInt = new BigInteger(tokenIdStr.substring(2), 16);
                return "0x" + tokenIdBigInt.toString(16);
            } catch (NumberFormatException e) {
                return tokenIdStr;
            }
        }
        return tokenIdStr.replaceFirst("^0+", "");
    }
}
