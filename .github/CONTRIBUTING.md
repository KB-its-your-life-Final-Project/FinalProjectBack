# FinalProjectBack

## 문서 바로가기

- [PR 템플릿](.github/PULL_REQUEST_TEMPLATE.md)

## Git branch 명명규칙

[타입]/[기능명]/[날짜]  
ex)  
feature/addMap/0715  
docs/simpleRules/0814

| 타입          | 설명             |
| ------------- | ---------------- |
| `feature/`    | 새로운 기능 개발 |
| `fix/`        | 버그 수정        |
| `refactor/`   | 리팩토링         |
| `docs/`       | 문서 관련 작업   |
| `chore/`      | 기타 수정 사항   |
| `bug_report/` | 버그 발생 보고   |

## 🧩 Gitmoji CLI 설치 (커밋 이모지 가이드라인)

우리 팀은 커밋 메시지 작성 시 [Gitmoji](https://gitmoji.dev/)를 사용합니다.

> Gitmoji는 커밋 메시지 앞에 이모지를 붙여 커밋 목적을 표현

### ✅ 설치 명령어

```bash
npm install -g gitmoji-cli
```

<br>

## 코딩 규칙

### CSS

- **Css를 지정할땐 class 사용 & 케밥 스타일**

```html
<!-- HTML -->
<div class="cate-box"></div>
```

- **자식들을 지정할땐 언더바 2개 이용**

```html
<!-- html -->
<div class="cate-box">
  <div class="cate-box__item">아이템</div>
</div>
```

- **px 대신 rem 사용. rem이 반응형에 더 좋기 때문** 다만 img 등 최소 고정값이 존재하고, felx-wrap 등이 적용되어야한다면 px 사용

```css
.cate-box {
  font-size: 1.8rem;
}
```

### ID

- **css는 class 사용 추천 & input 은 v-model 사용 추천** label 등에서 id가 필연적으로 쓰이는 곳이 있음. 이런 경우 사용함<br>

```html
<!-- HTML -->
<label for="cp-product-option">선택하기</label>
<input type="checkbox" id="cp-product-option" v-model="product" />
```

### DB

- **DB의 테이블 및 컬럼 이름은 스네이크 형식**

```sql
  -- SQL
  ALTER TABLE `cp_member` ADD COLUMN `user_email` VARCHAR(100);
```

- **DB문법은 무조건 대문자**

```xml
  <select id="findById">
    SELECT * FROM `member` WHERE `id` = #{id}
  </select>
```

### 변수선언

변수 선언시 변수 이름만 보고 타입을 알 수 있게 하면 좋음<br>

- 배열은 뒤에 Arr 혹은 List를 붙이면 좋음

```javascript
//Javascript
const likeArr = [1, 2, 3, 4, 5];
```

```java
  //Java
  List<String> likeList = new ArrayList<>();
  likeList.add("황병권");
  likeList.add("홍길동");
```

- 변수는 최소 두글자로 작성하면 좋음

```javascript
  //Javscript
  const userId;
  const userName
```

```java
  //Java
  public int userId;
  public String userName;
```

- 자바에서 함수 작성시 동사 + 대상

```java
  //Java
  public void selectCurrentId();
  public void findAllCategory();
```

- boolean 은 is 나 has를 붙이면 좋음<br>

```javascript
//Javascript
const isActive = true;
```

```java
  //Java
  public int hasHome = true;
```
