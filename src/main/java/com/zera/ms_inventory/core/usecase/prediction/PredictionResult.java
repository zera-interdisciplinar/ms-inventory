package com.zera.ms_inventory.core.usecase.prediction;

/**
 * O que a execucao fez. {@code itemsSkipped} e a contagem que importa no dia a dia: item sem ano de
 * fabricacao, sem aquisicao ou sem intensidade de uso nao pode ser previsto, e o numero denuncia
 * cadastro incompleto sem precisar abrir o banco.
 */
public record PredictionResult(
        int unitsVisited,
        int itemsEligible,
        int itemsSkipped,
        int predictionsStored,
        int predictionsFailed
) {
    static PredictionResult empty() {
        return new PredictionResult(0, 0, 0, 0, 0);
    }

    PredictionResult plus(PredictionResult other) {
        return new PredictionResult(
                unitsVisited + other.unitsVisited,
                itemsEligible + other.itemsEligible,
                itemsSkipped + other.itemsSkipped,
                predictionsStored + other.predictionsStored,
                predictionsFailed + other.predictionsFailed);
    }
}
