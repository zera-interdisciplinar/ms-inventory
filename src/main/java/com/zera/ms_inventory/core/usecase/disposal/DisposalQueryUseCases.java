package com.zera.ms_inventory.core.usecase.disposal;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zera.ms_inventory.core.domain.entity.Disposal;
import com.zera.ms_inventory.core.domain.exception.DisposalNotFoundException;
import com.zera.ms_inventory.core.domain.valueobject.DestinationType;
import com.zera.ms_inventory.core.domain.valueobject.PageResult;
import com.zera.ms_inventory.core.domain.valueobject.Pagination;
import com.zera.ms_inventory.core.repository.DisposalRepository;

/** Consulta e correcao do descarte; os itens nao voltam ao estoque, so o destino muda. */
@Service
public class DisposalQueryUseCases implements ListDisposals, FindDisposalById, CorrectDisposalDestination {

    private final DisposalRepository disposalRepository;

    public DisposalQueryUseCases(DisposalRepository disposalRepository) {
        this.disposalRepository = disposalRepository;
    }

    @Override
    public PageResult<Disposal> execute(UUID unitId, Pagination pagination) {
        return disposalRepository.findPage(unitId, pagination);
    }

    @Override
    public Disposal execute(UUID unitId, UUID id) {
        return disposalRepository.findById(unitId, id)
                .orElseThrow(() -> new DisposalNotFoundException(id));
    }

    @Override
    @Transactional
    public Disposal execute(UUID unitId, UUID id, DestinationType destination) {
        Disposal disposal = disposalRepository.findById(unitId, id)
                .orElseThrow(() -> new DisposalNotFoundException(id));
        disposal.correctDestination(destination);
        return disposalRepository.save(disposal);
    }
}
