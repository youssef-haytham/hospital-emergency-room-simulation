import java.util.LinkedList;
import java.util.Queue;

public class WaitingRoom {
    private final int capacity;
    private final Queue<Patient> queue = new LinkedList<>();

    private final Semaphore empty;   // free chairs
    private final Semaphore full;    // patients sitting
    private final Semaphore mutex;   // protects the queue

    public WaitingRoom(int capacity) {
        this.capacity = capacity;
        this.empty = new Semaphore(capacity);
        this.full  = new Semaphore(0);
        this.mutex = new Semaphore(1);
    }


    public void enter(Patient p) throws InterruptedException {
        if (!empty.tryAcquire(0)) {
            Logger.log("The waiting room is full." + System.lineSeparator()
                    + p + " is waiting outside for an available seat.");
            empty.acquire();
        }
        mutex.acquire();
        queue.add(p);
        assert queue.size() <= capacity;
        Logger.log(p + " entered the waiting room.");
        mutex.release();
        full.release();
    }


    public Patient take() throws InterruptedException {
        full.acquire();
        mutex.acquire();
        Patient p = queue.remove();
        mutex.release();
        empty.release();
        return p;
    }

    public void closeFor(int doctorCount) throws InterruptedException {
    for (int i = 0; i < doctorCount; i++) {
        // Adds null markers so each doctor thread unblocks and exits loop after all patients leave
        queue.add(null);
        full.release();
    }
}
}