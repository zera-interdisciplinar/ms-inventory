package com.zera.ms_inventory.core.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.domain.valueobject.ItemFilter;
import com.zera.ms_inventory.core.domain.valueobject.PageResult;
import com.zera.ms_inventory.core.domain.valueobject.Pagination;

/**
 * Toda leitura e escrita e escopada por unidade: nao existe caminho sem unitId. A unica excecao e
 * {@link #existsAnyWithId}, que so responde se um id ja esta ocupado, sem expor o item.
 */
public interface ItemRepository {
    Item save(Item item);
    Optional<Item> findById(UUID unitId, UUID id);
    List<Item> findAll(UUID unitId);
    PageResult<Item> findPage(UUID unitId, ItemFilter filter, Pagination pagination);
    PageResult<Item> findPageByModel(UUID unitId, UUID modelId, Pagination pagination);
    boolean existsByModel(UUID unitId, UUID modelId);
    /** Quantos itens usam o modelo; a reprovacao em cascata so cai num modelo sem outros itens. */
    long countByModel(UUID unitId, UUID modelId);
    boolean existsByDisplayCode(UUID unitId, String displayCode);
    boolean existsAnyWithId(UUID id);
    Optional<Item> findByBarcode(UUID unitId, String barcode);
    List<Item> findAllByModelIds(UUID unitId, List<UUID> modelIds);
    void deleteById(UUID unitId, UUID id);
}
