package com.zera.ms_inventory;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class MsInventoryApplicationTests {

    /**
     * O proprio {@code @SpringBootTest} ja sobe o contexto sob o perfil de teste; nao ha por que
     * chamar {@code main()} de novo aqui dentro. Fazer isso criava um SEGUNDO contexto que ignora
     * {@code @ActiveProfiles}, entao rodava sem o perfil de teste — so nao quebrava porque um bug
     * separado (arquivo de properties de teste sombreando o principal) zerava a configuracao real
     * por acidente. Removido esse bug, este teste exigiria Neo4j real com as credenciais de
     * producao para passar, o que teste nenhum deveria exigir.
     */
    @Test
    void contextLoads() {
    }

}