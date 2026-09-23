package com.zera.ms_inventory.core.domain.valueobject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Indicadores de descarte do periodo. A variacao compara com o periodo imediatamente anterior, de
 * mesma duracao; quando nao houve descarte antes, a variacao fica nula em vez de fingir 0%.
 */
public record DisposalIndicators(
        LocalDate from,
        LocalDate to,
        double totalWeightKg,
        double recyclingRatePercent,
        /** Diferenca em pontos percentuais da taxa de reciclagem. */
        Double recyclingRateChangePoints,
        /** Variacao percentual do peso total. */
        Double totalWeightChangePercent,
        List<MonthlyWeight> monthlyWeightKg,
        List<MaterialShare> weightByMaterial
) {

    public record MonthlyWeight(String month, double weightKg) {}

    public record MaterialShare(MaterialCode material, double weightKg, double percent) {}

    public static DisposalIndicators of(LocalDate from, LocalDate to, List<DisposedWeight> current,
                                        List<DisposedWeight> previous) {
        double total = totalOf(current);
        double previousTotal = totalOf(previous);
        double rate = recyclingRate(current, total);

        Double rateChange = previous.isEmpty() ? null
                : round(rate - recyclingRate(previous, previousTotal));
        Double weightChange = previousTotal == 0 ? null
                : round((total - previousTotal) * 100.0 / previousTotal);

        return new DisposalIndicators(from, to, round(total), round(rate), rateChange, weightChange,
                monthly(from, to, current), byMaterial(current, total));
    }

    private static double totalOf(List<DisposedWeight> weights) {
        return weights.stream().mapToDouble(DisposedWeight::weightOrZero).sum();
    }

    private static double recyclingRate(List<DisposedWeight> weights, double total) {
        if (total == 0) {
            return 0.0;
        }
        double recycled = weights.stream()
                .filter(weight -> weight.destination() == DestinationType.RECYCLING)
                .mapToDouble(DisposedWeight::weightOrZero)
                .sum();
        return recycled * 100.0 / total;
    }

    /** Todos os meses do intervalo aparecem, inclusive os sem descarte, para o grafico nao ter buracos. */
    private static List<MonthlyWeight> monthly(LocalDate from, LocalDate to, List<DisposedWeight> weights) {
        Map<YearMonth, Double> porMes = new java.util.HashMap<>();
        for (DisposedWeight weight : weights) {
            porMes.merge(YearMonth.from(weight.disposedAt()), weight.weightOrZero(), Double::sum);
        }
        List<MonthlyWeight> meses = new ArrayList<>();
        for (YearMonth mes = YearMonth.from(from); !mes.isAfter(YearMonth.from(to)); mes = mes.plusMonths(1)) {
            meses.add(new MonthlyWeight(mes.toString(), round(porMes.getOrDefault(mes, 0.0))));
        }
        return List.copyOf(meses);
    }

    /**
     * O peso do item e dividido igualmente entre os materiais do modelo, que nao guarda proporcao:
     * assim as porcentagens fecham em 100%. Item sem material cai em OTHER, para nao sumir do total.
     */
    private static List<MaterialShare> byMaterial(List<DisposedWeight> weights, double total) {
        Map<MaterialCode, Double> porMaterial = new EnumMap<>(MaterialCode.class);
        for (DisposedWeight weight : weights) {
            List<MaterialCode> materials = weight.materials().isEmpty()
                    ? List.of(MaterialCode.OTHER)
                    : weight.materials();
            double fatia = weight.weightOrZero() / materials.size();
            materials.forEach(material -> porMaterial.merge(material, fatia, Double::sum));
        }
        return porMaterial.entrySet().stream()
                .map(entry -> new MaterialShare(entry.getKey(), round(entry.getValue()),
                        total == 0 ? 0.0 : round(entry.getValue() * 100.0 / total)))
                .sorted(Comparator.comparingDouble(MaterialShare::weightKg).reversed()
                        .thenComparing(MaterialShare::material))
                .toList();
    }

    private static double round(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
