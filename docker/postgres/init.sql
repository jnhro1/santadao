-- 로컬 Postgres 최초 1회 세팅 (docker-compose가 컨테이너 최초 기동 시 자동 실행)
-- database만 만듭니다. santadao 스키마와 테이블은 앱이 시작될 때 Flyway가 생성합니다.
-- 운영(Supabase)은 기본 database(postgres)를 사용하므로 이 파일을 실행하지 않습니다.

CREATE DATABASE santadao_local;
CREATE DATABASE santadao_test;
