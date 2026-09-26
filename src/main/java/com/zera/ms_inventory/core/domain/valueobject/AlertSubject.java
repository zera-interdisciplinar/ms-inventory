package com.zera.ms_inventory.core.domain.valueobject;

import java.util.UUID;

/**
 * O que fez a regra disparar. Quase sempre um item; a regra de ocupacao e da unidade inteira e vem
 * sem item, e ai o assunto e a propria unidade.
 */
public record AlertSubject(UUID itemId, String displayCode, String name) {

    public static AlertSubject unit() {
        return new AlertSubject(null, null, null);
    }

    public boolean isUnitWide() {
        return itemId == null;
    }

    /** Identifica o assunto na deduplicacao; a unidade inteira usa um id fixo. */
    public UUID dedupKey(UUID unitId) {
        return itemId != null ? itemId : unitId;
    }

    public String describe() {
        if (isUnitWide()) {
            return "a unidade";
        }
        return "o item " + (displayCode != null ? displayCode : itemId)
                + (name != null && !name.isBlank() ? " (" + name + ")" : "");
    }
}
