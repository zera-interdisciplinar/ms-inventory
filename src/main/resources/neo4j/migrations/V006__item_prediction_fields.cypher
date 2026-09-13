// Campos usados pela previsao de quebra passam a ter o formato da regra de negocio:
// ano de fabricacao, intensidade LOW/MEDIUM/HIGH e data (sem hora) prevista de quebra.
MATCH (i:Item)
WHERE i.manufacturingDate IS NOT NULL
SET i.manufacturingYear = i.manufacturingDate
REMOVE i.manufacturingDate;

// A escala antiga era 0 a 10 (em QA, valores de 3 a 10): 0-3 LOW, 4-7 MEDIUM, 8 ou mais HIGH.
MATCH (i:Item)
WHERE i.usageIntensity IS NOT NULL AND valueType(i.usageIntensity) STARTS WITH 'INTEGER'
SET i.usageIntensity = CASE
  WHEN i.usageIntensity <= 3 THEN 'LOW'
  WHEN i.usageIntensity <= 7 THEN 'MEDIUM'
  ELSE 'HIGH'
END;

MATCH (i:Item)
WHERE i.nextPredictionDate IS NOT NULL
SET i.predictedFailureDate = date(i.nextPredictionDate)
REMOVE i.nextPredictionDate;
