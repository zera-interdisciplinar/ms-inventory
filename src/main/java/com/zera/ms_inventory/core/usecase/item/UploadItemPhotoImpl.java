package com.zera.ms_inventory.core.usecase.item;

import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.zera.ms_inventory.core.domain.entity.Item;
import com.zera.ms_inventory.core.domain.exception.ItemNotFoundException;
import com.zera.ms_inventory.core.repository.ItemRepository;
import com.zera.ms_inventory.core.repository.PhotoStorage;

/** Uma foto por item: a nova substitui a anterior. JPEG ou PNG de ate 5 MB. */
@Service
public class UploadItemPhotoImpl implements UploadItemPhoto {

    static final long MAX_BYTES = 5L * 1024 * 1024;
    private static final Map<String, String> EXTENSIONS = Map.of("image/jpeg", "jpg", "image/png", "png");
    private static final Logger log = LoggerFactory.getLogger(UploadItemPhotoImpl.class);

    private final ItemRepository itemRepository;
    private final PhotoStorage photoStorage;

    public UploadItemPhotoImpl(ItemRepository itemRepository, PhotoStorage photoStorage) {
        this.itemRepository = itemRepository;
        this.photoStorage = photoStorage;
    }

    @Override
    public Item execute(UUID unitId, UUID itemId, byte[] content, String contentType) {
        String extension = EXTENSIONS.get(contentType);
        if (extension == null) {
            throw new IllegalArgumentException("photo must be image/jpeg or image/png");
        }
        if (content.length == 0 || content.length > MAX_BYTES) {
            throw new IllegalArgumentException("photo must have between 1 byte and 5 MB");
        }
        Item item = itemRepository.findById(unitId, itemId).orElseThrow(() -> new ItemNotFoundException(itemId));

        String previousKey = item.getPhotoKey();
        String key = photoStorage.store("units/%s/items/%s/%s.%s".formatted(unitId, itemId, UUID.randomUUID(), extension),
                content, contentType);
        item.attachPhoto(key);
        Item saved = itemRepository.save(item);

        if (previousKey != null) {
            try {
                photoStorage.delete(previousKey);
            } catch (RuntimeException e) {
                // a foto nova ja esta salva; a antiga vira lixo no bucket, nao um erro para o app
                log.warn("Could not delete previous photo {}", previousKey, e);
            }
        }
        return saved;
    }
}
