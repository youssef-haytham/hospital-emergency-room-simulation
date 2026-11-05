public class Semaphore {

    private int permits;
    public Semaphore(int initial) {
        this.permits = initial;
    }
    
    public synchronized void release() {
    permits++;
    notify();
    }

    public synchronized void acquire() throws InterruptedException {
        while (permits <= 0) {   
            wait();                
        }
        permits--;
    }

    public synchronized boolean tryAcquire(long millis) throws InterruptedException  {
    long deadline = System.currentTimeMillis() + millis;
        while (permits <= 0) {
            long remaining = deadline - System.currentTimeMillis();
            if (remaining <= 0) return false;
            wait(remaining);
        }
        permits--;
        return true;
    }
}
