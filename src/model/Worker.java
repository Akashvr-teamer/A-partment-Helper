package model;

import java.sql.Timestamp;

public class Worker {
    private int workerId;
    private String fullName;
    private String email;
    private String phone;
    private String workerType;
    private String password;
    private Timestamp createdAt;

    // Derived statistics for directory and dashboard displays
    private int activeTasks;
    private int completedTasks;

    public Worker() {}

    public Worker(int workerId, String fullName, String email, String phone,
                  String workerType, String password, Timestamp createdAt) {
        this.workerId = workerId;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.workerType = workerType;
        this.password = password;
        this.createdAt = createdAt;
    }

    public int getWorkerId() { return workerId; }
    public void setWorkerId(int workerId) { this.workerId = workerId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getWorkerType() { return workerType; }
    public void setWorkerType(String workerType) { this.workerType = workerType; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public int getActiveTasks() { return activeTasks; }
    public void setActiveTasks(int activeTasks) { this.activeTasks = activeTasks; }

    public int getCompletedTasks() { return completedTasks; }
    public void setCompletedTasks(int completedTasks) { this.completedTasks = completedTasks; }

    @Override
    public String toString() {
        return fullName + " (" + workerType + ") [#" + workerId + "]";
    }
}
