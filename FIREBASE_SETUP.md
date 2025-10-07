# Firebase 설정 가이드

이 프로젝트는 Firebase Firestore를 사용하여 3대 기기 간 실시간 동기화를 지원합니다.

## 🔧 설정 단계

### 1. Firebase 프로젝트 생성

1. [Firebase Console](https://console.firebase.google.com/)에 접속
2. "프로젝트 추가" 클릭
3. 프로젝트 이름 입력 (예: `reminder-sync`)
4. Google 애널리틱스 설정 (선택사항)
5. 프로젝트 생성 완료

### 2. Android 앱 추가

1. Firebase Console에서 프로젝트 선택
2. "Android 아이콘" 클릭하여 앱 추가
3. **패키지 이름**: `com.reminder` (필수, 정확히 입력)
4. 앱 닉네임: `Reminder App` (선택사항)
5. SHA-1 서명 인증서: 추후 추가 가능 (선택사항)
6. "앱 등록" 클릭

### 3. google-services.json 다운로드

1. Firebase Console에서 `google-services.json` 파일 다운로드
2. 파일을 프로젝트의 `app/` 디렉토리에 복사
   ```bash
   mv ~/Downloads/google-services.json app/
   ```

⚠️ **중요**: `google-services.json`은 절대 Git에 커밋하지 마세요! (이미 .gitignore에 포함됨)

### 4. Firebase Authentication 활성화

1. Firebase Console > Authentication
2. "시작하기" 클릭
3. "익명" 제공업체 활성화
   - 익명 로그인을 사용하여 별도의 회원가입 없이 기기별 동기화 지원

### 5. Firestore Database 생성

1. Firebase Console > Firestore Database
2. "데이터베이스 만들기" 클릭
3. **모드 선택**:
   - 개발/테스트: "테스트 모드에서 시작"
   - 프로덕션: "프로덕션 모드에서 시작" (보안 규칙 필요)
4. **위치 선택**: `asia-northeast3 (Seoul)` 권장
5. "사용 설정" 클릭

### 6. Firestore 보안 규칙 설정

Firebase Console > Firestore Database > 규칙 탭에서 다음 규칙 적용:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // 인증된 사용자만 자신의 리마인더에 접근 가능
    match /users/{userId}/reminders/{reminderId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
```

## 📱 앱 실행

1. 설정 완료 후 프로젝트 빌드
   ```bash
   ./gradlew build
   ```

2. 앱 실행
   ```bash
   ./gradlew installDebug
   ```

## 🔄 동기화 작동 방식

- **Offline-first**: 로컬 데이터베이스(Room)가 우선
- **자동 동기화**: 네트워크 연결 시 백그라운드에서 자동 동기화
- **충돌 해결**: 마지막 수정 시간(updatedAt) 기준으로 최신 데이터 우선
- **3대 기기**: 동일한 익명 계정으로 로그인하여 동기화

## 🧪 테스트

### 다중 기기 테스트

1. 앱을 3대 기기/에뮬레이터에 설치
2. 각 기기에서 최초 실행 시 자동으로 익명 로그인
3. 한 기기에서 리마인더 생성/수정
4. 다른 기기에서 자동으로 동기화되는지 확인

### 오프라인 테스트

1. 비행기 모드 활성화
2. 리마인더 생성/수정
3. 네트워크 재연결
4. 자동 동기화 확인

## ⚠️ 주의사항

- `google-services.json`은 민감 정보가 포함되어 있으므로 절대 공개 저장소에 업로드하지 마세요
- Firebase 무료 플랜(Spark)은 다음 제한이 있습니다:
  - Firestore: 1일 읽기 50,000회, 쓰기 20,000회
  - 저장소: 1GB
  - 네트워크: 10GB/월
- 상업적 사용 시 Blaze 플랜(종량제) 고려

## 📚 참고 자료

- [Firebase Android 시작하기](https://firebase.google.com/docs/android/setup)
- [Firestore 문서](https://firebase.google.com/docs/firestore)
- [Firebase Auth 문서](https://firebase.google.com/docs/auth)
