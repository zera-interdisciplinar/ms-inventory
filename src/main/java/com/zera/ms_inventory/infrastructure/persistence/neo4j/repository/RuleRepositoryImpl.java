package com.zera.ms_inventory.infrastructure.persistence.neo4j.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.neo4j.driver.Record;
import org.neo4j.driver.Value;
import org.neo4j.driver.types.TypeSystem;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.zera.ms_inventory.core.domain.entity.Rule;
import com.zera.ms_inventory.core.domain.valueobject.RuleKind;
import com.zera.ms_inventory.core.domain.valueobject.RuleLimitUnit;
import com.zera.ms_inventory.core.domain.valueobject.RuleTarget;
import com.zera.ms_inventory.core.domain.valueobject.RuleTargetType;
import com.zera.ms_inventory.core.repository.RuleRepository;

/**
 * O alvo da regra mora na relacao {@code APPLIES_TO}, nao em propriedade, entao a leitura e a
 * escrita vao pelo Neo4jClient. E o mesmo motivo dos indicadores: o resultado tem colunas que nao
 * cabem num no, e relacao remontada pelo SDN ja duplicou aresta neste projeto antes.
 */
@Repository
public class RuleRepositoryImpl implements RuleRepository {

    private static final String RETURN_RULE = """
            OPTIONAL MATCH (r)-[:APPLIES_TO]->(t)
            RETURN r.id AS id, r.unitId AS unitId, r.name AS name, r.kind AS kind,
                   r.limitValue AS limitValue, r.limitUnit AS limitUnit, r.active AS active,
                   r.createdAt AS createdAt, r.updatedAt AS updatedAt,
                   head(labels(t)) AS targetLabel, t.id AS targetId
            """;

    private static final String SAVE = """
            MERGE (r:Rule {id: $id})
            SET r.unitId = $unitId, r.name = $name, r.kind = $kind, r.limitValue = $limitValue,
                r.limitUnit = $limitUnit, r.active = $active, r.createdAt = $createdAt,
                r.updatedAt = $updatedAt
            WITH r
            OPTIONAL MATCH (r)-[old:APPLIES_TO]->()
            DELETE old
            WITH r
            CALL (r) {
              WITH r
              OPTIONAL MATCH (target) WHERE $targetId IS NOT NULL AND target.id = $targetId
                    AND $targetLabel IN labels(target) AND target.unitId = $unitId
              FOREACH (t IN CASE WHEN target IS NULL THEN [] ELSE [target] END |
                       MERGE (r)-[:APPLIES_TO]->(t))
              RETURN count(*) AS ignored
            }
            WITH r
            """ + RETURN_RULE;

    private static final String FIND_BY_ID = "MATCH (r:Rule {id: $id, unitId: $unitId})\n" + RETURN_RULE;

    private static final String FIND_ALL = "MATCH (r:Rule {unitId: $unitId})\n" + RETURN_RULE
            + "ORDER BY r.createdAt ASC\n";

    private final Neo4jClient neo4jClient;

    public RuleRepositoryImpl(Neo4jClient neo4jClient) {
        this.neo4jClient = neo4jClient;
    }

    static Rule toRule(TypeSystem typeSystem, Record row) {
        Value limitValue = row.get("limitValue");
        Value limitUnit = row.get("limitUnit");
        Value targetLabel = row.get("targetLabel");
        Value targetId = row.get("targetId");
        return new Rule(
                UUID.fromString(row.get("id").asString()),
                UUID.fromString(row.get("unitId").asString()),
                row.get("name").isNull() ? null : row.get("name").asString(),
                RuleKind.valueOf(row.get("kind").asString()),
                limitValue.isNull() ? null : limitValue.asInt(),
                limitUnit.isNull() ? null : RuleLimitUnit.valueOf(limitUnit.asString()),
                targetLabel.isNull() || targetId.isNull() ? null
                        : RuleTarget.of(RuleTargetType.valueOf(targetLabel.asString().toUpperCase()),
                                UUID.fromString(targetId.asString())),
                row.get("active").asBoolean(false),
                row.get("createdAt").isNull() ? null : row.get("createdAt").asLocalDateTime(),
                row.get("updatedAt").isNull() ? null : row.get("updatedAt").asLocalDateTime());
    }

    private static Map<String, Object> parametersOf(Rule rule) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("id", rule.getId().toString());
        parameters.put("unitId", rule.getUnitId().toString());
        parameters.put("name", rule.getName());
        parameters.put("kind", rule.getKind().name());
        parameters.put("limitValue", rule.getLimitValue());
        parameters.put("limitUnit", rule.getLimitUnit() == null ? null : rule.getLimitUnit().name());
        parameters.put("active", rule.isActive());
        parameters.put("createdAt", rule.getCreatedAt());
        parameters.put("updatedAt", rule.getUpdatedAt());
        // o rotulo do no alvo e o proprio tipo, capitalizado como o grafo guarda
        parameters.put("targetLabel", rule.getTarget() == null ? null : labelOf(rule.getTarget().type()));
        parameters.put("targetId", rule.getTarget() == null ? null : rule.getTarget().id().toString());
        return parameters;
    }

    private static String labelOf(RuleTargetType type) {
        return type == RuleTargetType.MODEL ? "Model" : "Category";
    }

    @Override
    @Transactional
    public Rule save(Rule rule) {
        return neo4jClient.query(SAVE)
                .bindAll(parametersOf(rule))
                .fetchAs(Rule.class)
                .mappedBy(RuleRepositoryImpl::toRule)
                .one()
                .orElseThrow(() -> new IllegalStateException("rule " + rule.getId() + " was not saved"));
    }

    @Override
    @Transactional
    public List<Rule> saveAll(List<Rule> rules) {
        return rules.stream().map(this::save).toList();
    }

    @Override
    public Optional<Rule> findById(UUID unitId, UUID id) {
        return neo4jClient.query(FIND_BY_ID)
                .bindAll(Map.of("unitId", unitId.toString(), "id", id.toString()))
                .fetchAs(Rule.class)
                .mappedBy(RuleRepositoryImpl::toRule)
                .one();
    }

    @Override
    public List<Rule> findAll(UUID unitId) {
        return List.copyOf(neo4jClient.query(FIND_ALL)
                .bindAll(Map.of("unitId", unitId.toString()))
                .fetchAs(Rule.class)
                .mappedBy(RuleRepositoryImpl::toRule)
                .all());
    }

    @Override
    public long countByUnit(UUID unitId) {
        return neo4jClient.query("MATCH (r:Rule {unitId: $unitId}) RETURN count(r) AS total")
                .bindAll(Map.of("unitId", unitId.toString()))
                .fetchAs(Long.class)
                .mappedBy((typeSystem, row) -> row.get("total").asLong())
                .one()
                .orElse(0L);
    }

    @Override
    @Transactional
    public void deleteById(UUID unitId, UUID id) {
        neo4jClient.query("MATCH (r:Rule {id: $id, unitId: $unitId}) DETACH DELETE r")
                .bindAll(Map.of("unitId", unitId.toString(), "id", id.toString()))
                .run();
    }
}
