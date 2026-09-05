package com.zera.ms_inventory.core.usecase.item;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zera.ms_inventory.Fixtures;
import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.domain.entity.Model;
import com.zera.ms_inventory.core.repository.ItemRepository;
import com.zera.ms_inventory.core.repository.ModelRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SemanticSearchInventoryImplTest {

    @Mock
    private ModelRepository modelRepository;

    @Mock
    private ItemRepository itemRepository;

    @Test
    void shouldReturnItemsOfTheMatchedModels() {
        UUID modelId = UUID.randomUUID();
        Model model = Fixtures.model(modelId, Fixtures.UNIT);
        Item item = Fixtures.item(UUID.randomUUID(), Fixtures.UNIT, model);
        when(modelRepository.semanticSearch(Fixtures.UNIT, "bateria de litio", 5)).thenReturn(List.of(model));
        when(itemRepository.findAllByModelIds(Fixtures.UNIT, List.of(modelId))).thenReturn(List.of(item));

        List<Item> result = new SemanticSearchInventoryImpl(modelRepository, itemRepository)
                .execute(Fixtures.UNIT, "bateria de litio", 5);

        assertEquals(1, result.size());
        assertEquals(item.getId(), result.get(0).getId());
    }

    @Test
    void shouldPropagateTheUnitToBothRepositories() {
        when(modelRepository.semanticSearch(Fixtures.OTHER_UNIT, "qualquer", 5)).thenReturn(List.of());
        when(itemRepository.findAllByModelIds(Fixtures.OTHER_UNIT, List.of())).thenReturn(List.of());

        List<Item> result = new SemanticSearchInventoryImpl(modelRepository, itemRepository)
                .execute(Fixtures.OTHER_UNIT, "qualquer", 5);

        assertTrue(result.isEmpty());
        verify(modelRepository).semanticSearch(Fixtures.OTHER_UNIT, "qualquer", 5);
        verify(itemRepository).findAllByModelIds(Fixtures.OTHER_UNIT, List.of());
    }
}
