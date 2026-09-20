// Campos usados pela previsao de quebra passam a ter o formato da regra de negocio:
// ano de fabricacao e data (sem hora) prevista de quebra. A intensidade de uso segue
// na escala 0 a 10 do cadastro; a V008 desfaz a conversao para texto que esta migracao
// chegou a aplicar em bancos que rodaram a versao anterior dela.
MATCH (i:Item)
WHERE i.manufacturingDate IS NOT NULL
SET i.manufacturingYear = i.manufacturingDate
REMOVE i.manufacturingDate;

MATCH (i:Item)
WHERE i.nextPredictionDate IS NOT NULL
SET i.predictedFailureDate = date(i.nextPredictionDate)
REMOVE i.nextPredictionDate;
