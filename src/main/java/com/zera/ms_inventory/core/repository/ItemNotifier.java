package com.zera.ms_inventory.core.repository;

import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.domain.valueobject.Actor;

/**
 * Avisa quem cadastrou o item sobre a decisao do gestor. O envio real para o admin-core entra na
 * ZERA-256, junto com os tipos de notificacao novos (ZERA-265); ate la o adaptador so registra log,
 * porque a decisao nao pode falhar por causa do aviso.
 */
public interface ItemNotifier {
    void itemApproved(Item item, Actor reviewer);
    void itemRejected(Item item, Actor reviewer, String reason);
}
