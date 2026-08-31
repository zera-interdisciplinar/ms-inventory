package com.zera.ms_inventory.infrastructure.mcp.tools;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.domain.valueobject.Barcode;
import com.zera.ms_inventory.core.domain.valueobject.ItemStatus;
import com.zera.ms_inventory.core.usecase.item.FindItemById;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetItemDetailsToolTest {

    @Mock
    private FindItemById findItemById;

    @Test
    void shouldReturnItemDetails() {
        UUID itemId = UUID.randomUUID();
        Item item = new Item(itemId, new Barcode("BC-1"), ItemStatus.OK, null,
            null, null, null, null, "SN-1", LocalDate.now());
        when(findItemById.execute(itemId)).thenReturn(item);

        GetItemDetailsTool tool = new GetItemDetailsTool(findItemById);

        assertEquals(item, tool.getItemDetails(itemId));
    }
}
