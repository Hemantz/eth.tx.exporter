package com.eth.tx.exporter.util;

import com.eth.tx.exporter.model.EthTransaction;
import com.eth.tx.exporter.model.TransactionCategorizedResult;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

@Component
public class CsvExporter {

    public void exportToCsv(TransactionCategorizedResult categorizedResult, String filename) {
        try (FileWriter out = new FileWriter(filename);
             CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT
                     .withHeader("Transaction Hash", "Date & Time", "From Address", "To Address", "Transaction Type",
                             "Asset Contract Address", "Asset Symbol / Name", "Token ID", "Value / Amount", "Gas Fee (ETH)"))) {

            // Helper to print list of EthTransaction
            Consumer<List<EthTransaction>> printTxList = txList -> {
                try {
                    for (EthTransaction tx : txList) {
                        printer.printRecord(
                                tx.getTxHash(),
                                tx.getTimeStamp(),
                                tx.getFrom(),
                                tx.getTo(),
                                tx.getType(),
                                tx.getAssetContractAddress(),
                                tx.getAssetSymbol(),
                                tx.getTokenId(),
                                tx.getValue(),
                                tx.getGasFeeEth()
                        );
                    }
                } catch (IOException e) {
                    throw new RuntimeException("Error writing CSV file", e);
                }
            };

            // Print all categorized transactions
            printTxList.accept(categorizedResult.getExternalTransfers());
            printTxList.accept(categorizedResult.getInternalTransfers());
            printTxList.accept(categorizedResult.getErc20Transfers());
            printTxList.accept(categorizedResult.getErc721Transfers());

        } catch (IOException e) {
            throw new RuntimeException("Error writing CSV file", e);
        }
    }
}
