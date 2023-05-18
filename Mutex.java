import java.util.Queue;

public class Mutex {
    private Queue resourceBlocked;
    private int ownerID;

    private Status status;
    private Scheduler scheduler;

    public Mutex(Queue resourceBlocked, Status status, Scheduler scheduler) {
        this.resourceBlocked = resourceBlocked;
        this.status = status;
        this.scheduler = scheduler;
    }

    public Queue getResourceBlocked() {
        return resourceBlocked;
    }

    public void setResourceBlocked(Queue resourceBlocked) {
        this.resourceBlocked = resourceBlocked;
    }

    public int getOwnerID() {
        return ownerID;
    }

    public void setOwnerID(int ownerID) {
        this.ownerID = ownerID;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public void semWait(Process p) {
        if (status == status.ONE) {
            ownerID = p.getID();
            status = status.ZERO;
        } else {
            resourceBlocked.add(p);
            scheduler.getBlocked().add(p);
        }
    }

    public void semSignal(Process p) {
        if (ownerID == p.getID()) {
            if (resourceBlocked.isEmpty())
                status = status.ONE;
            else {
                Process pNew = (Process) resourceBlocked.remove();
                scheduler.getBlocked().remove(pNew);
                scheduler.getReady().add(pNew);
                ownerID = pNew.getID();
            }
        }
    }
}
