package com.zera.ms_inventory.infrastructure.persistence.neo4j.repository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.neo4j.driver.Record;
import org.neo4j.driver.Value;
import org.neo4j.driver.types.TypeSystem;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Repository;

import com.zera.ms_inventory.core.domain.entity.Rule;
import com.zera.ms_inventory.core.domain.valueobject.AlertSubject;
import com.zera.ms_inventory.core.domain.valueobject.ItemStatus;
import com.zera.ms_inventory.core.domain.valueobject.RuleWindow;
import com.zera.ms_inventory.core.repository.RuleEvaluationRepository;

/**
 * Uma consulta por regra, sempre partindo da unidade (indice {@code item_unit}) e recortando pelo
 * alvo da regra. O corte de tempo e calculado aqui e vai como data pronta: comparar datas no
 * Cypher e mais barato e evita depender da aritmetica de duracao do banco.
 */
@Repository
public class RuleEvaluationRepositoryImpl implements RuleEvaluationRepository {

    /** Regra com alvo so alcanca os itens daquele modelo ou categoria; sem alvo, a unidade toda. */
    private static final String SCOPE = """
            MATCH (i:Item {unitId: $unitId})-[:IS_MODEL]->(m:Model)
            WHERE i.status IN $activeStatuses
              AND ($targetId IS NULL
                   OR ($targetType = 'MODEL' AND m.id = $targetId)
                   OR ($targetType = 'CATEGORY'
                       AND EXISTS { (m)-[:BELONGS_TO]->(:Category {id: $targetId}) }))
            """;

    private static final String RETURN_SUBJECT =
            "RETURN i.id AS itemId, i.displayCode AS displayCode, i.name AS name\n";

    private static final List<String> ACTIVE_STATUSES = java.util.Arrays.stream(ItemStatus.values())
            .filter(ItemStatus::isActive)
            .map(ItemStatus::name)
            .toList();

    private final Neo4jClient neo4jClient;

    public RuleEvaluationRepositoryImpl(Neo4jClient neo4jClient) {
        this.neo4jClient = neo4jClient;
    }

    @Override
    public List<UUID> unitsWithRules() {
        return List.copyOf(neo4jClient.query("MATCH (r:Rule) RETURN DISTINCT r.unitId AS unitId")
                .fetchAs(UUID.class)
                .mappedBy((typeSystem, row) -> UUID.fromString(row.get("unitId").asString()))
                .all());
    }

    @Override
    public long countActiveItems(UUID unitId) {
        return neo4jClient.query(
                        "MATCH (i:Item {unitId: $unitId}) WHERE i.status IN $activeStatuses RETURN count(i) AS total")
                .bindAll(Map.of("unitId", unitId.toString(), "activeStatuses", ACTIVE_STATUSES))
                .fetchAs(Long.class)
                .mappedBy((typeSystem, row) -> row.get("total").asLong())
                .one()
                .orElse(0L);
    }

    @Override
    public List<AlertSubject> findViolatingItems(Rule rule, LocalDate reference) {
        String condition = conditionOf(rule);
        if (condition == null) {
            return List.of();
        }
        Map<String, Object> parameters = baseParameters(rule);
        parameters.put("limit", rule.getLimitValue());
        parameters.put("forward", RuleWindow.forward(reference, rule.getLimitValue(), rule.getLimitUnit()));
        parameters.put("backward", RuleWindow.backward(reference, rule.getLimitValue(), rule.getLimitUnit()));

        return List.copyOf(neo4jClient.query(SCOPE + condition + RETURN_SUBJECT)
                .bindAll(parameters)
                .fetchAs(AlertSubject.class)
                .mappedBy(RuleEvaluationRepositoryImpl::toSubject)
                .all());
    }

    static AlertSubject toSubject(TypeSystem typeSystem, Record row) {
        Value displayCode = row.get("displayCode");
        Value name = row.get("name");
        return new AlertSubject(UUID.fromString(row.get("itemId").asString()),
                displayCode.isNull() ? null : displayCode.asString(),
                name.isNull() ? null : name.asString());
    }

    private Map<String, Object> baseParameters(Rule rule) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("unitId", rule.getUnitId().toString());
        parameters.put("activeStatuses", ACTIVE_STATUSES);
        parameters.put("targetType", rule.getTarget() == null ? null : rule.getTarget().type().name());
        parameters.put("targetId", rule.getTarget() == null ? null : rule.getTarget().id().toString());
        return parameters;
    }

    /**
     * O recorte de cada tipo. Ocupacao e reciclavel no aterro nao aparecem aqui: a primeira e da
     * unidade e a segunda e avaliada no momento do descarte.
     */
    private static String conditionOf(Rule rule) {
        return switch (rule.getKind()) {
            // garantia e vida util contam a partir da aquisicao, com os meses vindos do modelo
            case WARRANTY_EXPIRATION -> """
                    AND i.acquiredAt IS NOT NULL AND m.warrantyMonths IS NOT NULL
                      AND i.acquiredAt + duration({months: m.warrantyMonths}) <= date($forward)
                    """;
            case LIFESPAN_EXPIRATION -> """
                    AND i.acquiredAt IS NOT NULL AND m.expectedLifespanMonths IS NOT NULL
                      AND i.acquiredAt + duration({months: m.expectedLifespanMonths}) <= date($forward)
                    """;
            case TIME_IN_STOCK_LIMIT -> "AND date(i.createdAt) <= date($backward)\n";
            case STALE_ITEM -> "AND i.lastEventAt IS NOT NULL AND date(i.lastEventAt) <= date($backward)\n";
            case USAGE_INTENSITY_LIMIT ->
                    "AND i.usageIntensity IS NOT NULL AND i.usageIntensity >= $limit\n";
            case PREDICTED_FAILURE ->
                    "AND i.predictedFailureDate IS NOT NULL AND i.predictedFailureDate <= date($forward)\n";
            case STOCK_QUANTITY_LIMIT, RECYCLABLE_TO_LANDFILL -> null;
        };
    }
}
