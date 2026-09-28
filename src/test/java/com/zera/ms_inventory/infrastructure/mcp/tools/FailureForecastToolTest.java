package com.zera.ms_inventory.infrastructure.mcp.tools;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zera.ms_inventory.Fixtures;
import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.domain.valueobject.Barcode;
import com.zera.ms_inventory.core.domain.valueobject.ItemStatus;
import com.zera.ms_inventory.core.usecase.item.FindAllItems;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FailureForecastToolTest {

    @Mock
    private FindAllItems findAllItems;

    private static Item item(ItemStatus status, LocalDate predictedFailureDate) {
        Item item = new Item(UUID.randomUUID(), new Barcode("123456"), status, Fixtures.UNIT,
                Fixtures.model(Fixtures.UNIT), null, 2024, 6, "SN", LocalDate.now().minusYears(1));
        if (predictedFailureDate != null) {
            item.recordPrediction(predictedFailureDate);
        }
        return item;
    }

    private void unitWith(Item... items) {
        when(findAllItems.execute(Fixtures.UNIT)).thenReturn(List.of(items));
    }

    @Test
    void shouldListTheSoonestFailuresFirstWithTheDaysRemaining() {
        unitWith(
                item(ItemStatus.IN_STOCK, LocalDate.now().plusDays(60)),
                item(ItemStatus.IN_STOCK, LocalDate.now().plusDays(10)));

        var forecast = new FailureForecastTool(findAllItems).getFailureForecast(Fixtures.UNIT, null, null);

        assertThat(forecast.items()).hasSize(2);
        assertThat(forecast.items().get(0).daysUntilPredictedFailure()).isEqualTo(10);
        assertThat(forecast.items().get(1).daysUntilPredictedFailure()).isEqualTo(60);
    }

    @Test
    void shouldLeaveOutWhatFallsBeyondTheHorizon() {
        unitWith(
                item(ItemStatus.IN_STOCK, LocalDate.now().plusDays(10)),
                item(ItemStatus.IN_STOCK, LocalDate.now().plusDays(400)));

        var forecast = new FailureForecastTool(findAllItems).getFailureForecast(Fixtures.UNIT, 30, null);

        assertThat(forecast.items()).hasSize(1);
    }

    /** Previsao ja vencida e exatamente o item que ninguem deveria ter esquecido. */
    @Test
    void shouldAlwaysIncludeOverduePredictions() {
        unitWith(item(ItemStatus.IN_STOCK, LocalDate.now().minusDays(30)));

        var forecast = new FailureForecastTool(findAllItems).getFailureForecast(Fixtures.UNIT, 7, null);

        assertThat(forecast.items()).hasSize(1);
        assertThat(forecast.items().get(0).daysUntilPredictedFailure()).isEqualTo(-30);
    }

    /** Item descartado nao vai quebrar; manter na lista so gastaria a atencao do gestor. */
    @Test
    void shouldIgnoreItemsThatAreNoLongerActive() {
        unitWith(
                item(ItemStatus.DISPOSED, LocalDate.now().plusDays(5)),
                item(ItemStatus.IN_STOCK, LocalDate.now().plusDays(5)));

        var forecast = new FailureForecastTool(findAllItems).getFailureForecast(Fixtures.UNIT, null, null);

        assertThat(forecast.items()).hasSize(1);
        assertThat(forecast.items().get(0).status()).isEqualTo("IN_STOCK");
    }

    /**
     * Lista curta pode significar frota saudavel ou previsao que nunca rodou; sem esta contagem o
     * assistente nao teria como distinguir as duas.
     */
    @Test
    void shouldReportHowManyActiveItemsHaveNoPredictionAtAll() {
        unitWith(
                item(ItemStatus.IN_STOCK, LocalDate.now().plusDays(5)),
                item(ItemStatus.IN_STOCK, null),
                item(ItemStatus.DISPOSED, null));

        var forecast = new FailureForecastTool(findAllItems).getFailureForecast(Fixtures.UNIT, null, null);

        assertThat(forecast.itemsWithoutPrediction()).isEqualTo(1);
    }

    @Test
    void shouldHonourTheResultLimit() {
        unitWith(
                item(ItemStatus.IN_STOCK, LocalDate.now().plusDays(5)),
                item(ItemStatus.IN_STOCK, LocalDate.now().plusDays(6)));

        var forecast = new FailureForecastTool(findAllItems).getFailureForecast(Fixtures.UNIT, null, 1);

        assertThat(forecast.items()).hasSize(1);
    }

    @Test
    void shouldRejectMissingUnitIdInsteadOfFallingBackToAGlobalRead() {
        FailureForecastTool tool = new FailureForecastTool(findAllItems);

        assertThrows(IllegalArgumentException.class, () -> tool.getFailureForecast(null, null, null));
        verifyNoInteractions(findAllItems);
    }
}
