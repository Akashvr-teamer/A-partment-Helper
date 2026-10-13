package model;

import java.sql.Date;

public class Payment {
    private int paymentId;
    private int residentId;
    private String monthYear;
    private double amount;
    private Date dueDate;
    private Date paymentDate;
    private String status;
    private String paymentMethod;
    private String paymentType;
    private Integer complaintId;

    // Optional display fields populated from JOINs
    private String residentName;
    private String apartmentNo;

    public Payment() {}

    public Payment(int paymentId, int residentId, String monthYear, double amount,
                   Date dueDate, Date paymentDate, String status,
                   String paymentMethod, String paymentType, Integer complaintId) {
        this.paymentId = paymentId;
        this.residentId = residentId;
        this.monthYear = monthYear;
        this.amount = amount;
        this.dueDate = dueDate;
        this.paymentDate = paymentDate;
        this.status = status;
        this.paymentMethod = paymentMethod;
        this.paymentType = paymentType;
        this.complaintId = complaintId;
    }

    public int getPaymentId() { return paymentId; }
    public void setPaymentId(int paymentId) { this.paymentId = paymentId; }

    public int getResidentId() { return residentId; }
    public void setResidentId(int residentId) { this.residentId = residentId; }

    public String getMonthYear() { return monthYear; }
    public void setMonthYear(String monthYear) { this.monthYear = monthYear; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public Date getDueDate() { return dueDate; }
    public void setDueDate(Date dueDate) { this.dueDate = dueDate; }

    public Date getPaymentDate() { return paymentDate; }
    public void setPaymentDate(Date paymentDate) { this.paymentDate = paymentDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getPaymentType() { return paymentType; }
    public void setPaymentType(String paymentType) { this.paymentType = paymentType; }

    public Integer getComplaintId() { return complaintId; }
    public void setComplaintId(Integer complaintId) { this.complaintId = complaintId; }

    public String getResidentName() { return residentName; }
    public void setResidentName(String residentName) { this.residentName = residentName; }

    public String getApartmentNo() { return apartmentNo; }
    public void setApartmentNo(String apartmentNo) { this.apartmentNo = apartmentNo; }
}
