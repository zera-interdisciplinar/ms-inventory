package com.zera.ms_inventory.infrastructure.http.response;

import java.util.List;
import java.util.UUID;

import com.zera.ms_inventory.core.domain.valueobject.CategoryBreakdown;

public record CategoryBreakdownResponse(List<CategoryShareResponse> categories, long totalItems) {

    public record CategoryShareResponse(UUID categoryId, String name, long itemCount, double percent) {}

    public static CategoryBreakdownResponse from(CategoryBreakdown breakdown) {
        if (breakdown == null) {
            return null;
        }
        return new CategoryBreakdownResponse(
                breakdown.categories().stream()
                        .map(share -> new CategoryShareResponse(share.categoryId(), share.name(),
                                share.itemCount(), share.percent()))
                        .toList(),
                breakdown.totalItems());
    }
}
