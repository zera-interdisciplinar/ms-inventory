package com.zera.ms_inventory.core.repository;

import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.domain.valueobject.Actor;

/**
 * Avisa quem cadastrou o item sobre a decisao do gestor, e avisa o gestor quando ha um cadastro
 * novo esperando por essa decisao. O envio real para o admin-core entra na ZERA-256, junto com os
 * tipos de notificacao novos (ZERA-265); ate la o adaptador so registra log, porque a decisao nao
 * pode falhar por causa do aviso.
 */
public interface ItemNotifier {
    void itemApproved(Item item, Actor reviewer);
    void itemRejected(Item item, Actor reviewer, String reason);

    /**
     * Item (e o modelo vinculado, quando tambem novo) submetido por um colaborador e aguardando
     * aprovacao do gestor da unidade. Aprovar/reprovar o item decide o modelo junto, entao um unico
     * aviso cobre os dois.
     */
    void itemPendingApproval(Item item, Actor submitter);
}
