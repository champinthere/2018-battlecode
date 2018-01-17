import bc.*;

import java.util.*;

public class GameManager {
    private GameController gc;
    public static Direction[] DIRECTIONS = {Direction.North, Direction.Northeast, Direction.East, Direction.Southeast, Direction.South, Direction.Southwest, Direction.West, Direction.Northwest};

    private Planet planet;

    public MapAnalyzer getMapAnalyzer() {
        return mapAnalyzer;
    }

    private MapAnalyzer mapAnalyzer;
    private GameMind mind;
    private ActionQueue actionQueue;
    private ActionQueue otherQueue;
    private HashMap<Integer, Robo> idMap; // needs to be updated upon unit creation
    private TreeSet<Robo> unassigned;
    private RoboLedger ledger;
    private ArrayList<Robo> myRobots;

    public GameManager(GameController gc) {
        this.gc = gc;
        planet = gc.planet();
        mapAnalyzer = new MapAnalyzer(this);
        actionQueue = new ActionQueue();
        otherQueue = new ActionQueue();
        idMap = new HashMap<>();
        ledger = new RoboLedger();
        myRobots = new ArrayList<>();
        unassigned = new TreeSet<>();
        initializeGameManager();
    }

    public Map<Integer, Robo> getIdMap() {
        return idMap;
    }

    public List<Action> getActions() { return null; }

    public void queueAction(Action action) {
        actionQueue.add(action);
    }

    public boolean hasAction(int unitid) { return false; }

    // returns null if no Action exists
    public Action getAction(int unitid) {
        return null;
    }

    public GameController controller() { return gc; }

    public void handleUnassignedRobos() {
        ArrayList<Robo> robos = new ArrayList<>(unassigned);
        for (Robo robo: robos) {
            if (robo.getType() == UnitType.Worker) {
                Action workerAction = new InitialWorkerAction(this, robo);
                queueAction(workerAction);
                unassigned.remove(robo);
            }
        }
    }

    private void initializeGameManager() {
        research();
        updateState();
        ArrayList<Robo> toBeRemoved = new ArrayList<>();
        for (Robo worker : unassigned) {
            queueAction(new InitialWorkerAction(this, worker));
        }
    }

    private void research() {
        if (planet == Planet.Earth) {
            gc.queueResearch(UnitType.Worker);
            gc.queueResearch(UnitType.Ranger);
            gc.queueResearch(UnitType.Ranger);
            gc.queueResearch(UnitType.Ranger);
            gc.queueResearch(UnitType.Rocket);
            gc.queueResearch(UnitType.Knight);
            gc.queueResearch(UnitType.Knight);
            gc.queueResearch(UnitType.Knight);
        }
    }

    public void gameStep() {
        try {
            updateState();
            ActionQueue tmpQ = otherQueue;
            otherQueue = actionQueue;
            actionQueue = tmpQ;
            while (otherQueue.size() > 0) {
                Action action = otherQueue.remove();
                action.updateMeta();
                ActionStatus status = new ActionStatus();
                try {
                    status = action.execute();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                if (status.isTerminated()) {
                    action.terminate();
                }
                else if (!status.isPaused()) {
                    actionQueue.add(action);
                }
            }
            handleUnassignedRobos();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            System.out.format("Team %s on Planet %s Completed Round %d\n",
                    (getTeam() == Team.Red ? "Red" : "Blue"),
                    (getPlanet() == Planet.Earth ? "Earth" : "Mars"),
                    gc.round());
            System.out.flush();
            System.err.flush();
            gc.nextTurn();
        }
    }

    public Team getTeam() {
        return gc.team();
    }

    public Planet getPlanet() {
        return planet;
    }

    public Team getOpposingTeam() {
        if (getTeam() == Team.Red)
            return Team.Blue;
        return Team.Red;
    }

    public List<Robo> getMyRobots() {
        return myRobots;
    }

    public RoboLedger getLedger() {
        return ledger;
    }

    public void updateState() {
        VecUnit myUnits = gc.myUnits();
        myRobots.clear();
        ledger.clear();
        for (int i = 0; i < myUnits.size(); ++i) {
            Unit unit = myUnits.get(i);
            if (!idMap.containsKey(unit.id())) {
                idMap.put(unit.id(), new Robo(this, unit.id()));
                unassigned.add(idMap.get(unit.id()));
            }
            Robo robot = idMap.get(unit.id());
            robot.setLoc(unit.location());
            robot.setRoundLastUpdated(gc.round());
            if (robot.getType() == UnitType.Factory || robot.getType() == UnitType.Rocket)
                robot.setBlueprint(unit.structureIsBuilt() == 0);
            if (robot.getHandler() == null)
                unassigned.add(robot);
            ledger.updateWithRobo(robot);
            myRobots.add(robot);
        }
        mapAnalyzer.updateWithUnitPositions();
        // add units to map
    }
}
