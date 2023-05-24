import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;

public class Kernel {

    private final Mutex fileAccess;
    private final Mutex input;
    private final Mutex printer;
    private final Scheduler scheduler;

    private Object input1 = null;
    private Object input2 = null;
    private Object input3 = null;

    private SystemCalls systemCalls;


    private Pair<String, Object>[] memory;
    public final String DISK_PATH = "disk.txt";

    public Kernel(int quantum) {
        this.scheduler = new Scheduler(this);
        this.fileAccess = new Mutex(this.scheduler);
        this.input = new Mutex(this.scheduler);
        this.printer = new Mutex(this.scheduler);
        this.memory = new Pair[40];
        this.systemCalls = new SystemCalls(this.memory);
    }

    public Pair<String, Object>[] getMemory() {
        return memory;
    }

    public Object getInputVar(int PID) {
        switch (PID) {
            case 1:
                return input1;
            case 2:
                return input2;
            case 3:
                return input3;
            default:
                return null;
        }

    }

    public void executeInstruction(Process process, String instruction) throws IOException {

        String[] ins = instruction.split(" ");

        if (ins[0].equals("assign")) {
            Object toAssign;
            String varName = ins[1];

            toAssign = getInputVar(process.getID());
            if(toAssign == null){
                toAssign = ins[2];
            }
            switch (process.getID()) {
                case 1: input1 = null;
                case 2: input2 = null;
                case 3: input3 = null;
            }

            try {
                toAssign = Integer.parseInt((String) toAssign);
            } catch (Exception e) {
            }

            // write (varName: toAssign) in process address space
//            process.addVar(varName, toAssign);
            systemCalls.writeToMem(varName, toAssign , process);



        }else if (ins[0].equals("input")) {
            System.out.println("Please enter a value: ");
            if(process.getID() == 1)
                input1 = systemCalls.takeInput();
            else if (process.getID() == 2)
                input2 = systemCalls.takeInput();
            else
                input3 = systemCalls.takeInput();

        } else if (ins[0].equals("readFile")) {

            String fileName = (String) systemCalls.readFromMem(ins[1] , process);

            if(process.getID() == 1)
                input1 = systemCalls.readFromDisk(fileName);
            else if (process.getID() == 2)
                input2 = systemCalls.readFromDisk(fileName);
            else
                input3 = systemCalls.readFromDisk(fileName);

        } else if (ins[0].equals("print")) {
            // fetch x from process address space
            Object data;

            if (ins[1].equals("readFile")) {
                String fileName = (String) systemCalls.readFromMem(ins[3] , process); //leh feeh readFile gowa print???

                data = systemCalls.readFromDisk(fileName);

            } else data = systemCalls.readFromMem(ins[1] , process);

            systemCalls.printData(data);

        } else if (ins[0].equals("writeFile")) {
            String fileName = (String) systemCalls.readFromMem(ins[1] , process);
            Object data = systemCalls.readFromMem(ins[2] , process);

            System.out.println("fileName: " + fileName);
            System.out.println("data: " + data);

            systemCalls.writeToDisk(fileName, data);

        } else if (ins[0].equals("printFromTo")) {
            int x = (int) systemCalls.readFromMem(ins[1] , process);
            int y = (int) systemCalls.readFromMem(ins[2] , process);

            for (int i = x; i <= y; i++) {
                systemCalls.printData(i);
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
        System.out.println("EXEC INSTRUCTION: " + ins);
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
                pairSize = p.getKey().length() + p.getValue().toString().length() + 4;
            }
            int numSpaces = (length - pairSize) / 2;
            String spaces = getSpaces(numSpaces);

            System.out.print("|" + spaces);
            System.out.print(p == null ? "" : (p.getKey() == null && p.getValue() == null) ? "reserved" : p);
            System.out.println(spaces + (pairSize % 2 == 0 ? "" : " ") + "|");

            if (i == memory.length - 1) System.out.println(getSpaces(5) + "+------------------------------+");
            else System.out.println(getSpaces(5) + "--------------------------------");
        }
    }

    public static void displayDisk(ArrayList<Pair<String, Object>> disk) {

        System.out.println(getSpaces(5) + "+------------------------------+");

        int length = 30;

        for (int i = 0; i < disk.size(); i++) {

            Pair<String, Object> p = disk.get(i);

            String strMemAddr = i + "";
            String addrWhitespc = getSpaces(4 - strMemAddr.length());

            System.out.print(i + ":" + addrWhitespc);

            int pairSize;
            if (p == null) {
                pairSize = 0;
            } else if (p.getKey() == null && p.getValue() == null) {
                pairSize = 8;       // reserved
            } else {
                pairSize = p.getKey().length() + p.getValue().toString().length() + 4;
            }
            int numSpaces = (length - pairSize) / 2;
            String spaces = getSpaces(numSpaces);

            System.out.print("|" + spaces);
            System.out.print(p == null ? "" : (p.getKey() == null && p.getValue() == null) ? "reserved" : p);
            System.out.println(spaces + (pairSize % 2 == 0 ? "" : " ") + "|");

            if (i == disk.size() - 1) System.out.println(getSpaces(5) + "+------------------------------+");
            else System.out.println(getSpaces(5) + "--------------------------------");
        }
    }


    public Pair<Integer, Integer> fitsInMemory(int progSize) throws IOException {

        int empty = 0;
        int startIdx = 0;

        for (int i = 0; i < memory.length; i++) {
            Pair<String, Object> p = memory[i];

            if (memory[i] == null) {
                empty++;
                if (empty == 1) startIdx = i;
            } else empty = 0;

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
        while ((line = br.readLine()) != null){
            progSize++;
            String[] lineSplit = line.split(" ");
            if((lineSplit[0].equals("assign") && (lineSplit[2].equals("input") || lineSplit[2].equals("readFile")) )
                || (lineSplit[0].equals("print") && lineSplit[1].equals("readFile")))
                progSize++;

        }
        br.close();

        //Plus 3 to accommodate for 3 locations for future variables
        Pair<Integer, Integer> range = fitsInMemory(progSize + 8);

        if (range == null) range = freeSpaceInMemory(progSize + 8);


        PCB pcb = new PCB(range.getKey() + 5, range.getKey(), range.getValue());

        Process process = new Process(pcb, memory);

        br = new BufferedReader(new FileReader(fileName));

        memory[process.getStartBoundary()] = new Pair<>("PID", process.getID());
        memory[process.getStartBoundary() + 1] = new Pair<>("State", process.getState());
        memory[process.getStartBoundary() + 2] = new Pair<>("PC", process.getPC());
        memory[process.getStartBoundary() + 3] = new Pair<>("StartBoundary", process.getStartBoundary());
        memory[process.getStartBoundary() + 4] = new Pair<>("EndBoundary", process.getEndBoundary());


        for (int i = pcb.getStartBoundary() + 5; (line = br.readLine()) != null; i++) {
            String[] lineSplit = line.split(" ");

            if (lineSplit[0].equals("assign")) {
                if (lineSplit[lineSplit.length - 1].equals("input")){
                    memory[i] = new Pair<>("ins", lineSplit[2]);
                    i++;
                    line = lineSplit[0] + " " + lineSplit[1];
                } else if(lineSplit[2].equals("readFile")) {
                    memory[i] = new Pair<>("ins", lineSplit[2] + " " + lineSplit[3]);
                    i++;
                    line = lineSplit[0] + " " + lineSplit[1];
                }

            } else if (lineSplit[0].equals("print") && lineSplit[1].equals("readFile")) {
                memory[i] = new Pair<>("ins", lineSplit[2] + " " + lineSplit[3]);
                i++;
                line = lineSplit[0];
            }

            Pair<String, Object> pair = new Pair<>("ins", line);
            memory[i] = pair;


        }

        //Reserve places for future variable insertions
        for (int j = pcb.getEndBoundary(); j > pcb.getEndBoundary() - 3; j--)
            memory[j] = new Pair<>();

        br.close();

        scheduler.getReady().add(process);
        process.setState(State.READY);


        return process;
    }

    public Pair<Integer, Integer> freeSpaceInMemory(int progSize) throws IOException {

        ArrayList<Pair<String, Object>> onMemory = new ArrayList<>();

        for (int i = 0; i < memory.length; i++) {

            if (memory[i] == null) continue;

            int pid = (int) memory[i].getValue();
            State state;
            int pc;
            int start = (int) memory[i + 3].getValue();
            int end = (int) memory[i + 4].getValue();

            if (scheduler.getRunning() != null && scheduler.getRunning().getID() == pid) {  //can't swap a running process
                i = end;
                continue;
            }


            for (int j = start; j <= end; j++) {
                onMemory.add(memory[j]);
                memory[j] = null;

            }

            Pair<Integer, Integer> range = fitsInMemory(progSize);

            if (range == null) {
                int c = 0;
                for (int j = start; j <= end; j++)
                    memory[j] = onMemory.remove(c);

                i = end;
                continue;
            } else {
                //transfer to disk
                PrintWriter pw = new PrintWriter(new FileOutputStream(DISK_PATH, true));

                for (Pair<String, Object> pair : onMemory) {
                    String data = pair.getKey() + "," + pair.getValue();
                    pw.append(data.toString());
                    pw.append("\n");
                }
                pw.close();

                LinkedList<Process> allProcesses = new LinkedList<>();
                allProcesses.addAll(scheduler.getBlocked());
                allProcesses.addAll(scheduler.getReady());
                for (Process p : allProcesses) {
                    if (p.getID() == pid) p.setOnDisk(true);
                }


                return range;
            }
        }

        return null;
    }


    public void loadFromDiskToMemory(ArrayList<Pair<String, Object>> dataOnDisk, Pair<Integer, Integer> range) {

//        System.out.println("IN LOAD FROM DISK");
        System.out.println("RANGE: " + range.getKey() + ", " + range.getValue());
        int memoryIdx = range.getKey();
        int oldStartBoundary = 0;

        for (Pair<String, Object> pair : dataOnDisk) {
//            System.out.println("load print");
//            System.out.println(pair);
//            System.out.println("START INDEX" + memoryIdx);


            if (!pair.isReserved()) {
                if (pair.getKey().equals("StartBoundary")) {
                    oldStartBoundary = (int) pair.getValue();
                    pair.setValue(range.getKey());
                }
                if (pair.getKey().equals("EndBoundary")) pair.setValue(range.getValue());
                if (pair.getKey().equals("State")) pair.setValue(State.RUNNING);
                if (pair.getKey().equals("PC")) {
                    System.out.println("OLD START: " + oldStartBoundary);
                    System.out.println("OLD PC: " + pair.getValue());
                    pair.setValue(range.getKey() + ((int) pair.getValue() - oldStartBoundary));
                    System.out.println("NEW PC: " + pair.getValue());
                }

            }

            memory[memoryIdx] = pair;
            memoryIdx++;
        }
    }


    public ArrayList<Pair<String, Object>> readDisk() throws IOException {

        ArrayList<Pair<String, Object>> list = new ArrayList<>();

        BufferedReader br = new BufferedReader(new FileReader(DISK_PATH));
        String line;

        while ((line = br.readLine()) != null) {
            String[] splitLine = line.split(",");

            String id = splitLine[0];
            String data = splitLine[1];
            Object object;

            if (id.equals("null")) {
                id = null;
                object = null;
            } else if (id.equals("State")) {
                object = switch (data) {
                    case "RUNNING" -> State.RUNNING;
                    case "BLOCKED" -> State.BLOCKED;
                    case "FINISHED" -> State.FINISHED;
                    default -> State.READY;
                };
            } else {
                try {
                    object = Integer.parseInt(data);
                } catch (Exception e) {
                    object = data;
                }
            }

            Pair<String, Object> pair = new Pair<>(id, object);
            list.add(pair);
        }

        br.close();

        System.out.println("READ DISK: " + list);

        return list;
    }

    public void eraseDisk() throws IOException {
        FileWriter fileWriter = new FileWriter(DISK_PATH, false);
        fileWriter.close();
    }


    public static void main(String[] args) throws Exception {
//        Pair<String, Object>[] memory = new Pair[40];
        Kernel kernel = new Kernel(2);
//        kernel.memory = memory;



//        kernel.eraseDisk();
//        State state = State.READY;
//        System.out.println(state);
//        kernel.readDisk();

//        Path path = Paths.get("disk.txt");
//        String content = Files.readString(path);
//        String[] separated = content.split("%%%%%%%%%%%%%");
//        String[] firstProc = separated[1].split("\\n");
//
//        String test = "pid 5\nstart 10\nend 20";
//        String[] splitTest = test.split("\n");
//
//
//        System.out.println(Arrays.toString(splitTest));


//        Process p1 = kernel.createNewProcess("Program_1.txt");
//
//        Process p2 = kernel.createNewProcess("Program_2.txt");
//
//        Process p3 = kernel.createNewProcess("Program_3.txt");


//        displayDisk(kernel.hardDisk);
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
//        kernel.run(p3);
//
//        kernel.run(p3);
//
//        kernel.run(p3);
//
//        kernel.run(p3);
//
//        kernel.run(p3);
//
//        kernel.run(p3);
//
//        kernel.run(p3);

//        kernel.run(p3);
//
//        kernel.run(p3);

//        displayMemory(memory);
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




