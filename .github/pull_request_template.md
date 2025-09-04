- 제목 : feat(#17): 회원 탈퇴 API 구현  
  ex) feat(#17): pull request template 작성  
  (확인 후 지워주세요)

## 🔘 Part

- [x] FE
- [ ] BE
- [ ] Docs
- [ ] Chore

<br/>

## 🔎 작업 내용

- 회원 탈퇴 API (`DELETE /auth/me`) 구현
- 탈퇴 시 is_active = false, deleted_at = now 처리
- 탈퇴 확인 alert 추가

<br/>

## ➕ 이슈 링크

- Closes #17

<br/>

## 📸 이미지 첨부

<img src="파일주소" width="50%" height="50%"/>

<br/>

## 📬 전달 사항

- 탈퇴 처리 이후 access token은 바로 만료되지 않음 → 추후 개선 필요

<br/>

## 🔧 앞으로의 과제

- 비밀번호 확인 후 탈퇴 처리 기능 추가 예정