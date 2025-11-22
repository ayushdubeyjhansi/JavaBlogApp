import javax.swing.*;
import java.util.Date;

public class AutoRefreshTask extends Thread {
    private volatile boolean running = true;
    private final JLabel statusLabel;

    public AutoRefreshTask(JLabel statusLabel) {
        this.statusLabel = statusLabel;
    }

    @Override
    public void run() {
        while (running) {
            try {
                Thread.sleep(10000); // 10 seconds

                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("Status: Auto-synced at " + new Date().toString());
                });

                // Synchronization example
                synchronized (MemoryStore.posts) {
                    // Thread-safe check could go here
                }

            } catch (InterruptedException e) {
                running = false;
            }
        }
    }

    public void stopTask() {
        running = false;
    }
}