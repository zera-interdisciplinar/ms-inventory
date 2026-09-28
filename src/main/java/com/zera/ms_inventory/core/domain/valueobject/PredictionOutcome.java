package com.zera.ms_inventory.core.domain.valueobject;

import java.time.LocalDate;
import java.util.UUID;

/**
 * O resultado de um item dentro do lote. O modelo devolve meses ate a quebra contados a partir da
 * aquisicao, e nao uma data: a conversao mora aqui para o caso de uso nao precisar conhecer a
 * convencao do modelo.
 *
 * <p>Um item invalido volta com {@code error} em vez de derrubar o lote inteiro, que e como o
 * proprio preditivo responde.</p>
 */
public record PredictionOutcome(UUID itemId, Double monthsToFailure, String error) {

    public static PredictionOutcome predicted(UUID itemId, double monthsToFailure) {
        return new PredictionOutcome(itemId, monthsToFailure, null);
    }

    public static PredictionOutcome failed(UUID itemId, String error) {
        return new PredictionOutcome(itemId, null, error == null ? "unknown error" : error);
    }

    public boolean isSuccess() {
        return error == null && monthsToFailure != null;
    }

    /**
     * Meses sao arredondados para o dia mais proximo do mes correspondente. Previsao negativa vira
     * a propria data de aquisicao: o modelo estima, e uma estimativa vencida ainda significa "ja
     * passou da hora", nao uma data no passado remoto.
     */
    public LocalDate failureDateFrom(LocalDate acquiredAt) {
        if (!isSuccess() || acquiredAt == null) {
            return null;
        }
        long months = Math.round(monthsToFailure);
        return months <= 0 ? acquiredAt : acquiredAt.plusMonths(months);
    }
}
