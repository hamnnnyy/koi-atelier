# Koi Orbit

마인크래프트 **1.21.1 Fabric** 클라이언트 모드. 내 캐릭터를 꾸미는 코스메틱과 편의 HUD를 한데 모았어요.
모든 꾸미기는 **나한테만** 보여요 (서버에 설치할 필요 없음).

## 기능

### 꾸미기
- **잉어**: 열대어 두 마리가 캐릭터 주위를 헤엄쳐 돎. 무늬 12종 × 염료 16색, 크기·반경·높이·속도(음수면 반대 방향)·출렁임 조절
- **오라**: 어깨/몸 둘레/발밑에서 피어오르는 파티클 (불꽃, 영혼 불꽃, 벚꽃잎, 엔드 막대 빛, 색 가루 등 9종)
- **발자국**: 걸어간 자리에 남는 파티클
- **펫**: 어깨 옆을 따라다니는 몹 12종 (알레이, 고양이, 여우, 앵무새, 아홀로틀 …)
- **후광 · 날개**: 도트 그림을 1/16블록 입체로 쌓은 마크 스타일 모델 (천사, 나비, 드래곤 날개)
- **머리 위 문구**: 이름표처럼 원하는 글자 표시
- **망토**: 잉어·불꽃·벚꽃·은하수·바다 무늬 (엘리트라에도 적용)

### HUD · 편의
- 좌표 · 방향 · 바이옴 표시
- 미니맵 (지형 표면만, 북쪽이 위)
- 도구/갑옷 내구도 경고
- 줌 (기본 **Z** 누르고 있기)
- 풀 브라이트 (기본 **B**, 서버에 따라 금지일 수 있음)

## 설정

- **설정 화면**: 채팅에 `/koi menu` (또는 Mod Menu의 설정 버튼). 꾸미기 페이지는 3인칭으로 바로 미리보기
- **명령어**: `/koi <설정> <값>` — Tab으로 자동완성. `/koi`로 전체 설정 보기
  - 예: `/koi 1 betty red white`, `/koi wings_type dragon`, `/koi head_text 안녕`
- 단축키: K 물고기 켜기/끄기, Z 줌, B 풀 브라이트 (설정 > 조작에서 변경)
- 설정 파일: `config/koi-orbit.json`

## 설치

1. 1.21.1용 [Fabric Loader](https://fabricmc.net/use/) 설치
2. `.minecraft/mods`에 [Fabric API](https://modrinth.com/mod/fabric-api) 와 이 모드 jar 넣기
3. (선택) [Mod Menu](https://modrinth.com/mod/modmenu)

## 빌드

Java 21 필요.

```
./gradlew build          # build/libs/koi-orbit-<버전>.jar
./gradlew runClient      # 개발용 클라이언트 실행
./gradlew runClient -Photswap   # 디버거 포트 5005를 열고 실행, 코드 수정 후 ./hotswap.sh 로 바로 반영
```

망토 텍스처는 `python tools/gen_textures.py`로 다시 만들 수 있어요 (Pillow 필요).
후광/날개 도트 그림은 `src/client/java/com/example/koiorbit/KoiSprites.java`에 글자로 들어 있어요.

## 라이선스

MIT
