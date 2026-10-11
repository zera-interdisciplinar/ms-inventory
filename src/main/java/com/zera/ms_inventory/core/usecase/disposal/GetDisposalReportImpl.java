package com.zera.ms_inventory.core.usecase.disposal;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.zera.ms_inventory.core.domain.entity.Category;
import com.zera.ms_inventory.core.domain.entity.Disposal;
import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.domain.entity.Model;
import com.zera.ms_inventory.core.domain.exception.ItemNotFoundException;
import com.zera.ms_inventory.core.domain.valueobject.DisposedItem;
import com.zera.ms_inventory.core.domain.valueobject.ItemCondition;
import com.zera.ms_inventory.core.usecase.item.FindItemById;

/**
 * O GET do descarte congela so patrimonio, nome e peso. Marca, modelo, serie e condicao continuam
 * no item, e o PDF precisa dos dois juntos. Cotacao e prazo nao tem coluna: ficam vazios.
 */
@Service
public class GetDisposalReportImpl implements GetDisposalReport {

    private final FindDisposalById findDisposalById;
    private final FindItemById findItemById;

    public GetDisposalReportImpl(FindDisposalById findDisposalById, FindItemById findItemById) {
        this.findDisposalById = findDisposalById;
        this.findItemById = findItemById;
    }

    @Override
    public DisposalReport execute(UUID unitId, UUID disposalId) {
        Disposal disposal = findDisposalById.execute(unitId, disposalId);
        String origin = unitId.toString();
        List<DisposedItem> disposedItems = disposal.getItems();
        List<DisposalReport.Line> lines = new ArrayList<>(disposedItems.size());
        for (int index = 0; index < disposedItems.size(); index++) {
            lines.add(line(index + 1, disposedItems.get(index), origin, unitId));
        }
        String issuedAt = disposal.getCreatedAt() == null ? "" : disposal.getCreatedAt().toLocalDate().toString();
        return new DisposalReport("", issuedAt, "", "", text(disposal.getCreatedByName()), List.copyOf(lines));
    }

    /**
     * Item removido depois do descarte nao apaga a linha: o snapshot ainda tem nome e patrimonio.
     * O estado DISPOSED vale para todo item desta lista, entao a situacao do PDF e a condicao fisica.
     */
    private DisposalReport.Line line(int number, DisposedItem disposed, String origin, UUID unitId) {
        String title = "Item " + number + " - " + text(disposed.name());
        String assetNumber = text(disposed.displayCode());
        Item item = findItem(unitId, disposed.itemId());
        if (item == null) {
            return new DisposalReport.Line(title, "", "", "", "1", assetNumber, "", origin, "", "");
        }
        Model model = item.getModel();
        Category category = model == null ? null : model.getCategory();
        ItemCondition condition = item.getCondition();
        return new DisposalReport.Line(
                title,
                category == null ? "" : text(category.getName()),
                model == null ? "" : text(model.getManufacturer()),
                model == null ? "" : text(model.getName()),
                "1",
                assetNumber,
                text(item.getSerialNumber()),
                origin,
                condition == null ? "" : condition.name(),
                text(item.getNotes()));
    }

    private Item findItem(UUID unitId, UUID itemId) {
        try {
            return findItemById.execute(unitId, itemId);
        } catch (ItemNotFoundException missing) {
            return null;
        }
    }

    private static String text(String value) {
        return value == null ? "" : value;
    }
}
