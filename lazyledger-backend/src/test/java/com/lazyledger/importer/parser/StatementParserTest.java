package com.lazyledger.importer.parser;

import com.lazyledger.importer.model.StatementRecord;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StatementParserTest {

    private final WeChatStatementParser weChatStatementParser = new WeChatStatementParser();
    private final AlipayStatementParser alipayStatementParser = new AlipayStatementParser();

    @Test
    void shouldParseWeChatCsv() throws IOException {
        try (InputStream stream = getClass().getResourceAsStream("/samples/wechat.csv")) {
            assertThat(stream).isNotNull();
            List<StatementRecord> records = weChatStatementParser.parse(stream);
            assertThat(records).hasSize(2);
            assertThat(records.get(0).merchantName()).isEqualTo("饿了么");
        }
    }

    @Test
    void shouldParseAlipayCsv() throws IOException {
        try (InputStream stream = getClass().getResourceAsStream("/samples/alipay.csv")) {
            assertThat(stream).isNotNull();
            List<StatementRecord> records = alipayStatementParser.parse(stream);
            assertThat(records).hasSize(2);
            assertThat(records.get(1).merchantName()).isEqualTo("李四");
        }
    }
}
