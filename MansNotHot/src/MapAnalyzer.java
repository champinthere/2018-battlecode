import bc.*;

import java.util.*;

public class MapAnalyzer {
    private GameManager manager;
    private PlanetMap earthMap;
    private PlanetMap marsMap;
    private Unit[][] unitMap;

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

    public boolean isPassable(MapLocation m) {
        return (map().isPassableTerrainAt(m) > 0 && unitMap[m.getX()][m.getY()] == null);
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
        unitMap = new Unit[width][height];
    }

    public MapLocation getNearbyDefensiveLocation(MapLocation loc) {
        // fix stuff here
        return loc;
    }

    public void updateWithUnitPositions() {
        for (int i = 0; i < width; ++i)
            for (int j = 0; j < height; ++j)
                unitMap[i][j] = null;
        for (int i = 0; i < width; ++i) {
            for (int j = 0; j < height; ++i) {
                try {
                    unitMap[i][j] = manager.controller().senseUnitAtLocation(new MapLocation(manager.getPlanet(), i, j));
                }
                catch (Exception e) {}
            }

        }
    }

    private class Coordinate implements Comparable<Coordinate> {
        int x;
        int y;
        public Coordinate(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public Coordinate(MapLocation loc) {
            this(loc.getX(), loc.getY());
        }

        public MapLocation toMapLocation() {
            return new MapLocation(manager.getPlanet(), x, y);
        }

        @Override
        public int compareTo(Coordinate o) {
            if (x != o.x)
                return x - o.x;
            return y - o.y;
        }

        @Override
        public int hashCode() {
            return Integer.hashCode(50 * x + y);
        }

        @Override
        public boolean equals (Object o) {
            if (!(o instanceof Coordinate))
                return false;
            Coordinate c = (Coordinate) o;
            return (c.x == x && c.y == y);
        }
    }

    private class PathNode {
        Coordinate c;
        ArrayList<PathNode> adj;
        boolean passable = false;

        public PathNode(Coordinate c) {
            this.c = c;
            adj = new ArrayList<>();
        }
    }

    private Map<Coordinate, PathNode> getPathNodeGraph() {
        Map<Coordinate, PathNode> result = new HashMap<>();
        for (int i = 0; i < width; ++i) {
            for (int j = 0; j < height; ++j) {
                Coordinate c = new Coordinate(i, j);
                PathNode node = new PathNode(c);
                if (unitMap[i][j] == null && map().isPassableTerrainAt(c.toMapLocation()) > 0)
                    node.passable = true;
                result.put(c, node);
            }
        }
        for (int i = 0; i < width; ++i) {
            for (int j = 0; j < height; ++j) {
                MapLocation l1 = new MapLocation(manager.getPlanet(), i, j);
                Coordinate c = new Coordinate(l1);
                VecMapLocation adjLocations = manager.controller().allLocationsWithin(l1, 2);
                PathNode node = result.get(c);
                for (int k = 0; k < adjLocations.size(); ++k) {
                    Coordinate f = new Coordinate(adjLocations.get(k));
                    if (result.get(f).passable)
                        node.adj.add(result.get(f));
                }
            }
        }
        return result;
    }

    public StaticPath getStaticPath(MapLocation start, MapLocation finish) {
        Coordinate begin = new Coordinate(start);
        Coordinate end = new Coordinate(finish);
        Map<Coordinate, PathNode> graph = getPathNodeGraph();
        PriorityQueue<StaticPath.StaticPathEntry> pq = new PriorityQueue<>();
        StaticPath startPath = new StaticPath(start);
        HashSet<Coordinate> visited = new HashSet<Coordinate>();
        visited.add(begin);
        StaticPath.StaticPathEntry b = new StaticPath.StaticPathEntry(StaticPath.getCost(start, finish), startPath);
        pq.add(b);
        while(!pq.isEmpty()) {
            StaticPath.StaticPathEntry e = pq.remove();
            Coordinate c = new Coordinate(e.path.getLoc());
            if (c.equals(end)) {
                StaticPath p = e.path;
                StaticPath t = p.getNext();
                p.setNext(null);
                while (t != null) {
                    StaticPath r = t.getNext();
                    t.setNext(p);
                    p = t;
                    t = r;
                }
                System.out.println("StaticPath found successfully");
                return p.getNext();
            }
            if (visited.contains(c))
                continue;
            visited.add(c);
            double g = e.cost - StaticPath.getCost(e.path.getLoc(), finish);
            PathNode node = graph.get(c);
            for (PathNode nearby: node.adj) {
                if (!visited.contains(nearby.c)) {
                    StaticPath newPath = new StaticPath(nearby.c.toMapLocation(), e.path);
                    StaticPath.StaticPathEntry newEntry = new StaticPath.StaticPathEntry(g +
                            StaticPath.getCost(nearby.c.toMapLocation(), finish), newPath);
                    pq.add(newEntry);
                }
            }
        }
        System.out.println("Path there was an error");
        return null;
    }

    public void registerMove(MapLocation oldLocation, MapLocation newLocation, Robo robo) {
        unitMap[newLocation.getX()][newLocation.getY()] = manager.controller().unit(robo.getUnitId());
        if (unitMap[oldLocation.getX()][oldLocation.getY()] != null &&
                unitMap[oldLocation.getX()][oldLocation.getY()].id() == robo.getUnitId())
            unitMap[oldLocation.getX()][oldLocation.getY()] = null;
    }


}
