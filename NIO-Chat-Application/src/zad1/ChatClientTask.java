/**
 *
 *  @author Percheklii Andrii s33232
 *
 */

package zad1;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

public class ChatClientTask implements Runnable {
    private ChatClient client;
    private List<String> msgs;
    private int wait;
    private CountDownLatch latch = new CountDownLatch(1);

    private ChatClientTask(ChatClient client, List<String> msgs, int wait) {
        this.client = client;
        this.msgs = msgs;
        this.wait = wait;
    }

    public static ChatClientTask create(ChatClient c, List<String> msgs, int wait) {
        return new ChatClientTask(c, msgs, wait);
    }

    @Override
    public void run() {
        try {
            client.login();
            if (wait != 0) Thread.sleep(wait);
            for (String msg : msgs) {
                client.send(msg);
                if (wait != 0) Thread.sleep(wait);
            }
            client.logout();
            if (wait != 0) Thread.sleep(wait);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            latch.countDown();
        }
    }

    public void get() throws InterruptedException, ExecutionException {
        latch.await();
    }

    public ChatClient getClient() {
        return client;
    }
}