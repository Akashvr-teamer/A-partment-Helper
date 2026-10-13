package model;

import java.sql.Timestamp;

public class Resident {
    private int residentId;
    private String fullName;
    private String email;
    private String phone;
    private String apartmentNo;
    private String password;
    private String vehicleNo;
    private String parkingSlot;
    private Timestamp createdAt;

    public Resident() {}

    public Resident(int residentId, String fullName, String email, String phone,
                    String apartmentNo, String password, String vehicleNo,
                    String parkingSlot, Timestamp createdAt) {
        this.residentId = residentId;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.apartmentNo = apartmentNo;
        this.password = password;
        this.vehicleNo = vehicleNo;
        this.parkingSlot = parkingSlot;
        this.createdAt = createdAt;
    }

    public int getResidentId() { return residentId; }
    public void setResidentId(int residentId) { this.residentId = residentId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getApartmentNo() { return apartmentNo; }
    public void setApartmentNo(String apartmentNo) { this.apartmentNo = apartmentNo; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getVehicleNo() { return vehicleNo; }
    public void setVehicleNo(String vehicleNo) { this.vehicleNo = vehicleNo; }

    public String getParkingSlot() { return parkingSlot; }
    public void setParkingSlot(String parkingSlot) { this.parkingSlot = parkingSlot; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return apartmentNo + " - " + fullName + " (ID: #" + residentId + ")";
    }
}
