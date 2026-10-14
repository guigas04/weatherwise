-- =============================================
-- Drop das tabelas e tipo na ordem correta
-- =============================================
DROP TABLE IF EXISTS Auditoria CASCADE; 
DROP TABLE IF EXISTS RegistoMeteorologico CASCADE; 
DROP TABLE IF EXISTS Localidade CASCADE; 
DROP TABLE IF EXISTS Autor CASCADE; 
DROP TYPE IF EXISTS CondicaoClimatica CASCADE;


-- =============================================
-- Criar tipo ENUM para CondicaoClimatica
-- =============================================
CREATE TYPE CondicaoClimatica AS ENUM (
    'SOL',
    'NUBLADO', 
    'CHUVA',
    'CHUVA_FORTE',
    'NEVE',
    'TEMPESTADE',
    'NEVOEIRO',
    'VENTO',
    'ENSOLARADO'
);

-- =============================================
-- Tabela Localidade
-- =============================================
CREATE TABLE Localidade (
    Id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL UNIQUE
);

-- =============================================
-- Tabela Autor
-- =============================================
CREATE TABLE Autor (
    Id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL
);

-- =============================================
-- Tabela RegistoMeteorologico
-- =============================================
CREATE TABLE RegistoMeteorologico (
    Id BIGSERIAL PRIMARY KEY,
    localidadeId BIGINT NOT NULL,
    condicaoClimatica CondicaoClimatica NOT NULL,
    data DATE NOT NULL,
    temperatura DOUBLE PRECISION NOT NULL,
    FOREIGN KEY (localidadeId) REFERENCES Localidade(Id),
    CONSTRAINT uk_registo_localidade_data UNIQUE (localidadeId, data)
);

-- =============================================
-- Tabela Auditoria
-- =============================================
CREATE TABLE Auditoria (
    Id BIGSERIAL PRIMARY KEY,
    autorId BIGINT NOT NULL,
    localidadeId BIGINT NOT NULL,
    tipoConsulta VARCHAR(255) NOT NULL,
    dataHora TIMESTAMP NOT NULL,
    FOREIGN KEY (autorId) REFERENCES Autor(Id),
    FOREIGN KEY (localidadeId) REFERENCES Localidade(Id)
);