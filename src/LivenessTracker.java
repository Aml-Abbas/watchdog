import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

public abstract class LivenessTracker {
    private Map<Long, Timeout> timeOuts;
    private Queue<Timeout> times;
    private Thread alertThread;

    public LivenessTracker() {
        timeOuts= new HashMap<>();
        times= new PriorityQueue<>();
        alertThread= new AlertThread();
        alertThread.start();
    }
    /** Called by threads to request a timeout after timeout milliseconds unless
     this method is called again. If timeout=0 no timeout will be generated.
     */
    public synchronized void reportActivity(long timeout) {
        Thread t= Thread.currentThread();
        Long now= System.currentTimeMillis();
        Timeout previous= timeOuts.get(t.getId());
        if (previous!=null){
            times.remove(previous);
        }
        if (timeout!=0){
            Timeout e= new Timeout( t,now+timeout);
            times.add(e);
            timeOuts.put(t.getId(),e);
            notifyAll();
        }

    }
    private synchronized Thread awaitTimeout() throws InterruptedException {
    Long now = System.currentTimeMillis();
    while (true){
        Timeout next= times.peek();
        if (next == null) {
            wait();
        }else if (next.timeout> now){
            wait(next.timeout-now);
        }
        now= System.currentTimeMillis();
        next= times.peek();
        if (next.timeout<=now){
            times.remove(next);
            return next.thread;
        }
    }

    }

    /** Automatically called when a timeout occurs for thread t.
     Implement the method in a subclass in order to describe
     what should happen at a timeout event.
     */
    protected abstract void timeoutAlert(Thread t);

    private class Timeout implements Comparable<Timeout> {
        final Thread thread;
        final long timeout;

        public Timeout(Thread thread, long timeout) {
            this.thread = thread;
            this.timeout = timeout;
        }
        public int compareTo(Timeout other) {
            return Long.compare(this.timeout, other.timeout);
        }
    }
    private class AlertThread extends Thread {
        public void run() {
            while(true) {
                try {
                    LivenessTracker.this.timeoutAlert(LivenessTracker.this.awaitTimeout());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
