package com.zera.ms_inventory.core.usecase.rule;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.zera.ms_inventory.core.domain.exception.CategoryNotFoundException;
import com.zera.ms_inventory.core.domain.exception.ModelNotFoundException;
import com.zera.ms_inventory.core.domain.valueobject.RuleTarget;
import com.zera.ms_inventory.core.domain.valueobject.RuleTargetType;
import com.zera.ms_inventory.core.repository.CategoryRepository;
import com.zera.ms_inventory.core.repository.ModelRepository;

/**
 * Confere que o alvo existe na unidade antes de gravar a regra. Sem isso um alvo inexistente ou de
 * outra unidade nao viraria relacao no grafo e a regra passaria a valer para a unidade inteira —
 * uma entrada invalida ampliando silenciosamente o alcance do alerta.
 */
@Component
public class RuleTargetResolver {

    private final ModelRepository modelRepository;
    private final CategoryRepository categoryRepository;

    public RuleTargetResolver(ModelRepository modelRepository, CategoryRepository categoryRepository) {
        this.modelRepository = modelRepository;
        this.categoryRepository = categoryRepository;
    }

    /** Alvo nulo e valido: a regra vale para a unidade inteira. */
    public void requireExists(UUID unitId, RuleTarget target) {
        if (target == null) {
            return;
        }
        if (target.type() == RuleTargetType.MODEL) {
            modelRepository.findById(unitId, target.id())
                    .orElseThrow(() -> new ModelNotFoundException(target.id()));
        } else {
            categoryRepository.findById(unitId, target.id())
                    .orElseThrow(() -> new CategoryNotFoundException(target.id()));
        }
    }
}
