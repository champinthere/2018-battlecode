import bc.*;

import java.util.ArrayList;
import java.util.HashMap;

import static bc.UnitType.Factory;
import static bc.UnitType.Ranger;
import static bc.UnitType.Worker;

public class GameManager {
    // Game Controller
    private GameController gc;

    // One-Time Variables
    AsteroidPattern asteroidP;
    PlanetMap map;
    Direction[] directions;

    // State Variables
    private HashMap<Integer, MapLocation> posGoal;
    private HashMap<Integer, Integer> idGoal; // attack or build
    private HashMap<Integer, Direction> dirGoal; // harvest or blueprint
    private HashMap<Integer, Integer> state; // 0 = none; 2^0 = moving, 2^1 = attack, 2^2 = build, 2^3 = harvesting, 2^4=blueprint
    private VecUnit myUnits;
    private ArrayList<Unit> workers, rangers, factories, bfactories, rockets, brockets, healers;
    private long numUnits;
    private boolean buildingFactory;

    public GameManager(GameController gc) {
        // Save GameController
        this.gc = gc;

        // Initialize one-time variables
        directions = Direction.values();
        asteroidP = gc.asteroidPattern();
        map = gc.startingMap(gc.planet());

        // Initialize State Variables
        state = new HashMap<>();

        posGoal = new HashMap<>();
        idGoal = new HashMap<>();
        dirGoal = new HashMap<>();

        workers = new ArrayList<>();
        rangers = new ArrayList<>();
        factories = new ArrayList<>();
        bfactories = new ArrayList<>();
        brockets = new ArrayList<>();
        rockets = new ArrayList<>();
        healers = new ArrayList<>();

        buildingFactory = false;

        // Create research queue
        research();
    }

    // Create Research Queue
    private void research() {
        if (gc.planet() == Planet.Earth) {
            gc.queueResearch(UnitType.Worker);
            gc.queueResearch(UnitType.Worker);
            gc.queueResearch(UnitType.Ranger);
            gc.queueResearch(UnitType.Ranger);
            gc.queueResearch(UnitType.Rocket);
            gc.queueResearch(UnitType.Healer);
            gc.queueResearch(UnitType.Healer);
            gc.queueResearch(UnitType.Rocket);
        }
    }

    // Update State
    public void updateState() {
        // reset state variables
        workers.clear();
        rangers.clear();
        factories.clear();
        rockets.clear();
        healers.clear();

        // get new unit data
        myUnits = gc.myUnits();
        numUnits = myUnits.size();

        // split up the units
        for(int i=0; i<numUnits; ++i) {
            Unit unit = myUnits.get(i);
            switch(unit.unitType()) {
                case Worker:
                    workers.add(unit);
                    break;
                case Ranger:
                    rangers.add(unit);
                    break;
                case Factory:
                    if(unit.structureIsBuilt()==0) bfactories.add(unit);
                    else factories.add(unit);
                    break;
                case Rocket:
                    if(unit.structureIsBuilt()==0) brockets.add(unit);
                    else rockets.add(unit);
                    break;
                case Healer:
                    healers.add(unit);
                    break;
            }
        }

        // act on the data
        actWorkers();
    }

    private void actWorkers() {

        // Lay new blueprints
        int toBuild = 0;
        while((factories.size()<4) && (bfactories.size() == 0) && !buildingFactory) { // need to lay a blueprint
            // take the first worker that is not harvesting
            while(toBuild<workers.size() && ((state.get(workers.get(toBuild).id())&(1<<3))>0)) ++toBuild;
            if(toBuild==workers.size()) --toBuild;

            // make the unit build
            Unit builder = workers.get(toBuild);
            for(Direction d: directions) {
                if (gc.canBlueprint(builder.id(), UnitType.Factory, d)) {
                    try {
                        gc.blueprint(builder.id(), UnitType.Factory, d);
                    } catch(Exception e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            state.put(builder.id(), (state.get(builder.id()) | (1 << 2)));
                            MapLocation blueprintLoc = builder.location().mapLocation().add(d);
                            idGoal.put(builder.id(), gc.senseUnitAtLocation(blueprintLoc).id());
                            buildingFactory = true;
                        } catch(Exception e) {
                            System.out.print("Big Error!");
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        // Move to Karbonite Stores


        // Act on State
        
    }
}
