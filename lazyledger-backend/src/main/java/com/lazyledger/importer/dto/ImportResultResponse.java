package com.lazyledger.importer.dto;

import java.util.List;

public record ImportResultResponse(
        ImportJobResponse job,
        List<ImportedTransactionDto> preview
) {
}
