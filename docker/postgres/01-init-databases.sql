-- Создание отдельной базы данных для AI-агента
CREATE DATABASE klawa_ai_db;

-- Включаем UUID в базе ядра
\c klawa_core_db
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Включаем pgvector и UUID в базе AI-агента
\c klawa_ai_db
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "vector";
