package com.zera.ms_inventory.core.usecase.item;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zera.ms_inventory.Fixtures;
import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.domain.exception.ItemNotFoundException;
import com.zera.ms_inventory.core.repository.ItemRepository;
import com.zera.ms_inventory.core.repository.PhotoStorage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UploadItemPhotoImplTest {

    private static final byte[] JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private PhotoStorage photoStorage;

    private UploadItemPhotoImpl useCase() {
        return new UploadItemPhotoImpl(itemRepository, photoStorage);
    }

    @Test
    void shouldStoreThePhotoUnderTheUnitAndAttachTheKey() {
        UUID id = UUID.randomUUID();
        Item item = Fixtures.item(id, Fixtures.UNIT);
        when(itemRepository.findById(Fixtures.UNIT, id)).thenReturn(Optional.of(item));
        when(photoStorage.store(anyString(), eq(JPEG), eq("image/jpeg"))).thenAnswer(i -> i.getArgument(0));
        when(itemRepository.save(item)).thenReturn(item);

        Item result = useCase().execute(Fixtures.UNIT, id, JPEG, "image/jpeg");

        assertTrue(result.getPhotoKey().startsWith("units/" + Fixtures.UNIT + "/items/" + id + "/"));
        assertTrue(result.getPhotoKey().endsWith(".jpg"));
        verify(photoStorage, never()).delete(anyString());
    }

    @Test
    void shouldReplaceThePreviousPhotoEvenIfItsDeletionFails() {
        UUID id = UUID.randomUUID();
        Item item = Fixtures.item(id, Fixtures.UNIT);
        item.attachPhoto("units/old.png");
        when(itemRepository.findById(Fixtures.UNIT, id)).thenReturn(Optional.of(item));
        when(photoStorage.store(anyString(), any(), eq("image/png"))).thenAnswer(i -> i.getArgument(0));
        when(itemRepository.save(item)).thenReturn(item);
        doThrow(new IllegalStateException("gcs down")).when(photoStorage).delete("units/old.png");

        Item result = useCase().execute(Fixtures.UNIT, id, JPEG, "image/png");

        assertTrue(result.getPhotoKey().endsWith(".png"));
        verify(photoStorage).delete("units/old.png");
    }

    @Test
    void shouldRejectUnsupportedTypesEmptyAndOversizedFiles() {
        UUID id = UUID.randomUUID();
        UploadItemPhotoImpl useCase = useCase();

        assertThrows(IllegalArgumentException.class, () -> useCase.execute(Fixtures.UNIT, id, JPEG, "image/gif"));
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(Fixtures.UNIT, id, new byte[0], "image/jpeg"));
        byte[] huge = new byte[(int) UploadItemPhotoImpl.MAX_BYTES + 1];
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(Fixtures.UNIT, id, huge, "image/jpeg"));
        verifyNoInteractions(itemRepository, photoStorage);
    }

    @Test
    void shouldNotStoreAnythingForAnItemOutsideTheUnit() {
        UUID id = UUID.randomUUID();
        when(itemRepository.findById(Fixtures.UNIT, id)).thenReturn(Optional.empty());

        UploadItemPhotoImpl useCase = useCase();

        assertThrows(ItemNotFoundException.class, () -> useCase.execute(Fixtures.UNIT, id, JPEG, "image/jpeg"));
        verifyNoInteractions(photoStorage);
    }
}
