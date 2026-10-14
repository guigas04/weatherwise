-- =============================================
-- Inserir localidades
-- =============================================
INSERT INTO Localidade (nome) VALUES
('Lisboa'), ('Porto'), ('Braga'), ('Coimbra'), ('Faro'),
('Aveiro'), ('Leiria'), ('Funchal'), ('Ponta Delgada'), ('Évora'),
('Setúbal'), ('Viana do Castelo'), ('Guarda'), ('Beja'), ('Santarém'),
('Viseu'), ('Vila Real'), ('Castelo Branco'), ('Bragança'), ('Portalegre')
ON CONFLICT (nome) DO NOTHING;

-- =============================================
-- Inserir autores
-- =============================================
INSERT INTO Autor (nome) VALUES
('João Silva'), ('Maria Rodrigues'), ('Pedro Alves'), ('Ana Pereira'),
('Tiago Santos'), ('Rita Fonseca'), ('Luís Costa'), ('Sofia Gomes'),
('Miguel Moreira'), ('Sistema Automático')
ON CONFLICT DO NOTHING;

-- =============================================
-- Inserir auditoria de consultas
-- =============================================
INSERT INTO Auditoria (autorId, localidadeId, tipoConsulta, dataHora)
VALUES
((SELECT id FROM Autor WHERE nome='João Silva'), (SELECT id FROM Localidade WHERE nome='Lisboa'), 'previsao_atual', NOW() - INTERVAL '30 minutes'),
((SELECT id FROM Autor WHERE nome='Maria Rodrigues'), (SELECT id FROM Localidade WHERE nome='Porto'), 'historico', NOW() - INTERVAL '2 hours'),
((SELECT id FROM Autor WHERE nome='Pedro Alves'), (SELECT id FROM Localidade WHERE nome='Braga'), 'previsao_atual', NOW() - INTERVAL '3 hours'),
((SELECT id FROM Autor WHERE nome='Ana Pereira'), (SELECT id FROM Localidade WHERE nome='Coimbra'), 'previsao_atual', NOW() - INTERVAL '4 hours'),
((SELECT id FROM Autor WHERE nome='Sistema Automático'), (SELECT id FROM Localidade WHERE nome='Faro'), 'estimar_previsao', NOW() - INTERVAL '5 hours'),
((SELECT id FROM Autor WHERE nome='Tiago Santos'), (SELECT id FROM Localidade WHERE nome='Setúbal'), 'previsao_atual', NOW() - INTERVAL '6 hours'),
((SELECT id FROM Autor WHERE nome='Rita Fonseca'), (SELECT id FROM Localidade WHERE nome='Viana do Castelo'), 'historico', NOW() - INTERVAL '7 hours'),
((SELECT id FROM Autor WHERE nome='Luís Costa'), (SELECT id FROM Localidade WHERE nome='Beja'), 'previsao_atual', NOW() - INTERVAL '8 hours'),
((SELECT id FROM Autor WHERE nome='Sofia Gomes'), (SELECT id FROM Localidade WHERE nome='Guarda'), 'previsao_atual', NOW() - INTERVAL '9 hours'),
((SELECT id FROM Autor WHERE nome='Miguel Moreira'), (SELECT id FROM Localidade WHERE nome='Santarém'), 'estimar_previsao', NOW() - INTERVAL '10 hours'),

((SELECT id FROM Autor WHERE nome='Sistema Automático'), (SELECT id FROM Localidade WHERE nome='Faro'), 'previsao_atual', NOW() - INTERVAL '15 minutes'),
((SELECT id FROM Autor WHERE nome='João Silva'), (SELECT id FROM Localidade WHERE nome='Faro'), 'historico', NOW() - INTERVAL '1 hour'),
((SELECT id FROM Autor WHERE nome='Maria Rodrigues'), (SELECT id FROM Localidade WHERE nome='Faro'), 'previsao_atual', NOW() - INTERVAL '2 hours'),
((SELECT id FROM Autor WHERE nome='Pedro Alves'), (SELECT id FROM Localidade WHERE nome='Faro'), 'previsao_atual', NOW() - INTERVAL '3 hours'),
((SELECT id FROM Autor WHERE nome='Ana Pereira'), (SELECT id FROM Localidade WHERE nome='Faro'), 'estimar_previsao', NOW() - INTERVAL '4 hours'),
((SELECT id FROM Autor WHERE nome='Tiago Santos'), (SELECT id FROM Localidade WHERE nome='Faro'), 'previsao_atual', NOW() - INTERVAL '5 hours'),
((SELECT id FROM Autor WHERE nome='Rita Fonseca'), (SELECT id FROM Localidade WHERE nome='Faro'), 'historico', NOW() - INTERVAL '6 hours'),
((SELECT id FROM Autor WHERE nome='Luís Costa'), (SELECT id FROM Localidade WHERE nome='Faro'), 'previsao_atual', NOW() - INTERVAL '7 hours'),
((SELECT id FROM Autor WHERE nome='Sofia Gomes'), (SELECT id FROM Localidade WHERE nome='Faro'), 'previsao_atual', NOW() - INTERVAL '8 hours'),
((SELECT id FROM Autor WHERE nome='Miguel Moreira'), (SELECT id FROM Localidade WHERE nome='Faro'), 'estimar_previsao', NOW() - INTERVAL '9 hours'),


-- Lisboa: 3x do mesmo autor (Ana Pereira)
((SELECT id FROM Autor WHERE nome='Ana Pereira'), (SELECT id FROM Localidade WHERE nome='Lisboa'), 'previsao_atual', NOW() - INTERVAL '18 minutes'),
((SELECT id FROM Autor WHERE nome='Ana Pereira'), (SELECT id FROM Localidade WHERE nome='Lisboa'), 'previsao_atual', NOW() - INTERVAL '48 minutes'),
((SELECT id FROM Autor WHERE nome='Ana Pereira'), (SELECT id FROM Localidade WHERE nome='Lisboa'), 'previsao_atual', NOW() - INTERVAL '1 hour 10 minutes'),

-- Lisboa: 4x do mesmo autor (Pedro Alves)
((SELECT id FROM Autor WHERE nome='Pedro Alves'), (SELECT id FROM Localidade WHERE nome='Lisboa'), 'previsao_atual', NOW() - INTERVAL '15 minutes'),
((SELECT id FROM Autor WHERE nome='Pedro Alves'), (SELECT id FROM Localidade WHERE nome='Lisboa'), 'previsao_atual', NOW() - INTERVAL '35 minutes'),
((SELECT id FROM Autor WHERE nome='Pedro Alves'), (SELECT id FROM Localidade WHERE nome='Lisboa'), 'previsao_atual', NOW() - INTERVAL '55 minutes'),
((SELECT id FROM Autor WHERE nome='Pedro Alves'), (SELECT id FROM Localidade WHERE nome='Lisboa'), 'previsao_atual', NOW() - INTERVAL '1 hour 15 minutes')

ON CONFLICT DO NOTHING;


-- =============================================
-- Inserir registos meteorológicos
-- =============================================
INSERT INTO RegistoMeteorologico (localidadeId, condicaoClimatica, data, temperatura) VALUES
-- Lisboa
((SELECT id FROM Localidade WHERE nome = 'Lisboa'), 'SOL', '2025-10-20', 26.1),
((SELECT id FROM Localidade WHERE nome = 'Lisboa'), 'NUBLADO', '2025-10-21', 23.4),
((SELECT id FROM Localidade WHERE nome = 'Lisboa'), 'CHUVA', '2025-10-22', 20.9),
((SELECT id FROM Localidade WHERE nome = 'Lisboa'), 'VENTO', '2025-10-23', 21.5),
((SELECT id FROM Localidade WHERE nome = 'Lisboa'), 'ENSOLARADO', '2025-10-24', 25.7),
((SELECT id FROM Localidade WHERE nome = 'Lisboa'), 'SOL', '2025-10-25', 26.1),
((SELECT id FROM Localidade WHERE nome = 'Lisboa'), 'NUBLADO', '2025-10-26', 23.4),
((SELECT id FROM Localidade WHERE nome = 'Lisboa'), 'CHUVA', '2025-10-27', 20.9),
((SELECT id FROM Localidade WHERE nome = 'Lisboa'), 'VENTO', '2025-10-28', 21.5),
((SELECT id FROM Localidade WHERE nome = 'Lisboa'), 'ENSOLARADO', '2025-10-29', 25.7),
((SELECT id FROM Localidade WHERE nome = 'Lisboa'), 'SOL', '2025-10-30', 26.1),
((SELECT id FROM Localidade WHERE nome = 'Lisboa'), 'NUBLADO', '2025-10-31', 23.4),
((SELECT id FROM Localidade WHERE nome = 'Lisboa'), 'SOL', '2025-11-01', 20.9),
((SELECT id FROM Localidade WHERE nome = 'Lisboa'), 'CHUVA', '2025-11-02', 19.5),

-- Porto
((SELECT id FROM Localidade WHERE nome = 'Porto'), 'CHUVA', '2025-10-20', 18.4),
((SELECT id FROM Localidade WHERE nome = 'Porto'), 'CHUVA', '2025-10-21', 17.2),
((SELECT id FROM Localidade WHERE nome = 'Porto'), 'NUBLADO', '2025-10-22', 19.0),
((SELECT id FROM Localidade WHERE nome = 'Porto'), 'SOL', '2025-10-23', 21.2),
((SELECT id FROM Localidade WHERE nome = 'Porto'), 'VENTO', '2025-10-24', 20.3),
((SELECT id FROM Localidade WHERE nome = 'Porto'), 'ENSOLARADO', '2025-10-25', 25.7),
((SELECT id FROM Localidade WHERE nome = 'Porto'), 'NUBLADO', '2025-10-29', 20.3),
((SELECT id FROM Localidade WHERE nome = 'Porto'), 'CHUVA',   '2025-10-30', 18.7),
((SELECT id FROM Localidade WHERE nome = 'Porto'), 'NUBLADO',   '2025-10-31', 17.9),
((SELECT id FROM Localidade WHERE nome = 'Porto'), 'NUBLADO',   '2025-11-01', 16.5),
((SELECT id FROM Localidade WHERE nome = 'Porto'), 'CHUVA',   '2025-11-02', 16.0),

-- Braga
((SELECT id FROM Localidade WHERE nome = 'Braga'), 'NEVOEIRO', '2025-10-20', 15.3),
((SELECT id FROM Localidade WHERE nome = 'Braga'), 'NUBLADO', '2025-10-21', 17.6),
((SELECT id FROM Localidade WHERE nome = 'Braga'), 'SOL', '2025-10-22', 20.1),
((SELECT id FROM Localidade WHERE nome = 'Braga'), 'CHUVA', '2025-10-23', 18.4),
((SELECT id FROM Localidade WHERE nome = 'Braga'), 'ENSOLARADO', '2025-10-24', 22.0),

-- Coimbra
((SELECT id FROM Localidade WHERE nome = 'Coimbra'), 'SOL', '2025-10-20', 23.1),
((SELECT id FROM Localidade WHERE nome = 'Coimbra'), 'NUBLADO', '2025-10-21', 21.0),
((SELECT id FROM Localidade WHERE nome = 'Coimbra'), 'VENTO', '2025-10-22', 20.5),
((SELECT id FROM Localidade WHERE nome = 'Coimbra'), 'CHUVA', '2025-10-23', 19.2),
((SELECT id FROM Localidade WHERE nome = 'Coimbra'), 'ENSOLARADO', '2025-10-24', 24.4),

-- Faro
((SELECT id FROM Localidade WHERE nome = 'Faro'), 'SOL', '2025-10-20', 27.8),
((SELECT id FROM Localidade WHERE nome = 'Faro'), 'VENTO', '2025-10-21', 26.3),
((SELECT id FROM Localidade WHERE nome = 'Faro'), 'ENSOLARADO', '2025-10-22', 28.1),
((SELECT id FROM Localidade WHERE nome = 'Faro'), 'NUBLADO', '2025-10-23', 25.0),
((SELECT id FROM Localidade WHERE nome = 'Faro'), 'CHUVA', '2025-10-24', 24.3),

-- Aveiro
((SELECT id FROM Localidade WHERE nome = 'Aveiro'), 'CHUVA', '2025-10-20', 18.9),
((SELECT id FROM Localidade WHERE nome = 'Aveiro'), 'NUBLADO', '2025-10-21', 19.7),
((SELECT id FROM Localidade WHERE nome = 'Aveiro'), 'SOL', '2025-10-22', 21.2),
((SELECT id FROM Localidade WHERE nome = 'Aveiro'), 'ENSOLARADO', '2025-10-23', 20.0),
((SELECT id FROM Localidade WHERE nome = 'Aveiro'), 'ENSOLARADO', '2025-10-24', 22.6),
((SELECT id FROM Localidade WHERE nome = 'Aveiro'), 'NUBLADO', '2025-10-29', 21.0),
((SELECT id FROM Localidade WHERE nome = 'Aveiro'), 'VENTO',   '2025-10-30', 19.2),
((SELECT id FROM Localidade WHERE nome = 'Aveiro'), 'CHUVA',   '2025-10-31', 18.0),
((SELECT id FROM Localidade WHERE nome = 'Aveiro'), 'CHUVA',   '2025-11-01', 17.5),
((SELECT id FROM Localidade WHERE nome = 'Aveiro'), 'CHUVA',   '2025-11-02', 17.0),

-- Guarda
((SELECT id FROM Localidade WHERE nome = 'Guarda'), 'NEVE', '2025-10-20', 4.2),
((SELECT id FROM Localidade WHERE nome = 'Guarda'), 'NEVOEIRO', '2025-10-21', 6.8),
((SELECT id FROM Localidade WHERE nome = 'Guarda'), 'NUBLADO', '2025-10-22', 7.3),
((SELECT id FROM Localidade WHERE nome = 'Guarda'), 'SOL', '2025-10-23', 9.4),
((SELECT id FROM Localidade WHERE nome = 'Guarda'), 'VENTO', '2025-10-24', 8.1),

-- Santarém
((SELECT id FROM Localidade WHERE nome = 'Santarém'), 'ENSOLARADO', '2025-10-20', 23.9),
((SELECT id FROM Localidade WHERE nome = 'Santarém'), 'NUBLADO', '2025-10-21', 22.4),
((SELECT id FROM Localidade WHERE nome = 'Santarém'), 'CHUVA', '2025-10-22', 21.7),
((SELECT id FROM Localidade WHERE nome = 'Santarém'), 'VENTO', '2025-10-23', 20.9),
((SELECT id FROM Localidade WHERE nome = 'Santarém'), 'SOL', '2025-10-24', 24.2);