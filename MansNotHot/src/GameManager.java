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
    private HashMap<Integer, Unit> idMap; // HashMap of UnitID to Units
    private HashSet<Integer> assigned; // ArrayList of Units that are currently not assigned to Actions
    private RoboLedger ledger; // Ledger of information about current units

    // Constructor
    public GameManager(GameController gc) {
        this.gc = gc;
        planet = gc.planet();
        mapAnalyzer = new MapAnalyzer(this);
        actionQueue = new ActionQueue();
        otherQueue = new ActionQueue();
        idMap = new HashMap<>();
        ledger = new RoboLedger();
        assigned = new HashSet<>();
        initializeGameManager();
    }

    private void initializeGameManager() {
        research(); // queue initial research targets
        updateState(); // get initial state and assign initial actions
    }

    private void research() { // create research queue
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

    private void updateState() {
        VecUnit myUnits = gc.myUnits();
        ledger.clear();
        idMap.clear();
        for (int i = 0; i < myUnits.size(); ++i) {
            Unit unit = myUnits.get(i);
            idMap.put(unit.id(), unit); // update idMap with unit
            ledger.updateWithUnit(unit); // update ledger with unit
            if(!assigned.contains(unit.id())) { // if unit is not assigned an action...
                handleUnassignedUnit(unit);     // ...assign an action to it
            }
        }
        mapAnalyzer.updateWithUnitPositions(); // add units to map
    }

    public void handleUnassignedUnit(Unit unit) {
        if (unit.unitType() == UnitType.Worker) {
            Action workerAction = new InitialWorkerAction(this, robo);
            queueAction(workerAction);
        }
        else if (unit.unitType() == UnitType.Factory) {
            Action factoryAction = new InitialFactoryAction(this, robo);
            queueAction(factoryAction);
        }
        else if (unit.unitType() == UnitType.Ranger) {
            Action rangerAction = new InitialRangerAction(this, robo);
            queueAction(rangerAction);
        }
        else {
            System.out.format("Unexpected unit type: %d\n", unit.unitType());
        }
        assigned.add(unit.id());
    }

    // Overall turn method
    public void gameStep() {
        try {
            updateState(); // update with new state and assign new actions

            // swap current actions and other
            // otherQueue is currently empty while actionQueue holds all current queued actions
            // we will switch them and then perform each action (now in otherQueue) and enqueue
            // the resulting actions (to actionQueue) to be considered in the next turn
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
        }
        catch (Exception e) { // print error
            e.printStackTrace();
        }
        finally { // debug information
            System.out.format("Team %s on Planet %s Completed Round %d\n",
                    (getTeam() == Team.Red ? "Red" : "Blue"),
                    (getPlanet() == Planet.Earth ? "Earth" : "Mars"),
                    gc.round());
            System.out.flush();
            System.err.flush();
            gc.nextTurn(); // proceed to next turn
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

    public RoboLedger getLedger() {
        return ledger;
    }

    public Map<Integer, Unit> getIdMap() {
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
}
