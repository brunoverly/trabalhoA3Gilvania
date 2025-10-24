-- Criação das tabelas do banco projeto_java_a3

DROP TABLE IF EXISTS `atualizacoes`;
CREATE TABLE `atualizacoes` (
  `id` int NOT NULL AUTO_INCREMENT,
  `tipo` varchar(100) NOT NULL,
  `cod_os` varchar(100) NOT NULL,
  `descricao` varchar(100) NOT NULL,
  `matricula` varchar(100) NOT NULL,
  `datahora` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `cod_item` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=797 DEFAULT CHARSET=utf8mb3;

DROP TABLE IF EXISTS `controle_retirada_itens`;
CREATE TABLE `controle_retirada_itens` (
  `id` int NOT NULL AUTO_INCREMENT,
  `id_item` int NOT NULL,
  `entregue_para` int DEFAULT NULL,
  `data_retirada` datetime DEFAULT CURRENT_TIMESTAMP,
  `cod_os` varchar(100) NOT NULL,
  `cod_operacao` varchar(100) NOT NULL,
  `entregue_por` int NOT NULL,
  `qtd_retirada` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `id_item` (`id_item`),
  CONSTRAINT `fk_id_item` FOREIGN KEY (`id_item`) REFERENCES `item` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=65 DEFAULT CHARSET=utf8mb3;

DROP TABLE IF EXISTS `controle_solicitacao_item`;
CREATE TABLE `controle_solicitacao_item` (
  `id` int NOT NULL AUTO_INCREMENT,
  `datahora_solicitacao` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `solicitador_por` int NOT NULL,
  `id_item` int NOT NULL,
  `qtd_solicitada` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_solicitador` (`solicitador_por`),
  KEY `fk_item` (`id_item`)
) ENGINE=InnoDB AUTO_INCREMENT=72 DEFAULT CHARSET=utf8mb3;

DROP TABLE IF EXISTS `item`;
CREATE TABLE `item` (
  `id` int NOT NULL AUTO_INCREMENT,
  `id_operacao` int NOT NULL,
  `cod_item` varchar(100) NOT NULL,
  `descricao` varchar(100) DEFAULT NULL,
  `qtd_pedido` decimal(10,2) NOT NULL,
  `qtd_recebida` decimal(10,2) NOT NULL DEFAULT '0.00',
  `status` varchar(255) DEFAULT 'Aguardando entrega',
  `localizacao` varchar(100) DEFAULT NULL,
  `ultima_atualizacao` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `id_operacao` (`id_operacao`),
  CONSTRAINT `item_ibfk_1` FOREIGN KEY (`id_operacao`) REFERENCES `operacao` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3078 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `operacao`;
CREATE TABLE `operacao` (
  `id` int NOT NULL AUTO_INCREMENT,
  `cod_operacao` varchar(100) DEFAULT NULL,
  `cod_os` varchar(100) DEFAULT NULL,
  `status` varchar(100) NOT NULL DEFAULT 'Em espera',
  PRIMARY KEY (`id`),
  KEY `fk_operacao_cod_os` (`cod_os`),
  CONSTRAINT `fk_operacao_cod_os` FOREIGN KEY (`cod_os`) REFERENCES `ordem_servico` (`cod_os`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=589 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `ordem_servico`;
CREATE TABLE `ordem_servico` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `datahora_abertura` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `cod_os` varchar(100) DEFAULT NULL,
  `status` varchar(255) DEFAULT 'Aberta',
  `datahora_encerramento` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ordem_servico_unique` (`cod_os`),
  UNIQUE KEY `numero_os` (`cod_os`)
) ENGINE=InnoDB AUTO_INCREMENT=74 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

DROP TABLE IF EXISTS `registro_pdf`;
CREATE TABLE `registro_pdf` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `datahora_emissao` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `emitido_por` int NOT NULL,
  `emitido_para` int NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb3;

DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `user_id` int NOT NULL AUTO_INCREMENT,
  `nome` varchar(100) NOT NULL,
  `cargo` varchar(100) DEFAULT NULL,
  `matricula` int NOT NULL,
  `pin` varchar(250) DEFAULT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `Users_unique` (`matricula`)
) ENGINE=InnoDB AUTO_INCREMENT=39 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

