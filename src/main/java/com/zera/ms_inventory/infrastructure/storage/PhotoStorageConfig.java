package com.zera.ms_inventory.infrastructure.storage;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.cloud.storage.StorageOptions;
import com.zera.ms_inventory.core.repository.PhotoStorage;

@Configuration
public class PhotoStorageConfig {

    private static final Logger log = LoggerFactory.getLogger(PhotoStorageConfig.class);

    @Bean
    PhotoStorage photoStorage(@Value("${zera.storage.photos-bucket:}") String bucket,
                              @Value("${zera.storage.photo-url-ttl:PT15M}") Duration urlTtl) {
        if (bucket.isBlank()) {
            log.warn("zera.storage.photos-bucket nao configurado — upload de fotos desativado");
            return new UnavailablePhotoStorage();
        }
        return new GcsPhotoStorage(StorageOptions.getDefaultInstance().getService(), bucket, urlTtl);
    }
}
