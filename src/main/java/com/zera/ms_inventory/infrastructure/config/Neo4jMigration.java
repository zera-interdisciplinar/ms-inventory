package com.zera.ms_inventory.infrastructure.config;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Uma migracao versionada lida de {@code neo4j/migrations/V<versao>__<descricao>.cypher}. Cada
 * statement termina em {@code ;} e roda na sua propria transacao (schema e dados nao podem dividir
 * a mesma transacao no Neo4j). Linhas comecando com {@code //} sao comentarios. O split e por
 * {@code ;}, entao textos dentro dos statements nao podem conter ponto e virgula.
 */
record Neo4jMigration(int version, String description, List<String> statements) {

    private static final Pattern FILE_NAME = Pattern.compile("V(\\d+)__(\\w+)\\.cypher");

    static Neo4jMigration parse(String fileName, String content) {
        Matcher matcher = FILE_NAME.matcher(fileName);
        if (!matcher.matches()) {
            throw new IllegalStateException("Invalid migration file name: " + fileName
                    + " (expected V<version>__<description>.cypher)");
        }
        String withoutComments = content.lines()
                .filter(line -> !line.strip().startsWith("//"))
                .reduce("", (acc, line) -> acc + line + "\n");
        List<String> statements = Arrays.stream(withoutComments.split(";"))
                .map(String::strip)
                .filter(statement -> !statement.isEmpty())
                .toList();
        if (statements.isEmpty()) {
            throw new IllegalStateException("Migration " + fileName + " has no statements");
        }
        return new Neo4jMigration(Integer.parseInt(matcher.group(1)), matcher.group(2).replace('_', ' '), statements);
    }
}
