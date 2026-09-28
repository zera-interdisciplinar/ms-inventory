package com.zera.ms_inventory.infrastructure.prediction;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import com.zera.ms_inventory.core.domain.valueobject.PredictionInput;
import com.zera.ms_inventory.core.domain.valueobject.PredictionOutcome;
import com.zera.ms_inventory.core.repository.FailurePredictionGateway;

/**
 * Usado quando a integracao esta desligada. Nao inventa previsao: devolve todos os itens como nao
 * previstos, para o painel mostrar "sem previsao" em vez de um numero que ninguem calculou.
 */
@Component
// condicionado pela propriedade, e nao por ausencia de bean: a ausencia depende da ordem de
// registro, e um empate deixaria a aplicacao com dois gateways ou nenhum
@ConditionalOnProperty(prefix = "zera.prediction", name = "enabled", havingValue = "false",
        matchIfMissing = true)
public class LoggingFailurePredictionGateway implements FailurePredictionGateway {

    private static final Logger log = LoggerFactory.getLogger(LoggingFailurePredictionGateway.class);

    @Override
    public List<PredictionOutcome> predict(List<PredictionInput> inputs) {
        log.info("Prediction integration disabled; {} item(s) left without a new prediction", inputs.size());
        return inputs.stream()
                .map(input -> PredictionOutcome.failed(input.itemId(), "prediction integration disabled"))
                .toList();
    }
}
