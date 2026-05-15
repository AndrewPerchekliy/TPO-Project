package zad3;

import java.util.concurrent.Future;

/**
 * Klasa opakowująca Future zadania wraz z informacjami do wyświetlenia
 */
public class TaskWrapper {
    private Future<?> future;
    private String taskName;
    private String description;
    
    public TaskWrapper(Future<?> future, String taskName, String description) {
        this.future = future;
        this.taskName = taskName;
        this.description = description;
    }
    
    public Future<?> getFuture() {
        return future;
    }
    
    public String getTaskName() {
        return taskName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getStatus() {
        if (future.isCancelled()) {
            return "ANULOWANE";
        } else if (future.isDone()) {
            return "GOTOWE";
        } else {
            return "W TRAKCIE";
        }
    }
    
    @Override
    public String toString() {
        return taskName + " [" + getStatus() + "]";
    }
}

