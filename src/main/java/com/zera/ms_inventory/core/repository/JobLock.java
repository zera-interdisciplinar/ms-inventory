package com.zera.ms_inventory.core.repository;

/** Trava de execucao compartilhada entre replicas. */
public interface JobLock {
    /**
     * {@code true} para quem ficou com a janela. As demais replicas que dispararem na mesma janela
     * recebem {@code false} e nao executam.
     */
    boolean acquire(String jobName, String window);
}
