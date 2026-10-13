package model;

import java.sql.Timestamp;

public class Complaint {
    private int complaintId;
    private int residentId;
    private Integer workerId;
    private String title;
    private String description;
    private String category;
    private String status;
    private Timestamp createdAt;
    private Timestamp completedAt;

    // Optional display fields populated from JOINs
    private String residentName;
    private String apartmentNo;
    private String workerName;

    public Complaint() {}

    public Complaint(int complaintId, int residentId, Integer workerId, String title,
                     String description, String category, String status,
                     Timestamp createdAt, Timestamp completedAt) {
        this.complaintId = complaintId;
        this.residentId = residentId;
        this.workerId = workerId;
        this.title = title;
        this.description = description;
        this.category = category;
        this.status = status;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
    }

    public int getComplaintId() { return complaintId; }
    public void setComplaintId(int complaintId) { this.complaintId = complaintId; }

    public int getResidentId() { return residentId; }
    public void setResidentId(int residentId) { this.residentId = residentId; }

    public Integer getWorkerId() { return workerId; }
    public void setWorkerId(Integer workerId) { this.workerId = workerId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getCompletedAt() { return completedAt; }
    public void setCompletedAt(Timestamp completedAt) { this.completedAt = completedAt; }

    public String getResidentName() { return residentName; }
    public void setResidentName(String residentName) { this.residentName = residentName; }

    public String getApartmentNo() { return apartmentNo; }
    public void setApartmentNo(String apartmentNo) { this.apartmentNo = apartmentNo; }

    public String getWorkerName() { return workerName; }
    public void setWorkerName(String workerName) { this.workerName = workerName; }
}
