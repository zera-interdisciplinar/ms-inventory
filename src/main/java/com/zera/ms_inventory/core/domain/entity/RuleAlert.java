package com.zera.ms_inventory.core.domain.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import com.zera.ms_inventory.core.domain.valueobject.AlertSeverity;
import com.zera.ms_inventory.core.domain.valueobject.AlertSubject;
import com.zera.ms_inventory.core.domain.valueobject.RuleKind;

/**
 * Regra que disparou para um assunto. O par (regra, assunto) e a chave de deduplicacao: o
 * admin-core reconhece o alerta repetido por ele, e o envio local tambem o usa para nao repetir a
 * chamada dentro da janela configurada.
 */
public record RuleAlert(
        UUID unitId,
        UUID ruleId,
        RuleKind kind,
        AlertSubject subject,
        AlertSeverity severity,
        String description,
        LocalDateTime occurredAt
) {

    public RuleAlert {
        if (unitId == null || ruleId == null || kind == null || subject == null) {
            throw new IllegalArgumentException("alert requires unit, rule, kind and subject");
        }
        occurredAt = occurredAt != null ? occurredAt : LocalDateTime.now();
        severity = severity != null ? severity : AlertSeverity.MEDIUM;
    }

    /** Identifica o alerta na deduplicacao: mesma regra e mesmo assunto. */
    public UUID dedupSubject() {
        return subject.dedupKey(unitId);
    }
}
