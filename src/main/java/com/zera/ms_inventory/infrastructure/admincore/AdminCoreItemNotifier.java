package com.zera.ms_inventory.infrastructure.admincore;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.domain.valueobject.Actor;
import com.zera.ms_inventory.core.repository.ItemNotifier;

/**
 * Notifica quem cadastrou o item sobre a decisao do gestor. Substitui o adaptador de log da
 * ZERA-245, que existia porque o admin-core ainda nao tinha esses tipos de notificacao.
 *
 * <p>Item sem autor registrado nao tem para quem avisar: acontece com o que foi migrado de antes
 * do createdBy, e nesse caso a notificacao e apenas registrada.
 */
@Component
@Primary
@ConditionalOnProperty(prefix = "zera.admin-core", name = "enabled", havingValue = "true")
public class AdminCoreItemNotifier implements ItemNotifier {

    private static final Logger log = LoggerFactory.getLogger(AdminCoreItemNotifier.class);

    private final AdminCoreClient client;
    private final AdminCoreProperties properties;

    public AdminCoreItemNotifier(AdminCoreClient client, AdminCoreProperties properties) {
        this.client = client;
        this.properties = properties;
    }

    @Override
    public void itemApproved(Item item, Actor reviewer) {
        notify(item, "ITEM_APPROVED", "LOW",
                "Seu cadastro do item %s foi aprovado".formatted(describe(item)));
    }

    @Override
    public void itemRejected(Item item, Actor reviewer, String reason) {
        notify(item, "ITEM_REJECTED", "MEDIUM",
                "Seu cadastro do item %s foi reprovado: %s".formatted(describe(item), reason));
    }

    private void notify(Item item, String kind, String severity, String description) {
        if (!properties.isConfigured() || item.getCreatedBy() == null) {
            log.info("[NOTIFY] {} item={} creator={}", kind, item.getId(), item.getCreatedBy());
            return;
        }
        // eventId e o proprio item: junto com a ausencia de regra, e o que identifica a notificacao
        client.postAlert(AdminCoreClient.alertPayload(item.getCreatedBy(), item.getUnitId(), null,
                item.getId(), kind, severity, description, LocalDateTime.now()));
    }

    private static String describe(Item item) {
        return item.getDisplayCode() != null ? item.getDisplayCode() : item.getId().toString();
    }
}
