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
public class WeChatStatementParser extends AbstractStatementParser {

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
                .filter(i -> lines.get(i).contains("交易时间") && lines.get(i).contains("金额"))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("未找到微信账单表头"));
    }

    @Override
    protected StatementRecord mapRecord(CSVRecord csvRecord) {
        String status = get(csvRecord, "当前状态");
        if (status != null && status.contains("已全额退款")) {
            return null;
        }
        LocalDateTime occurredAt = parseDate(get(csvRecord, "交易时间"));
        BigDecimal amount = parseAmount(get(csvRecord, "金额(元)"));
        TransactionDirection direction = parseDirection(get(csvRecord, "收/支"), amount);
        String merchant = get(csvRecord, "交易对方");
        String subject = get(csvRecord, "商品");
        if ((subject == null || subject.isBlank()) && csvRecord.isMapped("交易类型")) {
            subject = get(csvRecord, "交易类型");
        }
        String paymentMethod = get(csvRecord, "支付方式");
        String referenceId = get(csvRecord, "交易单号");
        return toRecord(occurredAt, merchant, subject, amount, direction, paymentMethod, referenceId, raw(csvRecord));
    }
}
