package com.ecommerce.sufi.dto;

import java.util.List;

public record CsvImportResponse(int imported, List<String> errors) {
}
