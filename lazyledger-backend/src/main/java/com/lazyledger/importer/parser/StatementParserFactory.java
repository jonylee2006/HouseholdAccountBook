package com.lazyledger.importer.parser;

import com.lazyledger.common.enums.ImportSourceType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

@Component
public class StatementParserFactory {

    private final Map<ImportSourceType, StatementParser> parserMap = new EnumMap<>(ImportSourceType.class);

    public StatementParserFactory(WeChatStatementParser weChatStatementParser,
                                  AlipayStatementParser alipayStatementParser) {
        parserMap.put(ImportSourceType.WECHAT, weChatStatementParser);
        parserMap.put(ImportSourceType.ALIPAY, alipayStatementParser);
    }

    public StatementParser getParser(ImportSourceType sourceType) {
        StatementParser parser = parserMap.get(sourceType);
        if (parser == null) {
            throw new IllegalArgumentException("暂不支持的账单来源: " + sourceType);
        }
        return parser;
    }
}
