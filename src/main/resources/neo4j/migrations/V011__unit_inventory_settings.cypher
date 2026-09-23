// Configuracao de estoque por unidade: uma unica configuracao por unidade, com a capacidade que
// serve de denominador da ocupacao no painel.
CREATE CONSTRAINT unit_inventory_settings_unit_unique IF NOT EXISTS
FOR (s:UnitInventorySettings) REQUIRE s.unitId IS UNIQUE;
