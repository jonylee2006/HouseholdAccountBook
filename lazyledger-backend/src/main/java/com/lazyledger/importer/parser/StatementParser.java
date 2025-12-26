package com.lazyledger.importer.parser;

import com.lazyledger.importer.model.StatementRecord;

import java.io.InputStream;
import java.util.List;

public interface StatementParser {

    List<StatementRecord> parse(InputStream inputStream);
}
