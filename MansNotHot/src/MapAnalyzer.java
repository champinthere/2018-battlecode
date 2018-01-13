import bc.AsteroidPattern;
import bc.MapLocation;
import bc.Planet;
import bc.PlanetMap;

public class MapAnalyzer {
    private GameManager manager;
    private PlanetMap earthMap;
    private PlanetMap marsMap;

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    private int width;
    private int height;
    private AsteroidPattern pattern;

    public PlanetMap map() {
        if (manager.getPlanet() == Planet.Earth)
            return earthMap;
        return marsMap;
    }

    public PlanetMap getEarthMap() {
        return earthMap;
    }

    public PlanetMap getMarsMap() {
        return marsMap;
    }

    public MapAnalyzer(GameManager gm) {
        this.manager = gm;
        earthMap = gm.controller().startingMap(Planet.Earth);
        marsMap = gm.controller().startingMap(Planet.Mars);
        width = (int) earthMap.getWidth();
        height = (int) earthMap.getHeight();
    }

    public MapLocation getNearbyDefensiveLocation(MapLocation loc) {
        // fix stuff here
        return loc;
    }


}
