DELIMITER ;;
CREATE DEFINER=`master`@`%` PROCEDURE `atualizar_item_entrada`(
    IN p_id INT,
    IN p_status VARCHAR(100),
    IN p_localizacao VARCHAR(100),
    IN p_qtd_recebida INT,
    IN p_tipo VARCHAR(100),
    IN p_cod_os VARCHAR(100),
    IN p_descricao VARCHAR(255),
    IN p_matricula INT,
    IN p_coditem VARCHAR(100)
)
BEGIN
    UPDATE item
    SET status = p_status,
        localizacao = p_localizacao,
        qtd_recebida = p_qtd_recebida,
        ultima_atualizacao = NOW()
    WHERE id = p_id;

    INSERT INTO atualizacoes (tipo, cod_os, descricao, matricula, cod_item)
    VALUES (p_tipo, p_cod_os, p_descricao, p_matricula, p_coditem);
END ;;
DELIMITER ;

DELIMITER ;;
CREATE DEFINER=`master`@`%` PROCEDURE `atualizar_item_saida`(
    IN p_id INT,
    IN p_cod_operacao VARCHAR(100),
    IN p_tipo VARCHAR(100),
    IN p_cod_os VARCHAR(100),
    IN p_entregue_para INT,
    IN p_entregue_por INT,
    IN p_descricao VARCHAR(255),
    IN p_matricula INT,
    IN p_coditem VARCHAR(100),
    IN p_qtd_retirada INT,
    IN p_status_item VARCHAR(100)
)
BEGIN
    DECLARE novo_id_retirada BIGINT;
    DECLARE novo_id_pdf BIGINT;

    INSERT INTO controle_retirada_itens
        (id_item, entregue_para, data_retirada, cod_os, cod_operacao, entregue_por, qtd_retirada)
    VALUES
        (p_id, p_entregue_para, NOW(), p_cod_os, p_cod_operacao, p_entregue_por, p_qtd_retirada);

    SET novo_id_retirada = LAST_INSERT_ID();

    UPDATE item
        SET status = p_status_item
        WHERE id = p_id;

    UPDATE ordem_servico
        SET status = 'Em andamento'
        WHERE cod_os = p_cod_os;

    INSERT INTO atualizacoes (tipo, cod_os, descricao, matricula, cod_item)
    VALUES (p_tipo, p_cod_os, p_descricao, p_matricula, p_coditem);

    INSERT INTO registro_pdf (emitido_por, emitido_para)
    VALUES (p_entregue_por, p_entregue_para);

    SET novo_id_pdf = LAST_INSERT_ID();

    SELECT novo_id_retirada AS id_retirada, novo_id_pdf AS id_pdf;
END ;;
DELIMITER ;

DELIMITER ;;
CREATE DEFINER=`master`@`%` PROCEDURE `buscar_itens_para_pdf`()
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
END ;;
DELIMITER ;

DELIMITER ;;
CREATE DEFINER=`master`@`%` PROCEDURE `buscar_operacao_id`(
    IN p_cod_operacao VARCHAR(100),
    IN p_cod_os VARCHAR(100),
    OUT p_id_operacao INT
)
BEGIN
    SELECT id INTO p_id_operacao
    FROM operacao
    WHERE cod_operacao = p_cod_operacao
      AND cod_os = p_cod_os
    LIMIT 1;
END ;;
DELIMITER ;

DELIMITER ;;
CREATE DEFINER=`master`@`%` PROCEDURE `cadastrar_usuario`(
    IN p_matricula INT,
    IN p_nome VARCHAR(100),
    IN p_cargo VARCHAR(100),
    IN p_pin VARCHAR(250)
)
BEGIN
    DECLARE v_existente INT;
    DECLARE v_resultado INT;

    SELECT COUNT(*) INTO v_existente
    FROM users
    WHERE matricula = p_matricula;

    IF v_existente = 0 THEN
        INSERT INTO users (nome, cargo, matricula, pin)
        VALUES (p_nome, p_cargo, p_matricula, p_pin);
        SET v_resultado = 1;
    ELSE
        SET v_resultado = 0;
    END IF;

    SELECT v_resultado AS resultado;
END ;;
DELIMITER ;

DELIMITER ;;
CREATE DEFINER=`master`@`%` PROCEDURE `consultar_historico_retirada`(
    IN p_data_inicio DATE,
    IN p_data_fim DATE
)
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
    WHERE cr.data_retirada >= p_data_inicio
      AND cr.data_retirada <= p_data_fim
    ORDER BY cr.data_retirada;
END ;;
DELIMITER ;

DELIMITER ;;
CREATE DEFINER=`master`@`%` PROCEDURE `consultar_historico_solicitacao`(
    IN p_data_inicio DATE,
    IN p_data_fim DATE
)
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
    WHERE cs.datahora_solicitacao >= p_data_inicio
      AND cs.datahora_solicitacao <= p_data_fim
    ORDER BY cs.datahora_solicitacao;
END ;;
DELIMITER ;

DELIMITER ;;
CREATE DEFINER=`master`@`%` PROCEDURE `consultar_item`(
    IN p_cod_os VARCHAR(100)
)
BEGIN
    DECLARE v_existente INT;

    SELECT COUNT(*) INTO v_existente
    FROM ordem_servico 
    WHERE cod_os = p_cod_os
      AND status <> 'Encerrada';

    IF v_existente = 0 THEN
        SELECT v_existente AS resultado;
    ELSE
        SELECT item.id,
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

        SELECT cod_operacao,
               MAX(id) AS id,
               MAX(status) AS status
        FROM operacao
        WHERE cod_os = p_cod_os
        GROUP BY cod_operacao;
    END IF;
END ;;
DELIMITER ;

DELIMITER ;;
CREATE DEFINER=`master`@`%` PROCEDURE `consultar_item_att_saida`(
    IN p_id_item INT
)
BEGIN
    SELECT localizacao, status, qtd_recebida
    FROM item
    WHERE id = p_id_item;
END ;;
DELIMITER ;

DELIMITER ;;
CREATE DEFINER=`master`@`%` PROCEDURE `consultar_os`(
    IN p_cod_os VARCHAR(100)
)
BEGIN
    SELECT COUNT(*) AS total
    FROM ordem_servico 
    WHERE cod_os = p_cod_os;

    SELECT item.id,
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

    SELECT cod_operacao,
           MAX(id) AS id,
           MAX(status) AS status
    FROM operacao
    WHERE cod_os = p_cod_os
    GROUP BY cod_operacao;
END ;;
DELIMITER ;

DELIMITER ;;
CREATE DEFINER=`master`@`%` PROCEDURE `encerrar_os`(
    IN p_cod_os VARCHAR(100)
)
BEGIN
    UPDATE ordem_servico
    SET status = 'Encerrado',
        datahora_encerramento = NOW()
    WHERE cod_os = p_cod_os;

    UPDATE operacao
    SET status = 'Encerrado'
    WHERE cod_os = p_cod_os;
END ;;
DELIMITER ;

DELIMITER ;;
CREATE DEFINER=`master`@`%` PROCEDURE `inserir_item`(
    IN p_id_operacao INT,
    IN p_cod_item VARCHAR(100),
    IN p_descricao VARCHAR(100),
    IN p_qtd_pedido DECIMAL(10,2)
)
BEGIN
    INSERT INTO item (id_operacao, cod_item, descricao, qtd_pedido, qtd_recebida, status)
    VALUES (p_id_operacao, p_cod_item, p_descricao, p_qtd_pedido, 0, 'Aguardando entrega');
END ;;
DELIMITER ;

