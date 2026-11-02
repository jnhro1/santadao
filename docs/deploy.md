# 배포 가이드 (Supabase + Render)

- **DB:** Supabase Postgres. 기본 database `postgres`의 `santadao` 스키마를 씁니다.
- **앱:** Render 웹 서비스입니다. Docker 이미지로 배포하고, 설정은 `render.yaml`과 `Dockerfile`에 있습니다.
- **스키마 관리:** 테이블은 앱이 시작될 때 Flyway가 `santadao` 스키마에 만듭니다. Supabase에서 SQL을 직접 실행할 필요가 없습니다.

> 대시보드 메뉴 이름은 서비스 업데이트에 따라 바뀔 수 있습니다.

## 1. Supabase 프로젝트 만들기

1. https://supabase.com 에서 New project를 만듭니다.
   - Region: 사용자와 가까운 곳(예: Northeast Asia (Seoul))
   - Database Password: 강한 비밀번호를 정하고 따로 보관합니다. 아래의 `DB_PASSWORD`입니다.
2. 프로젝트 상단의 **Connect** 버튼에서 **Session pooler** 연결 정보를 확인합니다.
   - Direct connection은 무료 플랜에서 IPv6만 지원해서 Render에서 접속이 안 될 수 있습니다.
   - Transaction pooler(6543)는 JDBC prepared statement와 충돌하므로 쓰지 않습니다.

   | Session pooler 항목 | Render 환경변수 |
   |------|------|
   | host (예: `aws-0-ap-northeast-2.pooler.supabase.com`) | `DB_HOST` |
   | port `5432` | `DB_PORT` |
   | user `postgres.<project-ref>` | `DB_USERNAME` |
   | 프로젝트를 만들 때 정한 비밀번호 | `DB_PASSWORD` |

3. **공개 API 노출을 확인합니다.**
   - Project Settings → Data API(API) → Exposed schemas에 `santadao`가 **없어야** 합니다. 기본값은 `public`, `graphql_public`입니다.
   - 테이블에는 RLS도 켜져 있어서 anon 키로는 조회할 수 없지만, 스키마 자체를 노출하지 않는 것이 1차 방어선입니다.

## 2. Render 배포

1. https://render.com 에서 GitHub 계정을 연결합니다.
2. New → **Blueprint**를 누르고 `jnhro1/santadao` 저장소를 선택합니다. `render.yaml`을 읽어 `santadao` 웹 서비스가 만들어집니다.
3. 환경변수 입력 화면에서 1단계의 값 4개(`DB_HOST`, `DB_PORT`, `DB_USERNAME`, `DB_PASSWORD`)를 입력합니다.
   - `SPRING_PROFILES_ACTIVE=prod`는 `render.yaml`에 이미 들어 있습니다.
4. 배포 로그에서 다음을 확인합니다.
   - `Creating schema "santadao"`, `Successfully applied 1 migration`: 첫 배포 때만 나옵니다.
   - `Started SantadaoApplication`
5. 발급된 `https://<서비스명>.onrender.com`으로 접속해 이벤트를 만들어 봅니다.

- **배포 브랜치:** `render.yaml`의 `branch: main` 기준입니다. develop에서 main으로 머지하면 자동으로 다시 배포됩니다.
- **무료 플랜:** 일정 시간 요청이 없으면 서비스가 잠듭니다. 잠든 뒤 첫 요청은 Spring이 다시 뜨는 시간(수십 초)만큼 느립니다.

## 3. 배포 후 문제 해결

| 증상 | 확인할 것 |
|------|-----------|
| `Could not resolve placeholder 'DB_HOST'` | Render 환경변수 4개가 모두 들어갔는지 |
| `Connection refused` / timeout | Session pooler 주소인지(Direct 주소 아님), 포트가 5432인지 |
| `password authentication failed` | `DB_USERNAME`이 `postgres.<project-ref>` 형식인지, 비밀번호가 맞는지 |
| `Validate failed: Migrations have failed validation` | 이미 적용된 마이그레이션 파일을 수정했는지. 적용된 파일은 수정하지 말고 새 버전 파일로 추가합니다 |
| 메모리 부족으로 재시작 | 무료 플랜은 512MB입니다. Dockerfile의 `MaxRAMPercentage`를 조정하거나 유료 플랜을 씁니다 |

## 로컬 개발 환경 (참고)

```bash
cp .env.example .env                                   # 로컬 Postgres 계정
cp application-secret.yml.example application-secret.yml   # 같은 계정 입력
docker compose up -d                                   # Postgres 17, santadao_local / santadao_test 생성
./gradlew test
./gradlew bootRun
```
