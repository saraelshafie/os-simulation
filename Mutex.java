import java.util.Queue;

public class Mutex {
    Queue blocked;
    int ownerID;
    Status status;

    public void semWait(Process p){
        if(status==status.ONE)
            ownerID = p.getID();
        else{
            blocked.add(p);
            status = status.ZERO;
        }
    }

    public void semSignal(Process p){
        if(ownerID==p.getID() && blocked.isEmpty())



    }

}
