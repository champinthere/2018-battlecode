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

    public GameManager(GameController gc) {
        this.gc = gc;
        planet = gc.planet();
        mapAnalyzer = new MapAnalyzer(this);
        actionQueue = new ActionQueue();
        otherQueue = new ActionQueue();
        idMap = new HashMap<>();
        initializeGameManager();
    }

    public Map<Integer, Robo> getIdMap() {
        return idMap;
    }

    public List<Action> getActions() { return null; }

    public int queueAction(Action action) { return 0; }

    public boolean hasAction(int unitid) { return false; }

    // returns null if no Action exists
    public Action getAction(int unitid) {
        return null;
    }

    public GameController controller() { return gc; }

    public void handleUnassignedRobos() {
        // should do stuff
    }

    private void initializeGameManager() {
        research();
        VecUnit myunits = gc.myUnits();
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
            gc.queueResearch(UnitType.Mage);
            gc.queueResearch(UnitType.Rocket);
            gc.queueResearch(UnitType.Mage);
            gc.queueResearch(UnitType.Ranger);
            gc.queueResearch(UnitType.Mage);
            gc.queueResearch(UnitType.Mage);
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
                ActionStatus status = action.execute();
                if (status.terminated) {
                    action.terminate();
                }
                else {
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

    public void updateState() {
        VecUnit myUnits = gc.myUnits();
        for (int i = 0; i < myUnits.size(); ++i) {
            Unit unit = myUnits.get(i);
            if (!idMap.containsKey(unit.id())) {
                idMap.put(unit.id(), new Robo(this, unit.id()));
                unassigned.add(idMap.get(unit.id()));
            }
            Robo robot = idMap.get(unit.id());
            robot.setLoc(unit.location());
            robot.setRoundLastUpdated(gc.round());
        }
        // add units to map
    }
}
