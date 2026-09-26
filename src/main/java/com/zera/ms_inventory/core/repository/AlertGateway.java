package com.zera.ms_inventory.core.repository;

import com.zera.ms_inventory.core.domain.entity.RuleAlert;

/**
 * Envio do alerta para fora. A falha nao propaga: um alerta que nao saiu nao pode derrubar o job
 * nem o descarte que o disparou.
 */
public interface AlertGateway {
    /** {@code true} quando o alerta saiu; {@code false} quando falhou depois das tentativas. */
    boolean send(RuleAlert alert);
}
