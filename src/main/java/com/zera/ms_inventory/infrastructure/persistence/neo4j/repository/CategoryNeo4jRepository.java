package com.zera.ms_inventory.infrastructure.persistence.neo4j.repository;

import java.util.UUID;

import org.springframework.data.neo4j.repository.Neo4jRepository;

import com.zera.ms_inventory.infrastructure.persistence.neo4j.entity.CategoryNode;

interface CategoryNeo4jRepository extends Neo4jRepository<CategoryNode, UUID> {
}
