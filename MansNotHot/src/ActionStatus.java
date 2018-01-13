public class ActionStatus {
    public boolean isSuccess() {
        return success;
    }

    public boolean isTerminated() {
        return terminated;
    }

    boolean success = true;
    boolean terminated = false;
    public ActionStatus() {}

    public ActionStatus(boolean success, boolean terminated) {
        this.success = success;
        this.terminated = terminated;
    }
}
