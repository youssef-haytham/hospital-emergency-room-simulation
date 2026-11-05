/**
 * A patient in the emergency room. Each patient runs as its own thread.
 *
 * Lifecycle: arrive -> enter waiting room -> wait for the doctor's outcome
 *            -> (treated: leave) or (sent back: try to enter again).
 */
public class Patient extends Thread {

    // The three states of the doctor's answer.
    private enum Outcome { NONE, TREATED, SENT_BACK }

    private final int id;
    private final WaitingRoom waitingRoom;

    // Written by the doctor thread, read by this patient thread.
    // Always accessed inside synchronized(this) methods.
    private Outcome outcome = Outcome.NONE;

    public Patient(int id, WaitingRoom waitingRoom) {
        this.id = id;
        this.waitingRoom = waitingRoom;
    }

    public String getPatientName() {
        return "P" + id;
    }

    @Override
    public String toString() {
        return getPatientName();
    }

    @Override
    public void run() {
        Logger.log(getPatientName() + " arrived.");
        try {
            boolean treated = false;
            while (!treated) {
                // Blocks (inside WaitingRoom) while the room is full.
                waitingRoom.enter(this);
                // Blocks until the doctor reports TREATED or SENT_BACK.
                treated = waitUntilOutcome();
            }
            Logger.log(getPatientName() + " left.");
        } catch (InterruptedException e) {
            // Not expected in normal flow. Restore the flag and stop.
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Called by the patient thread. Returns true if treated, false if sent back.
     * The outcome is reset so the same patient can be assigned again after a retry.
     */
    public synchronized boolean waitUntilOutcome() throws InterruptedException {
        // while (not if): protects against spurious wakeups and early signals.
        while (outcome == Outcome.NONE) {
            wait();
        }
        boolean treated = (outcome == Outcome.TREATED);
        outcome = Outcome.NONE;
        return treated;
    }

    /** Called by the doctor after the treatment finished. */
    public synchronized void signalTreated() {
        outcome = Outcome.TREATED;
        notifyAll();
    }

    /** Called by the doctor when it could not get a treatment room in time. */
    public synchronized void signalSentBack() {
        outcome = Outcome.SENT_BACK;
        notifyAll();
    }
}
