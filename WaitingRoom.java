import java.util.LinkedList;
import java.util.Queue;

public class WaitingRoom {

    private final Semaphore available_seats;
    private final Semaphore waiting_patients;
    private final Semaphore queue_lock;

    private final Queue<Patient> queue = new LinkedList<>();

    public WaitingRoom(int capacity) {
        this.available_seats = new Semaphore(capacity);
        this.waiting_patients = new Semaphore(0);
        this.queue_lock = new Semaphore(1);
    }

    public void enter(Patient p, boolean firstAttempt) throws InterruptedException {
    final String sep = System.lineSeparator();
    String arrived = firstAttempt ? p + " arrived." + sep : "";

    queue_lock.acquire();
    boolean seated = available_seats.tryAcquire(0);          
    if (seated) {
        queue.add(p);
        Status_printer.print_status(arrived + p + " entered the waiting room.");
    } else {
        Status_printer.print_status(arrived + "The waiting room is full." + sep + p + " is waiting outside for an available seat.");
    }
    queue_lock.release();

    if (seated) {
        waiting_patients.release();
        return;
    }

    available_seats.acquire();                              
    queue_lock.acquire();
    queue.add(p);
    Status_printer.print_status(p + " entered the waiting room.");
    queue_lock.release();
    waiting_patients.release();
}

    public Patient take() throws InterruptedException {
    waiting_patients.acquire();
    queue_lock.acquire();
    Patient p = queue.remove();      
    queue_lock.release();
    return p;                    
}

public void releaseSeat(String message) throws InterruptedException {
    queue_lock.acquire();
    Status_printer.print_status(message);
    available_seats.release();
    queue_lock.release();
}

public void closeFor(int doctorCount) throws InterruptedException {
    for (int i = 0; i < doctorCount; i++) {
        queue_lock.acquire();
        queue.add(null);
        queue_lock.release();
        waiting_patients.release();
    }
}   
}