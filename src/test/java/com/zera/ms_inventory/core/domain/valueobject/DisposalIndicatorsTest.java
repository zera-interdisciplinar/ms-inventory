package com.zera.ms_inventory.core.domain.valueobject;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DisposalIndicatorsTest {

    private static DisposedWeight kg(DestinationType destino, String data, double peso, MaterialCode... materiais) {
        return new DisposedWeight(destino, LocalDate.parse(data), peso, List.of(materiais));
    }

    /**
     * Cenario de valores conhecidos pedido no card: 10 kg no total, 6 reciclados (60%), divididos
     * entre os materiais e espalhados em dois meses.
     */
    @Test
    void shouldComputeTheIndicatorsOfAKnownScenario() {
        List<DisposedWeight> periodo = List.of(
                kg(DestinationType.RECYCLING, "2026-01-10", 4.0, MaterialCode.PLASTIC, MaterialCode.METAL),
                kg(DestinationType.RECYCLING, "2026-02-05", 2.0, MaterialCode.METAL),
                kg(DestinationType.LANDFILL, "2026-02-20", 3.0, MaterialCode.GLASS),
                kg(DestinationType.DONATION, "2026-02-25", 1.0, MaterialCode.PLASTIC));

        DisposalIndicators indicadores = DisposalIndicators.of(
                LocalDate.parse("2026-01-01"), LocalDate.parse("2026-02-28"), periodo, List.of());

        assertThat(indicadores.totalWeightKg()).isEqualTo(10.0);
        // 4 + 2 reciclados de 10
        assertThat(indicadores.recyclingRatePercent()).isEqualTo(60.0);
        assertThat(indicadores.monthlyWeightKg()).containsExactly(
                new DisposalIndicators.MonthlyWeight("2026-01", 4.0),
                new DisposalIndicators.MonthlyWeight("2026-02", 6.0));
        // os 4 kg do primeiro item viram 2 para PLASTIC e 2 para METAL
        assertThat(indicadores.weightByMaterial()).containsExactly(
                new DisposalIndicators.MaterialShare(MaterialCode.METAL, 4.0, 40.0),
                new DisposalIndicators.MaterialShare(MaterialCode.PLASTIC, 3.0, 30.0),
                new DisposalIndicators.MaterialShare(MaterialCode.GLASS, 3.0, 30.0));
        assertThat(indicadores.weightByMaterial().stream()
                .mapToDouble(DisposalIndicators.MaterialShare::percent).sum()).isEqualTo(100.0);
    }

    @Test
    void shouldCompareWithThePreviousPeriod() {
        List<DisposedWeight> anterior = List.of(
                kg(DestinationType.RECYCLING, "2025-12-10", 2.0, MaterialCode.METAL),
                kg(DestinationType.LANDFILL, "2025-12-20", 2.0, MaterialCode.GLASS));
        List<DisposedWeight> periodo = List.of(
                kg(DestinationType.RECYCLING, "2026-01-10", 6.0, MaterialCode.METAL),
                kg(DestinationType.LANDFILL, "2026-01-20", 2.0, MaterialCode.GLASS));

        DisposalIndicators indicadores = DisposalIndicators.of(
                LocalDate.parse("2026-01-01"), LocalDate.parse("2026-01-31"), periodo, anterior);

        // 75% agora contra 50% antes
        assertThat(indicadores.recyclingRatePercent()).isEqualTo(75.0);
        assertThat(indicadores.recyclingRateChangePoints()).isEqualTo(25.0);
        // 8 kg contra 4 kg
        assertThat(indicadores.totalWeightChangePercent()).isEqualTo(100.0);
    }

    /** Sem base de comparacao, a variacao fica nula em vez de fingir 0%. */
    @Test
    void shouldLeaveTheChangeNullWithoutAPreviousPeriod() {
        DisposalIndicators indicadores = DisposalIndicators.of(
                LocalDate.parse("2026-01-01"), LocalDate.parse("2026-01-31"),
                List.of(kg(DestinationType.RECYCLING, "2026-01-10", 5.0, MaterialCode.METAL)), List.of());

        assertThat(indicadores.recyclingRateChangePoints()).isNull();
        assertThat(indicadores.totalWeightChangePercent()).isNull();
    }

    @Test
    void shouldFillEveryMonthOfTheRangeIncludingTheEmptyOnes() {
        DisposalIndicators indicadores = DisposalIndicators.of(
                LocalDate.parse("2026-01-01"), LocalDate.parse("2026-04-30"),
                List.of(kg(DestinationType.LANDFILL, "2026-03-10", 1.0, MaterialCode.GLASS)), List.of());

        assertThat(indicadores.monthlyWeightKg()).extracting(DisposalIndicators.MonthlyWeight::month)
                .containsExactly("2026-01", "2026-02", "2026-03", "2026-04");
        assertThat(indicadores.monthlyWeightKg()).extracting(DisposalIndicators.MonthlyWeight::weightKg)
                .containsExactly(0.0, 0.0, 1.0, 0.0);
    }

    /** Item sem material cai em OTHER, senao o peso dele sumiria do grafico de composicao. */
    @Test
    void shouldPutWeightWithoutMaterialUnderOther() {
        DisposalIndicators indicadores = DisposalIndicators.of(
                LocalDate.parse("2026-01-01"), LocalDate.parse("2026-01-31"),
                List.of(kg(DestinationType.LANDFILL, "2026-01-10", 4.0)), List.of());

        assertThat(indicadores.weightByMaterial()).containsExactly(
                new DisposalIndicators.MaterialShare(MaterialCode.OTHER, 4.0, 100.0));
    }

    /** Item cujo modelo nao tem peso estimado nao soma, mas tambem nao quebra a conta. */
    @Test
    void shouldIgnoreItemsWithoutWeight() {
        DisposalIndicators indicadores = DisposalIndicators.of(
                LocalDate.parse("2026-01-01"), LocalDate.parse("2026-01-31"),
                List.of(new DisposedWeight(DestinationType.RECYCLING, LocalDate.parse("2026-01-10"), null,
                                List.of(MaterialCode.METAL)),
                        kg(DestinationType.RECYCLING, "2026-01-11", 2.0, MaterialCode.METAL)), List.of());

        assertThat(indicadores.totalWeightKg()).isEqualTo(2.0);
        assertThat(indicadores.recyclingRatePercent()).isEqualTo(100.0);
    }

    @Test
    void shouldReturnZeroedIndicatorsWithoutDisposals() {
        DisposalIndicators indicadores = DisposalIndicators.of(
                LocalDate.parse("2026-01-01"), LocalDate.parse("2026-01-31"), List.of(), List.of());

        assertThat(indicadores.totalWeightKg()).isZero();
        assertThat(indicadores.recyclingRatePercent()).isZero();
        assertThat(indicadores.weightByMaterial()).isEmpty();
        assertThat(indicadores.monthlyWeightKg()).hasSize(1);
    }

    @Test
    void shouldRoundToTwoDecimals() {
        DisposalIndicators indicadores = DisposalIndicators.of(
                LocalDate.parse("2026-01-01"), LocalDate.parse("2026-01-31"),
                List.of(kg(DestinationType.RECYCLING, "2026-01-10", 1.0,
                        MaterialCode.METAL, MaterialCode.PLASTIC, MaterialCode.GLASS)), List.of());

        // 1 kg dividido por 3 materiais
        assertThat(indicadores.weightByMaterial()).allSatisfy(share -> {
            assertThat(share.weightKg()).isEqualTo(0.33);
            assertThat(share.percent()).isEqualTo(33.33);
        });
    }
}
