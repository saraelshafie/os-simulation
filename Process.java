public class Process {
    private PCB pcb;


    public int getID(){
        return pcb.getPid();
    }

    public State getState(){
        return pcb.getState();
    }
    public void setState(State state){
        pcb.setState(state);
    }

    public int getPC(){
        return pcb.getPC();
    }

    public int getEndBoundary() {
        return pcb.getEndBoundary();
    }

    public int getStartBoundary() {
        return pcb.getStartBoundary();
    }

    public void execute(String instruction){

    }
}
