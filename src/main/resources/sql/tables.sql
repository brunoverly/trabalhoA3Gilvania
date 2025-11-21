-- 1) ORDEM_SERVICO
DROP TABLE IF EXISTS `ordem_servico`;
CREATE TABLE `ordem_servico` (
                                 `id` BIGINT NOT NULL AUTO_INCREMENT,
                                 `datahora_abertura` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 `cod_os` VARCHAR(100) DEFAULT NULL,
                                 `status` VARCHAR(255) DEFAULT 'Aberta',
                                 `datahora_encerramento` DATETIME DEFAULT NULL,
                                 PRIMARY KEY (`id`),
                                 UNIQUE KEY `u_cod_os` (`cod_os`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 2) OPERACAO
DROP TABLE IF EXISTS `operacao`;
CREATE TABLE `operacao` (
                            `id` INT NOT NULL AUTO_INCREMENT,
                            `cod_operacao` VARCHAR(100) DEFAULT NULL,
                            `cod_os` VARCHAR(100) DEFAULT NULL,
                            `status` VARCHAR(100) NOT NULL DEFAULT 'Em espera',
                            PRIMARY KEY (`id`),
                            KEY `fk_operacao_cod_os` (`cod_os`),
                            CONSTRAINT `fk_operacao_cod_os`
                                FOREIGN KEY (`cod_os`) REFERENCES `ordem_servico` (`cod_os`)
                                    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 3) ITEM
DROP TABLE IF EXISTS `item`;
CREATE TABLE `item` (
                        `id` INT NOT NULL AUTO_INCREMENT,
                        `id_operacao` INT NOT NULL,
                        `cod_item` VARCHAR(100) NOT NULL,
                        `descricao` VARCHAR(100) DEFAULT NULL,
                        `qtd_pedido` DECIMAL(10,2) NOT NULL,
                        `qtd_recebida` DECIMAL(10,2) NOT NULL DEFAULT '0.00',
                        `status` VARCHAR(255) DEFAULT 'Aguardando entrega',
                        `localizacao` VARCHAR(100) DEFAULT NULL,
                        `ultima_atualizacao` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        PRIMARY KEY (`id`),
                        KEY `idx_operacao` (`id_operacao`),
                        CONSTRAINT `fk_item_operacao`
                            FOREIGN KEY (`id_operacao`) REFERENCES `operacao` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 4) CONTROLE_SOLICITACAO_ITEM
DROP TABLE IF EXISTS `controle_solicitacao_item`;
CREATE TABLE `controle_solicitacao_item` (
                                             `id` INT NOT NULL AUTO_INCREMENT,
                                             `datahora_solicitacao` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                             `solicitador_por` INT NOT NULL,
                                             `id_item` INT NOT NULL,
                                             `qtd_solicitada` INT NOT NULL,
                                             PRIMARY KEY (`id`),
                                             KEY `fk_solicitador` (`solicitador_por`),
                                             KEY `fk_item` (`id_item`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5) CONTROLE_RETIRADA_ITENS
DROP TABLE IF EXISTS `controle_retirada_itens`;
CREATE TABLE `controle_retirada_itens` (
                                           `id` INT NOT NULL AUTO_INCREMENT,
                                           `id_item` INT NOT NULL,
                                           `entregue_para` INT DEFAULT NULL,
                                           `data_retirada` DATETIME DEFAULT CURRENT_TIMESTAMP,
                                           `cod_os` VARCHAR(100) NOT NULL,
                                           `cod_operacao` VARCHAR(100) NOT NULL,
                                           `entregue_por` INT NOT NULL,
                                           `qtd_retirada` INT NOT NULL,
                                           PRIMARY KEY (`id`),
                                           KEY `idx_item` (`id_item`),
                                           CONSTRAINT `fk_id_item` FOREIGN KEY (`id_item`) REFERENCES `item` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6) ATUALIZACOES
DROP TABLE IF EXISTS `atualizacoes`;
CREATE TABLE `atualizacoes` (
                                `id` INT NOT NULL AUTO_INCREMENT,
                                `tipo` VARCHAR(100) NOT NULL,
                                `cod_os` VARCHAR(100) NOT NULL,
                                `descricao` VARCHAR(100) NOT NULL,
                                `matricula` VARCHAR(100) NOT NULL,
                                `datahora` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                `cod_item` VARCHAR(100) DEFAULT NULL,
                                PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7) REGISTRO_PDF
DROP TABLE IF EXISTS `registro_pdf`;
CREATE TABLE `registro_pdf` (
                                `id` BIGINT NOT NULL AUTO_INCREMENT,
                                `datahora_emissao` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                `emitido_por` INT NOT NULL,
                                `emitido_para` INT NOT NULL,
                                PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 8) USERS
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
                         `user_id` INT NOT NULL AUTO_INCREMENT,
                         `nome` VARCHAR(100) NOT NULL,
                         `cargo` VARCHAR(100) DEFAULT NULL,
                         `matricula` INT NOT NULL,
                         `pin` VARCHAR(250) DEFAULT NULL,
                         PRIMARY KEY (`user_id`),
                         UNIQUE KEY `Users_unique` (`matricula`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- INSERT DE USUÁRIOS PARA TESTE
INSERT INTO `users` (`nome`, `cargo`, `matricula`) VALUES
                                                       ('Bruno Verly Santos', 'Administrador', 47219),
                                                       ('Carla Mendes Oliveira', 'Aprovisionador', 58302),
                                                       ('Lucas Silva Ferreira', 'Aprovisionador', 69047),
                                                       ('Rafael Souza Lima', 'Mecânico', 25138),
                                                       ('Mariana Costa Alves', 'Mecânico', 83714),
                                                       ('Thiago Lima Rocha', 'Mecânico', 41625);
