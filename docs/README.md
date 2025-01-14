## 기능 요구 사항

- 체스판
    - [X] 가로는 왼쪽부터 a ~ h로 나타낸다.
    - [X] 세로는 아래부터 위로 1 ~ 8로 나타낸다.
    - [X] 게임 시작시, 각 좌표에 체스 말(기물)을 배치한다.
    - [X] 체스판을 DB에 저장한다.
        - 게임을 종료하고 재시작할 경우, 이전 게임을 이어서 할 수 있다.
        - 저장하는 게임 방 이름은 1글자 이상, 10글자 이하여야 한다.

- 체스 말
    - [x] 각 진영은 대, 소문자로 구분한다.
        - 검은색 : 대문자
        - 흰색 : 소문자
    - [x] 각 기물의 이동 규칙에 따라 이동한다.
    - [x] 이동하고자 하는 위치에
        - 자신의 기물이 있을 경우, 이동할 수 없다.
        - 상대방의 기물이 있을 경우, 해당 기물을 잡을 수 있다.
    - [X] 폰(Pawn)이 상대방의 마지막 rank에 도착하면, 폰과 킹을 제외한 기물로 승격할 수 있다.
    - [X] 킹(King)이 잡힌 경우, 게임이 종료된다.
    - [X] 각 기물은 점수가 존재한다
        - Queen : 9점
        - Rook : 5점
        - Bishop : 3점
        - Knight : 2.5점
        - Pawn : 1점
            - 같은 세로 줄(File)에 같은 색의 폰이 존재할 경우 0.5점

- 게임 진행
    - [x] `start` 입력시, 게임을 시작한다.
    - [x] `end` 입력시, 게임을 종료한다.
    - [X] `move source위치 target위치` 입력시, source 위치에 있는 기물이 target 위치로 이동한다.
    - [X] 게임 종료 후, `status` 입력시, 각 진영의 점수와 승리 진영을 출력한다.
    - [X] 사용자(혹은 방) 이름을 입력하여, 이전 게임을 이어서 할 수 있다.
