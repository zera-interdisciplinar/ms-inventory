package com.zera.ms_inventory.infrastructure.http.response;

import java.util.List;
import java.util.function.Function;

import com.zera.ms_inventory.core.domain.valueobject.PageResult;

public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {

    public static <D, T> PageResponse<T> from(PageResult<D> result, Function<D, T> mapper) {
        PageResult<T> mapped = result.map(mapper);
        return new PageResponse<>(mapped.content(), mapped.page(), mapped.size(), mapped.totalElements(),
                mapped.totalPages());
    }
}
