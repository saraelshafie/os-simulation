import java.util.LinkedList;
import java.util.Queue;

public class Scheduler {
    private Queue blocked;
    private Queue ready;
    private Process running;

    public Scheduler() {
        blocked = new LinkedList<>();
        ready = new LinkedList<>();
    }

    public Queue getBlocked() {
        return blocked;
    }

    public void setBlocked(Queue blocked) {
        this.blocked = blocked;
    }

    public Queue getReady() {
        return ready;
    }

    public void setReady(Queue ready) {
        this.ready = ready;
    }

    public Process getNextProcess() {
        Process p = (Process) ready.remove();
        p.setState(State.RUNNING);

        return p;

    }
}
