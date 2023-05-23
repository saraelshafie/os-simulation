import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Scanner;

public class Process {
    private PCB pcb;
    public Pair<String, Object>[] memory;


    public Process(PCB pcb, Pair[] memory) {
        this.pcb = pcb;
        this.memory = memory;
    }


    public int getProgSize() {
        return this.getEndBoundary() - this.getStartBoundary() + 5;
    }

    public int getID() {
        return pcb.getPid();
    }

    public State getState() {
        return pcb.getState();
    }

    public void setState(State state) {
        pcb.setState(state);
    }

    public int getPC() {
        return pcb.getPC();
    }

    public void setPC(int pc) {
        pcb.setPC(pc);
    }

    public int getEndBoundary() {
        return pcb.getEndBoundary();
    }

    public int getStartBoundary() {
        return pcb.getStartBoundary();
    }

    public void setStartBoundary(int startBoundary) {
        pcb.setStartBoundary(startBoundary);
    }

    public void setEndBoundary(int endBoundary) {
        pcb.setEndBoundary(endBoundary);
    }

    public boolean addVar(String varName, Object value) {
        Pair<String, Object> pair = new Pair<>(varName, value);

        for (int i = this.getStartBoundary(); i < this.getEndBoundary(); i++) {
            if (memory[i].getKey() != null && memory[i].getValue() != null) continue;
            memory[i] = pair;
            return true;
        }

        //if this is reached, no memory location is free
        //don't think we'll need this however
//        System.out.println("ADDRESS SPACE FULL, COULD NOT DEFINE MORE VARIABLES");
        return false;
    }

    public Object getVar(String varName) {
        for (int i = this.getStartBoundary(); i < this.getEndBoundary(); i++) {
            if (memory[i] == null) continue;
            if (memory[i].getKey().equals(varName))
                return memory[i].getValue();
        }
        return null;
    }


    public PCB getPCB() {
        return pcb;
    }

    public String toString() {
        return "PROCESS " + this.getID();
    }

}


