package model;

public class Parking {
    private int parkingId;
    private String slotNumber;
    private String parkingType;
    private String status;
    private Integer residentId;
    private Integer visitorId;

    public Parking() {}

    public Parking(int parkingId, String slotNumber, String parkingType, String status, Integer residentId, Integer visitorId) {
        this.parkingId = parkingId;
        this.slotNumber = slotNumber;
        this.parkingType = parkingType;
        this.status = status;
        this.residentId = residentId;
        this.visitorId = visitorId;
    }

    public int getParkingId() { return parkingId; }
    public void setParkingId(int parkingId) { this.parkingId = parkingId; }

    public String getSlotNumber() { return slotNumber; }
    public void setSlotNumber(String slotNumber) { this.slotNumber = slotNumber; }

    public String getParkingType() { return parkingType; }
    public void setParkingType(String parkingType) { this.parkingType = parkingType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getResidentId() { return residentId; }
    public void setResidentId(Integer residentId) { this.residentId = residentId; }

    public Integer getVisitorId() { return visitorId; }
    public void setVisitorId(Integer visitorId) { this.visitorId = visitorId; }
}
