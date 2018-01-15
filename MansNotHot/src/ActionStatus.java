public class ActionStatus {
    public boolean isSuccess() {
        return success;
    }

    public boolean isTerminated() {
        return terminated;
    }

    public boolean isPaused() { return paused; }

    private boolean success = true;
    private boolean terminated = false;
    private boolean paused = false;

    public ActionStatus() {}

    public ActionStatus(boolean success, boolean terminated) {
        this.success = success;
        this.terminated = terminated;
    }

    public ActionStatus(boolean success, boolean terminated, boolean paused) {
        this.success = success;
        this.terminated = terminated;
        this.paused = paused;
    }
}
