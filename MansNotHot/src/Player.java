import bc.GameController;

public class Player {
    public static void main(String[] args) {
        GameController gc = new GameController();
        GameManager manager = new GameManager(gc);
        while (true) {
            manager.gameStep();
        }
//        MapLocation loc1 = new MapLocation(Planet.Earth, 0, 0);
//        MapLocation loc2 = new MapLocation(Planet.Earth, 0, 4);
//        StaticPath path = manager.getMapAnalyzer().getStaticPath(loc1, loc2);
    }
}
