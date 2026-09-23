package com.zera.ms_inventory.infrastructure.persistence.neo4j.repository;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.zera.ms_inventory.core.domain.entity.Disposal;
import com.zera.ms_inventory.core.domain.exception.ItemNotFoundException;
import com.zera.ms_inventory.core.domain.valueobject.DisposedItem;
import com.zera.ms_inventory.core.domain.valueobject.PageResult;
import com.zera.ms_inventory.core.domain.valueobject.Pagination;
import com.zera.ms_inventory.core.repository.DisposalRepository;
import com.zera.ms_inventory.infrastructure.persistence.neo4j.entity.DisposalNode;
import com.zera.ms_inventory.infrastructure.persistence.neo4j.entity.DisposedItemRelationship;
import com.zera.ms_inventory.infrastructure.persistence.neo4j.entity.ItemNode;
import com.zera.ms_inventory.infrastructure.persistence.neo4j.mapper.DisposalMapper;

@Repository
public class DisposalRepositoryImpl implements DisposalRepository {

    private final DisposalNeo4jRepository neo4jRepository;
    private final ItemNeo4jRepository itemNeo4jRepository;
    private final DisposalMapper mapper;

    public DisposalRepositoryImpl(DisposalNeo4jRepository neo4jRepository,
                                  ItemNeo4jRepository itemNeo4jRepository,
                                  DisposalMapper mapper) {
        this.neo4jRepository = neo4jRepository;
        this.itemNeo4jRepository = itemNeo4jRepository;
        this.mapper = mapper;
    }

    /**
     * Os itens de um descarte nao mudam depois de registrados: o unico ajuste e o destino. Por isso
     * a correcao reaproveita o no ja gravado, com as arestas e os seus ids internos, em vez de
     * remontar a colecao — arestas remontadas nao tem id e o SDN as gravaria como novas,
     * duplicando os pesos e inflando os indicadores de kg.
     */
    @Override
    @Transactional
    public Disposal save(Disposal disposal) {
        Optional<DisposalNode> stored = neo4jRepository.findByIdAndUnitId(disposal.getId(), disposal.getUnitId());
        if (stored.isPresent()) {
            DisposalNode node = stored.get();
            node.setDestination(disposal.getDestination());
            node.setUpdatedAt(disposal.getUpdatedAt());
            return mapper.toDomain(neo4jRepository.save(node));
        }
        return mapper.toDomain(neo4jRepository.save(withItems(disposal)));
    }

    /** Resolve cada item pelo par (id, unidade) para anexar o no ja gravado, nao um parcial do mapper. */
    private DisposalNode withItems(Disposal disposal) {
        DisposalNode node = mapper.toNode(disposal);
        Set<DisposedItemRelationship> relationships = new HashSet<>();
        for (DisposedItem item : disposal.getItems()) {
            ItemNode itemNode = itemNeo4jRepository.findByIdAndUnitId(item.itemId(), disposal.getUnitId())
                    .orElseThrow(() -> new ItemNotFoundException(item.itemId()));
            relationships.add(mapper.toRelationship(itemNode, item.weightKg()));
        }
        node.setItems(relationships);
        return node;
    }

    @Override
    public Optional<Disposal> findById(UUID unitId, UUID id) {
        return neo4jRepository.findByIdAndUnitId(id, unitId).map(mapper::toDomain);
    }

    @Override
    public PageResult<Disposal> findPage(UUID unitId, Pagination pagination) {
        long total = neo4jRepository.countByUnit(unitId);
        if (total == 0) {
            return new PageResult<>(List.of(), pagination.page(), pagination.size(), 0);
        }
        List<DisposalNode> nodes = neo4jRepository.findPageByUnit(unitId,
                (long) pagination.page() * pagination.size(), pagination.size());
        return new PageResult<>(nodes.stream().map(mapper::toDomain).toList(), pagination.page(),
                pagination.size(), total);
    }
}
