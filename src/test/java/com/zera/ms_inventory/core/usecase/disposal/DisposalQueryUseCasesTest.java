package com.zera.ms_inventory.core.usecase.disposal;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zera.ms_inventory.Fixtures;
import com.zera.ms_inventory.core.domain.entity.Disposal;
import com.zera.ms_inventory.core.domain.exception.DisposalNotFoundException;
import com.zera.ms_inventory.core.domain.valueobject.DestinationType;
import com.zera.ms_inventory.core.domain.valueobject.DisposedItem;
import com.zera.ms_inventory.core.domain.valueobject.PageResult;
import com.zera.ms_inventory.core.domain.valueobject.Pagination;
import com.zera.ms_inventory.core.repository.DisposalRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DisposalQueryUseCasesTest {

    @Mock private DisposalRepository disposalRepository;

    private DisposalQueryUseCases useCase() {
        return new DisposalQueryUseCases(disposalRepository);
    }

    private Disposal disposal(DestinationType destination) {
        return Disposal.register(Fixtures.UNIT, destination, null, null, null, null,
                List.of(new DisposedItem(UUID.randomUUID(), "100001", "Notebook", 2.0)), Fixtures.OPERATOR);
    }

    @Test
    void shouldPageTheDisposalsOfTheUnit() {
        Pagination pagination = new Pagination(0, 20);
        when(disposalRepository.findPage(Fixtures.UNIT, pagination))
                .thenReturn(new PageResult<>(List.of(disposal(DestinationType.RECYCLING)), 0, 20, 1));

        PageResult<Disposal> result = useCase().execute(Fixtures.UNIT, pagination);

        assertThat(result.content()).hasSize(1);
        assertThat(result.totalElements()).isEqualTo(1);
    }

    @Test
    void shouldFindTheDisposalById() {
        Disposal disposal = disposal(DestinationType.DONATION);
        when(disposalRepository.findById(Fixtures.UNIT, disposal.getId())).thenReturn(Optional.of(disposal));

        assertThat(useCase().execute(Fixtures.UNIT, disposal.getId())).isSameAs(disposal);
    }

    @Test
    void shouldThrowWhenTheDisposalIsNotInTheUnit() {
        UUID id = UUID.randomUUID();
        when(disposalRepository.findById(Fixtures.OTHER_UNIT, id)).thenReturn(Optional.empty());

        DisposalQueryUseCases useCase = useCase();

        assertThatThrownBy(() -> useCase.execute(Fixtures.OTHER_UNIT, id))
                .isInstanceOf(DisposalNotFoundException.class);
    }

    /** Corrigir o destino muda a taxa de reciclagem; os itens seguem descartados. */
    @Test
    void shouldCorrectTheDestination() {
        Disposal disposal = disposal(DestinationType.LANDFILL);
        when(disposalRepository.findById(Fixtures.UNIT, disposal.getId())).thenReturn(Optional.of(disposal));
        when(disposalRepository.save(disposal)).thenReturn(disposal);

        Disposal result = useCase().execute(Fixtures.UNIT, disposal.getId(), DestinationType.RECYCLING);

        assertThat(result.getDestination()).isEqualTo(DestinationType.RECYCLING);
        assertThat(result.getItems()).hasSize(1);
        verify(disposalRepository).save(disposal);
    }

    @Test
    void shouldNotCorrectADisposalFromAnotherUnit() {
        UUID id = UUID.randomUUID();
        when(disposalRepository.findById(Fixtures.OTHER_UNIT, id)).thenReturn(Optional.empty());

        DisposalQueryUseCases useCase = useCase();

        assertThatThrownBy(() -> useCase.execute(Fixtures.OTHER_UNIT, id, DestinationType.RECYCLING))
                .isInstanceOf(DisposalNotFoundException.class);
        verify(disposalRepository, never()).save(any());
    }
}
