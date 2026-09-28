package com.zera.ms_inventory.core.usecase.prediction;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zera.ms_inventory.Fixtures;
import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.domain.entity.Model;
import com.zera.ms_inventory.core.domain.valueobject.Barcode;
import com.zera.ms_inventory.core.domain.valueobject.ItemStatus;
import com.zera.ms_inventory.core.domain.valueobject.PredictionInput;
import com.zera.ms_inventory.core.domain.valueobject.PredictionOutcome;
import com.zera.ms_inventory.core.repository.FailurePredictionGateway;
import com.zera.ms_inventory.core.repository.ItemRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateFailurePredictionsImplTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private FailurePredictionGateway gateway;

    private UpdateFailurePredictionsImpl useCase(int batchSize) {
        return new UpdateFailurePredictionsImpl(itemRepository, gateway, "TROPICAL", batchSize);
    }

    private static Model model(String manufacturer, String name) {
        return new Model(UUID.randomUUID(), Fixtures.UNIT, name, manufacturer, 24, 60, Set.of(), null, null,
                Fixtures.category(Fixtures.UNIT));
    }

    private static Item item(Model model, Integer year, Integer usage, LocalDate acquiredAt) {
        return new Item(UUID.randomUUID(), new Barcode("123456"), ItemStatus.IN_STOCK, Fixtures.UNIT, model,
                null, year, usage, "SN", acquiredAt);
    }

    private static Item complete() {
        return item(model("Acme", "Latitude"), 2021, 7, LocalDate.of(2022, 1, 10));
    }

    private void unitWith(Item... items) {
        when(itemRepository.unitsWithItems()).thenReturn(List.of(Fixtures.UNIT));
        when(itemRepository.findAll(Fixtures.UNIT)).thenReturn(List.of(items));
    }

    private void answering(double months) {
        when(gateway.predict(anyList())).thenAnswer(call -> {
            List<PredictionInput> inputs = call.getArgument(0);
            return inputs.stream().map(i -> PredictionOutcome.predicted(i.itemId(), months)).toList();
        });
    }

    @Test
    void shouldStoreThePredictedDateCountedFromAcquisition() {
        Item item = complete();
        unitWith(item);
        answering(24);

        PredictionResult result = useCase(100).execute();

        verify(itemRepository).save(item);
        assertThat(item.getPredictedFailureDate()).isEqualTo(LocalDate.of(2024, 1, 10));
        assertThat(item.getPredictionUpdatedAt()).isNotNull();
        assertThat(result.predictionsStored()).isEqualTo(1);
    }

    /** O contrato manda a zona climatica por item, mas ela e configuracao do servico. */
    @Test
    void shouldSendTheConfiguredClimateZoneAndTheContractFields() {
        unitWith(complete());
        answering(12);

        useCase(100).execute();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<PredictionInput>> batch = ArgumentCaptor.forClass(List.class);
        verify(gateway).predict(batch.capture());
        PredictionInput sent = batch.getValue().get(0);
        assertThat(sent.climateZone()).isEqualTo("TROPICAL");
        assertThat(sent.category()).isEqualTo(Fixtures.category(Fixtures.UNIT).getName());
        assertThat(sent.manufacturer()).isEqualTo("Acme");
        assertThat(sent.usageIntensity()).isEqualTo(7);
        assertThat(sent.manufacturingYear()).isEqualTo(2021);
    }

    /**
     * Item incompleto nao vai para o modelo: o preditivo recusaria a linha, ou pior, responderia
     * sobre um chute nosso, e o chute viraria data de quebra na tela do gestor.
     */
    @Test
    void shouldSkipItemsMissingAnyFieldOfTheContract() {
        unitWith(
                item(model("Acme", "Latitude"), null, 7, LocalDate.now()),
                item(model("Acme", "Latitude"), 2021, null, LocalDate.now()),
                item(model("Acme", "Latitude"), 2021, 7, null),
                item(model(null, "Latitude"), 2021, 7, LocalDate.now()),
                item(null, 2021, 7, LocalDate.now()));

        PredictionResult result = useCase(100).execute();

        verify(gateway, never()).predict(anyList());
        assertThat(result.itemsSkipped()).isEqualTo(5);
        assertThat(result.itemsEligible()).isZero();
    }

    /** Uma passagem por lote: cem itens juntos custam quase o mesmo que um. */
    @Test
    void shouldSplitTheUnitIntoBatchesOfTheConfiguredSize() {
        List<Item> items = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            items.add(complete());
        }
        unitWith(items.toArray(Item[]::new));
        answering(12);

        useCase(2).execute();

        verify(gateway, org.mockito.Mockito.times(3)).predict(anyList());
    }

    /** Gravar o mesmo valor de novo so envelheceria o updatedAt sem mudar nada. */
    @Test
    void shouldNotRewriteAPredictionThatDidNotChange() {
        Item item = complete();
        item.recordPrediction(LocalDate.of(2024, 1, 10));
        unitWith(item);
        answering(24);

        PredictionResult result = useCase(100).execute();

        verify(itemRepository, never()).save(any());
        assertThat(result.predictionsStored()).isEqualTo(1);
    }

    @Test
    void shouldCountItemsThePredictorCouldNotAnswerFor() {
        Item item = complete();
        unitWith(item);
        when(gateway.predict(anyList())).thenAnswer(call -> {
            List<PredictionInput> inputs = call.getArgument(0);
            return inputs.stream().map(i -> PredictionOutcome.failed(i.itemId(), "nope")).toList();
        });

        PredictionResult result = useCase(100).execute();

        verify(itemRepository, never()).save(any());
        assertThat(result.predictionsFailed()).isEqualTo(1);
    }

    /** Previsao e informacao acessoria: uma unidade com problema nao pode levar as outras junto. */
    @Test
    void shouldKeepGoingWhenOneUnitFails() {
        when(itemRepository.unitsWithItems()).thenReturn(List.of(Fixtures.OTHER_UNIT, Fixtures.UNIT));
        when(itemRepository.findAll(Fixtures.OTHER_UNIT)).thenThrow(new IllegalStateException("banco fora"));
        when(itemRepository.findAll(Fixtures.UNIT)).thenReturn(List.of(complete()));
        answering(12);

        PredictionResult result = useCase(100).execute();

        assertThat(result.unitsVisited()).isEqualTo(1);
        assertThat(result.predictionsStored()).isEqualTo(1);
    }

    @Test
    void shouldDoNothingWhenNoUnitHasItems() {
        when(itemRepository.unitsWithItems()).thenReturn(List.of());

        assertThat(useCase(100).execute()).isEqualTo(PredictionResult.empty());
    }
}
