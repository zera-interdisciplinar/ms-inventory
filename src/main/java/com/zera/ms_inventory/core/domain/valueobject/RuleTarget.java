package com.zera.ms_inventory.core.domain.valueobject;

import java.util.UUID;

/**
 * A quem a regra se aplica. Vira a relacao {@code (:Rule)-[:APPLIES_TO]->(:Model|:Category)} no
 * grafo; regra sem alvo vale para a unidade inteira, e e assim que as regras padrao nascem.
 */
public record RuleTarget(RuleTargetType type, UUID id) {

    public RuleTarget {
        if (type == null || id == null) {
            throw new IllegalArgumentException("a rule target needs both type and id");
        }
    }

    public static RuleTarget model(UUID modelId) {
        return new RuleTarget(RuleTargetType.MODEL, modelId);
    }

    public static RuleTarget category(UUID categoryId) {
        return new RuleTarget(RuleTargetType.CATEGORY, categoryId);
    }

    /** Monta o alvo a partir do que veio do banco ou da API; os dois nulos significam unidade toda. */
    public static RuleTarget of(RuleTargetType type, UUID id) {
        if (type == null && id == null) {
            return null;
        }
        return new RuleTarget(type, id);
    }
}
