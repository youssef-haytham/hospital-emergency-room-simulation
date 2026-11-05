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

    public void enter(Patient p) throws InterruptedException { // redundant safety check ?

        if (available_seats.tryAcquire(0) == false) {
            Status_printer.print_status("The waiting room is full." + System.lineSeparator() + p + " is waiting outside for an available seat.");
            
            available_seats.acquire(); // decrement available_seats and block current thread and force it to wait
        }

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

        available_seats.release();

        return p;
    }

    public void closeFor(int doctorCount) throws InterruptedException {
        for (int i = 0; i < doctorCount; i++) {
            // Adds null markers so each doctor thread unblocks and exits loop after all patients leave
            queue.add(null);
            waiting_patients.release();
        }
    }
}