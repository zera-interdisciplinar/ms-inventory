package com.zera.ms_inventory.infrastructure.storage;

import java.net.URL;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.zera.ms_inventory.core.repository.PhotoStorage;

/**
 * Fotos no Google Cloud Storage. Credenciais pelo ADC (workload identity no GKE); a URL assinada V4
 * exige que a service account possa assinar blobs (roles/iam.serviceAccountTokenCreator nela mesma).
 */
public class GcsPhotoStorage implements PhotoStorage {

    private static final Logger log = LoggerFactory.getLogger(GcsPhotoStorage.class);

    private final Storage storage;
    private final String bucket;
    private final Duration urlTtl;

    public GcsPhotoStorage(Storage storage, String bucket, Duration urlTtl) {
        this.storage = storage;
        this.bucket = bucket;
        this.urlTtl = urlTtl;
    }

    @Override
    public String store(String key, byte[] content, String contentType) {
        storage.create(BlobInfo.newBuilder(BlobId.of(bucket, key)).setContentType(contentType).build(), content);
        return key;
    }

    @Override
    public void delete(String key) {
        storage.delete(BlobId.of(bucket, key));
    }

    @Override
    public Optional<URL> signedUrl(String key) {
        try {
            return Optional.of(storage.signUrl(BlobInfo.newBuilder(BlobId.of(bucket, key)).build(),
                    urlTtl.toSeconds(), TimeUnit.SECONDS, Storage.SignUrlOption.withV4Signature()));
        } catch (RuntimeException e) {
            // sem URL o app mostra o icone da categoria; o item continua carregando
            log.warn("Could not sign photo URL for {}", key, e);
            return Optional.empty();
        }
    }
}
