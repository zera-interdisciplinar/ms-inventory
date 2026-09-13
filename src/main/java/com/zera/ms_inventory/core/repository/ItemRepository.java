package com.zera.ms_inventory.core.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.domain.valueobject.PageResult;
import com.zera.ms_inventory.core.domain.valueobject.Pagination;

/** Toda leitura e escrita e escopada por unidade: nao existe caminho sem unitId. */
public interface ItemRepository {
    Item save(Item item);
    Optional<Item> findById(UUID unitId, UUID id);
    List<Item> findAll(UUID unitId);
    PageResult<Item> findPage(UUID unitId, Pagination pagination);
    PageResult<Item> findPageByModel(UUID unitId, UUID modelId, Pagination pagination);
    List<Item> findAllByModelIds(UUID unitId, List<UUID> modelIds);
    void deleteById(UUID unitId, UUID id);
}
