package Players;

import Game.CaptureGoBoard;
import Game.Cell;

public interface GoAI {
    Cell chooseMove(CaptureGoBoard board);

    String getName();
}
