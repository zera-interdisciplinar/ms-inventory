package com.zera.ms_inventory.core.usecase.prediction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.domain.entity.Model;
import com.zera.ms_inventory.core.domain.valueobject.PredictionInput;
import com.zera.ms_inventory.core.domain.valueobject.PredictionOutcome;
import com.zera.ms_inventory.core.repository.FailurePredictionGateway;
import com.zera.ms_inventory.core.repository.ItemRepository;

/**
 * Mantem {@code predictedFailureDate} atualizado a partir do sistema preditivo. Roda por unidade e
 * em lotes: o modelo faz uma passagem unica por lote, entao mandar cem itens juntos custa quase o
 * mesmo que mandar um, e cem chamadas separadas custariam cem vezes a ida e volta.
 *
 * <p>Falha de uma unidade nao derruba as outras: previsao e informacao acessoria, e deixar o job
 * inteiro cair por causa de uma unidade significaria perder tambem as que funcionariam.</p>
 */
@Service
public class UpdateFailurePredictionsImpl implements UpdateFailurePredictions {

    private static final Logger log = LoggerFactory.getLogger(UpdateFailurePredictionsImpl.class);

    private final ItemRepository itemRepository;
    private final FailurePredictionGateway gateway;
    private final String climateZone;
    private final int batchSize;

    public UpdateFailurePredictionsImpl(ItemRepository itemRepository, FailurePredictionGateway gateway,
                                        @Value("${zera.prediction.climate-zone:TROPICAL}") String climateZone,
                                        @Value("${zera.prediction.batch-size:100}") int batchSize) {
        this.itemRepository = itemRepository;
        this.gateway = gateway;
        this.climateZone = climateZone;
        this.batchSize = batchSize <= 0 ? 100 : batchSize;
    }

    @Override
    public PredictionResult execute() {
        PredictionResult total = PredictionResult.empty();
        for (UUID unitId : itemRepository.unitsWithItems()) {
            try {
                total = total.plus(runUnit(unitId));
            } catch (RuntimeException e) {
                log.warn("Failure prediction skipped for unit {}: {}", unitId, e.getMessage());
            }
        }
        log.info("Failure prediction finished: {}", total);
        return total;
    }

    private PredictionResult runUnit(UUID unitId) {
        List<Item> items = itemRepository.findAll(unitId);
        Map<UUID, Item> byId = new HashMap<>();
        List<PredictionInput> inputs = new ArrayList<>();
        int skipped = 0;

        for (Item item : items) {
            PredictionInput input = inputOf(item);
            if (input == null) {
                skipped++;
                continue;
            }
            byId.put(item.getId(), item);
            inputs.add(input);
        }

        int stored = 0;
        int failed = 0;
        for (int start = 0; start < inputs.size(); start += batchSize) {
            List<PredictionInput> batch = inputs.subList(start, Math.min(start + batchSize, inputs.size()));
            for (PredictionOutcome outcome : gateway.predict(batch)) {
                if (store(byId.get(outcome.itemId()), outcome)) {
                    stored++;
                } else {
                    failed++;
                }
            }
        }
        return new PredictionResult(1, inputs.size(), skipped, stored, failed);
    }

    /** Previsao que nao mudou nao e gravada: reescrever o mesmo valor so envelheceria o updatedAt. */
    private boolean store(Item item, PredictionOutcome outcome) {
        if (item == null || !outcome.isSuccess()) {
            return false;
        }
        var predicted = outcome.failureDateFrom(item.getAcquiredAt());
        if (predicted == null) {
            return false;
        }
        if (predicted.equals(item.getPredictedFailureDate())) {
            return true;
        }
        item.recordPrediction(predicted);
        itemRepository.save(item);
        return true;
    }

    /**
     * Item so vai para o modelo com o conjunto completo do contrato. Faltando qualquer campo, o
     * preditivo recusaria a linha ou responderia sobre um chute nosso, e um chute vira data de
     * quebra na tela do gestor.
     */
    private PredictionInput inputOf(Item item) {
        Model model = item.getModel();
        if (model == null || model.getCategory() == null || isBlank(model.getManufacturer())
                || isBlank(model.getName()) || item.getAcquiredAt() == null
                || item.getManufacturingYear() == null || item.getUsageIntensity() == null) {
            return null;
        }
        return new PredictionInput(item.getId(), model.getCategory().getName(), model.getManufacturer(),
                model.getName(), climateZone, item.getUsageIntensity(), item.getManufacturingYear(),
                item.getAcquiredAt());
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
