package model;

import java.sql.Timestamp;

public class Admin {
    private int adminId;
    private String fullName;
    private String email;
    private String phone;
    private String password;
    private Timestamp createdAt;

    public Admin() {}

    public Admin(int adminId, String fullName, String email, String phone, String password, Timestamp createdAt) {
        this.adminId = adminId;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.createdAt = createdAt;
    }

    public int getAdminId() { return adminId; }
    public void setAdminId(int adminId) { this.adminId = adminId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
