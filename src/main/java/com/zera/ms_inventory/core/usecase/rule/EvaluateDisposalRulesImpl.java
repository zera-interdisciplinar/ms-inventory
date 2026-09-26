package com.zera.ms_inventory.core.usecase.rule;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.zera.ms_inventory.core.domain.entity.Disposal;
import com.zera.ms_inventory.core.domain.entity.Rule;
import com.zera.ms_inventory.core.domain.entity.RuleAlert;
import com.zera.ms_inventory.core.domain.valueobject.AlertSeverity;
import com.zera.ms_inventory.core.domain.valueobject.AlertSubject;
import com.zera.ms_inventory.core.domain.valueobject.DestinationType;
import com.zera.ms_inventory.core.domain.valueobject.DisposedItem;
import com.zera.ms_inventory.core.domain.valueobject.RuleKind;
import com.zera.ms_inventory.core.repository.ItemRepository;
import com.zera.ms_inventory.core.repository.RuleRepository;

/**
 * Reciclavel enviado ao aterro. E avaliada no descarte, e nao no job: o alerta so ajuda se chegar
 * a tempo de rever o destino, e o proprio PATCH de correcao existe para isso.
 */
@Service
public class EvaluateDisposalRulesImpl implements EvaluateDisposalRules {

    private static final Logger log = LoggerFactory.getLogger(EvaluateDisposalRulesImpl.class);

    private final RuleRepository ruleRepository;
    private final ItemRepository itemRepository;
    private final AlertDispatcher dispatcher;

    public EvaluateDisposalRulesImpl(RuleRepository ruleRepository, ItemRepository itemRepository,
                                     AlertDispatcher dispatcher) {
        this.ruleRepository = ruleRepository;
        this.itemRepository = itemRepository;
        this.dispatcher = dispatcher;
    }

    @Override
    public void execute(Disposal disposal) {
        if (disposal.getDestination() != DestinationType.LANDFILL) {
            return;
        }
        List<Rule> regras = ruleRepository.findAll(disposal.getUnitId()).stream()
                .filter(Rule::isActive)
                .filter(rule -> rule.getKind() == RuleKind.RECYCLABLE_TO_LANDFILL)
                .toList();
        if (regras.isEmpty()) {
            return;
        }

        for (DisposedItem item : disposal.getItems()) {
            if (!isRecyclable(disposal.getUnitId(), item.itemId())) {
                continue;
            }
            for (Rule rule : regras) {
                RuleAlert alerta = new RuleAlert(disposal.getUnitId(), rule.getId(),
                        RuleKind.RECYCLABLE_TO_LANDFILL,
                        new AlertSubject(item.itemId(), item.displayCode(), item.name()),
                        AlertSeverity.HIGH,
                        "Material reciclavel enviado ao aterro: %s".formatted(
                                new AlertSubject(item.itemId(), item.displayCode(), item.name()).describe()),
                        java.time.LocalDateTime.now());
                dispatcher.dispatch(alerta);
            }
        }
    }

    /** Reciclavel vem do catalogo de materiais do modelo, herdado pelo item. */
    private boolean isRecyclable(UUID unitId, UUID itemId) {
        return itemRepository.findById(unitId, itemId)
                .filter(item -> item.getModel() != null)
                .map(item -> item.getModel().getMaterials().stream()
                        .anyMatch(com.zera.ms_inventory.core.domain.entity.Material::isRecyclable))
                .orElse(false);
    }
}
