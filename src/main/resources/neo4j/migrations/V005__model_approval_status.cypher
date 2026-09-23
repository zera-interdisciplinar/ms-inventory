// Modelos anteriores ao fluxo de aprovacao foram cadastrados por gestores: ficam aprovados.
MATCH (m:Model)
WHERE m.approvalStatus IS NULL
SET m.approvalStatus = 'APPROVED';
