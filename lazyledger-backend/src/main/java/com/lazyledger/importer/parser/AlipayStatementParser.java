package com.lazyledger.importer.parser;

import com.lazyledger.common.enums.TransactionDirection;
import com.lazyledger.importer.model.StatementRecord;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

@Component
public class AlipayStatementParser extends AbstractStatementParser {

    @Override
    public List<StatementRecord> parse(InputStream inputStream) {
        List<String> lines = readMeaningfulLines(inputStream);
        int headerIndex = locateHeaderIndex(lines);
        try (CSVParser parser = buildCsvParser(lines, headerIndex)) {
            return collectRecords(parser);
        } catch (IOException e) {
            throw new IllegalStateException("关闭 CSV 解析器失败", e);
        }
    }

    private int locateHeaderIndex(List<String> lines) {
        return IntStream.range(0, lines.size())
                .filter(i -> lines.get(i).contains("交易号") && lines.get(i).contains("交易创建时间"))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("未找到支付宝账单表头"));
    }

    @Override
    protected StatementRecord mapRecord(CSVRecord csvRecord) {
        String tradeStatus = get(csvRecord, "交易状态");
        if (tradeStatus != null && tradeStatus.contains("关闭")) {
            return null;
        }
        LocalDateTime occurredAt = parseDate(firstNonBlank(
                get(csvRecord, "付款时间"),
                get(csvRecord, "交易创建时间")));
        BigDecimal amount = parseAmount(get(csvRecord, "金额（元）"));
        TransactionDirection direction = parseDirection(get(csvRecord, "收/支"), amount);
        String merchant = firstNonBlank(get(csvRecord, "交易对方"), get(csvRecord, "交易来源地"));
        String subject = get(csvRecord, "商品名称");
        String paymentMethod = get(csvRecord, "资金状态");
        String referenceId = firstNonBlank(get(csvRecord, "商家订单号"), get(csvRecord, "交易号"));
        return toRecord(occurredAt, merchant, subject, amount, direction, paymentMethod, referenceId, raw(csvRecord));
    }

    private String firstNonBlank(String primary, String fallback) {
        if (primary != null && !primary.isBlank()) {
            return primary;
        }
        return fallback;
    }
}
