package domain.piece;

import domain.coordinate.MovePosition;
import domain.coordinate.Position;
import domain.coordinate.Route;

import java.util.Collections;
import java.util.List;

public class InitPawn extends PawnFeature {

    private static final int INIT_PAWN_DISTANCE = 2;

    public InitPawn(final Color color) {
        super(color, PieceType.INIT_PAWN);
    }

    @Override
    public Route findRoute(MovePosition movePosition) {
        validateMovable(movePosition);

        return new Route(getRoute(movePosition));
    }

    @Override
    protected boolean isMovable(MovePosition movePosition) {
        int direction = chooseDirection();

        int diffY = movePosition.diffY();
        int diffX = movePosition.diffX();

        return isPawnMovable(direction, diffY, diffX) || diffY == direction * INIT_PAWN_DISTANCE && diffX == 0;
    }

    private List<Position> getRoute(MovePosition movePosition) {
        if (movePosition.diffY() == chooseDirection()) {
            return Collections.emptyList();
        }

        Position source = movePosition.getSource();
        return List.of(source.move(0, chooseDirection()));
    }

}
