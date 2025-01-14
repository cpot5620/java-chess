package chess.controller;

import chess.dao.JdbcDao;
import chess.domain.ChessGame;
import chess.domain.RoomName;
import chess.domain.board.Chessboard;
import chess.domain.board.Square;
import chess.domain.piece.Piece;
import chess.domain.piece.PieceType;
import chess.dto.BoardDto;
import chess.dto.GameRoomDto;
import chess.util.PieceRenderer;
import chess.util.SquareRenderer;
import chess.view.Command;
import chess.view.InputView;
import chess.view.OutputView;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class ChessController {
    private final InputView inputView;
    private final OutputView outputView;
    private final JdbcDao jdbcDao;

    public ChessController() {
        this.inputView = new InputView();
        this.outputView = new OutputView();
        this.jdbcDao = new JdbcDao();
    }

    public void run() {
        RoomName roomName = new RoomName(retryOnInvalidUserInput(inputView::requestRoomName));
        ChessGame chessGame = new ChessGame(roomName);

        outputView.printStartMessage();
        initializeGameState(chessGame);
        if (retryOnInvalidUserInput(this::isStartCommand)) {
            play(chessGame);
        }
        outputView.printChessBoard(chessGame.getChessboard());
        outputView.printScoreMessage(chessGame);

        updateGameState(chessGame);
    }

    private void initializeGameState(ChessGame chessGame) {
        setCorrectTurn(chessGame);
        setRecordedBoard(chessGame);
    }

    private void setRecordedBoard(ChessGame chessGame) {
        List<BoardDto> recordedBoard = jdbcDao.findBoardByRoomName(chessGame.getRoomName());

        Chessboard chessboard = chessGame.getChessboard();
        for (BoardDto boardDto : recordedBoard) {
            Square source = SquareRenderer.render(boardDto.getSource());
            Piece piece = PieceRenderer.render(boardDto.getPiece());

            chessboard.putPiece(source, piece);
        }
    }

    private void setCorrectTurn(ChessGame chessGame) {
        jdbcDao.findGameRoomByName(chessGame.getRoomName())
                .ifPresent(gameRoom -> {
                    if (!gameRoom.isWhiteTurn()) {
                        chessGame.passTurn();
                    }
                });
    }

    private boolean isStartCommand() {
        Command command = getMainCommand(requestCommand());

        if (command.isStartCommand()) {
            return true;
        }

        throw new IllegalArgumentException("아직 게임이 시작되지 않았습니다.");
    }

    private Command getMainCommand(List<String> command) {
        String mainCommand = command.get(Index.MAIN_COMMAND.value);

        return Command.renderToCommand(mainCommand);
    }

    private List<String> requestCommand() {
        return retryOnInvalidUserInput(inputView::requestCommand);
    }

    private void play(ChessGame chessGame) {
        Optional<List<String>> commands;
        do {
            outputView.printChessBoard(chessGame.getChessboard());
            commands = retryOnInvalidUserInput(this::handleCommand);

            commands.ifPresent(command -> actionForCommand(chessGame, command));
        } while (commands.isPresent() && chessGame.isBothKingAlive());
    }

    private Optional<List<String>> handleCommand() {
        List<String> commands = requestCommand();
        Command command = getMainCommand(commands);

        if (command.isStartCommand()) {
            throw new IllegalArgumentException("이미 게임이 실행중입니다.");
        }

        if (command.isEndCommand()) {
            return Optional.empty();
        }

        return Optional.of(commands);
    }

    private void actionForCommand(ChessGame chessGame, List<String> command) {
        Command mainCommand = getMainCommand(command);

        if (mainCommand.isStatusCommand()) {
            outputView.printScoreMessage(chessGame);
            return;
        }

        movePiece(chessGame, command);
        checkPromotion(chessGame, command);
    }

    private void movePiece(ChessGame chessGame, List<String> command) {
        String sourceCommand = command.get(Index.SOURCE_SQUARE.value);
        String targetCommand = command.get(Index.TARGET_SQUARE.value);
        Square source = SquareRenderer.render(sourceCommand);
        Square target = SquareRenderer.render(targetCommand);

        retryOnInvalidAction(() -> chessGame.move(source, target));
    }

    private void checkPromotion(ChessGame chessGame, List<String> command) {
        Square movedSquare = SquareRenderer.render(command.get(Index.TARGET_SQUARE.value));

        if (chessGame.canPromotion(movedSquare)) {
            PieceType pieceType = requestPieceType();
            chessGame.promotePawn(movedSquare, pieceType);
        }
    }

    private PieceType requestPieceType() {
        return retryOnInvalidUserInput(inputView::requestPiece);
    }

    private void updateGameState(ChessGame chessGame) {
        if (!chessGame.isBothKingAlive()) {
            jdbcDao.deleteAllByName(chessGame.getRoomName());
            return;
        }

        if (jdbcDao.findGameRoomByName(chessGame.getRoomName()).isEmpty()) {
            GameRoomDto gameRoomDto = new GameRoomDto(chessGame.getRoomName(), chessGame.isWhiteTurn());
            jdbcDao.save(createBoardDto(chessGame), gameRoomDto);
            return;
        }

        GameRoomDto gameRoomDto = new GameRoomDto(chessGame.getRoomName(), chessGame.isWhiteTurn());
        jdbcDao.update(createBoardDto(chessGame), gameRoomDto);
    }

    private List<BoardDto> createBoardDto(ChessGame chessGame) {
        Chessboard board = chessGame.getChessboard();

        List<BoardDto> boardDtoList = new ArrayList<>();
        for (Square square : board.getBoardMap().keySet()) {
            String source = SquareRenderer.render(square);
            String piece = PieceRenderer.render(board.getPieceAt(square));

            boardDtoList.add(new BoardDto(source, piece));
        }

        return boardDtoList;
    }


    private <T> T retryOnInvalidUserInput(Supplier<T> request) {
        try {
            return request.get();
        } catch (IllegalArgumentException e) {
            outputView.printError(e.getMessage());
            return retryOnInvalidUserInput(request);
        }
    }

    private void retryOnInvalidAction(ActionFunction request) {
        try {
            request.run();
        } catch (IllegalArgumentException e) {
            outputView.printError(e.getMessage());
        }
    }

    private enum Index {
        MAIN_COMMAND(0),
        SOURCE_SQUARE(1),
        TARGET_SQUARE(2),
        FILE(0),
        RANK(1);

        private final int value;

        Index(int value) {
            this.value = value;
        }
    }
}
