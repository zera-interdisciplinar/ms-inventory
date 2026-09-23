package com.zera.ms_inventory.infrastructure.persistence.neo4j.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zera.ms_inventory.Fixtures;
import com.zera.ms_inventory.core.domain.entity.UnitInventorySettings;
import com.zera.ms_inventory.infrastructure.persistence.neo4j.entity.UnitInventorySettingsNode;
import com.zera.ms_inventory.infrastructure.persistence.neo4j.mapper.UnitInventorySettingsMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UnitInventorySettingsRepositoryImplTest {

    @Mock private UnitInventorySettingsNeo4jRepository neo4jRepository;

    private final UnitInventorySettingsMapper mapper = new UnitInventorySettingsMapper();

    @Test
    void shouldMapTheStoredSettings() {
        LocalDateTime moment = LocalDateTime.of(2026, 9, 20, 12, 0);
        when(neo4jRepository.findById(Fixtures.UNIT)).thenReturn(Optional.of(
                new UnitInventorySettingsNode(Fixtures.UNIT, 420, Fixtures.MANAGER.userId(), "Kevin Gestor",
                        moment)));

        UnitInventorySettings settings = new UnitInventorySettingsRepositoryImpl(neo4jRepository, mapper)
                .findByUnit(Fixtures.UNIT).orElseThrow();

        assertThat(settings.getStockCapacity()).isEqualTo(420);
        assertThat(settings.getUpdatedByName()).isEqualTo("Kevin Gestor");
        assertThat(settings.getUpdatedAt()).isEqualTo(moment);
    }

    @Test
    void shouldReturnEmptyForAUnitWithoutSettings() {
        when(neo4jRepository.findById(Fixtures.OTHER_UNIT)).thenReturn(Optional.empty());

        assertThat(new UnitInventorySettingsRepositoryImpl(neo4jRepository, mapper)
                .findByUnit(Fixtures.OTHER_UNIT)).isEmpty();
    }

    @Test
    void shouldSaveAndMapBack() {
        UnitInventorySettings settings = UnitInventorySettings.notConfigured(Fixtures.UNIT);
        settings.changeCapacity(90, Fixtures.MANAGER);
        when(neo4jRepository.save(any(UnitInventorySettingsNode.class))).thenAnswer(i -> i.getArgument(0));

        UnitInventorySettings saved = new UnitInventorySettingsRepositoryImpl(neo4jRepository, mapper)
                .save(settings);

        assertThat(saved.getUnitId()).isEqualTo(Fixtures.UNIT);
        assertThat(saved.getStockCapacity()).isEqualTo(90);
    }

    @Test
    void shouldReturnNullForMissingObjects() {
        assertThat(mapper.toDomain(null)).isNull();
        assertThat(mapper.toNode(null)).isNull();
        assertThat(new UnitInventorySettingsNode().getUnitId()).isNull();
    }
}
