public class PCB {
    private int pid;
    private State state;
    private int PC;
    private int startBoundary;
    private int endBoundary;

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public int getPC() {
        return PC;
    }

    public void setPC(int PC) {
        this.PC = PC;
    }

    public int getStartBoundary() {
        return startBoundary;
    }

    public void setStartBoundary(int startBoundary) {
        this.startBoundary = startBoundary;
    }

    public int getEndBoundary() {
        return endBoundary;
    }

    public void setEndBoundary(int endBoundary) {
        this.endBoundary = endBoundary;
    }
}
