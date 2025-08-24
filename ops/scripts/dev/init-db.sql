-- El usuario gasolinera_dev ya existe (definido en docker-compose.dev.yml)
-- Solo crear las bases de datos adicionales

CREATE DATABASE auth_service_dev;
CREATE DATABASE coupon_service_dev;
CREATE DATABASE station_service_dev;
CREATE DATABASE redemption_service_dev;
CREATE DATABASE ad_engine_dev;
CREATE DATABASE raffle_service_dev;

-- Otorgar permisos
GRANT ALL PRIVILEGES ON DATABASE auth_service_dev TO gasolinera_dev;
GRANT ALL PRIVILEGES ON DATABASE coupon_service_dev TO gasolinera_dev;
GRANT ALL PRIVILEGES ON DATABASE station_service_dev TO gasolinera_dev;
GRANT ALL PRIVILEGES ON DATABASE redemption_service_dev TO gasolinera_dev;
GRANT ALL PRIVILEGES ON DATABASE ad_engine_dev TO gasolinera_dev;
GRANT ALL PRIVILEGES ON DATABASE raffle_service_dev TO gasolinera_dev;

-- Conectar a cada base de datos y otorgar permisos de esquema
\c auth_service_dev;
GRANT ALL ON SCHEMA public TO gasolinera_dev;

\c coupon_service_dev;
GRANT ALL ON SCHEMA public TO gasolinera_dev;

\c station_service_dev;
GRANT ALL ON SCHEMA public TO gasolinera_dev;

\c redemption_service_dev;
GRANT ALL ON SCHEMA public TO gasolinera_dev;

\c ad_engine_dev;
GRANT ALL ON SCHEMA public TO gasolinera_dev;

\c raffle_service_dev;
GRANT ALL ON SCHEMA public TO gasolinera_dev;