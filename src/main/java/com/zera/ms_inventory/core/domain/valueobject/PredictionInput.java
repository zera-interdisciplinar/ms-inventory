package com.zera.ms_inventory.core.domain.valueobject;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Os dados de um item como o sistema preditivo os espera. O {@code itemId} nao vai para o modelo,
 * que o descarta: ele existe aqui para casar a resposta de volta com o item, porque a previsao em
 * lote responde por posicao e nao repete a chave.
 */
public record PredictionInput(
        UUID itemId,
        String category,
        String manufacturer,
        String model,
        String climateZone,
        int usageIntensity,
        int manufacturingYear,
        LocalDate acquiredAt
) {
}
