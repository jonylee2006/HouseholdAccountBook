package com.lazyledger.importer.remote;

import com.lazyledger.ledger.domain.ImportAuthorization;

import java.io.InputStream;

public interface RemoteStatementClient {

    InputStream fetch(ImportAuthorization authorization);
}
