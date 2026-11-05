public class Patient extends Thread {

    private enum Outcome {
        NONE, TREATED, SENT_BACK
    }

    private final int id;
    private final WaitingRoom waitingRoom;

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
        try {
            boolean treated = false;
            boolean firstAttempt = true;
            while (!treated) {
                waitingRoom.enter(this, firstAttempt);
                firstAttempt = false;                 
                treated = waitUntilOutcome();
            }
            Status_printer.print_status(getPatientName() + " left.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public synchronized boolean waitUntilOutcome() throws InterruptedException {
        while (outcome == Outcome.NONE) {
            wait();
        }
        boolean treated = (outcome == Outcome.TREATED);
        outcome = Outcome.NONE;
        return treated;
    }

    public synchronized void signalTreated() {
        outcome = Outcome.TREATED;
        notifyAll();
    }

    public synchronized void signalSentBack() {
        outcome = Outcome.SENT_BACK;
        notifyAll();
    }
}
