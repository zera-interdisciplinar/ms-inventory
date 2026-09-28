package com.zera.ms_inventory.core.repository;

import java.util.List;

import com.zera.ms_inventory.core.domain.valueobject.PredictionInput;
import com.zera.ms_inventory.core.domain.valueobject.PredictionOutcome;

/** Saida para o sistema preditivo. */
public interface FailurePredictionGateway {

    /**
     * Um resultado por entrada, na mesma ordem. Itens que o modelo nao conseguiu prever voltam com
     * erro preenchido, em vez de sumirem da lista.
     */
    List<PredictionOutcome> predict(List<PredictionInput> inputs);
}
