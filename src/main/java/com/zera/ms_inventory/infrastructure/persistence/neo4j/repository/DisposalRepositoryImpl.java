package com.zera.ms_inventory.infrastructure.persistence.neo4j.repository;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.neo4j.driver.Record;
import org.neo4j.driver.Value;
import org.neo4j.driver.types.TypeSystem;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.zera.ms_inventory.core.domain.entity.Disposal;
import com.zera.ms_inventory.core.domain.exception.ItemNotFoundException;
import com.zera.ms_inventory.core.domain.valueobject.DestinationType;
import com.zera.ms_inventory.core.domain.valueobject.DisposedItem;
import com.zera.ms_inventory.core.domain.valueobject.DisposedWeight;
import com.zera.ms_inventory.core.domain.valueobject.MaterialCode;
import com.zera.ms_inventory.core.domain.valueobject.PageResult;
import com.zera.ms_inventory.core.domain.valueobject.Pagination;
import com.zera.ms_inventory.core.repository.DisposalRepository;
import com.zera.ms_inventory.infrastructure.persistence.neo4j.entity.DisposalNode;
import com.zera.ms_inventory.infrastructure.persistence.neo4j.entity.DisposedItemRelationship;
import com.zera.ms_inventory.infrastructure.persistence.neo4j.entity.ItemNode;
import com.zera.ms_inventory.infrastructure.persistence.neo4j.mapper.DisposalMapper;

@Repository
public class DisposalRepositoryImpl implements DisposalRepository {

    /** Uma linha por item descartado: o agrupamento por (descarte, item) evita juntar itens iguais. */
    private static final String DISPOSED_WEIGHTS = """
            MATCH (d:Disposal {unitId: $unitId})-[inc:INCLUDES]->(i:Item)
            WHERE d.disposedAt >= $from AND d.disposedAt <= $to
            OPTIONAL MATCH (i)-[:IS_MODEL]->(:Model)-[:MADE_OF]->(mat:Material)
            WITH d, i, inc.weightKg AS weightKg, collect(DISTINCT mat.code) AS materials
            RETURN d.destination AS destination, d.disposedAt AS disposedAt, weightKg, materials
            """;

    private final DisposalNeo4jRepository neo4jRepository;
    private final ItemNeo4jRepository itemNeo4jRepository;
    private final DisposalMapper mapper;
    private final Neo4jClient neo4jClient;

    public DisposalRepositoryImpl(DisposalNeo4jRepository neo4jRepository,
                                  ItemNeo4jRepository itemNeo4jRepository,
                                  DisposalMapper mapper,
                                  Neo4jClient neo4jClient) {
        this.neo4jRepository = neo4jRepository;
        this.itemNeo4jRepository = itemNeo4jRepository;
        this.mapper = mapper;
        this.neo4jClient = neo4jClient;
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

    /**
     * Read model dos indicadores: nao e um no, entao vai pelo Neo4jClient com mapeamento explicito.
     * Projecao por interface do Neo4jRepository nao serve aqui, porque ela e resolvida contra o
     * DisposalNode e nao contra as colunas da consulta.
     */
    @Override
    public List<DisposedWeight> findDisposedWeights(UUID unitId, LocalDate from, LocalDate to) {
        return List.copyOf(neo4jClient.query(DISPOSED_WEIGHTS)
                .bindAll(Map.of("unitId", unitId.toString(), "from", from, "to", to))
                .fetchAs(DisposedWeight.class)
                .mappedBy(DisposalRepositoryImpl::toDisposedWeight)
                .all());
    }

    static DisposedWeight toDisposedWeight(TypeSystem typeSystem, Record row) {
        Value peso = row.get("weightKg");
        return new DisposedWeight(
                DestinationType.valueOf(row.get("destination").asString()),
                row.get("disposedAt").asLocalDate(),
                peso.isNull() ? null : peso.asDouble(),
                row.get("materials").asList(Value::asString).stream().map(MaterialCode::valueOf).toList());
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
