# ChatBot 프로젝트

Spring Boot를 활용한 ChatGPT 연동 챗봇 애플리케이션입니다.

## 🚀 주요 기능

- ChatGPT API 연동
- 실시간 채팅 웹 인터페이스
- REST API 제공
- 최신 Java 기능 활용

## 🛠 기술 스택

- **Java 21** - 최신 언어 기능
- **Spring Boot 3.5.5** - 웹 애플리케이션 프레임워크
- **Thymeleaf** - 서버사이드 템플릿 엔진
- **OpenAI GPT-4o** - 최신 ChatGPT 모델 연동
- **OpenAI Java Client** - ChatGPT API 연동
- **Gradle** - 빌드 도구


## 🏃‍♂️ 실행 방법

### 1. 환경 설정

```bash
# OpenAI API 키 설정
export OPENAI_API_KEY="your-api-key-here"
```

### 2. 로컬 실행 (H2 데이터베이스)

```bash
# 빌드
./gradlew build

# 실행
./gradlew bootRun
```

### 3. Docker Compose 실행 (PostgreSQL)

```bash
# 환경 변수 설정
cp env.example .env
# .env 파일에서 OPENAI_API_KEY 설정

# Docker Compose로 실행
docker-compose up -d

# 로그 확인
docker-compose logs -f

# 중지
docker-compose down
```

### 4. 접속

- 웹 인터페이스: http://localhost:2800
- REST API: http://localhost:2800/api/chat/message
- H2 콘솔 (로컬): http://localhost:2800/h2-console
- PostgreSQL (Docker): localhost:5432

## 🧪 테스트 실행

```bash
# 전체 테스트 실행
./gradlew test

# 특정 테스트 실행
./gradlew test --tests "ChatGptServiceTest"
```

## 📁 프로젝트 구조

```
src/
├── main/
│   ├── java/com/app/chatboat/
│   │   ├── config/          # 설정 클래스
│   │   ├── controller/      # REST 컨트롤러
│   │   ├── dto/            # 데이터 전송 객체
│   │   ├── service/        # 비즈니스 로직
│   │   └── ChatboatApplication.java
│   └── resources/
│       ├── application.yml # 설정 파일
│       └── templates/      # Thymeleaf 템플릿
└── test/
    └── java/com/app/chatboat/
        ├── controller/     # 컨트롤러 테스트
        ├── integration/    # 통합 테스트
        └── service/        # 서비스 테스트
```

## 🔧 API 사용법

### 채팅 메시지 전송

```bash
curl -X POST http://localhost:2800/api/chat/message \
  -H "Content-Type: application/json" \
  -d '{"role": "user", "content": "안녕하세요!"}'
```

### 헬스 체크

```bash
curl http://localhost:2800/api/chat/health
```

## 📝 주요 기능

- **Record**: 불변 데이터 클래스 (`ChatMessage`, `OpenAiProperties`)
- **Sealed Interface**: 제한된 상속 (`ValidationResult`, `RequestValidationResult`)
- **Switch Expression**: 패턴 매칭과 함께 사용
- **Text Blocks**: 멀티라인 문자열
- **var 키워드**: 타입 추론

## 🤖 AI 모델 정보

- **모델**: GPT-4o (최신 멀티모달 모델)
- **최대 토큰**: 2,000 토큰
- **온도**: 0.7 (창의성과 일관성의 균형)
- **특징**: 
  - 더 나은 추론 능력
  - 향상된 한국어 이해
  - 더 정확한 응답 생성
  - 멀티모달 지원 (텍스트, 이미지 등)

## 🤝 기여하기

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다.
