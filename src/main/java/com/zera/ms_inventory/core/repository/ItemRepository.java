package com.zera.ms_inventory.core.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.zera.ms_inventory.core.domain.entity.Item;

public interface ItemRepository {
    Item save(Item item);
    Optional<Item> findById(UUID id);
    List<Item> findAll();
    void deleteById(UUID id);
}
