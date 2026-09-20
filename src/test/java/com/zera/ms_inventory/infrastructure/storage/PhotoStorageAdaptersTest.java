package com.zera.ms_inventory.infrastructure.storage;

import java.net.URL;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.zera.ms_inventory.core.domain.exception.PhotoStorageUnavailableException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PhotoStorageAdaptersTest {

    private final Storage storage = mock(Storage.class);
    private final GcsPhotoStorage gcs = new GcsPhotoStorage(storage, "zera-photos", Duration.ofMinutes(15));

    @Test
    void shouldUploadToTheBucketWithTheContentType() {
        byte[] content = {1, 2, 3};

        String key = gcs.store("units/u/items/i/p.jpg", content, "image/jpeg");

        assertThat(key).isEqualTo("units/u/items/i/p.jpg");
        verify(storage).create(BlobInfo.newBuilder(BlobId.of("zera-photos", key)).setContentType("image/jpeg").build(),
                content);
    }

    @Test
    void shouldDeleteFromTheBucket() {
        gcs.delete("units/u/items/i/p.jpg");

        verify(storage).delete(BlobId.of("zera-photos", "units/u/items/i/p.jpg"));
    }

    @Test
    void shouldSignAV4UrlWithTheConfiguredTtl() throws Exception {
        URL url = new URL("https://storage.googleapis.com/zera-photos/p.jpg?X-Goog-Signature=abc");
        when(storage.signUrl(any(BlobInfo.class), eq(900L), eq(TimeUnit.SECONDS), any(Storage.SignUrlOption.class)))
                .thenReturn(url);

        assertThat(gcs.signedUrl("p.jpg")).contains(url);
    }

    @Test
    void shouldReturnNoUrlWhenSigningFails() {
        when(storage.signUrl(any(BlobInfo.class), anyLong(), any(TimeUnit.class), any(Storage.SignUrlOption.class)))
                .thenThrow(new IllegalStateException("no signer"));

        assertThat(gcs.signedUrl("p.jpg")).isEmpty();
    }

    @Test
    void shouldRefuseUploadsAndServeNoUrlsWhenNotConfigured() {
        UnavailablePhotoStorage unavailable = new UnavailablePhotoStorage();

        assertThatThrownBy(() -> unavailable.store("k", new byte[] {1}, "image/png"))
                .isInstanceOf(PhotoStorageUnavailableException.class);
        assertThat(unavailable.signedUrl("k")).isEmpty();
        unavailable.delete("k");
    }

    @Test
    void shouldFallBackToTheUnavailableStorageWithoutABucket() {
        assertThat(new PhotoStorageConfig().photoStorage("", Duration.ofMinutes(15)))
                .isInstanceOf(UnavailablePhotoStorage.class);
    }
}
