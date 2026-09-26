package com.zera.ms_inventory.core.usecase.rule;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zera.ms_inventory.Fixtures;
import com.zera.ms_inventory.core.domain.entity.Disposal;
import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.domain.entity.Material;
import com.zera.ms_inventory.core.domain.entity.Model;
import com.zera.ms_inventory.core.domain.entity.Rule;
import com.zera.ms_inventory.core.domain.entity.RuleAlert;
import com.zera.ms_inventory.core.domain.valueobject.AlertSeverity;
import com.zera.ms_inventory.core.domain.valueobject.DestinationType;
import com.zera.ms_inventory.core.domain.valueobject.DisposedItem;
import com.zera.ms_inventory.core.domain.valueobject.MaterialCode;
import com.zera.ms_inventory.core.domain.valueobject.RuleKind;
import com.zera.ms_inventory.core.repository.ItemRepository;
import com.zera.ms_inventory.core.repository.RuleRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EvaluateDisposalRulesImplTest {

    @Mock private RuleRepository ruleRepository;
    @Mock private ItemRepository itemRepository;
    @Mock private AlertDispatcher dispatcher;

    private EvaluateDisposalRulesImpl useCase() {
        return new EvaluateDisposalRulesImpl(ruleRepository, itemRepository, dispatcher);
    }

    private Rule regraAtiva() {
        return new Rule(UUID.randomUUID(), Fixtures.UNIT, "Reciclavel no aterro",
                RuleKind.RECYCLABLE_TO_LANDFILL, null, null, null, true);
    }

    private Item itemCom(UUID id, boolean reciclavel) {
        Material material = new Material(UUID.randomUUID(),
                reciclavel ? MaterialCode.METAL : MaterialCode.OTHER,
                reciclavel ? "Metal" : "Outros", reciclavel, false, "guia");
        Model modelo = new Model(UUID.randomUUID(), Fixtures.UNIT, "Notebook", "Z", 24, 60,
                Set.of(material), 2.5, null, Fixtures.category(Fixtures.UNIT));
        return Fixtures.item(id, Fixtures.UNIT, modelo);
    }

    private Disposal descarte(DestinationType destino, UUID itemId) {
        return Disposal.register(Fixtures.UNIT, destino, null, null, LocalDate.now(), null,
                List.of(new DisposedItem(itemId, "100001", "Notebook", 2.5)), Fixtures.OPERATOR);
    }

    @Test
    void shouldAlertWhenRecyclableGoesToLandfill() {
        UUID itemId = UUID.randomUUID();
        when(ruleRepository.findAll(Fixtures.UNIT)).thenReturn(List.of(regraAtiva()));
        when(itemRepository.findById(Fixtures.UNIT, itemId)).thenReturn(Optional.of(itemCom(itemId, true)));

        useCase().execute(descarte(DestinationType.LANDFILL, itemId));

        ArgumentCaptor<RuleAlert> alerta = ArgumentCaptor.forClass(RuleAlert.class);
        verify(dispatcher).dispatch(alerta.capture());
        assertThat(alerta.getValue().kind()).isEqualTo(RuleKind.RECYCLABLE_TO_LANDFILL);
        assertThat(alerta.getValue().severity()).isEqualTo(AlertSeverity.HIGH);
        assertThat(alerta.getValue().description()).contains("100001");
        assertThat(alerta.getValue().dedupSubject()).isEqualTo(itemId);
    }

    @Test
    void shouldStaySilentForRecyclingAndDonation() {
        UUID itemId = UUID.randomUUID();
        lenient().when(ruleRepository.findAll(Fixtures.UNIT)).thenReturn(List.of(regraAtiva()));

        useCase().execute(descarte(DestinationType.RECYCLING, itemId));
        useCase().execute(descarte(DestinationType.DONATION, itemId));

        verify(dispatcher, never()).dispatch(any());
    }

    /** Item sem material reciclavel indo ao aterro e o destino correto: nao ha o que avisar. */
    @Test
    void shouldStaySilentForANonRecyclableItem() {
        UUID itemId = UUID.randomUUID();
        when(ruleRepository.findAll(Fixtures.UNIT)).thenReturn(List.of(regraAtiva()));
        when(itemRepository.findById(Fixtures.UNIT, itemId)).thenReturn(Optional.of(itemCom(itemId, false)));

        useCase().execute(descarte(DestinationType.LANDFILL, itemId));

        verify(dispatcher, never()).dispatch(any());
    }

    @Test
    void shouldStaySilentWhenTheUnitTurnedTheRuleOff() {
        UUID itemId = UUID.randomUUID();
        Rule desligada = new Rule(UUID.randomUUID(), Fixtures.UNIT, "x", RuleKind.RECYCLABLE_TO_LANDFILL,
                null, null, null, false);
        when(ruleRepository.findAll(Fixtures.UNIT)).thenReturn(List.of(desligada));

        useCase().execute(descarte(DestinationType.LANDFILL, itemId));

        verify(dispatcher, never()).dispatch(any());
        verify(itemRepository, never()).findById(any(), any());
    }

    @Test
    void shouldStaySilentWithoutTheRuleConfigured() {
        when(ruleRepository.findAll(Fixtures.UNIT)).thenReturn(List.of());

        useCase().execute(descarte(DestinationType.LANDFILL, UUID.randomUUID()));

        verify(dispatcher, never()).dispatch(any());
    }
}
