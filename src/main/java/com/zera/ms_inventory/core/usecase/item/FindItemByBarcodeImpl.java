package com.zera.ms_inventory.core.usecase.item;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.domain.exception.ItemNotFoundException;
import com.zera.ms_inventory.core.repository.ItemRepository;

/** Leitura do scanner: encontrou abre o item; 404 leva o app para o cadastro. */
@Service
public class FindItemByBarcodeImpl implements FindItemByBarcode {
    private final ItemRepository itemRepository;

    public FindItemByBarcodeImpl(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @Override
    public Item execute(UUID unitId, String barcode) {
        return itemRepository.findByBarcode(unitId, barcode)
                .orElseThrow(() -> ItemNotFoundException.withBarcode(barcode));
    }
}
