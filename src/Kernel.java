import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class Kernel {

    private Mutex fileAccess;
    private Mutex input;
    private Mutex printer;
    private Scheduler scheduler;

    private Pair<String, Object>[] memory;
    private ArrayList<Pair<String, Object>> hardDisk;

    public Kernel(int quantum) {
        this.scheduler = new Scheduler(quantum, this);
        this.fileAccess = new Mutex(this.scheduler);
        this.input = new Mutex(this.scheduler);
        this.printer = new Mutex(this.scheduler);
        this.memory = new Pair[40];
        this.hardDisk = new ArrayList<>();
    }

    public Object readFromDisk(String filename) {

        return null;
    }

    public void writeToDisk(String filename, Object data) {

    }


    public void executeInstruction(Process process, String instruction) throws IOException {
        Scanner scanner = new Scanner(System.in);

        String[] ins = instruction.split(" ");

        if (ins[0].equals("assign")) {
            Object toAssign;
            String varName = ins[1];

            if (ins[2].equals("input")) {
                System.out.println("Please enter a value: ");
                toAssign = scanner.nextLine();
//                System.out.println("x = " + input);

            } else if (ins[2].equals("readFile")) {

                String fileName = (String) process.getVar(ins[3]);
                BufferedReader br = new BufferedReader(new FileReader(fileName));
                toAssign = br.readLine();

            } else toAssign = ins[2];

            try {
                toAssign = Integer.parseInt((String) toAssign);
            } catch (Exception e) {
            }

            // write (varName: toAssign) in process address space
            process.addVar(varName, toAssign);


        } else if (ins[0].equals("print")) {
            // fetch x from process address space
            Object data;

            if (ins[1].equals("readFile")) {
                String fileName = (String) process.getVar(ins[3]);
                BufferedReader br = new BufferedReader(new FileReader(fileName));
                data = br.readLine();
            } else
                data = process.getVar(ins[1]);

            System.out.println(data);

        } else if (ins[0].equals("writeFile")) {
            String fileName = (String) process.getVar(ins[1]);
            Object data = process.getVar(ins[2]);

            PrintWriter pw = new PrintWriter(new FileOutputStream(fileName, true));
            pw.append(data.toString());
            pw.append("\n");
            pw.close();

        } else if (ins[0].equals("printFromTo")) {
            int x = (int) process.getVar(ins[1]);
            int y = (int) process.getVar(ins[2]);

            for (int i = x; i <= y; i++) {
                System.out.println(i);
            }

        } else if (ins[0].equals("semWait")) {

            switch (ins[1]) {
                case "userOutput" -> printer.semWait(process);
                case "userInput" -> input.semWait(process);
                case "file" -> fileAccess.semWait(process);
            }

        } else if (ins[0].equals("semSignal")) {
            switch (ins[1]) {
                case "userOutput" -> printer.semSignal(process);
                case "userInput" -> input.semSignal(process);
                case "file" -> fileAccess.semSignal(process);
            }
        }
    }

    public void run(Process process) throws IOException {
        int pc = process.getPC();
        process.setPC(pc + 1);

        String ins = (String) memory[pc].getValue();
        executeInstruction(process, ins);
    }

    public static String getSpaces(int n) {
        String spaces = "";
        for (int i = 0; i < n; i++) {
            spaces += " ";
        }

        return spaces;
    }

    public static void displayMemory(Pair<String, Object>[] memory) {

        System.out.println(getSpaces(5) + "+------------------------------+");

        int length = 30;

        for (int i = 0; i < memory.length; i++) {

            Pair<String, Object> p = memory[i];

            String strMemAddr = i + "";
            String addrWhitespc = getSpaces(4 - strMemAddr.length());

            System.out.print(i + ":" + addrWhitespc);

            int pairSize;
            if (p == null) {
                pairSize = 0;
            } else if (p.getKey() == null && p.getValue() == null) {
                pairSize = 8;       // reserved
            } else {
                pairSize = ((String) p.getKey()).length() + p.getValue().toString().length() + 4;
            }
            int numSpaces = (length - pairSize) / 2;
            String spaces = getSpaces(numSpaces);

            System.out.print("|" + spaces);
            System.out.print(p == null ? "" : (p.getKey() == null && p.getValue() == null) ? "reserved" : p);
            System.out.println(spaces + (pairSize % 2 == 0 ? "" : " ") + "|");

            if (i == memory.length - 1)
                System.out.println(getSpaces(5) + "+------------------------------+");
            else
                System.out.println(getSpaces(5) + "--------------------------------");
        }
    }


    public Pair<Integer, Integer> fitsInMemory(int progSize) throws IOException {

        int empty = 0;
        int startIdx = 0;

        for (int i = 0; i < memory.length; i++) {
            Pair<String, Object> p = memory[i];

            if (memory[i] == null) {
                empty++;
                if (empty == 1)
                    startIdx = i;
            } else
                empty = 0;

            if (empty == progSize) {
                return new Pair<Integer, Integer>(startIdx, startIdx + progSize - 1);
            }
        }

        return null;
    }

    public Process createNewProcess(String fileName) throws IOException {

        BufferedReader br = new BufferedReader(new FileReader(fileName));
        String line;
        int progSize = 0;

        //Accumulate to count instructions
        while ((line = br.readLine()) != null) {
            progSize++;
        }
        br.close();


        PCB pcb = new PCB();
        Process process = new Process(pcb, memory);

        hardDisk.add(new Pair<>("PCB", pcb));

        while ((line = br.readLine()) != null) {
            Pair<String, Object> pair = new Pair<>("ins", line);
            hardDisk.add(pair);
        }

        //Plus 3 to accomodate for 3 locations for future variables
        Pair<Integer, Integer> range = fitsInMemory(progSize + 3);

        if (range == null) {
            // RAM CANNOT ACCOMMODATE PROCESS
            // SWAP FROM DISK
//            swapFromMemToDisk(progSize + 3);
//            range = fitsInMemory(progSize + 3);
            swapFromDiskToMem(process, progSize + 4);

        }

//        PCB pcb = new PCB(range.getKey(), range.getKey(), range.getValue());


        br = new BufferedReader(new FileReader(fileName));

        for (int i = pcb.getStartBoundary(); (line = br.readLine()) != null; i++) {
            Pair<String, Object> pair = new Pair<>("ins", line);
            memory[i] = pair;
            hardDisk.add(pair);
        }

        //Reserve places for future variable insertions
        for (int j = pcb.getEndBoundary(); j > pcb.getEndBoundary() - 3; j--) {
            memory[j] = new Pair<>();
            hardDisk.add(new Pair<>());
        }

        br.close();

        scheduler.getReady().add(process);
        process.setState(State.READY);


        return process;
    }


    public void swapFromDiskToMem(Process processOnDisk, int progSize) throws IOException {
        ArrayList<Pair<String, Object>> temp = new ArrayList<>();

//        int progSize = processOnDisk.getEndBoundary() - processOnDisk.getStartBoundary() + 1;

        for (int i = 0; i < memory.length; i++) {

            if (memory[i] == null)
                continue;

            PCB pcb = (PCB) memory[i].getValue();

            if (scheduler.getRunning().getID() == pcb.getPid()) {  //can't swap a running process
                i = pcb.getEndBoundary();
                continue;
            }

            for (int j = pcb.getStartBoundary(); j <= pcb.getEndBoundary(); j++) {
                temp.add(memory[j]);
                memory[j] = null;
            }

            Pair<Integer, Integer> range = fitsInMemory(progSize);

            if (range == null) {
                int c = 0;
                for (int j = pcb.getStartBoundary(); j <= pcb.getEndBoundary(); j++) {
                    memory[j] = temp.remove(c);
                }
                i = pcb.getEndBoundary();
            } else {
                loadFromDiskToMemory(processOnDisk, range);
                updateOnDisk(pcb);  //need to pass temp as process already removed
                pcb.setOnDisk(true);
                return;
            }

        }

    }

    public void loadFromDiskToMemory(Process processOnDisk, Pair<Integer, Integer> range) {
        Pair<Integer, Integer> rangeOnDisk = null; //startBound
        for (int i = 0; i < hardDisk.size(); i++) {
            Pair<String, Object> pair = hardDisk.get(i);

            if (pair.getKey().equals("PCB") && ((PCB) pair.getValue()).getPid() == processOnDisk.getID()) {
                rangeOnDisk = new Pair<>(i, i + (range.getValue() - range.getKey()));
                break;
            }


        }

        int j = range.getKey();

        for (int i = rangeOnDisk.getKey(); i <= rangeOnDisk.getValue(); i++) {
            memory[j] = hardDisk.get(i);
            j++;
        }
    }


    public void swapFromMemToDisk(int progSize) throws IOException {

        ArrayList<Pair<String, Object>> temp = new ArrayList<>();

        for (int i = 0; i < memory.length; i++) {

            if (memory[i] == null)
                continue;

            PCB pcb = (PCB) memory[i].getValue();

            if (scheduler.getRunning().getID() == pcb.getPid()) {  //can't swap a running process
                i = pcb.getEndBoundary();
                continue;
            }

            for (int j = pcb.getStartBoundary(); j <= pcb.getEndBoundary(); j++) {
                temp.add(memory[j]);
                memory[j] = null;
            }

            if (fitsInMemory(progSize) == null) {
                int c = 0;
                for (int j = pcb.getStartBoundary(); j <= pcb.getEndBoundary(); j++) {
                    memory[j] = temp.remove(c);
                }
                i = pcb.getEndBoundary();
            } else {
                updateOnDisk(pcb);
                pcb.setOnDisk(true);
                return;
            }

        }


    }

    public void updateOnDisk(PCB processOnMemPCB) {
        int startOnDisk = 0;

        for (int i = 0; i < hardDisk.size(); i++) {
            Pair<String, Object> pair = hardDisk.get(i);

            if (pair.getKey().equals("PCB") && ((PCB) pair.getValue()).getPid() == processOnMemPCB.getPid()) {
                startOnDisk = i;
                break;
            }

        }
        for (int i = processOnMemPCB.getStartBoundary(); i <= processOnMemPCB.getEndBoundary(); i++) {
            hardDisk.add(startOnDisk, memory[i]);
            startOnDisk++;
        }
    }


    public static void main(String[] args) throws IOException {
        Pair<String, Object>[] memory = new Pair[40];
        Kernel kernel = new Kernel(2);
        kernel.memory = memory;

        Process p1 = kernel.createNewProcess("Program_1.txt");
//
        Process p2 = kernel.createNewProcess("Program_2.txt");
//
        Process p3 = kernel.createNewProcess("Program_3.txt");


        kernel.scheduler.schedule();
//
//        memory[2] = new Pair<>("x", new Integer(3));
//        memory[3] = new Pair<>("ins", "assign x input");
//        memory[4] = new Pair<>("ins", "print y");
//
//        memory[8] = new Pair<>();
//        memory[9] = new Pair<>();
//
//        displayMemory(memory);
//
//



        /* ------------------------ PROGRAM 1 RUN------------------------------------------*/
//        kernel.run(p1);
//
//        kernel.run(p1);
//
//        kernel.run(p1);
//
//        kernel.run(p1);
//
//        kernel.run(p1);
//
//        kernel.run(p1);
//
//        kernel.run(p1);


        /* ------------------------ PROGRAM 2 RUN------------------------------------------*/

////        //semWait userInput
//        kernel.run(p2);
////
////        //assign a input
//        kernel.run(p2);
////
////        //assign b input
//        kernel.run(p2);
////
////        //semSignal userInput
//        kernel.run(p2);
////
////        //semWait file
//        kernel.run(p2);
////
////        //writeFile a b
//        kernel.run(p2);
////
////        //semSignal file
//        kernel.run(p2);

        /* ------------------------ PROGRAM 3 RUN------------------------------------------*/
        kernel.run(p3);

        kernel.run(p3);

        kernel.run(p3);

        kernel.run(p3);

        kernel.run(p3);

        kernel.run(p3);

        kernel.run(p3);

        kernel.run(p3);

        kernel.run(p3);

//        System.out.println(String.format("%16s", "null"));

//        loadProcessIntoMemory();
//        Process p = kernel.createNewProcess("program_1.txt");
//        System.out.println(p.getStartBoundary() + ", " + p.getEndBoundary());
//        displayMemory(memory);
//        kernel.run(p);
//        kernel.run(p);
//        kernel.run(p);
//        kernel.run(p);

//        kernel.fitsInMemory(memory, 4);

//        try {
//            kernel.createNewProcess("program_1.txt");
//        } catch (Exception e) {
//            System.out.println(e.getMessage());
//        }

    }
}




