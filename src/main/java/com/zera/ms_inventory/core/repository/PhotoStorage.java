package com.zera.ms_inventory.core.repository;

import java.net.URL;
import java.util.Optional;

/** Armazenamento das fotos dos itens. Guarda o binario e devolve a chave que o item referencia. */
public interface PhotoStorage {

    String store(String key, byte[] content, String contentType);

    void delete(String key);

    /** URL temporaria para o app exibir a foto; vazia quando a foto nao pode ser servida. */
    Optional<URL> signedUrl(String key);
}
