package com.zera.ms_inventory.core.repository;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Registro local do ultimo envio por (regra, assunto). O admin-core ja reconhece o alerta repetido,
 * mas sem este registro o job refaria a chamada HTTP a cada execucao para tudo que continua fora do
 * limite.
 */
public interface AlertDispatchLog {

    boolean sentSince(UUID unitId, UUID ruleId, UUID subjectId, LocalDateTime since);

    void recordSent(UUID unitId, UUID ruleId, UUID subjectId, LocalDateTime sentAt);
}
