import bc.*;

import java.util.*;
import java.util.stream.Collectors;

public class Player {
    int DESIRED_WORKERS = 5;
    int DESIRED_FACTORIES = 4;
    int DESIRED_RANGERS = 200;
    int STAGGER = 8; //
    int WINDOW_SIZE = 32;
    int lastRocketRound;
    int lastFactoryRound;
    boolean wantRocket;
    boolean wantFactory;


    class Purpose {
        int roundCreated;
        String desr;

        public Purpose(int round, String description) {
            roundCreated = round;
            desr = description;
        }
    }
    class Metric {
        HashMap<Integer, Integer> obstacleDistance;
        public Metric() {
            obstacleDistance = new HashMap<>();
            PriorityQueue<Integer> pq = new PriorityQueue<>(width * height,
                    (a, b) -> obstacleDistance.get(a) - obstacleDistance.get(b));
            for (int i = 0; i < width; ++i) {
                for (int j = 0; j < height; ++j) {
                    MapLocation mloc = new MapLocation(planet, i, j);
                    if (map.isPassableTerrainAt(mloc) == 0) {
                        int tmpkey = mtoi(mloc);
                        obstacleDistance.put(tmpkey, 0);
                        pq.add(tmpkey);
                    }
                    else if (i == 0 || i == width - 1 || j == 0 || j == height - 1) {
                        int tmpkey = mtoi(mloc);
                        obstacleDistance.put(tmpkey, 1);
                        pq.add(tmpkey);
                    }
                }
            }
            while (!pq.isEmpty()) {
                int mkey = pq.remove();
                int x = mkey / height;
                int y = mkey % height;
                int xp = x + 1, yp = y;
                if (xp < width) {
                    MapLocation mploc = new MapLocation(planet, xp, yp);
                    int mpkey = mtoi(mploc);
                    if (!obstacleDistance.containsKey(mpkey)) {
                        obstacleDistance.put(mpkey, obstacleDistance.get(mkey) + 1);
                        pq.add(mpkey);
                    }
                }
                xp = x - 1; yp = y;
                if (xp >= 0) {
                    MapLocation mploc = new MapLocation(planet, xp, yp);
                    int mpkey = mtoi(mploc);
                    if (!obstacleDistance.containsKey(mpkey)) {
                        obstacleDistance.put(mpkey, obstacleDistance.get(mkey) + 1);
                        pq.add(mpkey);
                    }
                }
                xp = x; yp = y - 1;
                if (yp >= 0) {
                    MapLocation mploc = new MapLocation(planet, xp, yp);
                    int mpkey = mtoi(mploc);
                    if (!obstacleDistance.containsKey(mpkey)) {
                        obstacleDistance.put(mpkey, obstacleDistance.get(mkey) + 1);
                        pq.add(mpkey);
                    }
                }
                xp = x; yp = y + 1;
                if (yp < height) {
                    MapLocation mploc = new MapLocation(planet, xp, yp);
                    int mpkey = mtoi(mploc);
                    if (!obstacleDistance.containsKey(mpkey)) {
                        obstacleDistance.put(mpkey, obstacleDistance.get(mkey) + 1);
                        pq.add(mpkey);
                    }
                }
            }
        }
        public String toString() {
            StringBuffer result = new StringBuffer();
            for (int y = height - 1; y >= 0; --y) {
                for (int x = 0; x < width; ++x) {
                    int mkey = x * height + y;
                    int od = obstacleDistance.get(mkey);
                    result.append(od);
                    result.append(" ");
                }
                result.append("\n");
            }
            return result.toString();
        }
    }
    class Ledger {
        int numWorkers;
        int numKnights;
        int numRangers;
        int numMages;
        int numHealers;
        int numFactories;
        int numFactoryBlueprints;
        int numRockets;
        int numRocketBlueprints;

        public void updateWithUnit(Unit unit) {
            UnitType type = unit.unitType();
            switch (type) {
                case Worker:
                    ++numWorkers;
                    break;
                case Knight:
                    ++numKnights;
                    break;
                case Ranger:
                    ++numRangers;
                    break;
                case Mage:
                    ++numMages;
                    break;
                case Healer:
                    ++numHealers;
                    break;
                case Factory:
                    ++numFactories;
                    break;
                case Rocket:
                    ++numRockets;
                    break;
            }
        }

        public void clear() {
            numWorkers = 0;
            numKnights = 0;
            numRangers = 0;
            numMages = 0;
            numHealers = 0;
            numFactories = 0;
            numFactoryBlueprints = 0;
            numRockets = 0;
            numRocketBlueprints = 0;
        }
    }
    class RangerNearestNeighbor {
        // Spatial Hashing
        // Needs some mechanism of marking a unit as dead
        // How to do...
        int CELL_SIZE = 5;
        HashSet<Integer>[][] spatialHash;
        int[][] spatialCache;
        int SEARCH_DEPTH = 2;
        int ww; // spatial hash width
        int hh; // spatial hash height

        public RangerNearestNeighbor() {
            ww = (width / CELL_SIZE) + 1;
            hh = (height / CELL_SIZE) + 1;
            spatialHash = new HashSet[ww][hh];
            spatialCache = new int[ww][hh];
            for (int i = 0; i < ww; ++i) {
                for (int j = 0; j < hh; ++j) {
                    spatialHash[i][j] = new HashSet<>();
                }
            }
        }

        // adds opposing units on maps to spatial hash
        public void add(Unit u) {
            if (u.location().isOnMap() && u.team() == opponent) {
                MapLocation mloc = u.location().mapLocation();
                int x = mloc.getX(), y = mloc.getY();
                int xp = x / CELL_SIZE, yp = y / CELL_SIZE;
                spatialHash[xp][yp].add(u.id());
            }
        }

        // returns good attack target for ranger
        public Unit neighbor(Unit r) {
            int minRange = (int) r.rangerCannotAttackRange();
            int maxRange = (int) r.attackRange();
            MapLocation rloc = r.location().mapLocation();
            int rx = rloc.getX(), ry = rloc.getY();
            int rxp = rx / CELL_SIZE, ryp = ry / CELL_SIZE;
            boolean targetFound = false;
            Unit target = null;
            ArrayDeque<Integer> xq = new ArrayDeque<>(),
                    yq = new ArrayDeque<>(),
                    depthq = new ArrayDeque<>();
            xq.addLast(rxp);
            yq.addLast(ryp);
            depthq.addLast(0);
            if (spatialCache[rxp][ryp] > 0) {
                try {
                    int uid = spatialCache[rxp][ryp];
                    Unit u = gc.unit(uid);
                    int dist = (int) rloc.distanceSquaredTo(u.location().mapLocation());
                    if (dist > minRange && dist <= maxRange) {
                        target = u;
                        targetFound = true;
                    }
                }
                catch (Exception e) {
                    spatialCache[rxp][ryp] = 0;
                }
            }
            HashSet<Integer> visited = new HashSet<>();
            while (!targetFound && xq.size() > 0) {
                int cx = xq.removeFirst();
                int cy = yq.removeFirst();
                if (visited.contains(hh * cx + cy))
                    continue;
                visited.add(hh * cx + cy);
                int depth = depthq.removeFirst();
                HashSet<Integer> targets = spatialHash[cx][cy];
                ArrayList<Unit> suitableTargets = new ArrayList<>(targets.size());
                ArrayList<Integer> deletions = new ArrayList<>();
                for (int uid: targets) {
                    try {
                        Unit u = gc.unit(uid);
                        int dist = (int) rloc.distanceSquaredTo(u.location().mapLocation());
                        if (dist > minRange && dist <= maxRange)
                            suitableTargets.add(u);
                    }
                    catch (Exception e) {
                        // implies that uid either is no longer visible
                        // or that this unit has already died
                        deletions.add(uid);
                    }
                }
                for (int i: deletions)
                    targets.remove(i);

                if (suitableTargets.size() > 0) {
                    targetFound = true;
                    for (Unit u : suitableTargets) {
                        if (target == null)
                            target = u;
                        else if (computePriority(target) < computePriority(u))
                            target = u;
                    }
                }
                else if (depth < SEARCH_DEPTH) {
                    if (inHashRange(cx + 1, cy)) {
                        xq.addLast(cx + 1);
                        yq.addLast(cy);
                        depthq.addLast(depth + 1);
                    }
                    if (inHashRange(cx - 1, cy)) {
                        xq.addLast(cx - 1);
                        yq.addLast(cy);
                        depthq.addLast(depth + 1);
                    }
                    if (inHashRange(cx, cy + 1)) {
                        xq.addLast(cx);
                        yq.addLast(cy + 1);
                        depthq.addLast(depth + 1);
                    }
                    if (inHashRange(cx, cy - 1)) {
                        xq.addLast(cx);
                        yq.addLast(cy - 1);
                        depthq.addLast(depth + 1);
                    }
                }

                // need to select units in targets that are suitable, firstly
                // secondly, of those units in targets that are suitable
                // want to return good choice of target
            }
            spatialCache[rxp][ryp] = (target == null) ? 0 : target.id();
            return target;
        }

        public int computePriority(Unit u) {
            return ((int) -u.health()) +
                    100 * (u.unitType() == UnitType.Ranger ? 1 : 0);
        }

        public boolean inHashRange(int x, int y) {
            return x >= 0 && y >= 0 && x < ww && y < hh;
        }

        // Clear hash so can be recreated on new round
        public void clear() {
            for (int i = 0; i < ww; ++i) {
                for (int j = 0; j < hh; ++j) {
                    spatialHash[i][j].clear();
                    spatialCache[i][j] = 0;
                }
            }
        }
    }
    class WorkerNearestNeighbor {
        // Spatial Hashing
        // Needs some mechanism of marking a unit as dead
        // How to do...
        int CELL_SIZE = 5;
        HashSet<Integer>[][] spatialHash;
        int[][] spatialCache;
        int SEARCH_DEPTH = 2;
        int ww; // spatial hash width
        int hh; // spatial hash height

        public WorkerNearestNeighbor() {
            ww = (width / CELL_SIZE) + 1;
            hh = (height / CELL_SIZE) + 1;
            spatialHash = new HashSet[ww][hh];
            spatialCache = new int[ww][hh];
            for (int i = 0; i < ww; ++i) {
                for (int j = 0; j < hh; ++j) {
                    spatialHash[i][j] = new HashSet<>();
                }
            }
        }

        // adds opposing units on maps to spatial hash
        public void add(Unit u) {
            if (u.location().isOnMap() && u.team() == opponent) {
                MapLocation mloc = u.location().mapLocation();
                int x = mloc.getX(), y = mloc.getY();
                int xp = x / CELL_SIZE, yp = y / CELL_SIZE;
                spatialHash[xp][yp].add(u.id());
            }
        }

        // returns good attack target for ranger
        public Unit neighbor(Unit r) {
            int minRange = 0;
            int maxRange = (int) r.visionRange();
            MapLocation rloc = r.location().mapLocation();
            int rx = rloc.getX(), ry = rloc.getY();
            int rxp = rx / CELL_SIZE, ryp = ry / CELL_SIZE;
            boolean targetFound = false;
            Unit target = null;
            ArrayDeque<Integer> xq = new ArrayDeque<>(),
                    yq = new ArrayDeque<>(),
                    depthq = new ArrayDeque<>();
            xq.addLast(rxp);
            yq.addLast(ryp);
            depthq.addLast(0);
            if (spatialCache[rxp][ryp] > 0) {
                try {
                    int uid = spatialCache[rxp][ryp];
                    Unit u = gc.unit(uid);
                    int dist = (int) rloc.distanceSquaredTo(u.location().mapLocation());
                    if (dist > minRange && dist <= maxRange) {
                        target = u;
                        targetFound = true;
                    }
                }
                catch (Exception e) {
                    spatialCache[rxp][ryp] = 0;
                }
            }
            HashSet<Integer> visited = new HashSet<>();
            while (!targetFound && xq.size() > 0) {
                int cx = xq.removeFirst();
                int cy = yq.removeFirst();
                if (visited.contains(hh * cx + cy))
                    continue;
                visited.add(hh * cx + cy);
                int depth = depthq.removeFirst();
                HashSet<Integer> targets = spatialHash[cx][cy];
                ArrayList<Unit> suitableTargets = new ArrayList<>(targets.size());
                ArrayList<Integer> deletions = new ArrayList<>();
                for (int uid: targets) {
                    try {
                        Unit u = gc.unit(uid);
                        int dist = (int) rloc.distanceSquaredTo(u.location().mapLocation());
                        if (dist > minRange && dist <= maxRange)
                            suitableTargets.add(u);
                    }
                    catch (Exception e) {
                        // implies that uid either is no longer visible
                        // or that this unit has already died
                        deletions.add(uid);
                    }
                }
                for (int i: deletions)
                    targets.remove(i);

                if (suitableTargets.size() > 0) {
                    targetFound = true;
                    for (Unit u : suitableTargets) {
                        if (target == null)
                            target = u;
                        else if (computePriority(target) < computePriority(u))
                            target = u;
                    }
                }
                else if (depth < SEARCH_DEPTH) {
                    if (inHashRange(cx + 1, cy)) {
                        xq.addLast(cx + 1);
                        yq.addLast(cy);
                        depthq.addLast(depth + 1);
                    }
                    if (inHashRange(cx - 1, cy)) {
                        xq.addLast(cx - 1);
                        yq.addLast(cy);
                        depthq.addLast(depth + 1);
                    }
                    if (inHashRange(cx, cy + 1)) {
                        xq.addLast(cx);
                        yq.addLast(cy + 1);
                        depthq.addLast(depth + 1);
                    }
                    if (inHashRange(cx, cy - 1)) {
                        xq.addLast(cx);
                        yq.addLast(cy - 1);
                        depthq.addLast(depth + 1);
                    }
                }

                // need to select units in targets that are suitable, firstly
                // secondly, of those units in targets that are suitable
                // want to return good choice of target
            }
            spatialCache[rxp][ryp] = (target == null) ? 0 : target.id();
            return target;
        }

        public int computePriority(Unit u) {
            return ((int) -u.health()) +
                    100 * (u.unitType() == UnitType.Ranger ? 1 : 0);
        }

        public boolean inHashRange(int x, int y) {
            return x >= 0 && y >= 0 && x < ww && y < hh;
        }

        // Clear hash so can be recreated on new round
        public void clear() {
            for (int i = 0; i < ww; ++i) {
                for (int j = 0; j < hh; ++j) {
                    spatialHash[i][j].clear();
                    spatialCache[i][j] = 0;
                }
            }
        }
    }

    GameController gc;
    List<Direction> mapdirs;
    HashMap<Integer, Purpose> actions;
    HashMap<Integer, Integer> lastUpdated;
    HashMap<Integer, Unit> idMap;
    Unit[][] unitMap;
    int[][][] hcaMap; // should plan 64 rounds in advance
    Planet planet;
    Team team;
    Team opponent;
    PlanetMap map;
    PlanetMap earthMap;
    PlanetMap marsMap;
    HashMap<Integer, ArrayList<MapLocation>> adjacent;
    ArrayList<Unit> blueprints;
    ArrayList<Unit> rockets;
    ArrayList<Unit> workerRockets;
    public int mtoi (MapLocation l) { return (height * l.getX() + l.getY()); }
    public MapLocation itom (int i) { return new MapLocation(planet, i / height, i % height); }
    int width;
    int height;
    HashMap<Integer, MapLocation> posGoal;
    HashMap<Integer, StaticPath> pathCache;
    Ledger ledger;
    Ledger opposition;
    Metric metric;
    RangerNearestNeighbor rnn;
    WorkerNearestNeighbor wnn;
    Random rnd;
    MapLocation knownEnemy[], knownEnemy2[];
    Integer keTurn[], ke2Turn[];
    HashSet<Integer> attackRangers, baseRangers;
    int rangerCounter;
    HashSet<Integer> wrockets;
    int numBaseRangers;
    double DESIRED_WORKERS_ADD, DESIRED_WORKERS_INC;
    public MapLocation randomMarsLoc() {
        int x = rnd.nextInt(width);
        int y = rnd.nextInt(height);
        int key = x * height + y;
        int area = width * height;
        while (true) {
            int kx = key / height;
            int ky = key % height;
            MapLocation m = new MapLocation(Planet.Mars, kx, ky);
            if (marsMap.isPassableTerrainAt(m) > 0)
                return m;
            key = (key + 1) % (area);
        }
    }

    public void updateLocation(int id, MapLocation oldpos, MapLocation newpos) {
        Unit u = gc.unit(id);
        int ox = oldpos.getX(), oy = oldpos.getY();
        int nx = newpos.getX(), ny = newpos.getY();
        if (unitMap[ox][oy].id() == id)
            unitMap[ox][oy] = null;
        unitMap[nx][ny] = u;
    }

    public Player(GameController gc) {
        System.out.println("Player Initializing");
        rnd = new Random(234892352);
        this.gc = gc;
        mapdirs = Arrays.asList(mapdirections);
        planet = gc.planet();
        team = gc.team();
        if (team == Team.Red)
            for (int i = 0; i < 20; ++i) rnd.nextDouble();
        opponent = (team == Team.Red ? Team.Blue : Team.Red);
        map = gc.startingMap(planet);
        earthMap = gc.startingMap(Planet.Earth);
        marsMap = gc.startingMap(Planet.Mars);
        actions = new HashMap<>();
        lastUpdated = new HashMap<>();
        idMap = new HashMap<>();
        adjacent = new HashMap<>();
        width = (int) map.getWidth();
        height = (int) map.getHeight();
        unitMap = new Unit[width][height];
        hcaMap = new int[width][height][1001];
        posGoal = new HashMap<>();
        pathCache = new HashMap<>();
        blueprints = new ArrayList<>();
        rockets = new ArrayList<>();
        workerRockets = new ArrayList<>();
        wrockets = new HashSet<>();
        ledger = new Ledger();
        metric = new Metric();
        rnn = new RangerNearestNeighbor();
        wnn = new WorkerNearestNeighbor();
        knownEnemy = new MapLocation[10];
        knownEnemy2 = new MapLocation[10];
        keTurn = new Integer[10];
        ke2Turn = new Integer[10];
        attackRangers = new HashSet<>();
        baseRangers = new HashSet<>();
        rangerCounter = 0;
        numBaseRangers = 0;
        DESIRED_WORKERS_ADD = 0;
        for (int i = 0; i < width; ++i) {
            for (int j = 0; j < height; ++j) {
                ArrayList<MapLocation> nearby = new ArrayList<>();
                MapLocation m = new MapLocation(planet, i, j);
                VecMapLocation vec = gc.allLocationsWithin(m, 2);
                for (int k = 0; k < vec.size(); ++k) {
                    MapLocation l = vec.get(k);
                    if (m.getX() != l.getX() || m.getY() != l.getY())
                        if (map.isPassableTerrainAt(l) > 0)
                            nearby.add(l);
                }
                adjacent.put(mtoi(m), nearby);
            }
        }

        // consider map size
        double mapsize = Math.sqrt(width*height);
        if(planet==Planet.Earth) DESIRED_WORKERS = (((int)mapsize) + 10)/6;
        else DESIRED_WORKERS = 5;
        DESIRED_WORKERS_ADD = DESIRED_WORKERS;
        //
        if(mapsize<25) {
            DESIRED_WORKERS_INC = 0.6;
        } else if(mapsize<30) {
            DESIRED_WORKERS_INC = 0.8;
        } else if(mapsize<35) {
            DESIRED_WORKERS_INC = 1.2;
        } else if(mapsize<40) {
            DESIRED_WORKERS_INC = 1.6;
        } else if(mapsize<45) {
            DESIRED_WORKERS_INC = 2.2;
        } else {
            DESIRED_WORKERS_INC = 3;
        }

        research();
        updateState();
        System.out.println("Player done initializing");
        System.out.println(metric);
    }

    private void research() {
        if (planet == Planet.Earth) {
            gc.queueResearch(UnitType.Worker);
            gc.queueResearch(UnitType.Worker);
            gc.queueResearch(UnitType.Ranger);
            gc.queueResearch(UnitType.Rocket);
            gc.queueResearch(UnitType.Ranger);
            gc.queueResearch(UnitType.Ranger);
            gc.queueResearch(UnitType.Rocket);
            gc.queueResearch(UnitType.Rocket);
            gc.queueResearch(UnitType.Worker);
        }
    }

    public void updateState() {

        if(planet==Planet.Earth) {
            if(gc.round()<60 && gc.round()%10==0) {
                DESIRED_WORKERS_ADD += DESIRED_WORKERS_INC;
                DESIRED_WORKERS = (int)DESIRED_WORKERS_ADD;
            }
        }
        else if(planet == Planet.Mars && gc.round()>740) DESIRED_WORKERS = 400;

        VecUnit allUnits = gc.units();
        int round = (int) gc.round();
        ledger.clear();
        blueprints.clear();
        rockets.clear();
        workerRockets.clear();
        wrockets.clear();
        rnn.clear();
        wnn.clear();

        for (int i = 0; i < width; ++i)
            for (int j = 0; j < height; ++j)
                unitMap[i][j] = null;

        for (int i = 0; i < allUnits.size(); ++i) {
            Unit u = allUnits.get(i);
            int id = u.id();
            if (u.location().isOnMap()) {
                MapLocation m = u.location().mapLocation();
                unitMap[m.getX()][m.getY()] = u;
                rnn.add(u);
                wnn.add(u);
            }

            if (u.team() == team) {
                if(baseRangers.contains(u.id())) ++numBaseRangers;
                idMap.put(id, u);
                if (!actions.containsKey(id))
                    actions.put(id, new Purpose(round, ""));
                ledger.updateWithUnit(u);
                if ((u.unitType() == UnitType.Factory || u.unitType() == UnitType.Rocket) && u.structureIsBuilt() == 0)
                    blueprints.add(u);
                else if (u.unitType() == UnitType.Rocket && planet == Planet.Earth) {
                    int maxCapacity = (int) u.structureMaxCapacity();
                    VecUnitID garrison = u.structureGarrison();
                    if (garrison.size() < maxCapacity) {
                        rockets.add(u);
                        int numWorkers = 0;
                        for (int k = 0; k < garrison.size(); ++k) {
                            Unit kunit = gc.unit(garrison.get(k));
                            if (kunit.unitType() == UnitType.Worker)
                                ++numWorkers;
                        }
                        if (numWorkers < 2)
                            workerRockets.add(u);
                    }
                }
            }
            lastUpdated.put(id, round);
        }
        if(gc.karbonite()>400) {
            DESIRED_FACTORIES = ledger.numFactories + ledger.numFactoryBlueprints + 2;
        }
    }

    public void run() {
        updateState();
        if (wantRocket) {
            System.out.println(gc.round() + " " + "want rocket");
        }
        ArrayList<Integer> toBeRemoved = new ArrayList<Integer>();
        int round = (int) gc.round();
        for (int unitid: actions.keySet()) {
            if (lastUpdated.get(unitid) != round) {
                toBeRemoved.add(unitid);
                continue;
            }

            Unit u = idMap.get(unitid);
            switch (u.unitType()) {
                case Worker:
                    doWorker(u);
                    break;
                case Factory:
                    doFactory(u);
                    break;
                case Ranger:
                    doRanger(u);
                    break;
                case Rocket:
                    doRocket(u);
            }
        }

        for (int id : toBeRemoved) {
            actions.remove(id);
        }
    }


    public void doWorker(Unit u) {
        try {
            if (!u.location().isOnMap())
                return;

//            if (ledger.numFactories + ledger.numFactoryBlueprints >=  DESIRED_FACTORIES / 2)
//                DESIRED_WORKERS = 8;
//
//            if (gc.round() >= 80)
//                DESIRED_WORKERS = 12;
//
//            if (gc.round() >= 750)
//                DESIRED_WORKERS = 400;

            int id = u.id();
            Unit neighbor = wnn.neighbor(gc.unit(id));
            if (neighbor != null) {
                // add this to the known locations of enemies we are currently attacking
                for(int i=9; i>0; --i) knownEnemy2[i] = knownEnemy2[i-1];
                for(int i=9; i>0; --i) ke2Turn[i] = ke2Turn[i-1];
                knownEnemy2[0] = neighbor.location().mapLocation();
                ke2Turn[0] = (int)gc.round();
            }
            MapLocation wloc = u.location().mapLocation();
            ArrayList<Direction> dirs = availableDirections(wloc);
            Direction dir = dirs.size() > 0 ? dirs.get(0) : Direction.Center;

            if (gc.round() > 4 && gc.round() <= 50 && (ledger.numFactories + ledger.numFactoryBlueprints) < DESIRED_FACTORIES) {
                wantFactory = true;
            }
            else if (gc.round() > 50 && gc.round() > 25 + lastFactoryRound && (ledger.numFactories + ledger.numFactoryBlueprints) < DESIRED_FACTORIES) {
                wantFactory = true;
            }
            else {
                wantFactory = false;
            }

            if (gc.round() > 200 && ledger.numRockets < 10 && ((gc.round() > 75 + lastRocketRound) || gc.round() > 675 || (numBaseRangers>7 && (gc.round() > 35 + lastRocketRound)))) {
                wantRocket = true;
            }
            else {
                wantRocket = false;
            }


            if (dirs.size() > 0 && gc.canReplicate(id, dir) && ledger.numWorkers < DESIRED_WORKERS) {
                gc.replicate(id, dir);
                ++ledger.numWorkers;
            } else if (planet == Planet.Earth && dirs.size() > 0 && wantFactory && gc.canBlueprint(id, UnitType.Factory, dir)) {
                gc.blueprint(id, UnitType.Factory, dir);
                ++ledger.numFactoryBlueprints;
                lastFactoryRound = (int) gc.round();
            } else if (planet == planet.Earth && wantRocket && gc.canBlueprint(id, UnitType.Rocket, dir)) {
                gc.blueprint(id, UnitType.Rocket, dir);
                ++ledger.numRocketBlueprints;
                lastRocketRound = (int) gc.round();
            }

            long dsq = Integer.MAX_VALUE;
            Unit blueprint = null;
            MapLocation bloc = null;
            for (Unit b : blueprints) {
                if (b.location().mapLocation().distanceSquaredTo(wloc) < dsq) {
                    bloc = b.location().mapLocation();
                    dsq = bloc.distanceSquaredTo(wloc);
                    blueprint = b;
                }
            }

            long dsq2 = Integer.MAX_VALUE;
            MapLocation targetLocation = null;
            Unit target = null;
            for (Unit b : workerRockets) {
                if (b.location().mapLocation().distanceSquaredTo(wloc) < dsq2) {
                    targetLocation = b.location().mapLocation();
                    dsq2 = targetLocation.distanceSquaredTo(wloc);
                    target = b;
                }
            }


            //continue
            if (gc.isMoveReady(id) && blueprint != null && wloc.distanceSquaredTo(bloc) > 2 && wloc.distanceSquaredTo(bloc) <= 144) {
                StaticPath path = getStaticPath(wloc, bloc); // very inefficient!!!
                if (path != null) { // shouldn't be null but sometimes is
                    Direction d = wloc.directionTo(path.getLoc());
                    if (blueprint != null && gc.canMove(id, d)) {
                        gc.moveRobot(id, d);
                        updateLocation(id, wloc, path.getLoc());
                    }
                }
            } else if (blueprint != null && wloc.distanceSquaredTo(bloc) <= 2) {
                if (gc.canBuild(id, blueprint.id()))
                    gc.build(id, blueprint.id());
            }
            else if (gc.isMoveReady(id)){
                long nearbyRadius = 144l;
                VecMapLocation nearbyLocations = gc.allLocationsWithin(wloc, nearbyRadius);
                MapLocation closest = null;
                long maxsq = Integer.MAX_VALUE;
                for (int i = 0; i < nearbyLocations.size(); ++i) {
                    MapLocation m = nearbyLocations.get(i);
                    try {
                        if (wloc.distanceSquaredTo(m) < maxsq && gc.karboniteAt(m) > 0) {
                            maxsq = wloc.distanceSquaredTo(m);
                            closest = m;
                        }
                    }
                    catch (Exception e) {}
                }
                if (target != null && target.structureGarrison().size() < target.structureMaxCapacity()) {
                    if(dsq2>2 && dsq2<=121) {
                        StaticPath path = getStaticPath(wloc, targetLocation); // very inefficient!!!
                        if (path != null) { // shouldn't be null but sometimes is
                            Direction d = wloc.directionTo(path.getLoc());
                            if (gc.canMove(id, d)) {
                                gc.moveRobot(id, d);
                                updateLocation(id, wloc, path.getLoc());
                            }
                        }
                    }
                    else if(dsq2<=2 && gc.canLoad(target.id(), id) && !wrockets.contains(target.id())) {
                        gc.load(target.id(), id);
                        unitMap[wloc.getX()][wloc.getY()] = null;
                        wrockets.add(target.id());
                        System.out.println("Loaded onto rocket");
                        return;
                    }
                }
                else if (closest != null) {
                    if (maxsq > 2) {
                        StaticPath path = getStaticPath(wloc, closest); // very inefficient!!!
                        if (path != null) {
                            Direction d = wloc.directionTo(path.getLoc());
                            if (closest != null && gc.canMove(id, d)) {
                                gc.moveRobot(id, d);
                                updateLocation(id, wloc, path.getLoc());
                            }
                        }
                        else {
                            ArrayList<Direction> around = availableDirections(wloc);
                            if (around.size() > 0) {
                                Direction selected = around.get((int) (rnd.nextDouble() * around.size()));
                                if (gc.canMove(id, selected)) {
                                    gc.moveRobot(id, selected);
                                    updateLocation(id, wloc, wloc.add(selected));
                                }
                            }
                        }
                    }
                    else {
                        Direction d = wloc.directionTo(closest);
                        if (gc.canHarvest(id, d))
                            gc.harvest(id, d);
                    }
                }
                else {
                    ArrayList<Direction> around = availableDirections(wloc);
                    if (around.size() > 0) {
                        Direction selected = around.get((int) (rnd.nextDouble() * around.size()));
                        if (gc.canMove(id, selected)) {
                            gc.moveRobot(id, selected);
                            updateLocation(id, wloc, wloc.add(selected));
                        }
                    }
                }
            }
        } catch (Exception e) {e.printStackTrace();}
    }

    public void doFactory(Unit u) {
        try {
            int id = u.id();
            if (u.structureIsBuilt() == 0)
                return;
            int cutoff = 100;
            if (wantRocket)
                cutoff = 190;
            if (wantFactory)
                cutoff = 240;
            if(ledger.numWorkers<5 && gc.canProduceRobot(id, UnitType.Worker)) {
                gc.produceRobot(id, UnitType.Worker);
                ++ledger.numWorkers;
            }
            else if (gc.karbonite() >= cutoff && gc.canProduceRobot(id, UnitType.Ranger) && ledger.numRangers < DESIRED_RANGERS) {
                gc.produceRobot(id, UnitType.Ranger);
                ++ledger.numRangers;
            }
            VecUnitID garrison = gc.unit(id).structureGarrison();
            MapLocation floc = u.location().mapLocation();
            ArrayList<Direction> dirs = availableDirections(floc);
            int gi = 0, di = 0;
            while (gi < garrison.size() && di < dirs.size()) {
                gc.unload(id, dirs.get(di));
                MapLocation newunitloc = floc.add(dirs.get(di));
                Unit newunit = gc.senseUnitAtLocation(newunitloc);
                unitMap[newunitloc.getX()][newunitloc.getY()] = newunit;
                ++gi;
                ++di;
            }
        } catch (Exception e) {e.printStackTrace();}
    }

    public void doRocket(Unit u) {
        try {
            if (u.structureIsBuilt() == 0)
                return;
            if (planet == Planet.Earth) {
                if (u.structureGarrison().size() < u.structureMaxCapacity() && gc.round() < 748 && u.health()>0.6*u.maxHealth())
                    return;
                MapLocation launchLocation = randomMarsLoc();
                gc.launchRocket(u.id(), launchLocation);
            }
            else {
                int id = u.id();
                VecUnitID garrison = gc.unit(id).structureGarrison();
                MapLocation rocketLocation = u.location().mapLocation();
                ArrayList<Direction> dirs = availableDirections(rocketLocation);
                int gi = 0, di = 0;
                while (gi < garrison.size() && di < dirs.size()) {
                    gc.unload(id, dirs.get(di));
                    MapLocation newunitloc = rocketLocation.add(dirs.get(di));
                    Unit newunit = gc.senseUnitAtLocation(newunitloc);
                    unitMap[newunitloc.getX()][newunitloc.getY()] = newunit;
                    ++gi;
                    ++di;
                }
            }
        }
        catch (Exception e) {e.printStackTrace();}
    }

    int leadRanger = 0;
    public void doRanger(Unit u) {
        try {
            if (!u.location().isOnMap())
                return;

            int id = u.id();
            int round = (int) gc.round();
            boolean attacking = false;
            MapLocation rloc = u.location().mapLocation();
            long dsq = Integer.MAX_VALUE;
            Unit target = null;
            MapLocation targetLocation = null;

            // Attack
            if (gc.isAttackReady(id)) {
                Unit neighbor = rnn.neighbor(gc.unit(id));
                if (neighbor != null) {
                    // add this to the known locations of enemies we are currently attacking
                    for(int i=9; i>0; --i) knownEnemy[i] = knownEnemy[i-1];
                    for(int i=9; i>0; --i) keTurn[i] = keTurn[i-1];
                    knownEnemy[0] = neighbor.location().mapLocation();
                    keTurn[0] = (int)gc.round();

                    int nid = neighbor.id();
                    if (gc.canAttack(id, nid)) {
                        gc.attack(id, nid);
                        attacking = true;
                    }
                }
            }

            if(attackRangers.contains(id)) {
                if (planet == Planet.Earth && gc.round() > 674) {
                    attackRangers.remove(id);
                    baseRangers.add(id);
                    return;
                }
                int attackCase = -1;
                if(attacking) attackCase = 3;
                else if(knownEnemy[0]!=null && keTurn[0]>=gc.round()-10) attackCase = 1;
                else if(knownEnemy2[0]!=null && ke2Turn[0]>=gc.round()-10) attackCase = 2;
                if(attackCase==1) {
                    StaticPath path = getStaticPath2(rloc, knownEnemy[0]);
                    if(path!=null) {
                        Direction d = rloc.directionTo(path.getLoc());
                        if (gc.canMove(id, d) && gc.isMoveReady(id)) {
                            gc.moveRobot(id, d);
                            updateLocation(id, rloc, path.getLoc());
                        }
                    } else {
                        if(rnd.nextDouble()<0.3) {
                            ArrayList<Direction> dirs = availableDirections(rloc);
                            if (dirs.size() > 0) {
                                Direction dir = dirs.get((int) (rnd.nextDouble() * dirs.size()));
                                if (gc.canMove(id, dir) && gc.isMoveReady(id)) {
                                    gc.moveRobot(id, dir);
                                    updateLocation(id, rloc, rloc.add(dir));
                                }
                            }
                        } else {
                        }
                    }
                }
                else if(attackCase==2) {
                    StaticPath path = getStaticPath2(rloc, knownEnemy2[0]);
                    if(path!=null) {
                        Direction d = rloc.directionTo(path.getLoc());
                        if (gc.canMove(id, d) && gc.isMoveReady(id)) {
                            gc.moveRobot(id, d);
                            updateLocation(id, rloc, path.getLoc());
                        }
                    } else {
                        if(rnd.nextDouble()<0.3) {
                            ArrayList<Direction> dirs = availableDirections(rloc);
                            if (dirs.size() > 0) {
                                Direction dir = dirs.get((int) (rnd.nextDouble() * dirs.size()));
                                if (gc.canMove(id, dir) && gc.isMoveReady(id)) {
                                    gc.moveRobot(id, dir);
                                    updateLocation(id, rloc, rloc.add(dir));
                                }
                            }
                        } else {
                        }
                    }
                }
                else { // not case 1 or 2; move randomly
                    ArrayList<Direction> dirs = availableDirections(rloc);
                    if (dirs.size() > 0) {
                        Direction dir = dirs.get((int) (rnd.nextDouble() * dirs.size()));
                        if (gc.canMove(id, dir) && gc.isMoveReady(id)) {
                            gc.moveRobot(id, dir);
                            updateLocation(id, rloc, rloc.add(dir));
                        }
                    }
                }
            }
            else if(baseRangers.contains(id)) {
                if (planet == Planet.Mars) {
                    attackRangers.add(id);
                    baseRangers.remove(id);
                    return;
                }
                for (Unit b : rockets) {
                    if (b.location().mapLocation().distanceSquaredTo(rloc) < dsq) {
                        targetLocation = b.location().mapLocation();
                        dsq = targetLocation.distanceSquaredTo(rloc);
                        target = b;
                    }
                }
                if(gc.isMoveReady(id)) {
                    if(target!=null) {
                        if(dsq>2 && target.structureGarrison().size() < target.structureMaxCapacity()) {
                            StaticPath path = getStaticPath(rloc, targetLocation); // very inefficient!!!
                            if (path != null) { // shouldn't be null but sometimes is
                                Direction d = rloc.directionTo(path.getLoc());
                                if (gc.canMove(id, d)) {
                                    gc.moveRobot(id, d);
                                    updateLocation(id, rloc, path.getLoc());
                                }
                            }
                        }
                        else if(dsq<=2 && gc.canLoad(target.id(), id)) {
                            gc.load(target.id(), id);
                            unitMap[rloc.getX()][rloc.getY()] = null;
                            baseRangers.remove(id);
                            --numBaseRangers;
                            return;
                        }
                    }
                    else { // no rocket to load; move randomly
                        ArrayList<Direction> dirs = availableDirections(rloc);
                        if (dirs.size() > 0) {
                            Direction dir = dirs.get((int) (rnd.nextDouble() * dirs.size()));
                            if (gc.canMove(id, dir)) {
                                gc.moveRobot(id, dir);
                                updateLocation(id, rloc, rloc.add(dir));
                            }
                        }
                    }
                }
            }
            else {
                if(planet==Planet.Earth) {
                    int ratio = 4;
                    if(round>600) ratio = 2;
                    if (baseRangers.size() > attackRangers.size() / ratio && round<675) {
                        attackRangers.add(id);
                    }
                    else {
                        baseRangers.add(id);
                        ++numBaseRangers;
                    }
                } else {
                    attackRangers.add(id);
                }
            }
        }
        catch (Exception e) {e.printStackTrace();}
    }

    public Unit findTarget(Unit r) {
        MapLocation rloc = r.location().mapLocation();
        long rng = r.attackRange();
        long minrng = r.rangerCannotAttackRange();
        long dsq = Integer.MIN_VALUE;
        VecUnit vecTargets = gc.senseNearbyUnitsByTeam(rloc, rng, opponent);
        List<Unit> targets = new ArrayList<Unit>();
        for (int i = 0; i < vecTargets.size(); ++i)
            targets.add(vecTargets.get(i));
        targets = targets.stream()
                .filter(u -> u.location().mapLocation().distanceSquaredTo(rloc) > minrng)
                .collect(Collectors.toList());
        Collections.sort(targets, (a, b) -> (int)
                (1000 * (a.health() - b.health()) +
                        100000 * (a.unitType() == UnitType.Ranger ? 1 : 0) +
                        (rloc.distanceSquaredTo(a.location().mapLocation())) -
                        (rloc.distanceSquaredTo(b.location().mapLocation()))));
        if (targets.size() > 0)
            return targets.get(0);
        return null;
    }

    public boolean isPassable(MapLocation m) {
        return (map.isPassableTerrainAt(m) > 0 && unitMap[m.getX()][m.getY()] == null);
    }

    public ArrayList<Direction> availableDirections(MapLocation l) {
        ArrayList<Direction> dirs = new ArrayList<Direction>();
        for (MapLocation m: adjacent.get(mtoi(l))) {
            if (isPassable(m))
                dirs.add(l.directionTo(m));
        }
        return dirs;
    }

    public StaticPath getStaticPath(MapLocation start, MapLocation finish) {
        int si = mtoi(start), fi = mtoi(finish);
        PriorityQueue<StaticPath.StaticPathEntry> pq = new PriorityQueue<>();
        StaticPath startPath = new StaticPath(start);
        HashSet<Integer> visited = new HashSet<>();
        StaticPath.StaticPathEntry b = new StaticPath.StaticPathEntry(StaticPath.getCost(start, finish), startPath);
        pq.add(b);
        while(!pq.isEmpty()) {
            StaticPath.StaticPathEntry e = pq.remove();
            MapLocation current = e.path.getLoc();
            int ci = mtoi(current);
            if (visited.contains(ci))
                continue;
            visited.add(ci);
            if (ci == fi) {
                StaticPath p = e.path;
                StaticPath t = p.getNext();
                p.setNext(null);
                while (t != null) {
                    StaticPath r = t.getNext();
                    t.setNext(p);
                    p = t;
                    t = r;
                }
                return p.getNext();
            }
            int g = e.cost - StaticPath.getCost(e.path.getLoc(), finish);
            for (MapLocation m: adjacent.get(ci)) {
                if ((isPassable(m) || mtoi(m) == fi) && !visited.contains(mtoi(m))) {
                    StaticPath newPath = new StaticPath(m, e.path);
                    StaticPath.StaticPathEntry newEntry = new StaticPath.StaticPathEntry((g + 1) +
                            StaticPath.getCost(m, finish), newPath);
                    pq.add(newEntry);
                }
            }
        }
        return null;
    }

    public int dsqbtw(int p1, int p2) {
        return (p1/height - p2/height) * (p1/height - p2/height) + (p1%height - p2%height) * (p1%height - p2%height);
    }

    public StaticPath getStaticPath2(MapLocation start, MapLocation finish) {
        int si = mtoi(start), fi = mtoi(finish);
        PriorityQueue<StaticPath.StaticPathEntry> pq = new PriorityQueue<>();
        StaticPath startPath = new StaticPath(start);
        HashSet<Integer> visited = new HashSet<>();
        StaticPath.StaticPathEntry b = new StaticPath.StaticPathEntry(StaticPath.getCost(start, finish), startPath);
        pq.add(b);
        while(!pq.isEmpty()) {
            StaticPath.StaticPathEntry e = pq.remove();
            MapLocation current = e.path.getLoc();
            int ci = mtoi(current);
            if (visited.contains(ci))
                continue;
            visited.add(ci);
            if (dsqbtw(ci, fi) <= 40) {
                StaticPath p = e.path;
                StaticPath t = p.getNext();
                p.setNext(null);
                while (t != null) {
                    StaticPath r = t.getNext();
                    t.setNext(p);
                    p = t;
                    t = r;
                }
                return p.getNext();
            }
            int g = e.cost - StaticPath.getCost(e.path.getLoc(), finish);

            for (MapLocation m: adjacent.get(ci)) {
                if ((dsqbtw(mtoi(m), si)>10 || unitMap[m.getX()][m.getY()] == null || mtoi(m) == fi) && (map.isPassableTerrainAt(m)>0) && !visited.contains(mtoi(m))) {
                    StaticPath newPath = new StaticPath(m, e.path);
                    StaticPath.StaticPathEntry newEntry = new StaticPath.StaticPathEntry((g + 1) +
                            StaticPath.getCost(m, finish), newPath);
                    pq.add(newEntry);
                }
            }
        }
        return null;
    }

    public static void main (String[] args) {
        GameController gc = new GameController();
        Player player = new Player(gc);
        while (true) {
            if (gc.getTimeLeftMs() < 100) {
                gc.nextTurn();
                gc.nextTurn();
                gc.nextTurn();
                gc.nextTurn();
            }
            try {
                player.run();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                gc.nextTurn();
            }
        }
    }

    static Direction[] mapdirections = {Direction.North, Direction.Northeast, Direction.East, Direction.Southeast, Direction.South, Direction.Southwest, Direction.West, Direction.Northwest};
}
