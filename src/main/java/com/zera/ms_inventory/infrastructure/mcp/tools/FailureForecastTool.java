package com.zera.ms_inventory.infrastructure.mcp.tools;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.usecase.item.FindAllItems;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;

/**
 * A previsao de quebra existia no item e nenhuma ferramenta a consultava, entao a pergunta mais
 * direta do gestor — "o que vai quebrar primeiro?" — nao tinha resposta pelo assistente.
 */
@Component
public class FailureForecastTool {

    private static final int DEFAULT_HORIZON_DAYS = 180;
    private static final int DEFAULT_LIMIT = 20;

    private final FindAllItems findAllItems;

    public FailureForecastTool(FindAllItems findAllItems) {
        this.findAllItems = findAllItems;
    }

    @McpTool(
        name = "failure_forecast",
        description = "List the unit's items predicted to fail within a horizon, soonest first, with the "
                + "days remaining and when the prediction was last refreshed. Items with no prediction are "
                + "reported as a count, not silently dropped.",
        annotations = @McpTool.McpAnnotations(
            readOnlyHint = true,
            title = "Failure Forecast"
        )
    )
    public FailureForecast getFailureForecast(
            @McpToolParam(description = McpToolScope.UNIT_ID_DESCRIPTION, required = true) UUID unitId,
            @McpToolParam(description = "Days ahead to look (default: 180). Predictions already overdue "
                    + "are always included.", required = false) Integer horizonDays,
            @McpToolParam(description = "Maximum number of results (default: 20)", required = false)
                    Integer limit) {

        List<Item> items = findAllItems.execute(McpToolScope.require(unitId));
        LocalDate today = LocalDate.now();
        LocalDate deadline = today.plusDays(horizonDays == null || horizonDays < 0
                ? DEFAULT_HORIZON_DAYS : horizonDays);
        int maxResults = limit == null || limit <= 0 ? DEFAULT_LIMIT : limit;

        List<ForecastEntry> forecast = items.stream()
                .filter(item -> item.getStatus() != null && item.getStatus().isActive())
                .filter(item -> item.getPredictedFailureDate() != null)
                // ja vencida entra sempre: e exatamente o item que ninguem deveria ter esquecido
                .filter(item -> !item.getPredictedFailureDate().isAfter(deadline))
                .sorted(Comparator.comparing(Item::getPredictedFailureDate))
                .limit(maxResults)
                .map(item -> new ForecastEntry(
                        item.getId(), item.getDisplayCode(), item.getName(),
                        item.getModel() == null ? null : item.getModel().getName(),
                        item.getStatus().name(),
                        item.getPredictedFailureDate(),
                        ChronoUnit.DAYS.between(today, item.getPredictedFailureDate()),
                        item.getPredictionUpdatedAt()))
                .toList();

        long withoutPrediction = items.stream()
                .filter(item -> item.getStatus() != null && item.getStatus().isActive())
                .filter(item -> item.getPredictedFailureDate() == null)
                .count();

        return new FailureForecast(forecast, withoutPrediction);
    }

    /**
     * {@code itemsWithoutPrediction} vem junto de proposito: uma lista curta pode significar frota
     * saudavel ou previsao que nunca rodou, e o assistente nao teria como distinguir as duas.
     */
    public record FailureForecast(List<ForecastEntry> items, long itemsWithoutPrediction) {
    }

    public record ForecastEntry(
            UUID itemId,
            String displayCode,
            String name,
            String modelName,
            String status,
            LocalDate predictedFailureDate,
            long daysUntilPredictedFailure,
            LocalDateTime predictionUpdatedAt
    ) {
    }
}
