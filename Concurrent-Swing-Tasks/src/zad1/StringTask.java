package zad1;

public class StringTask implements Runnable {
  private String text;
  private int count;
  private volatile String result;
  private volatile TaskState state;
  private Thread thread;

  public StringTask(String text, int count) {
    this.text = text;
    this.count = count;
    this.result = "";
    this.state = TaskState.CREATED;
    this.thread = null;
  }

  public String getResult() {
    return result;
  }

  public TaskState getState() {
    return state;
  }

  public void start() {
    if (state == TaskState.CREATED) {
      thread = new Thread(this);
      thread.start();
    }
  }

  public void abort() {
    if (thread != null && thread.isAlive()) {
      thread.interrupt();
      if (state == TaskState.RUNNING) {
        state = TaskState.ABORTED;
      }
    }
  }

  public boolean isDone() {
    return state == TaskState.READY || state == TaskState.ABORTED;
  }

  @Override
  public void run() {
    if (state != TaskState.CREATED) {
      return;
    }
    state = TaskState.RUNNING;

    try {
      for (int i = 0; i < count; i++) {
        if (Thread.currentThread().isInterrupted()) {
          state = TaskState.ABORTED;
          return;
        }
        result = result + text;
      }
      
      if (state == TaskState.RUNNING) {
        state = TaskState.READY;
      }
    } catch (Exception e) {
      state = TaskState.ABORTED;
    }
  }
}

