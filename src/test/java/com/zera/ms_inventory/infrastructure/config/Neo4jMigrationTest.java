package com.zera.ms_inventory.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class Neo4jMigrationTest {

    @Test
    void shouldParseFilesWithWindowsLineEndings() {
        Neo4jMigration migration = Neo4jMigration.parse("V2__crlf.cypher",
                "// comentario\r\nCREATE (n:A)\r\nSET n.x = 1;\r\n\r\nMATCH (n:A) RETURN n;\r\n");

        assertThat(migration.statements()).containsExactly("CREATE (n:A)\nSET n.x = 1", "MATCH (n:A) RETURN n");
    }

    @Test
    void shouldKeepTheLastStatementWhenItHasNoTrailingSemicolon() {
        Neo4jMigration migration = Neo4jMigration.parse("V3__no_semicolon.cypher", "RETURN 1;\nRETURN 2");

        assertThat(migration.statements()).containsExactly("RETURN 1", "RETURN 2");
    }

    @Test
    void shouldReadTheVersionFromZeroPaddedNames() {
        Neo4jMigration migration = Neo4jMigration.parse("V012__padded_name.cypher", "RETURN 1;");

        assertThat(migration.version()).isEqualTo(12);
        assertThat(migration.description()).isEqualTo("padded name");
    }

    @ParameterizedTest
    @ValueSource(strings = {"v1__lowercase.cypher", "V1_single_underscore.cypher", "V__no_version.cypher",
            "V1__wrong_extension.txt", "V1__.cypher", "V1__with-dash.cypher", "V1a__letters.cypher"})
    void shouldRejectFileNamesOutsideTheConvention(String fileName) {
        assertThatThrownBy(() -> Neo4jMigration.parse(fileName, "RETURN 1;"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(fileName);
    }

    @Test
    void shouldRejectAMigrationMadeOnlyOfCommentsAndBlankStatements() {
        assertThatThrownBy(() -> Neo4jMigration.parse("V4__only_noise.cypher", "// nada\n;\n  ;\n// fim"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("V4__only_noise.cypher");
    }
}
