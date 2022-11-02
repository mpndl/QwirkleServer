package za.nmu.wrpv.qwirkle;

public class CountdownThread extends Thread{
    private static final int timeoutMills = GamesHandler.timeout;
    private static int currentSeconds = timeoutMills/1000;
    private final Runnable runnable;

    public CountdownThread(Runnable runnable) {
        this.runnable = runnable;
    }
    @Override
    public void run() {
        try {
            do {
                Thread.sleep(1000);
                --currentSeconds;
            } while (currentSeconds > 0);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            currentSeconds = timeoutMills / 1000;
            runnable.run();
        }
    }

    public int getTimeoutMills() {
        return timeoutMills;
    }

    public static int getCurrentSeconds() {
        return currentSeconds;
    }
}
