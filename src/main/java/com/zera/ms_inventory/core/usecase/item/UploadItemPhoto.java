package com.zera.ms_inventory.core.usecase.item;

import java.util.UUID;

import com.zera.ms_inventory.core.domain.entity.Item;

public interface UploadItemPhoto {
    Item execute(UUID unitId, UUID itemId, byte[] content, String contentType);
}
