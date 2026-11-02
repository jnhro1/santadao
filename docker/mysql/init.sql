-- 새 환경의 MySQL 최초 1회 세팅 (스키마 생성)
-- 테이블은 만들지 않습니다. 앱이 시작될 때 Flyway(src/main/resources/db/migration)가 생성합니다.
--
-- 실행 예 (root 계정):
--   mysql -uroot -p < docker/mysql/init.sql
-- docker-compose를 쓴다면 /docker-entrypoint-initdb.d/에 마운트하면 컨테이너 최초 기동 시 자동 실행됩니다.

CREATE DATABASE IF NOT EXISTS `santadao-local` CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE DATABASE IF NOT EXISTS `santadao-test` CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

-- 운영/스테이징 서버에서는 해당 환경의 스키마만 만듭니다.
-- CREATE DATABASE IF NOT EXISTS `santadao` CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

-- 앱 전용 계정을 쓸 경우 (비밀번호는 각 환경에서 정해서 application-secret.yml / 환경변수에 넣습니다)
-- CREATE USER IF NOT EXISTS 'santadao'@'%' IDENTIFIED BY '<password>';
-- GRANT ALL PRIVILEGES ON `santadao-local`.* TO 'santadao'@'%';
-- GRANT ALL PRIVILEGES ON `santadao-test`.* TO 'santadao'@'%';
