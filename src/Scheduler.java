import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

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

        int c = 1;
        Scanner scanner = new Scanner(System.in);

        while (!ready.isEmpty()) {

//            System.out.println("READY QUEUE: " + ready);
//            System.out.println("BLOCKED QUEUE: " + blocked);


            Process process = ready.remove();
            process.setState(State.RUNNING);
            running = process;
            Pair<Integer, Integer> range;

            if (process.getPCB().isOnDisk()) {

                System.out.println("PROCESS " + process.getID() + " SWAPPED FROM DISK");

                if ((range = kernel.fitsInMemory(process.getProgSize())) != null) {
                    kernel.loadFromDiskToMemory(process, range);
                } else {
                    kernel.swapFromDiskToMem(process, process.getProgSize());
                }

                process.getPCB().setOnDisk(false);
            }

            for (int i = 0; i < quantum; i++) {
                System.out.println("PROCESS " + process.getID() + " IS CURRENTLY RUNNING");
                System.out.println("INS# " + c);
                System.out.println("PC = " + process.getPC());
                System.out.println("READY QUEUE: " + ready);
                System.out.println("BLOCKED QUEUE: " + blocked);

                kernel.run(process);
                Kernel.displayMemory(kernel.getMemory());
                System.out.print("Enter dummy: ");
                String dummy = scanner.nextLine();
                System.out.println("___________________________________________");
                c++;
                if (process.getPC() > process.getEndBoundary() - 3 || blocked.contains(process))
                    break;
            }


            if (process.getState() == State.BLOCKED) {
                continue;
            } else if (process.getPC() <= process.getEndBoundary() - 3) {  //adds to readyList if process didn't finish executing
                ready.add(process);
                process.setState(State.READY);
            } else {
                process.setState(State.FINISHED);  //still need to delete it from memory
                ArrayList<Pair<String, Object>> onMemory = new ArrayList<>();

                for (int i = process.getStartBoundary(); i <= process.getEndBoundary(); i++) {
                    onMemory.add(kernel.getMemory()[i]);
                    kernel.getMemory()[i] = null;
                }

                kernel.updateOnDisk(process.getPCB(), onMemory);
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
