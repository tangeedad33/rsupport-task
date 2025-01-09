# 데이터베이스 스키마와 애플리케이션 기능

## 데이터베이스 스키마

### 1. `users` 테이블
```sql
CREATE TABLE `users` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`username` VARCHAR(50) NOT NULL COLLATE 'utf8mb4_uca1400_ai_ci',
	`password` VARCHAR(255) NOT NULL COLLATE 'utf8mb4_uca1400_ai_ci',
	`enabled` TINYINT(1) NULL DEFAULT '1',
	PRIMARY KEY (`id`) USING BTREE,
	UNIQUE INDEX `username` (`username`) USING BTREE
)
```

### 2. `roles` 테이블
```sql
CREATE TABLE `roles` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`name` VARCHAR(50) NOT NULL COLLATE 'utf8mb4_uca1400_ai_ci',
	PRIMARY KEY (`id`) USING BTREE,
	UNIQUE INDEX `name` (`name`) USING BTREE
)
```

### 3. `user_roles` 테이블
```sql
CREATE TABLE `user_roles` (
	`user_id` BIGINT(20) NOT NULL,
	`role_id` BIGINT(20) NOT NULL,
	PRIMARY KEY (`user_id`, `role_id`) USING BTREE,
	INDEX `role_id` (`role_id`) USING BTREE,
	CONSTRAINT `user_roles_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON UPDATE RESTRICT ON DELETE CASCADE,
	CONSTRAINT `user_roles_ibfk_2` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON UPDATE RESTRICT ON DELETE CASCADE
)
```

### 4. `articles` 테이블
```sql
CREATE TABLE `articles` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`user_id` BIGINT(20) NOT NULL,
	`title` VARCHAR(255) NOT NULL COLLATE 'utf8mb4_uca1400_ai_ci',
	`content` VARCHAR(255) NOT NULL COLLATE 'utf8mb4_uca1400_ai_ci',
	`start_date` DATETIME NULL DEFAULT NULL,
	`end_date` DATETIME NULL DEFAULT NULL,
	`reg_date` DATETIME NOT NULL DEFAULT current_timestamp(),
	`last_update_date` DATETIME NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
	`read_count` BIGINT(20) NOT NULL DEFAULT '0',
	PRIMARY KEY (`id`) USING BTREE,
	INDEX `FK_articles_users` (`user_id`) USING BTREE,
	INDEX `start_date_end_date` (`start_date`, `end_date`) USING BTREE,
	INDEX `id_title_content_start_date_end_date` (`start_date`, `end_date`, `title`, `content`, `id`) USING BTREE,
	INDEX `id_title_content` (`title`, `content`, `id`) USING BTREE,
	CONSTRAINT `FK_articles_users` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON UPDATE NO ACTION ON DELETE NO ACTION
)
```

### 5. `article_files` 테이블
```sql
CREATE TABLE `article_files` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`article_id` BIGINT(20) NOT NULL,
	`file_name` VARCHAR(255) NOT NULL COLLATE 'utf8mb4_uca1400_ai_ci',
	`file_path` VARCHAR(512) NOT NULL COLLATE 'utf8mb4_uca1400_ai_ci',
	`file_size` BIGINT(20) NOT NULL,
	`file_type` VARCHAR(255) NOT NULL COLLATE 'utf8mb4_uca1400_ai_ci',
	`upload_date` DATETIME NOT NULL DEFAULT current_timestamp(),
	`uploaded_by` VARCHAR(255) NULL DEFAULT NULL COLLATE 'utf8mb4_uca1400_ai_ci',
	PRIMARY KEY (`id`) USING BTREE,
	INDEX `fk_article_files_article_id` (`article_id`) USING BTREE,
	CONSTRAINT `fk_article_files_article_id` FOREIGN KEY (`article_id`) REFERENCES `articles` (`id`) ON UPDATE RESTRICT ON DELETE CASCADE
)

```

## 주요 기능 및 API

### 1. 사용자 관리
- **회원 가입:**
    - **엔드포인트:** `POST /register`
    - **설명:** 새 사용자를 기본 권한 `ROLE_USER`와 함께 등록합니다.
    - **검증:** 중복 사용자명을 확인하고 비밀번호를 암호화하여 저장합니다.

- **로그인:**
    - **엔드포인트:** `POST /login`
    - **설명:** 사용자를 인증하고 JWT 토큰을 발급합니다.
    - **검증:** 사용자명과 비밀번호 조합을 확인합니다.

- **사용자 비활성화:**
    - **엔드포인트:** `DELETE /api/users/{id}`
    - **설명:** 사용자를 물리적으로 삭제하지 않고 `enabled` 필드를 `false`로 설정합니다.

### 2. 게시글 관리
- **게시글 작성:**
    - **엔드포인트:** `POST /api/articles`
    - **설명:** 새로운 게시글을 생성하고 인증된 사용자와 연결합니다.
    - **기능:** 여러 파일 업로드를 지원합니다.

- **게시글 수정:**
    - **엔드포인트:** `PUT /api/articles/{id}`
    - **설명:** 기존 게시글의 세부 정보를 업데이트하고 파일을 추가/수정합니다.

- **게시글 삭제:**
    - **엔드포인트:** `DELETE /api/articles/{id}`
    - **설명:** 게시글 ID로 게시글을 삭제합니다. 연관된 파일도 함께 삭제됩니다.

- **게시글 조회:**
    - **엔드포인트:** `GET /api/articles`
    - **설명:** 페이지네이션된 게시글 목록을 검색합니다. 제목 또는 내용을 기준으로 검색을 지원합니다.

- **게시글 상세 조회:**
    - **엔드포인트:** `GET /api/articles/{id}`
    - **설명:** 특정 ID의 게시글 세부 정보를 조회합니다.

### 3. 파일 관리
- **파일 업로드:**
    - 게시글 작성 및 수정 API 내에서 처리됩니다.
    - 파일의 세부 정보(이름, 크기, 타입, 경로)는 `article_files` 테이블에 저장됩니다.

### 4. 권한 관리
- **사용자 권한 할당:**
    - 사용자 등록 시 또는 관리 기능을 통해 처리됩니다.
    - 기본 권한: `ROLE_USER`.

### 인증 및 권한
- **JWT 인증:**
    - 로그인 성공 시 JWT 토큰 발급.
    - 보호된 엔드포인트는 `Authorization` 헤더에 유효한 JWT 토큰을 요구합니다.

### 에러 처리
- 상세 에러 메시지 및 상태 코드:
    - `400 Bad Request`: 잘못된 입력 또는 검증 실패.
    - `401 Unauthorized`: 인증 정보 누락 또는 잘못된 인증.
    - `403 Forbidden`: 접근 권한 위반.
    - `404 Not Found`: 존재하지 않는 리소스.
    - `500 Internal Server Error`: 예기치 않은 서버 오류.

## 실행 방법
1. **데이터베이스 설정:**
    - 위 제공된 SQL 스크립트를 사용하여 데이터베이스 스키마를 생성합니다.

2. **애플리케이션 실행:**
    - Spring Boot 애플리케이션을 실행합니다.

3. **API 테스트:**
    - Postman 또는 기타 REST 클라이언트를 사용하여 엔드포인트를 테스트합니다.
    - 보호된 엔드포인트의 경우 `Authorization` 헤더에 유효한 JWT 토큰을 포함해야 합니다.

## Postman 요청 예시
### 회원 가입
```
POST /register
{
  "username": "testuser",
  "password": "testpass"
}
```

### 로그인
```
POST /login
{
  "username": "testuser",
  "password": "testpass"
}
```

### 게시글 작성
```
POST /api/articles
Headers: Authorization: Bearer <token>
Form-Data:
- article: {"title": "Sample Title", "content": "Sample Content", "startDate": "2025-01-01", "endDate": "2025-01-10"}
- files: [file1, file2]
```

