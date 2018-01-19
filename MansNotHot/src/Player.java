import bc.GameController;
import bc.Unit;
import bc.VecUnit;

import java.util.HashMap;

public class Player {
    public static void main(String[] args) {
        GameController gc = new GameController();
        GameManager gm = new GameManager(gc);
        while (true) {
            gm.updateState();

        }
    }
}