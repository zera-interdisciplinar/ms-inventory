package com.zera.ms_inventory.infrastructure.http.response;

import java.net.URI;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.zera.ms_inventory.Fixtures;
import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.repository.PhotoStorage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ItemResponsesTest {

    private final PhotoStorage photoStorage = mock(PhotoStorage.class);
    private final ItemResponses responses = new ItemResponses(photoStorage);

    @Test
    void shouldResolveTheSignedUrlOfThePhoto() throws Exception {
        Item item = Fixtures.item(Fixtures.UNIT);
        item.attachPhoto("units/u/items/i/p.jpg");
        when(photoStorage.signedUrl("units/u/items/i/p.jpg"))
                .thenReturn(Optional.of(URI.create("https://storage.googleapis.com/b/p.jpg?sig=1").toURL()));

        assertEquals("https://storage.googleapis.com/b/p.jpg?sig=1", responses.from(item).photoUrl());
    }

    @Test
    void shouldLeaveTheUrlEmptyWhenTheItemHasNoPhotoOrItCannotBeSigned() {
        Item withoutPhoto = Fixtures.item(Fixtures.UNIT);
        assertNull(responses.from(withoutPhoto).photoUrl());
        verifyNoInteractions(photoStorage);

        Item unsigned = Fixtures.item(Fixtures.UNIT);
        unsigned.attachPhoto("units/u/items/i/q.jpg");
        when(photoStorage.signedUrl("units/u/items/i/q.jpg")).thenReturn(Optional.empty());
        assertNull(responses.from(unsigned).photoUrl());
        assertNull(responses.from(null));
    }
}
