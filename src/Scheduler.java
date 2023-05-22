import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

public class Scheduler {
    private Queue<Process> blocked;
    private Queue<Process> ready;

    private Process running;
    private final int quantum;

    private Kernel kernel;

    public Scheduler(int quantum, Kernel kernel) {
        blocked = new LinkedList<>();
        ready = new LinkedList<>();
        this.quantum = quantum;
        this.kernel = kernel;
    }

    public Process getRunning() {
        return running;
    }

    public void setRunning(Process running) {
        this.running = running;
    }

    public Queue<Process> getBlocked() {
        return blocked;
    }

    public void setBlocked(Queue<Process> blocked) {
        this.blocked = blocked;
    }

    public Queue<Process> getReady() {
        return ready;
    }

    public void setReady(Queue<Process> ready) {
        this.ready = ready;
    }


    public void schedule() throws IOException {
        while (!ready.isEmpty()) {
            Process process = ready.remove();
            process.setState(State.RUNNING);
            running = process;
            Pair<Integer, Integer> range;
            if (process.getPCB().isOnDisk()) {
                if ((range = kernel.fitsInMemory(process.getEndBoundary() - process.getStartBoundary() + 1)) != null) {
                    kernel.loadFromDiskToMemory(process, range);
                } else {
                    kernel.swapFromDiskToMem(process);
                }

                process.getPCB().setOnDisk(false);
            }

            for (int i = 0; i < quantum; i++)
                kernel.run(process);

            if (process.getPC() <= process.getEndBoundary() - 3) {  //adds to readyList if process didn't finish executing
                ready.add(process);
                process.setState(State.READY);
            } else {
                process.setState(State.FINISHED);  //still need to delete it from memory
                kernel.updateOnDisk(process.getPCB());
            }
        }
    }

    public boolean readyListContainsID(int pid) {
        for (Process p : ready) {
            if (p.getID() == pid)
                return true;
        }
        return false;
    }


}
