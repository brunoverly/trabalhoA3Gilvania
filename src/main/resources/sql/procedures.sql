DELIMITER $$

CREATE PROCEDURE atualizar_item_entrada(
    IN p_id INT, IN p_status VARCHAR(100),
    IN p_localizacao VARCHAR(100), IN p_qtd_recebida INT,
    IN p_tipo VARCHAR(100), IN p_cod_os VARCHAR(100),
    IN p_descricao VARCHAR(255), IN p_matricula INT,
    IN p_coditem VARCHAR(100))
BEGIN
UPDATE item
SET status = p_status,
    localizacao = p_localizacao,
    qtd_recebida = p_qtd_recebida,
    ultima_atualizacao = NOW()
WHERE id = p_id;

INSERT INTO atualizacoes (tipo, cod_os, descricao, matricula, cod_item)
VALUES (p_tipo, p_cod_os, p_descricao, p_matricula, p_coditem);
END$$

CREATE PROCEDURE atualizar_item_saida(
    IN p_id INT, IN p_cod_operacao VARCHAR(100),
    IN p_tipo VARCHAR(100), IN p_cod_os VARCHAR(100),
    IN p_entregue_para INT, IN p_entregue_por INT,
    IN p_descricao VARCHAR(255), IN p_matricula INT,
    IN p_coditem VARCHAR(100), IN p_qtd_retirada INT,
    IN p_status_item VARCHAR(100))
BEGIN
    DECLARE novo_id_retirada BIGINT;
    DECLARE novo_id_pdf BIGINT;

INSERT INTO controle_retirada_itens
(id_item, entregue_para, data_retirada, cod_os, cod_operacao, entregue_por, qtd_retirada)
VALUES (p_id, p_entregue_para, NOW(), p_cod_os, p_cod_operacao, p_entregue_por, p_qtd_retirada);

SET novo_id_retirada = LAST_INSERT_ID();

UPDATE item SET status = p_status_item WHERE id = p_id;

UPDATE ordem_servico SET status = 'Em andamento'
WHERE cod_os = p_cod_os;

INSERT INTO atualizacoes (tipo, cod_os, descricao, matricula, cod_item)
VALUES (p_tipo, p_cod_os, p_descricao, p_matricula, p_coditem);

INSERT INTO registro_pdf (emitido_por, emitido_para)
VALUES (p_entregue_por, p_entregue_para);

SET novo_id_pdf = LAST_INSERT_ID();

SELECT novo_id_retirada AS id_retirada, novo_id_pdf AS id_pdf;
END$$

CREATE PROCEDURE buscar_itens_para_pdf()
BEGIN
SELECT
    i.id AS id_item,
    i.id_operacao,
    i.cod_item,
    i.descricao,
    cs.qtd_solicitada,
    COALESCE(SUM(cr.qtd_retirada), 0) AS qtd_retirada,
    MAX(cr.entregue_para) AS matricula_solicitador,
    o.cod_os AS cod_os,
    o.cod_operacao AS cod_operacao,
    cs.solicitador_por
FROM item i
         INNER JOIN controle_solicitacao_item cs ON i.id = cs.id_item
         INNER JOIN operacao o ON i.id_operacao = o.id
         INNER JOIN ordem_servico os ON o.cod_os = os.cod_os
         LEFT JOIN controle_retirada_itens cr ON i.id = cr.id_item
WHERE os.status <> 'Encerrado'
GROUP BY i.id, i.id_operacao, i.cod_item, i.descricao, cs.qtd_solicitada, cs.solicitador_por, o.cod_os, o.cod_operacao
HAVING SUM(cr.qtd_retirada) > 0;
END$$

CREATE PROCEDURE buscar_operacao_id(
    IN p_cod_operacao VARCHAR(100), IN p_cod_os VARCHAR(100),
    OUT p_id_operacao INT)
BEGIN
SELECT id INTO p_id_operacao
FROM operacao
WHERE cod_operacao = p_cod_operacao
  AND cod_os = p_cod_os
    LIMIT 1;
END$$

CREATE PROCEDURE cadastrar_usuario(
    IN p_matricula INT, IN p_nome VARCHAR(100),
    IN p_cargo VARCHAR(100), IN p_pin VARCHAR(250))
BEGIN
    DECLARE v_existente INT;
    DECLARE v_resultado INT;

SELECT COUNT(*) INTO v_existente
FROM users WHERE matricula = p_matricula;

IF v_existente = 0 THEN
        INSERT INTO users (nome, cargo, matricula, pin)
        VALUES (p_nome, p_cargo, p_matricula, p_pin);
        SET v_resultado = 1;
ELSE
        SET v_resultado = 0;
END IF;

SELECT v_resultado AS resultado;
END$$

CREATE PROCEDURE consultar_historico_retirada(
    IN p_data_inicio DATE, IN p_data_fim DATE)
BEGIN
SELECT
    cr.id AS id_retirada,
    cr.data_retirada,
    cr.id_item,
    cr.cod_os,
    cr.cod_operacao,
    i.cod_item,
    i.descricao,
    cr.qtd_retirada,
    cr.entregue_para
FROM controle_retirada_itens cr
         INNER JOIN item i ON cr.id_item = i.id
WHERE cr.data_retirada BETWEEN p_data_inicio AND p_data_fim
ORDER BY cr.data_retirada;
END$$

CREATE PROCEDURE consultar_historico_solicitacao(
    IN p_data_inicio DATE, IN p_data_fim DATE)
BEGIN
SELECT
    cs.id AS id_solicitacao,
    cs.datahora_solicitacao,
    i.cod_item,
    i.descricao,
    o.cod_os,
    o.cod_operacao,
    cs.qtd_solicitada,
    cs.solicitador_por
FROM controle_solicitacao_item cs
         INNER JOIN item i ON cs.id_item = i.id
         INNER JOIN operacao o ON i.id_operacao = o.id
WHERE cs.datahora_solicitacao BETWEEN p_data_inicio AND p_data_fim
ORDER BY cs.datahora_solicitacao;
END$$

CREATE PROCEDURE consultar_item(IN p_cod_os VARCHAR(100))
BEGIN
    DECLARE v_existente INT;

SELECT COUNT(*) INTO v_existente
FROM ordem_servico
WHERE cod_os = p_cod_os
  AND status <> 'Encerrada';

IF v_existente = 0 THEN
SELECT v_existente AS resultado;
ELSE
SELECT item.id, item.cod_item, item.id_operacao,
       operacao.cod_operacao, item.descricao,
       item.qtd_pedido, item.qtd_recebida, item.status,
       COALESCE(cs.qtd_solicitada, 0) AS qtd_solicitada,
       COALESCE(cr.qtd_retirada, 0) AS AS qtd_retirada
FROM item
         JOIN operacao ON operacao.id = item.id_operacao
         LEFT JOIN controle_solicitacao_item cs ON cs.id_item = item.id
         LEFT JOIN controle_retirada_itens cr ON cr.id_item = item.id
WHERE operacao.cod_os = p_cod_os;

SELECT cod_operacao, MAX(id) AS id, MAX(status) AS status
FROM operacao
WHERE cod_os = p_cod_os
GROUP BY cod_operacao;
END IF;
END$$

CREATE PROCEDURE consultar_item_att_saida(IN p_id_item INT)
BEGIN
SELECT localizacao, status, qtd_recebida
FROM item WHERE id = p_id_item;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `consultar_os`(IN p_cod_os VARCHAR(100))
BEGIN
SELECT COUNT(*) AS total
FROM ordem_servico
WHERE cod_os = p_cod_os;

SELECT
    item.id,
    item.cod_item,
    item.id_operacao,
    operacao.cod_operacao,
    item.descricao,
    item.qtd_pedido,
    item.qtd_recebida,
    item.status,
    COALESCE(cs.qtd_solicitada, 0) AS qtd_solicitada,
    COALESCE(cr.qtd_retirada, 0) AS qtd_retirada
FROM item
         JOIN operacao ON operacao.id = item.id_operacao
         LEFT JOIN controle_solicitacao_item cs ON cs.id_item = item.id
         LEFT JOIN controle_retirada_itens cr ON cr.id_item = item.id
WHERE operacao.cod_os = p_cod_os;

-- 3º RESULTSET: retorna operações da OS
SELECT
    cod_operacao,
    MAX(id) AS id,
    MAX(status) AS status
FROM operacao
WHERE cod_os = p_cod_os
GROUP BY cod_operacao;
END;


CREATE PROCEDURE encerrar_os(
    IN p_cod_os VARCHAR(100), IN p_tipo VARCHAR(100),
    IN p_descricao VARCHAR(255), IN p_matricula INT)
BEGIN
    DECLARE v_status_os VARCHAR(100);
    DECLARE v_resultado INT;

SELECT status INTO v_status_os
FROM ordem_servico
WHERE cod_os = p_cod_os
    LIMIT 1;

IF v_status_os IS NULL THEN
        SET v_resultado = 0;
    ELSEIF v_status_os = 'Encerrada' THEN
        SET v_resultado = 1;
ELSE
UPDATE ordem_servico
SET status = 'Encerrada',
    datahora_encerramento = NOW()
WHERE cod_os = p_cod_os;

UPDATE operacao
SET status = 'OS encerrada'
WHERE cod_os = p_cod_os;

UPDATE item i
    INNER JOIN operacao o ON i.id_operacao = o.id
    SET i.status = 'OS encerrada'
WHERE o.cod_os = p_cod_os;

INSERT INTO atualizacoes (tipo, cod_os, descricao, matricula, datahora)
VALUES (p_tipo, p_cod_os, p_descricao, p_matricula, NOW());

SET v_resultado = 2;
END IF;

SELECT v_resultado AS resultado;
END$$

CREATE PROCEDURE encerrar_os_dados(IN p_cod_os VARCHAR(100))
BEGIN
    DECLARE v_count INT;

SELECT COUNT(*) INTO v_count
FROM operacao WHERE cod_os = p_cod_os;

IF v_count = 0 THEN
SELECT 0 AS resultado;
ELSE
SELECT 1 AS resultado;

SELECT item.cod_item, item.id_operacao, operacao.cod_operacao,
       item.descricao, item.qtd_pedido, item.qtd_recebida,
       item.status, COALESCE(cs.qtd_solicitada, 0),
       COALESCE(cr.qtd_retirada, 0)
FROM item
         JOIN operacao ON operacao.id = item.id_operacao
         LEFT JOIN controle_solicitacao_item cs ON cs.id_item = item.id
         LEFT JOIN controle_retirada_itens cr ON cr.id_item = item.id
WHERE operacao.cod_os = p_cod_os;

SELECT cod_operacao, MAX(id) AS id, MAX(status) AS status
FROM operacao
WHERE cod_os = p_cod_os
GROUP BY cod_operacao;
END IF;
END$$

CREATE PROCEDURE excel_verificar(
    IN p_cod_os VARCHAR(100), IN p_matricula INT,
    OUT p_os_existente TINYINT(1), OUT p_os_inserida TINYINT(1))
BEGIN
    DECLARE v_count INT DEFAULT 0;

SELECT COUNT(*) INTO v_count
FROM ordem_servico WHERE cod_os = p_cod_os;

IF v_count > 0 THEN
        SET p_os_existente = TRUE;
        SET p_os_inserida = FALSE;
ELSE
        INSERT INTO ordem_servico (datahora_abertura, cod_os)
        VALUES (NOW(), p_cod_os);

        SET p_os_existente = FALSE;
        SET p_os_inserida = TRUE;

INSERT INTO atualizacoes (tipo, cod_os, descricao, matricula)
VALUES ('Ordem de serviço', p_cod_os, 'Cadastro de nova ordem de serviço', p_matricula);
END IF;
END$$

CREATE PROCEDURE inicio_dashboard_tableview()
BEGIN
SELECT
    SUM(status != 'Encerrada') AS abertas,
    SUM(status = 'Encerrada') AS encerradas,
    SUM(status = 'Em andamento') AS em_andamento
FROM ordem_servico;

SELECT datahora, tipo, cod_os, descricao, matricula, cod_item
FROM atualizacoes
ORDER BY datahora DESC
    LIMIT 30;
END$$

CREATE PROCEDURE inserir_item(
    IN p_id_operacao INT, IN p_cod_item VARCHAR(100),
    IN p_descricao VARCHAR(255), IN p_qtd_pedido INT)
BEGIN
INSERT INTO item (id_operacao, cod_item, descricao, qtd_pedido)
VALUES (p_id_operacao, p_cod_item, p_descricao, p_qtd_pedido);
END$$

CREATE PROCEDURE inserir_operacao(
    IN p_cod_operacao VARCHAR(100), IN p_cod_os VARCHAR(100),
    OUT p_id_operacao INT)
BEGIN
INSERT INTO operacao (cod_operacao, cod_os)
VALUES (p_cod_operacao, p_cod_os);

SET p_id_operacao = LAST_INSERT_ID();
END$$

CREATE PROCEDURE lancar_entrada_itens_por_operacao(
    IN p_id INT, IN p_cod_os VARCHAR(100),
    IN p_cod_operacao VARCHAR(50),
    IN p_status VARCHAR(100), IN p_tipo VARCHAR(100),
    IN p_descricao VARCHAR(255), IN p_matricula INT,
    IN p_coditem VARCHAR(100))
BEGIN
    DECLARE v_id_operacao INT;

SELECT id INTO v_id_operacao
FROM operacao
WHERE cod_operacao = p_cod_operacao
  AND cod_os = p_cod_os
    LIMIT 1;

UPDATE item
SET status = p_status,
    qtd_recebida = qtd_pedido
WHERE id_operacao = v_id_operacao;

INSERT INTO atualizacoes (tipo, cod_os, descricao, matricula, cod_item)
VALUES (p_tipo, p_cod_os, p_descricao, p_matricula, p_coditem);
END$$

CREATE PROCEDURE login(IN p_matricula INT)
BEGIN
SELECT nome, matricula, cargo, pin
FROM users
WHERE matricula = p_matricula
    LIMIT 1;
END$$

CREATE PROCEDURE registrar_pdf(
    IN p_emitido_por INT, IN p_emitido_para INT,
    OUT p_id_pdf INT)
BEGIN
INSERT INTO registro_pdf (datahora_emissao, emitido_por, emitido_para)
VALUES (NOW(), p_emitido_por, p_emitido_para);

SET p_id_pdf = LAST_INSERT_ID();
END$$

CREATE PROCEDURE remover_usuario(IN p_matricula INT)
BEGIN
    DECLARE v_existente INT;
    DECLARE v_resultado INT;

SELECT COUNT(*) INTO v_existente
FROM users WHERE matricula = p_matricula;

IF v_existente = 0 THEN
        SET v_resultado = 0;
SELECT v_resultado AS resultado;
ELSE
DELETE FROM users WHERE matricula = p_matricula;
SET v_resultado = 1;
SELECT v_resultado AS resultado;
END IF;
END$$

CREATE PROCEDURE remover_usuario_dados(IN p_matricula INT)
BEGIN
    IF EXISTS (SELECT 1 FROM users WHERE matricula = p_matricula) THEN
SELECT nome, cargo, matricula, 1 AS resultado
FROM users WHERE matricula = p_matricula;
ELSE
SELECT NULL AS nome, NULL AS cargo, NULL AS matricula, 0 AS resultado;
END IF;
END$$

CREATE PROCEDURE solicitar_item(
    IN p_id INT, IN p_id_operacao INT, IN p_cod_os VARCHAR(100),
    IN p_solicitado_por INT, IN p_matricula INT,
    IN p_status_item VARCHAR(100), IN p_status_operacao VARCHAR(100),
    IN p_cod_item VARCHAR(100), IN p_qtd_solicitada INT,
    IN p_tipo VARCHAR(100), IN p_descricao VARCHAR(100))
BEGIN
UPDATE item SET status = p_status_item WHERE id = p_id;

UPDATE operacao SET status = p_status_operacao WHERE id = p_id_operacao;

INSERT INTO atualizacoes (tipo, cod_os, descricao, matricula, cod_item)
VALUES (p_tipo, p_cod_os, p_descricao, p_matricula, p_cod_item);

INSERT INTO controle_solicitacao_item (solicitador_por, id_item, qtd_solicitada)
VALUES (p_solicitado_por, p_id, p_qtd_solicitada);
END$$

CREATE PROCEDURE somar_retiradas_item(IN p_id_item INT)
BEGIN
SELECT
    COALESCE(SUM(qtd_retirada), 0) AS total_retirado
FROM controle_retirada_itens
WHERE id_item = p_id_item;

SELECT
    qtd_solicitada,
    solicitador_por
FROM controle_solicitacao_item
WHERE id_item = p_id_item;
END$$

CREATE PROCEDURE verificar_disponibilidade_item(IN p_id_item INT)
BEGIN
SELECT
    i.qtd_recebida - COALESCE(SUM(c.qtd_solicitada), 0) AS qtd_disponivel,
    COALESCE(SUM(c.qtd_solicitada), 0) AS qtd_solicitada
FROM item i
         LEFT JOIN controle_solicitacao_item c ON i.id = c.id_item
WHERE i.id = p_id_item
GROUP BY i.qtd_recebida;
END$$

CREATE PROCEDURE verificar_ou_inserir_os(
    IN p_cod_os VARCHAR(100), IN p_matricula INT,
    OUT p_os_existente TINYINT(1), OUT p_os_inserida TINYINT(1))
BEGIN
    DECLARE v_count INT DEFAULT 0;

SELECT COUNT(*) INTO v_count
FROM ordem_servico WHERE cod_os = p_cod_os;

IF v_count > 0 THEN
        SET p_os_existente = TRUE;
        SET p_os_inserida = FALSE;
ELSE
        INSERT INTO ordem_servico (datahora_abertura, cod_os)
        VALUES (NOW(), p_cod_os);

        SET p_os_existente = FALSE;
        SET p_os_inserida = TRUE;

INSERT INTO atualizacoes (tipo, cod_os, descricao, matricula)
VALUES ('Ordem de serviço', p_cod_os, 'Cadastro de nova ordem de serviço', p_matricula);
END IF;
END$$

DELIMITER ;
