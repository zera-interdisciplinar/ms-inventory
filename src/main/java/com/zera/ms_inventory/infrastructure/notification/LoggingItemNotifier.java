package com.zera.ms_inventory.infrastructure.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.domain.valueobject.Actor;
import com.zera.ms_inventory.core.repository.ItemNotifier;

/**
 * Adaptador provisorio: registra a decisao no log ate o cliente do admin-core existir (ZERA-256).
 * O admin-core ainda nao tem tipo de notificacao para aprovacao de item (AlertKind so tem STORAGE e
 * TIME) nem autenticacao servico-a-servico, entao enviar agora nao teria para onde ir.
 */
@Component
public class LoggingItemNotifier implements ItemNotifier {

    private static final Logger log = LoggerFactory.getLogger(LoggingItemNotifier.class);

    @Override
    public void itemApproved(Item item, Actor reviewer) {
        log.info("Item {} approved by {}; notify creator {}", item.getId(),
                reviewer != null ? reviewer.userId() : null, item.getCreatedBy());
    }

    @Override
    public void itemRejected(Item item, Actor reviewer, String reason) {
        log.info("Item {} rejected by {} ({}); notify creator {}", item.getId(),
                reviewer != null ? reviewer.userId() : null, reason, item.getCreatedBy());
    }
}
