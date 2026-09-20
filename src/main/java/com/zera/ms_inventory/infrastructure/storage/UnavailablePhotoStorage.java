package com.zera.ms_inventory.infrastructure.storage;

import java.net.URL;
import java.util.Optional;

import com.zera.ms_inventory.core.domain.exception.PhotoStorageUnavailableException;
import com.zera.ms_inventory.core.repository.PhotoStorage;

/** Usado quando nenhum bucket foi configurado (dev, testes): upload responde 503 e nao ha URL. */
public class UnavailablePhotoStorage implements PhotoStorage {

    @Override
    public String store(String key, byte[] content, String contentType) {
        throw new PhotoStorageUnavailableException("Photo storage is not configured (zera.storage.photos-bucket)");
    }

    @Override
    public void delete(String key) {
        // nada armazenado, nada a apagar
    }

    @Override
    public Optional<URL> signedUrl(String key) {
        return Optional.empty();
    }
}
