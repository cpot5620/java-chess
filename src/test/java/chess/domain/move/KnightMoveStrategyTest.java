package chess.domain.move;

import static org.assertj.core.api.Assertions.assertThat;

import chess.domain.board.Board;
import chess.domain.board.BoardInitializer;
import chess.domain.board.Position;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class KnightMoveStrategyTest {

    private Board board;
    private KnightMoveStrategy knightMoveStrategy;

    @BeforeEach
    void setUp() {
        board = new Board(BoardInitializer.initialize());
        knightMoveStrategy = new KnightMoveStrategy();
    }

    @Test
    @DisplayName("나이트가 움직일 수 있다.")
    void isMovable() {
        Position source = Position.valueOf("b8");
        Position target = Position.valueOf("c6");

        assertThat(knightMoveStrategy.isMovable(board, source, target)).isTrue();
    }

    @Test
    @DisplayName("나이트 이동 패턴이 아니다.")
    void isMovableNotKnightMovePattern() {
        Position source = Position.valueOf("b8");
        Position target = Position.valueOf("c7");

        assertThat(knightMoveStrategy.isMovable(board, source, target)).isFalse();
    }

    @Test
    @DisplayName("Target 에 우리편 기물이 있을 때 false")
    void isMovableWhenTargetTeamSame() {
        board.movePiece(Position.valueOf("b7"), Position.valueOf("c6"));

        Position source = Position.valueOf("b8");
        Position target = Position.valueOf("c6");

        assertThat(knightMoveStrategy.isMovable(board, source, target)).isFalse();
    }
}