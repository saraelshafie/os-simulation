import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

public class Scheduler {
    private Queue<Process> blocked;
    private Queue<Process> ready;

    private Process running;


    public Kernel kernel;

    public Scheduler(Kernel kernel) {
        blocked = new LinkedList<>();
        ready = new LinkedList<>();
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


    public void schedule() throws Exception {

        Scanner scanner = new Scanner(System.in);

        int cycle = 0;

        System.out.println("Please enter a value for first process arrival time: ");
        int firstProcessArrival = scanner.nextInt();

        System.out.println("Please enter a value for second process arrival time: ");
        int secondProcessArrival = scanner.nextInt();

        System.out.println("Please enter a value for third process arrival time: ");
        int thirdProcessArrival = scanner.nextInt();

        System.out.println("Please enter a value for quantum: ");
        int quantum = scanner.nextInt();

        int maxArrival = Math.max(firstProcessArrival, Math.max(secondProcessArrival, thirdProcessArrival));

        int currQuantum = -1;
        Process process = null;

        while (true) {

            if (cycle == firstProcessArrival)
                kernel.createNewProcess("Program_1.txt");
            if (cycle == secondProcessArrival)
                kernel.createNewProcess("Program_2.txt");
            if (cycle == thirdProcessArrival)
                kernel.createNewProcess("Program_3.txt");

            if (cycle > maxArrival && process == null && ready.isEmpty())
                break;

            if (process == null && !ready.isEmpty()) {
                System.out.println("READY QUEUE: " + ready);
                System.out.println("BLOCKED QUEUE: " + blocked);

                process = ready.remove();
                process.setState(State.RUNNING);

                this.running = process;

                currQuantum = quantum;

                if (process.isOnDisk()) {
                    System.out.println("IN ID IS ON DISK");
                    System.out.println("PROCESS " + process.getID() + " SWAPPED FROM DISK");
                    ArrayList<Pair<String, Object>> dataOnDisk = kernel.readDisk();

                    kernel.eraseDisk();

                    Pair<Integer, Integer> range = kernel.fitsInMemory(process.getProgSize());

                    if (range == null) {
                        range = kernel.freeSpaceInMemory(process.getProgSize());
                    }

                    //load with range
                    kernel.loadFromDiskToMemory(dataOnDisk, range);
                    process.setPC((process.getPC() - process.getStartBoundary()) + range.getKey());
                    process.setStartBoundary(range.getKey());
                    process.setEndBoundary(range.getValue());

                    process.setOnDisk(false);
                }

                updateState(process, process.getState());
            }

            if (process != null) {
                System.out.println("PROCESS " + process.getID() + " IS CURRENTLY RUNNING");
                System.out.println("PC = " + process.getPC());

                System.out.println("CYCLE: " + cycle);

                kernel.run(process);
                kernel.getMemory()[process.getStartBoundary() + 2].setValue(process.getPC());  //UPDATE PC
                currQuantum--;

                //FINISHED PROCESS
                if (process.getPC() > process.getEndBoundary() - 3) {
                    process.setState(State.FINISHED);

                    //delete it from memory
                    for (int i = process.getStartBoundary(); i <= process.getEndBoundary(); i++)
                        kernel.getMemory()[i] = null;

                    process = null;
                } else if (process.getState() == State.BLOCKED) {
                    updateState(process, process.getState());
                    process = null;
                } else if (currQuantum == 0) {
                    ready.add(process);
                    process.setState(State.READY);
                    updateState(process, process.getState());
                    process = null;
                }
            }

            System.out.println("READY QUEUE: " + ready);
            System.out.println("BLOCKED QUEUE: " + blocked);

            Kernel.displayMemory(kernel.getMemory());
            System.out.print("Enter dummy: ");
            String dummy = scanner.nextLine();
            System.out.println("_________________________________________________________________________");

            cycle++;
        }

    }


//    public void schedule2() throws IOException {
//
//        int c = 1;
//        Scanner scanner = new Scanner(System.in);
//        int cycle = 0;
//        System.out.println("Please enter a value for first process arrival time: ");
//        int firstProcessArrival = scanner.nextInt();
//
//        System.out.println("Please enter a value for second process arrival time: ");
//        int secondProcessArrival = scanner.nextInt();
//
//        System.out.println("Please enter a value for third process arrival time: ");
//        int thirdProcessArrival = scanner.nextInt();
//
//        System.out.println("Please enter a value for quantum: ");
//        int quantum = scanner.nextInt();
//
//        int maxArrival = Math.max(firstProcessArrival, Math.max(secondProcessArrival, thirdProcessArrival));
//
//        while (true) {
//            if (!ready.isEmpty()) {
//                Process process = ready.remove();
//                process.setState(State.RUNNING);
//                running = process;
//                Pair<Integer, Integer> range;
//
//                if (process.isOnDisk()) {
//
//                    System.out.println("PROCESS " + process.getID() + " SWAPPED FROM DISK");
//
//                    if ((range = kernel.fitsInMemory(process.getProgSize())) != null) {
//                        kernel.loadFromDiskToMemory(process, range);
//                    } else {
//                        kernel.swapFromDiskToMem(process, process.getProgSize());
//                    }
//
//                    process.setOnDisk(false);
//                }
//
//                for (int i = 0; i < quantum; i++) {
//                    System.out.println("PROCESS " + process.getID() + " IS CURRENTLY RUNNING");
//                    System.out.println("PC = " + process.getPC());
//                    System.out.println("READY QUEUE: " + ready);
//                    System.out.println("BLOCKED QUEUE: " + blocked);
//                    System.out.println("CYCLE: " + cycle);
//
//                    kernel.run(process);
//                    Kernel.displayMemory(kernel.getMemory());
//                    System.out.print("Enter dummy: ");
//                    String dummy = scanner.nextLine();
//                    System.out.println("___________________________________________");
//                    c++;
//
//                    if (process.getPC() > process.getEndBoundary() - 3 || blocked.contains(process))
//                        break;
//
//                    if (cycle == firstProcessArrival)
//                        kernel.createNewProcess("Program_1.txt");
//                    if (cycle == secondProcessArrival)
//                        kernel.createNewProcess("Program_2.txt");
//                    if (cycle == thirdProcessArrival)
//                        kernel.createNewProcess("Program_3.txt");
//                    cycle++;
//                }
//
//
//                if (process.getState() == State.BLOCKED) {
//                    continue;
//                } else if (process.getPC() <= process.getEndBoundary() - 3) {  //adds to readyList if process didn't finish executing
//                    ready.add(process);
//                    process.setState(State.READY);
//                } else {
//                    process.setState(State.FINISHED);  //still need to delete it from memory
//                    ArrayList<Pair<String, Object>> onMemory = new ArrayList<>();
//
//                    for (int i = process.getStartBoundary(); i <= process.getEndBoundary(); i++) {
//                        onMemory.add(kernel.getMemory()[i]);
//                        kernel.getMemory()[i] = null;
//                    }
//
//                    kernel.updateOnDisk(process.getPCB(), onMemory);
//                }
//
//            } else if (cycle <= maxArrival) {
//                if (cycle == firstProcessArrival)
//                    kernel.createNewProcess("Program_1.txt");
//                if (cycle == secondProcessArrival)
//                    kernel.createNewProcess("Program_2.txt");
//                if (cycle == thirdProcessArrival)
//                    kernel.createNewProcess("Program_3.txt");
//                cycle++;
//            } else
//                break;
//        }
//    }

    public boolean readyListContainsID(int pid) {
        for (Process p : ready) {
            if (p.getID() == pid)
                return true;
        }
        return false;
    }

    public void updateState(Process process, State statetoUpdate) {
        kernel.getMemory()[process.getStartBoundary() + 1].setValue(statetoUpdate);
    }


}
