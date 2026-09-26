package com.zera.ms_inventory.infrastructure.mcp.tools;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.zera.ms_inventory.core.domain.valueobject.ItemCondition;
import com.zera.ms_inventory.core.domain.valueobject.ItemFilter;
import com.zera.ms_inventory.core.domain.valueobject.ItemStatus;
import com.zera.ms_inventory.core.domain.valueobject.PageResult;
import com.zera.ms_inventory.core.domain.valueobject.Pagination;
import com.zera.ms_inventory.core.usecase.item.ListItems;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;

@Component
public class SearchInventoryTool {

    private static final int DEFAULT_LIMIT = 20;

    private final ListItems listItems;

    public SearchInventoryTool(ListItems listItems) {
        this.listItems = listItems;
    }

    // a busca era feita em memoria sobre a unidade inteira e so aceitava numero de serie; agora
    // desce como filtro para o banco, que e o mesmo caminho da tela de itens
    @McpTool(
        name = "search_inventory",
        description = "Search inventory items by status, physical condition and/or a text term matching "
                + "short code, item name, model name or material. Returns one page with the total count. "
                + "Use semantic_search_inventory instead when the user describes the equipment in natural language.",
        annotations = @McpTool.McpAnnotations(
            readOnlyHint = true,
            title = "Search Inventory"
        )
    )
    public InventoryPage searchInventory(
            @McpToolParam(description = McpToolScope.UNIT_ID_DESCRIPTION, required = true) UUID unitId,
            @McpToolParam(description = "Filter by item status: " + McpToolScope.STATUS_VALUES + ". Optional.",
                    required = false) String status,
            @McpToolParam(description = "Filter by physical condition: " + McpToolScope.CONDITION_VALUES + ". Optional.",
                    required = false) String condition,
            @McpToolParam(description = "Text term matching short code, item name, model name or material. Optional.",
                    required = false) String query,
            @McpToolParam(description = "Maximum number of results (default: 20, max: 100)", required = false)
                    Integer limit,
            @McpToolParam(description = "Page number, zero-based (default: 0)", required = false) Integer page) {

        ItemFilter filter = new ItemFilter(parse(ItemStatus.class, status), null, null, query, null, null,
                parse(ItemCondition.class, condition));
        int size = Math.clamp(limit == null ? DEFAULT_LIMIT : limit, 1, Pagination.MAX_SIZE);
        Pagination pagination = new Pagination(Math.max(0, page == null ? 0 : page), size);

        PageResult<McpItemView> result = listItems.execute(McpToolScope.require(unitId), filter, pagination)
                .map(McpItemView::of);
        return new InventoryPage(result.content(), result.totalElements(), result.page(), result.size());
    }

    /** Valor fora do enum vira filtro nulo em vez de excecao: o assistente erra o nome as vezes. */
    private static <E extends Enum<E>> E parse(Class<E> type, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Enum.valueOf(type, value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public record InventoryPage(List<McpItemView> items, long totalItems, int page, int size) {
    }
}
