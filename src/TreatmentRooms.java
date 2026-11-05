package src;

public class TreatmentRooms {

    private final Semaphore semaphore = new Semaphore(2);

    public void release() {
        semaphore.release();
    }

    public boolean tryAcquire(long millis) throws InterruptedException {
        return semaphore.tryAcquire(millis);
    }
}
