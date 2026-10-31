package com.zera.ms_inventory.core.domain.valueobject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

/**
 * Contagem de itens ativos por categoria. A porcentagem e sobre o total ativo da unidade, para o
 * grafico do dashboard fechar em 100% mesmo com item sem categoria.
 */
public record CategoryBreakdown(List<CategoryShare> categories, long totalItems) {

    public record CategoryShare(UUID categoryId, String name, long itemCount, double percent) {}

    public static CategoryBreakdown of(List<CategoryItemCount> rows) {
        long total = rows.stream().mapToLong(CategoryItemCount::itemCount).sum();
        List<CategoryShare> shares = rows.stream()
                .map(row -> new CategoryShare(row.categoryId(), row.name(), row.itemCount(),
                        total == 0 ? 0.0 : round(row.itemCount() * 100.0 / total)))
                .toList();
        return new CategoryBreakdown(List.copyOf(shares), total);
    }

    private static double round(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
