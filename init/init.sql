-- Создание пользователя, если не существует
DO
$$
    BEGIN
        IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'scrapper_user') THEN
            CREATE ROLE scrapper_user LOGIN PASSWORD 'scrapper_password';
        END IF;
    END
$$;

-- Создание схемы и назначение владельца
CREATE SCHEMA IF NOT EXISTS scrapper_schema AUTHORIZATION scrapper_user;

-- Выдать права на подключение к БД
GRANT CONNECT, TEMP ON DATABASE scrapper TO scrapper_user;

-- Выдать доступ ко всей схеме
GRANT USAGE, CREATE ON SCHEMA scrapper_schema TO scrapper_user;

-- Права на все уже созданные объекты в схеме
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA scrapper_schema TO scrapper_user;
GRANT USAGE, SELECT, UPDATE ON ALL SEQUENCES IN SCHEMA scrapper_schema TO scrapper_user;

-- Права на все будущие объекты
ALTER DEFAULT PRIVILEGES IN SCHEMA scrapper_schema
    GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO scrapper_user;

ALTER DEFAULT PRIVILEGES IN SCHEMA scrapper_schema
    GRANT USAGE, SELECT, UPDATE ON SEQUENCES TO scrapper_user;
