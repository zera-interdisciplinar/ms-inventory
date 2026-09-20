package com.zera.ms_inventory.core.usecase.item;

import java.util.random.RandomGenerator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zera.ms_inventory.Fixtures;
import com.zera.ms_inventory.core.repository.ItemRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DisplayCodeGeneratorTest {

    @Mock
    private ItemRepository itemRepository;

    @Test
    void shouldGenerateASixDigitCodeThatIsFreeInTheUnit() {
        when(itemRepository.existsByDisplayCode(eq(Fixtures.UNIT), anyString())).thenReturn(false);

        String code = new DisplayCodeGenerator(itemRepository).next(Fixtures.UNIT);

        assertTrue(code.matches("\\d{6}"), code);
    }

    @Test
    void shouldRetryWhenTheCodeAlreadyExists() {
        RandomGenerator random = mock(RandomGenerator.class);
        when(random.nextInt(anyInt(), anyInt())).thenReturn(265964, 265964, 481516);
        when(itemRepository.existsByDisplayCode(Fixtures.UNIT, "265964")).thenReturn(true);
        when(itemRepository.existsByDisplayCode(Fixtures.UNIT, "481516")).thenReturn(false);

        assertEquals("481516", new DisplayCodeGenerator(itemRepository, random).next(Fixtures.UNIT));
        verify(itemRepository, times(2)).existsByDisplayCode(Fixtures.UNIT, "265964");
    }

    @Test
    void shouldGiveUpAfterTheMaximumAttempts() {
        RandomGenerator random = mock(RandomGenerator.class);
        when(random.nextInt(anyInt(), anyInt())).thenReturn(265964);
        when(itemRepository.existsByDisplayCode(Fixtures.UNIT, "265964")).thenReturn(true);

        DisplayCodeGenerator generator = new DisplayCodeGenerator(itemRepository, random);

        assertThrows(IllegalStateException.class, () -> generator.next(Fixtures.UNIT));
        verify(itemRepository, times(DisplayCodeGenerator.MAX_ATTEMPTS)).existsByDisplayCode(Fixtures.UNIT, "265964");
    }
}
