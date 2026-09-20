package com.zera.ms_inventory.infrastructure.http.response;

import java.time.LocalDate;
import java.util.List;

import com.zera.ms_inventory.core.domain.valueobject.DisposalIndicators;
import com.zera.ms_inventory.core.domain.valueobject.MaterialCode;

/**
 * Variacoes nulas quando nao houve descarte no periodo anterior: sem base de comparacao, nao ha
 * variacao a mostrar.
 */
public record DisposalIndicatorsResponse(
        LocalDate from,
        LocalDate to,
        double totalWeightKg,
        double recyclingRatePercent,
        Double recyclingRateChangePoints,
        Double totalWeightChangePercent,
        List<MonthlyWeightResponse> monthlyWeightKg,
        List<MaterialShareResponse> weightByMaterial
) {
    public record MonthlyWeightResponse(String month, double weightKg) {}

    /** O peso do item e dividido igualmente entre os materiais do modelo. */
    public record MaterialShareResponse(MaterialCode material, double weightKg, double percent) {}

    public static DisposalIndicatorsResponse from(DisposalIndicators indicators) {
        if (indicators == null) {
            return null;
        }
        return new DisposalIndicatorsResponse(indicators.from(), indicators.to(), indicators.totalWeightKg(),
                indicators.recyclingRatePercent(), indicators.recyclingRateChangePoints(),
                indicators.totalWeightChangePercent(),
                indicators.monthlyWeightKg().stream()
                        .map(month -> new MonthlyWeightResponse(month.month(), month.weightKg())).toList(),
                indicators.weightByMaterial().stream()
                        .map(share -> new MaterialShareResponse(share.material(), share.weightKg(),
                                share.percent())).toList());
    }
}
