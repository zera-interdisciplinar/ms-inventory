package com.zera.ms_inventory.infrastructure.persistence.neo4j.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.neo4j.driver.Record;
import org.neo4j.driver.types.TypeSystem;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Repository;

import com.zera.ms_inventory.core.domain.valueobject.InventoryCounts;
import com.zera.ms_inventory.core.domain.valueobject.ItemStatus;
import com.zera.ms_inventory.core.repository.DashboardRepository;

@Repository
public class DashboardRepositoryImpl implements DashboardRepository {

    /**
     * Uma passada so pelos itens da unidade. O estoque de {@code $since} e aproximado pelo item que
     * ja existia e que ou continua ativo, ou so saiu depois daquela data — o que a data do ultimo
     * evento responde sem precisar reconstruir o historico inteiro.
     */
    private static final String COUNTS = """
            MATCH (i:Item {unitId: $unitId})
            RETURN
              sum(CASE WHEN i.status IN $activeStatuses THEN 1 ELSE 0 END) AS activeItems,
              sum(CASE WHEN i.createdAt <= $since
                        AND (i.status IN $activeStatuses
                             OR (i.lastEventAt IS NOT NULL AND i.lastEventAt > $since))
                       THEN 1 ELSE 0 END) AS activeItemsBefore,
              sum(CASE WHEN i.status = 'PENDING_APPROVAL' THEN 1 ELSE 0 END) AS pendingApproval,
              sum(CASE WHEN i.status = 'IN_MAINTENANCE' THEN 1 ELSE 0 END) AS inMaintenance,
              sum(CASE WHEN i.status = 'AWAITING_EVALUATION' THEN 1 ELSE 0 END) AS awaitingEvaluation,
              sum(CASE WHEN i.status = 'DRAFT' THEN 1 ELSE 0 END) AS draft,
              sum(CASE WHEN i.status = 'REJECTED' THEN 1 ELSE 0 END) AS rejected
            """;

    private static final String DISPOSALS_SINCE = """
            MATCH (d:Disposal {unitId: $unitId})
            WHERE d.disposedAt >= $since
            RETURN count(d) AS total
            """;

    private static final String LAST_REJECTIONS = """
            MATCH (e:Event {unitId: $unitId, type: 'REJECTED'})
            WHERE e.itemId IN $itemIds AND e.reason IS NOT NULL
            WITH e.itemId AS itemId, e ORDER BY e.occurredAt DESC
            WITH itemId, head(collect(e.reason)) AS reason
            RETURN itemId, reason
            """;

    private static final List<String> ACTIVE_STATUSES = java.util.Arrays.stream(ItemStatus.values())
            .filter(ItemStatus::isActive)
            .map(ItemStatus::name)
            .toList();

    private final Neo4jClient neo4jClient;

    public DashboardRepositoryImpl(Neo4jClient neo4jClient) {
        this.neo4jClient = neo4jClient;
    }

    @Override
    public InventoryCounts countsOf(UUID unitId, LocalDateTime since) {
        return neo4jClient.query(COUNTS)
                .bindAll(Map.of("unitId", unitId.toString(), "since", since,
                        "activeStatuses", ACTIVE_STATUSES))
                .fetchAs(InventoryCounts.class)
                .mappedBy(DashboardRepositoryImpl::toCounts)
                .one()
                .orElseGet(InventoryCounts::empty);
    }

    static InventoryCounts toCounts(TypeSystem typeSystem, Record row) {
        return new InventoryCounts(
                row.get("activeItems").asLong(),
                row.get("activeItemsBefore").asLong(),
                row.get("pendingApproval").asLong(),
                row.get("inMaintenance").asLong(),
                row.get("awaitingEvaluation").asLong(),
                row.get("draft").asLong(),
                row.get("rejected").asLong());
    }

    static long toTotal(TypeSystem typeSystem, Record row) {
        return row.get("total").asLong();
    }

    @Override
    public long countDisposalsSince(UUID unitId, LocalDate since) {
        return neo4jClient.query(DISPOSALS_SINCE)
                .bindAll(Map.of("unitId", unitId.toString(), "since", since))
                .fetchAs(Long.class)
                .mappedBy(DashboardRepositoryImpl::toTotal)
                .one()
                .orElse(0L);
    }

    @Override
    public Map<UUID, String> lastRejectionReasons(UUID unitId, List<UUID> itemIds) {
        if (itemIds.isEmpty()) {
            return Map.of();
        }
        Map<UUID, String> reasons = new HashMap<>();
        neo4jClient.query(LAST_REJECTIONS)
                .bindAll(Map.of("unitId", unitId.toString(),
                        "itemIds", itemIds.stream().map(UUID::toString).toList()))
                .fetch()
                .all()
                .forEach(row -> reasons.put(UUID.fromString((String) row.get("itemId")),
                        (String) row.get("reason")));
        return Map.copyOf(reasons);
    }
}
