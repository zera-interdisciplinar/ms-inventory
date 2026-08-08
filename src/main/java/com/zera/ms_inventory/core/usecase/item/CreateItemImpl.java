package com.zera.ms_inventory.core.usecase.item;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.domain.valueobject.Barcode;
import com.zera.ms_inventory.core.domain.valueobject.ItemStatus;
import com.zera.ms_inventory.core.repository.ItemRepository;

public class CreateItemImpl implements CreateItem {
    private final ItemRepository itemRepository;

    public CreateItemImpl(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @Override
    public Item execute(Barcode barcode, ItemStatus status, UUID unitId, LocalDateTime createdAt, LocalDateTime updatedAt,
                         LocalDateTime lastEventAt, LocalDateTime nextPredictionDate, Integer manufacturingDate,
                         Integer usageIntensity, String serialNumber, LocalDate acquiredAt) {
        Item item = new Item(UUID.randomUUID(), barcode, status, unitId, createdAt, updatedAt, lastEventAt,
                nextPredictionDate, manufacturingDate, usageIntensity, serialNumber, acquiredAt);
        return itemRepository.save(item);
    }
}
