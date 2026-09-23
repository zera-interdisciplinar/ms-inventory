package com.zera.ms_inventory.core.usecase.item;

import java.util.UUID;
import java.util.random.RandomGenerator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.zera.ms_inventory.core.repository.ItemRepository;

/**
 * Sorteia o codigo de 6 digitos do item e confere que ainda nao existe na unidade. A constraint
 * (unitId, displayCode) do Neo4j cobre a corrida entre dois cadastros simultaneos.
 */
@Component
public class DisplayCodeGenerator {

    static final int MAX_ATTEMPTS = 10;

    private final ItemRepository itemRepository;
    private final RandomGenerator random;

    @Autowired
    public DisplayCodeGenerator(ItemRepository itemRepository) {
        this(itemRepository, RandomGenerator.of("L64X128MixRandom"));
    }

    DisplayCodeGenerator(ItemRepository itemRepository, RandomGenerator random) {
        this.itemRepository = itemRepository;
        this.random = random;
    }

    public String next(UUID unitId) {
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            String code = String.valueOf(random.nextInt(100_000, 1_000_000));
            if (!itemRepository.existsByDisplayCode(unitId, code)) {
                return code;
            }
        }
        throw new IllegalStateException("Could not generate a unique display code for unit " + unitId);
    }
}
