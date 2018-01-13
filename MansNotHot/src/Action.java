import java.util.TreeSet;

public abstract class Action implements Comparable<Action> {
    private RoboLedger ledger;
    private int priority = 10; // priority goes in decreasing order from 1 to any integer (suggested range 1-10)
    private TreeSet<Robo> units;
    private Runnable uponTermination;
    private GameManager manager;
    private boolean terminated = false;
    private int roundCreated;
    private int projectedCompletion = 1000;

    public TreeSet<Robo> getUnits() {
        return units;
    }

    public Runnable getUponTermination() {
        return uponTermination;
    }

    public void setUponTermination(Runnable uponTermination) {
        this.uponTermination = uponTermination;
    }

    public GameManager getManager() {
        return manager;
    }

    public int getRoundCreated() {
        return roundCreated;
    }

    public int getProjectedCompletion() {
        return projectedCompletion;
    }

    public void setProjectedCompletion(int projectedCompletion) {
        this.projectedCompletion = projectedCompletion;
    }

    public void initialize(GameManager manager) {
        units = new TreeSet<>();
        this.manager = manager;
        this.roundCreated = (int) manager.controller().round();
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }

    public int compareTo(Action other) {
        return priority - other.priority;
    }

    public void remove(Robo robo) {
        units.remove(robo);
        ledger.remove(robo);
    }

    public boolean isTerminated() {
        return terminated;
    }

    protected void setTerminated(boolean terminated) {
        this.terminated = terminated;
    }

    public abstract ActionStatus execute();

    public void updateMeta() {
        // called alongside execute to do general tasks
    }

    public void terminate() {
        if (uponTermination != null)
            uponTermination.run();
    }
}
