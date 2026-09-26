package com.zera.ms_inventory.infrastructure.admincore;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zera.ms_inventory.Fixtures;
import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.domain.entity.RuleAlert;
import com.zera.ms_inventory.core.domain.valueobject.AlertSeverity;
import com.zera.ms_inventory.core.domain.valueobject.AlertSubject;
import com.zera.ms_inventory.core.domain.valueobject.RuleKind;
import com.zera.ms_inventory.core.repository.AlertGateway;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AdminCoreItemNotifierTest {

    @Mock private AdminCoreClient client;

    private static AdminCoreProperties properties(String secret) {
        return new AdminCoreProperties(true, "http://admin-core", "ms-inventory", secret,
                Duration.ofSeconds(1), 1, Duration.ofMillis(1), Duration.ofMinutes(1));
    }

    private Item itemDoOperario() {
        Item item = Fixtures.item(UUID.randomUUID(), Fixtures.UNIT);
        item.assignDisplayCode("100001");
        item.registerBy(Fixtures.OPERATOR);
        return item;
    }

    @Test
    void shouldNotifyTheCreatorOnApproval() {
        Item item = itemDoOperario();

        new AdminCoreItemNotifier(client, properties("segredo")).itemApproved(item, Fixtures.MANAGER);

        ArgumentCaptor<Map<String, Object>> payload = ArgumentCaptor.forClass(Map.class);
        verify(client).postAlert(payload.capture());
        assertThat(payload.getValue())
                .containsEntry("kind", "ITEM_APPROVED")
                .containsEntry("userId", Fixtures.OPERATOR.userId().toString())
                .containsEntry("eventId", item.getId().toString())
                .containsEntry("ruleId", null);
        assertThat((String) payload.getValue().get("description")).contains("100001").contains("aprovado");
    }

    @Test
    void shouldCarryTheReasonOnRejection() {
        Item item = itemDoOperario();

        new AdminCoreItemNotifier(client, properties("segredo"))
                .itemRejected(item, Fixtures.MANAGER, "Foto ilegivel");

        ArgumentCaptor<Map<String, Object>> payload = ArgumentCaptor.forClass(Map.class);
        verify(client).postAlert(payload.capture());
        assertThat(payload.getValue()).containsEntry("kind", "ITEM_REJECTED")
                .containsEntry("severity", "MEDIUM");
        assertThat((String) payload.getValue().get("description")).contains("Foto ilegivel");
    }

    /** Item migrado de antes do createdBy nao tem para quem avisar. */
    @Test
    void shouldOnlyLogWhenTheItemHasNoCreator() {
        Item semAutor = Fixtures.item(UUID.randomUUID(), Fixtures.UNIT);

        new AdminCoreItemNotifier(client, properties("segredo")).itemApproved(semAutor, Fixtures.MANAGER);

        verify(client, never()).postAlert(any());
    }

    @Test
    void shouldOnlyLogWhenTheIntegrationIsNotConfigured() {
        new AdminCoreItemNotifier(client, properties("")).itemApproved(itemDoOperario(), Fixtures.MANAGER);

        verify(client, never()).postAlert(any());
    }

    /** Sem a integracao ligada o alerta vai para o log e o job segue o fluxo normal. */
    @Test
    void shouldLetTheLoggingGatewayReportSuccess() {
        AlertGateway gateway = new LoggingAlertGateway();

        assertThat(gateway.send(new RuleAlert(Fixtures.UNIT, UUID.randomUUID(), RuleKind.STALE_ITEM,
                AlertSubject.unit(), AlertSeverity.LOW, "parado", null))).isTrue();
    }
}
