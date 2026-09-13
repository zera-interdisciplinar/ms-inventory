package com.zera.ms_inventory.infrastructure.http.response;

import java.net.URL;

import org.springframework.stereotype.Component;

import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.repository.PhotoStorage;

/** Monta as respostas de item resolvendo a URL assinada (temporaria) da foto. */
@Component
public class ItemResponses {

    private final PhotoStorage photoStorage;

    public ItemResponses(PhotoStorage photoStorage) {
        this.photoStorage = photoStorage;
    }

    public ItemResponse from(Item item) {
        if (item == null) {
            return null;
        }
        String photoUrl = item.getPhotoKey() == null ? null
                : photoStorage.signedUrl(item.getPhotoKey()).map(URL::toString).orElse(null);
        return ItemResponse.from(item, photoUrl);
    }
}
