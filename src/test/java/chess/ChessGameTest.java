package chess;

import chess.domain.ChessGame;
import chess.domain.RoomName;
import chess.domain.board.Chessboard;
import chess.domain.board.File;
import chess.domain.board.Rank;
import chess.domain.board.Square;
import chess.domain.piece.Camp;
import chess.domain.piece.Piece;
import chess.domain.piece.PieceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChessGameTest {
    private ChessGame chessGame;
    private Chessboard chessboard;

    @BeforeEach
    void setup() {
        chessGame = new ChessGame(new RoomName("test"));
        chessboard = chessGame.getChessboard();
    }

    @ParameterizedTest(name = "잘못된 위치 입력시 예외가 발생한다")
    @MethodSource("invalidSquareProvider")
    void moveToInvalidSquare(Square source, Square target) {
        assertThatThrownBy(() -> chessGame.move(source, target))
                .isInstanceOf(IllegalArgumentException.class);
    }

    static Stream<Arguments> invalidSquareProvider() {
        return Stream.of(
                Arguments.arguments(Square.getInstanceOf(File.A, Rank.ONE), Square.getInstanceOf(File.A, Rank.THREE)),
                Arguments.arguments(Square.getInstanceOf(File.A, Rank.TWO), Square.getInstanceOf(File.A, Rank.FIVE)),
                Arguments.arguments(Square.getInstanceOf(File.A, Rank.THREE), Square.getInstanceOf(File.A, Rank.FOUR)),
                Arguments.arguments(Square.getInstanceOf(File.A, Rank.TWO), Square.getInstanceOf(File.B, Rank.FOUR)),
                Arguments.arguments(Square.getInstanceOf(File.A, Rank.TWO), Square.getInstanceOf(File.B, Rank.THREE)),
                Arguments.arguments(Square.getInstanceOf(File.A, Rank.TWO), Square.getInstanceOf(File.B, Rank.TWO))
        );
    }

    @ParameterizedTest(name = "이동 가능한 위치 입력시 기물이 이동한다")
    @MethodSource("validSquareProvider")
    void moveToValidSquare(Square source, Square target) {
        Piece expectedPiece = chessboard.getPieceAt(source);
        chessGame.move(source, target);

        assertThat(chessboard.getPieceAt(target))
                .isEqualTo(expectedPiece);
    }

    static Stream<Arguments> validSquareProvider() {
        return Stream.of(
                Arguments.arguments(Square.getInstanceOf(File.A, Rank.TWO), Square.getInstanceOf(File.A, Rank.THREE)),
                Arguments.arguments(Square.getInstanceOf(File.A, Rank.TWO), Square.getInstanceOf(File.A, Rank.FOUR))
        );
    }

    @DisplayName("대각선에 상대 폰이 있을 경우, 이동할 수 있다.")
    @Test
    void movePawnIfPresentEnemyAtDiagonal() {
        Square source = Square.getInstanceOf(File.A, Rank.TWO);
        Square target = Square.getInstanceOf(File.B, Rank.THREE);
        Piece expectedPiece = chessboard.getPieceAt(source);

        chessboard.swapPiece(Square.getInstanceOf(File.B, Rank.SEVEN), Square.getInstanceOf(File.B, Rank.THREE));
        chessGame.move(source, target);

        assertThat(chessboard.getPieceAt(target))
                .isEqualTo(expectedPiece);
    }

    @DisplayName("Pawn의 시작 위치가 아닌 경우, 2칸을 이동할 수 없다.")
    @Test
    void moveTwoRankPawnIfNotStartSquareFailTest() {
        Square source = Square.getInstanceOf(File.A, Rank.TWO);
        Square movedSquare = Square.getInstanceOf(File.A, Rank.THREE);
        Square target = Square.getInstanceOf(File.A, Rank.FIVE);

        chessboard.swapPiece(source, movedSquare);

        assertThatThrownBy(() -> chessGame.move(movedSquare, target))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("Pawn 앞에 장애물(아군, 적 상관 x)이 있을 경우, 이동할 수 없다.")
    @Test
    void movePawnIfPresentPieceAtTargetSquareFailTest() {
        Square source = Square.getInstanceOf(File.A, Rank.TWO);
        Square movedSquare = Square.getInstanceOf(File.A, Rank.SIX);
        Square target = Square.getInstanceOf(File.A, Rank.SEVEN);

        chessboard.swapPiece(source, movedSquare);

        assertThatThrownBy(() -> chessGame.move(movedSquare, target))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("Pawn이 상대편 끝 Rank에 도달하지 않은 경우, Promotion을 할 수 없다.")
    @Test
    void canPromotePawnFailTest() {
        Square source = Square.getInstanceOf(File.A, Rank.TWO);
        Square target = Square.getInstanceOf(File.A, Rank.SEVEN);

        chessboard.swapPiece(source, target);

        assertThat(chessGame.canPromotion(target))
                .isFalse();
    }

    @DisplayName("Pawn이 상대편 끝 Rank에 도달한 경우, Promotion을 할 수 있다.")
    @Test
    void canPromotePawnSuccessTest() {
        Square source = Square.getInstanceOf(File.A, Rank.TWO);
        Square target = Square.getInstanceOf(File.A, Rank.EIGHT);

        chessboard.swapPiece(source, target);

        assertThat(chessGame.canPromotion(target))
                .isTrue();
    }

    @DisplayName("Pawn이 상대편 끝 Rank에 도달한 경우, 원하는 기물로 Promotion을 할 수 있다.")
    @Test
    void promotePawnSuccessTest() {
        Square source = Square.getInstanceOf(File.A, Rank.TWO);
        Square target = Square.getInstanceOf(File.A, Rank.EIGHT);

        chessboard.swapPiece(source, target);
        chessGame.promotePawn(target, PieceType.BISHOP);

        assertThat(chessboard.getPieceAt(target).getPieceType())
                .isEqualTo(PieceType.BISHOP);
    }

    @DisplayName("King이 죽었는지 확인할 수 있다.")
    @Test
    void isKingAliveSuccessTest() {
        Square source = Square.getInstanceOf(File.A, Rank.TWO);
        Square BlackKingSquare = Square.getInstanceOf(File.E, Rank.EIGHT);

        assertThat(chessGame.isBothKingAlive())
                .isTrue();

        chessboard.swapPiece(source, BlackKingSquare);

        assertThat(chessGame.isBothKingAlive())
                .isFalse();
    }


    @ParameterizedTest(name = "특정 진영의 점수를 계산할 수 있다.")
    @MethodSource("sourceAndTargetSquareAndScoreProvider")
    void calculateScoreSuccessTest(Square source, Square target, double score) {
        chessboard.swapPiece(source, target);

        assertThat(chessGame.calculateScoreOf(Camp.WHITE))
                .isEqualTo(score);
    }

    static Stream<Arguments> sourceAndTargetSquareAndScoreProvider() {
        return Stream.of(
                Arguments.arguments(    // 모든 기물이 살아있는 경우
                        Square.getInstanceOf(File.C, Rank.THREE), Square.getInstanceOf(File.C, Rank.THREE), 38.0
                ),
                Arguments.arguments(    // Pawn이 동일한 File에 존재
                        Square.getInstanceOf(File.B, Rank.TWO), Square.getInstanceOf(File.C, Rank.THREE), 37.0
                ),
                Arguments.arguments(    // 퀸이 죽은 경우
                        Square.getInstanceOf(File.C, Rank.THREE), Square.getInstanceOf(File.D, Rank.ONE), 29.0
                )
        );
    }
}
