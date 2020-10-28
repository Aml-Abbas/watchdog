public class MessageLoggingTracker extends LivenessTracker {
    protected void timeoutAlert(Thread t) {
        System.out.println("Warning! Thread timed out: "+t.toString());
    }
}