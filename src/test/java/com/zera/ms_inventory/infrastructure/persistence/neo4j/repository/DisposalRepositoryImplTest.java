package com.zera.ms_inventory.infrastructure.persistence.neo4j.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zera.ms_inventory.Fixtures;
import com.zera.ms_inventory.core.domain.entity.Disposal;
import com.zera.ms_inventory.core.domain.exception.ItemNotFoundException;
import com.zera.ms_inventory.core.domain.valueobject.DestinationType;
import com.zera.ms_inventory.core.domain.valueobject.DisposedItem;
import com.zera.ms_inventory.core.domain.valueobject.PageResult;
import com.zera.ms_inventory.core.domain.valueobject.Pagination;
import com.zera.ms_inventory.infrastructure.persistence.neo4j.entity.DisposalNode;
import com.zera.ms_inventory.infrastructure.persistence.neo4j.entity.ItemNode;
import com.zera.ms_inventory.infrastructure.persistence.neo4j.mapper.CategoryMapper;
import com.zera.ms_inventory.infrastructure.persistence.neo4j.mapper.DisposalMapper;
import com.zera.ms_inventory.infrastructure.persistence.neo4j.mapper.ItemMapper;
import com.zera.ms_inventory.infrastructure.persistence.neo4j.mapper.ModelMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DisposalRepositoryImplTest {

    @Mock private DisposalNeo4jRepository neo4jRepository;
    @Mock private ItemNeo4jRepository itemNeo4jRepository;

    private final ItemMapper itemMapper = new ItemMapper(new ModelMapper(new CategoryMapper()));
    private final DisposalMapper mapper = new DisposalMapper();

    private DisposalRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        repository = new DisposalRepositoryImpl(neo4jRepository, itemNeo4jRepository, mapper);
    }

    private ItemNode itemNode(UUID id, String displayCode) {
        ItemNode node = itemMapper.toNode(Fixtures.item(id, Fixtures.UNIT));
        node.setDisplayCode(displayCode);
        return node;
    }

    /** So o save resolve o item no banco; a leitura ja recebe as arestas montadas. */
    private ItemNode storedItem(UUID id, String displayCode) {
        ItemNode node = itemNode(id, displayCode);
        when(itemNeo4jRepository.findByIdAndUnitId(id, Fixtures.UNIT)).thenReturn(Optional.of(node));
        return node;
    }

    /**
     * O item e anexado a partir do no ja gravado, nao de uma instancia montada pelo mapper: e o que
     * evita o save da aresta reescrever o item sem as relacoes dele.
     */
    @Test
    void shouldAttachTheStoredItemNodesOnSave() {
        UUID itemId = UUID.randomUUID();
        ItemNode stored = storedItem(itemId, "100001");
        Disposal disposal = Disposal.register(Fixtures.UNIT, DestinationType.RECYCLING, null, null,
                LocalDate.now(), null, List.of(new DisposedItem(itemId, "100001", "Notebook", 2.5)),
                Fixtures.OPERATOR);
        when(neo4jRepository.findByIdAndUnitId(disposal.getId(), Fixtures.UNIT)).thenReturn(Optional.empty());
        when(neo4jRepository.save(any(DisposalNode.class))).thenAnswer(i -> i.getArgument(0));

        repository.save(disposal);

        ArgumentCaptor<DisposalNode> saved = ArgumentCaptor.forClass(DisposalNode.class);
        verify(neo4jRepository).save(saved.capture());
        assertThat(saved.getValue().getItems()).singleElement().satisfies(relationship -> {
            assertThat(relationship.getItem()).isSameAs(stored);
            assertThat(relationship.getWeightKg()).isEqualTo(2.5);
        });
    }

    @Test
    void shouldRefuseToSaveWithAnItemFromAnotherUnit() {
        UUID itemId = UUID.randomUUID();
        when(itemNeo4jRepository.findByIdAndUnitId(itemId, Fixtures.UNIT)).thenReturn(Optional.empty());
        Disposal disposal = Disposal.register(Fixtures.UNIT, DestinationType.RECYCLING, null, null,
                LocalDate.now(), null, List.of(new DisposedItem(itemId, "100001", "Notebook", 1.0)),
                Fixtures.OPERATOR);
        when(neo4jRepository.findByIdAndUnitId(disposal.getId(), Fixtures.UNIT)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> repository.save(disposal)).isInstanceOf(ItemNotFoundException.class);
        verify(neo4jRepository, never()).save(any());
    }

    /**
     * Regressao: a correcao do destino remontava as arestas, e aresta sem id interno o SDN grava
     * como nova. O descarte ficava com o dobro de itens e o peso dobrado nos indicadores.
     */
    @Test
    void shouldReuseTheStoredEdgesWhenCorrectingAnExistingDisposal() {
        UUID id = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        DisposalNode gravado = new DisposalNode(id, Fixtures.UNIT, DestinationType.RECYCLING, null, null,
                LocalDate.now(), null, null, null, null, null);
        gravado.setItems(java.util.Set.of(mapper.toRelationship(itemNode(itemId, "100001"), 2.5)));
        when(neo4jRepository.findByIdAndUnitId(id, Fixtures.UNIT)).thenReturn(Optional.of(gravado));
        when(neo4jRepository.save(any(DisposalNode.class))).thenAnswer(i -> i.getArgument(0));

        Disposal corrigido = new Disposal(id, Fixtures.UNIT, DestinationType.LANDFILL, null, null,
                LocalDate.now(), null, List.of(new DisposedItem(itemId, "100001", "Notebook", 2.5)),
                null, null, null, null);

        Disposal resultado = repository.save(corrigido);

        ArgumentCaptor<DisposalNode> salvo = ArgumentCaptor.forClass(DisposalNode.class);
        verify(neo4jRepository).save(salvo.capture());
        assertThat(salvo.getValue()).isSameAs(gravado);
        assertThat(salvo.getValue().getDestination()).isEqualTo(DestinationType.LANDFILL);
        assertThat(salvo.getValue().getItems()).hasSize(1);
        assertThat(resultado.getItems()).hasSize(1);
        assertThat(resultado.totalWeightKg()).isEqualTo(2.5);
        // nao vai atras do item de novo: as arestas ja gravadas sao reaproveitadas
        verify(itemNeo4jRepository, never()).findByIdAndUnitId(any(), any());
    }

    @Test
    void shouldFindTheDisposalWithinTheUnit() {
        UUID id = UUID.randomUUID();
        DisposalNode node = new DisposalNode(id, Fixtures.UNIT, DestinationType.DONATION, null, null,
                LocalDate.now(), null, null, null, null, null);
        node.setItems(java.util.Set.of(mapper.toRelationship(itemNode(UUID.randomUUID(), "100001"), 1.0)));
        when(neo4jRepository.findByIdAndUnitId(id, Fixtures.UNIT)).thenReturn(Optional.of(node));

        assertThat(repository.findById(Fixtures.UNIT, id)).isPresent()
                .get().extracting(Disposal::getDestination).isEqualTo(DestinationType.DONATION);
    }

    @Test
    void shouldPageTheDisposalsOfTheUnit() {
        DisposalNode node = new DisposalNode(UUID.randomUUID(), Fixtures.UNIT, DestinationType.LANDFILL, null,
                null, LocalDate.now(), null, null, null, null, null);
        node.setItems(java.util.Set.of(mapper.toRelationship(itemNode(UUID.randomUUID(), "100001"), 3.0)));
        when(neo4jRepository.countByUnit(Fixtures.UNIT)).thenReturn(5L);
        when(neo4jRepository.findPageByUnit(Fixtures.UNIT, 2L, 2)).thenReturn(List.of(node));

        PageResult<Disposal> result = repository.findPage(Fixtures.UNIT, new Pagination(1, 2));

        assertThat(result.content()).hasSize(1);
        assertThat(result.totalElements()).isEqualTo(5);
        assertThat(result.totalPages()).isEqualTo(3);
    }

    @Test
    void shouldReturnAnEmptyPageWithoutQueryingWhenThereIsNoDisposal() {
        when(neo4jRepository.countByUnit(Fixtures.UNIT)).thenReturn(0L);

        PageResult<Disposal> result = repository.findPage(Fixtures.UNIT, new Pagination(0, 20));

        assertThat(result.content()).isEmpty();
        verify(neo4jRepository, never()).findPageByUnit(any(), anyLong(), anyInt());
    }
}
