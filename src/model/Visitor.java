package model;

import java.sql.Timestamp;

public class Visitor {
    private int visitorId;
    private int residentId;
    private String visitorName;
    private String phone;
    private boolean hasVehicle;
    private String vehicleNo;
    private String accessHash;
    private String exitCode;
    private Timestamp validFrom;
    private Timestamp validUntil;
    private Timestamp entryTime;
    private Timestamp exitTime;
    private String status;
    private String parkingSlot;
    private Timestamp createdAt;

    // Optional display fields populated from JOINs
    private String residentName;
    private String apartmentNo;

    public Visitor() {}

    public Visitor(int visitorId, int residentId, String visitorName, String phone,
                   boolean hasVehicle, String vehicleNo, String accessHash,
                   String exitCode, Timestamp validFrom, Timestamp validUntil,
                   Timestamp entryTime, Timestamp exitTime, String status,
                   String parkingSlot, Timestamp createdAt) {
        this.visitorId = visitorId;
        this.residentId = residentId;
        this.visitorName = visitorName;
        this.phone = phone;
        this.hasVehicle = hasVehicle;
        this.vehicleNo = vehicleNo;
        this.accessHash = accessHash;
        this.exitCode = exitCode;
        this.validFrom = validFrom;
        this.validUntil = validUntil;
        this.entryTime = entryTime;
        this.exitTime = exitTime;
        this.status = status;
        this.parkingSlot = parkingSlot;
        this.createdAt = createdAt;
    }

    public int getVisitorId() { return visitorId; }
    public void setVisitorId(int visitorId) { this.visitorId = visitorId; }

    public int getResidentId() { return residentId; }
    public void setResidentId(int residentId) { this.residentId = residentId; }

    public String getVisitorName() { return visitorName; }
    public void setVisitorName(String visitorName) { this.visitorName = visitorName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public boolean isHasVehicle() { return hasVehicle; }
    public void setHasVehicle(boolean hasVehicle) { this.hasVehicle = hasVehicle; }

    public String getVehicleNo() { return vehicleNo; }
    public void setVehicleNo(String vehicleNo) { this.vehicleNo = vehicleNo; }

    public String getAccessHash() { return accessHash; }
    public void setAccessHash(String accessHash) { this.accessHash = accessHash; }

    public String getExitCode() { return exitCode; }
    public void setExitCode(String exitCode) { this.exitCode = exitCode; }

    public Timestamp getValidFrom() { return validFrom; }
    public void setValidFrom(Timestamp validFrom) { this.validFrom = validFrom; }

    public Timestamp getValidUntil() { return validUntil; }
    public void setValidUntil(Timestamp validUntil) { this.validUntil = validUntil; }

    public Timestamp getEntryTime() { return entryTime; }
    public void setEntryTime(Timestamp entryTime) { this.entryTime = entryTime; }

    public Timestamp getExitTime() { return exitTime; }
    public void setExitTime(Timestamp exitTime) { this.exitTime = exitTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getParkingSlot() { return parkingSlot; }
    public void setParkingSlot(String parkingSlot) { this.parkingSlot = parkingSlot; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getResidentName() { return residentName; }
    public void setResidentName(String residentName) { this.residentName = residentName; }

    public String getApartmentNo() { return apartmentNo; }
    public void setApartmentNo(String apartmentNo) { this.apartmentNo = apartmentNo; }
}
