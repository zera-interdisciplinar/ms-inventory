// A intensidade de uso volta a ser a escala 0 a 10 que o cadastro envia e o sistema preditivo
// consome. Bancos que rodaram a versao anterior da V006 guardaram LOW/MEDIUM/HIGH: o valor
// original nao da para recuperar, entao cada faixa volta pelo seu meio (0-3, 4-7, 8-10).
MATCH (i:Item)
WHERE i.usageIntensity IS NOT NULL AND valueType(i.usageIntensity) STARTS WITH 'STRING'
SET i.usageIntensity = CASE i.usageIntensity
  WHEN 'LOW' THEN 2
  WHEN 'MEDIUM' THEN 6
  WHEN 'HIGH' THEN 9
  ELSE null
END;
