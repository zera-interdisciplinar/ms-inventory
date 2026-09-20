// Catalogo global de materiais: alimenta o guia "Como descartar", os indicadores de residuos por
// material e o alerta de reciclavel enviado ao aterro. Nao e escopado por unidade.
CREATE CONSTRAINT material_code_unique IF NOT EXISTS
FOR (m:Material) REQUIRE m.code IS UNIQUE;

UNWIND [
  {code: 'PLASTIC', name: 'Plástico', recyclable: true, hazardous: false,
   disposalGuide: 'Separe de restos de comida e líquidos e leve a um ponto de coleta seletiva de plásticos.'},
  {code: 'METAL', name: 'Metal', recyclable: true, hazardous: false,
   disposalGuide: 'Carcaças, parafusos e peças metálicas vão para ferro-velho ou coleta seletiva de metais.'},
  {code: 'GLASS', name: 'Vidro', recyclable: true, hazardous: false,
   disposalGuide: 'Embale peças quebradas em papelão para evitar cortes e leve à coleta seletiva de vidro.'},
  {code: 'PAPER', name: 'Papel e papelão', recyclable: true, hazardous: false,
   disposalGuide: 'Mantenha seco e dobrado. Manuais, caixas e embalagens vão para a coleta seletiva de papel.'},
  {code: 'BATTERY', name: 'Pilhas e baterias', recyclable: true, hazardous: true,
   disposalGuide: 'Nunca descarte no lixo comum. Leve a pontos de coleta de pilhas e baterias (logística reversa).'},
  {code: 'CIRCUIT_BOARD', name: 'Placas eletrônicas', recyclable: true, hazardous: true,
   disposalGuide: 'Contêm metais pesados. Encaminhe a recicladoras de eletroeletrônicos.'},
  {code: 'CABLE', name: 'Cabos e fios', recyclable: true, hazardous: false,
   disposalGuide: 'Enrole e prenda os cabos. Recicladoras de eletrônicos recuperam o cobre e o plástico.'},
  {code: 'SCREEN', name: 'Telas e monitores', recyclable: true, hazardous: true,
   disposalGuide: 'Não quebre a tela. Leve inteira a uma recicladora de eletroeletrônicos.'},
  {code: 'OTHER', name: 'Outros', recyclable: false, hazardous: false,
   disposalGuide: 'Consulte o assistente ou um ponto de coleta para confirmar o destino correto.'}
] AS material
MERGE (m:Material {code: material.code})
ON CREATE SET m.id = randomUUID(),
              m.name = material.name,
              m.recyclable = material.recyclable,
              m.hazardous = material.hazardous,
              m.disposalGuide = material.disposalGuide;
