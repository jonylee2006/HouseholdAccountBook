package com.lazyledger.importer.remote;

import com.lazyledger.ledger.domain.ImportAuthorization;

import java.io.InputStream;
import java.time.LocalDate;

public interface RemoteStatementClient {

    InputStream fetch(ImportAuthorization authorization, LocalDate billDate);
}
