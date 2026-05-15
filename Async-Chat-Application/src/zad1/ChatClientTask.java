/**
 *
 *  @author Percheklii Andrii s33232
 *
 */

package zad1;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class ChatClientTask extends FutureTask<ChatClient> {
    private ChatClient client;

    private ChatClientTask(Callable<ChatClient> callable, ChatClient client) {
        super(callable);
        this.client = client;
    }

    public ChatClient getClient() {
        return client;
    }

    public static ChatClientTask create(ChatClient c, List<String> msgs, int wait) {
        Callable<ChatClient> callable = () -> {
            c.login();
            if (wait != 0) Thread.sleep(wait);
            for (String msg : msgs) {
                c.send(msg);
                if (wait != 0) Thread.sleep(wait);
            }
            c.logout();
            if (wait != 0) Thread.sleep(wait);
            return c;
        };
        return new ChatClientTask(callable, c);
    }
}