package com.zera.ms_inventory.core.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.zera.ms_inventory.core.domain.entity.Rule;
import com.zera.ms_inventory.core.domain.valueobject.AlertSubject;

/** Consultas que respondem "o que esta violando esta regra agora". */
public interface RuleEvaluationRepository {

    /** Unidades com ao menos uma regra configurada; o job so visita essas. */
    List<UUID> unitsWithRules();

    /**
     * Itens que a regra alcanca e que estao fora do limite. Regra de unidade inteira devolve lista
     * vazia: quem avalia a ocupacao e {@link #countActiveItems}, porque o numero e da unidade.
     */
    List<AlertSubject> findViolatingItems(Rule rule, LocalDate reference);

    long countActiveItems(UUID unitId);
}
