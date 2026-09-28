// Trava de execucao dos jobs: o id e (job, janela), entao a segunda replica que tentar a mesma
// janela esbarra na constraint e desiste, em vez de rodar o job em dobro.
CREATE CONSTRAINT job_run_id_unique IF NOT EXISTS
FOR (r:JobRun) REQUIRE r.id IS UNIQUE;

// Registro do ultimo envio de alerta por (unidade, regra, assunto), para nao repetir a chamada
// ao admin-core a cada execucao enquanto o item continua fora do limite.
CREATE CONSTRAINT alert_dispatch_id_unique IF NOT EXISTS
FOR (d:AlertDispatch) REQUIRE d.id IS UNIQUE;

CREATE INDEX alert_dispatch_sent_at IF NOT EXISTS
FOR (d:AlertDispatch) ON (d.sentAt);
