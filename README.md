# FinalProjectBack

## 문서 바로가기
- [PR 템플릿 보기](./.github/PULL_REQUEST_TEMPLATE.md)

## 🔤 변수명 / 함수명 네이밍 규칙

### ✅ 변수명

- **기능 기반으로 의미 있게 작성**
  - `id` ❌ → `userId` ✅
  - `name` ❌ → `userName` ✅

### ✅ 함수/메서드명

- **CRUD 접두어 + 기능명**
  - `insert`, `select`, `update`, `delete`
### git branch 명명규칙

[타입]/[기능명] or [이슈번호]-[기능명]

| 타입 | 설명 |
| --- | --- |
| `feature/` | 새로운 기능 개발 |
| `fix/` | 버그 수정 |
| `refactor/` | 리팩토링 |
| `docs/` | 문서 관련 작업 |
| `chore/` | 기타 수정 사항 |
| `bug_report/` | 버그 발생 보고|
EX) 

- `feature/[branch]` :  기능 개발을 진행하는 브랜치
- `fix/[branch]` : 버그를 수정하는 브랜치
- `refactor/[branch]` : 리팩토링을 진행하는 브랜치
- `docs/[branch]` : 문서 관련 작업 
- `bug_report/[branch]` : 버그 발생 보고 진행하는 브랜치
- `chore/[branch]` : 기타 수정 사항 진행하는 브랜치


## 🧩 Gitmoji CLI 설치 (커밋 이모지 가이드라인)

우리 팀은 커밋 메시지 작성 시 [Gitmoji](https://gitmoji.dev/)를 사용합니다.

> Gitmoji는 커밋 메시지 앞에 이모지를 붙여  
> 커밋 목적을 표현

### ✅ 설치 명령어

```bash
npm install -g gitmoji-cli
