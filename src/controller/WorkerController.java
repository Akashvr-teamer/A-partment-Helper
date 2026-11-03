package controller;

import database.DBConnection;
import model.Complaint;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class WorkerController {

    public List<Complaint> getAllComplaints() throws SQLException {
        List<Complaint> list = new ArrayList<>();
        String sql =
            "SELECT c.complaint_id, c.resident_id, r.full_name AS resident_name, r.apartment_no, " +
            "       c.title, c.category, c.worker_id, COALESCE(w.full_name, 'Unassigned') AS worker_name, " +
            "       c.status, c.created_at, c.completed_at, c.description " +
            "FROM complaints c " +
            "JOIN residents r ON c.resident_id = r.resident_id " +
            "LEFT JOIN workers w ON c.worker_id = w.worker_id " +
            "ORDER BY c.complaint_id DESC";

        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapComplaintRow(rs));
            }
        }
        return list;
    }

    public String getComplaintDescription(int complaintId) throws SQLException {
        String sql = "SELECT description FROM complaints WHERE complaint_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, complaintId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("description");
                }
            }
        }
        return null;
    }

    public boolean claimComplaint(int complaintId, int workerId) throws SQLException {
        String sql = "UPDATE complaints SET worker_id = ?, status = 'ASSIGNED' WHERE complaint_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, workerId);
            ps.setInt(2, complaintId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean markInProgress(int complaintId) throws SQLException {
        String sql = "UPDATE complaints SET status = 'IN_PROGRESS' WHERE complaint_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, complaintId);
            return ps.executeUpdate() > 0;
        }
    }

    public Complaint getComplaintById(int complaintId) throws SQLException {
        String sql =
            "SELECT c.complaint_id, c.resident_id, r.full_name AS resident_name, r.apartment_no, " +
            "       c.title, c.category, c.worker_id, COALESCE(w.full_name, 'Unassigned') AS worker_name, " +
            "       c.status, c.created_at, c.completed_at, c.description " +
            "FROM complaints c " +
            "JOIN residents r ON c.resident_id = r.resident_id " +
            "LEFT JOIN workers w ON c.worker_id = w.worker_id " +
            "WHERE c.complaint_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, complaintId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapComplaintRow(rs);
                }
            }
        }
        return null;
    }

    public boolean completeComplaint(int complaintId, int workerId, String note, Double repairFee) throws SQLException {
        Complaint c = getComplaintById(complaintId);
        if (c == null) return false;
        int residentId = c.getResidentId();
        String title = c.getTitle();
        Date dueDate = Date.valueOf(java.time.LocalDate.now().plusDays(7));
        return completeComplaint(complaintId, workerId, residentId, title, note, repairFee, dueDate);
    }

    public boolean completeComplaint(int complaintId, int workerId, int residentId,
                                     String complaintTitle, String note,
                                     Double repairFee, Date dueDate) throws SQLException {
        String sql = "UPDATE complaints SET status = 'COMPLETED', completed_at = CURRENT_TIMESTAMP, " +
                     "description = CASE WHEN ? IS NOT NULL AND ? != '' THEN description || '\n\n[Worker Completion Note]: ' || ? ELSE description END " +
                     "WHERE complaint_id = ? AND worker_id = ?";
        boolean ok;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            String cleanNote = note != null ? note.trim() : "";
            ps.setString(1, cleanNote);
            ps.setString(2, cleanNote);
            ps.setString(3, cleanNote);
            ps.setInt(4, complaintId);
            ps.setInt(5, workerId);
            ok = ps.executeUpdate() > 0;
        }

        if (ok && repairFee != null && repairFee > 0 && residentId > 0 && dueDate != null) {
            insertWorkerCompletionBill(residentId, complaintId, complaintTitle, repairFee, dueDate);
        }
        return ok;
    }

    private int insertWorkerCompletionBill(int residentId, int complaintId, String complaintTitle, double amount, Date dueDate) throws SQLException {
        String billTitle = "Repair Fee: " + complaintTitle;
        if (billTitle.length() > 255) {
            billTitle = billTitle.substring(0, 255);
        }

        String sql = "INSERT INTO payments (resident_id, month_year, amount, due_date, status, payment_type, complaint_id) " +
                     "VALUES (?, ?, ?, ?, 'PENDING', 'MAINTENANCE_WORK', ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, residentId);
            ps.setString(2, billTitle);
            ps.setDouble(3, amount);
            ps.setDate(4, dueDate);
            ps.setInt(5, complaintId);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet gk = ps.getGeneratedKeys()) {
                    if (gk.next()) return gk.getInt(1);
                }
            }
        }
        return 0;
    }

    private Complaint mapComplaintRow(ResultSet rs) throws SQLException {
        Complaint c = new Complaint();
        c.setComplaintId(rs.getInt("complaint_id"));
        c.setResidentId(rs.getInt("resident_id"));
        int wId = rs.getInt("worker_id");
        c.setWorkerId(rs.wasNull() ? null : wId);
        c.setTitle(rs.getString("title"));
        c.setDescription(rs.getString("description"));
        c.setCategory(rs.getString("category"));
        c.setStatus(rs.getString("status"));
        c.setCreatedAt(rs.getTimestamp("created_at"));
        c.setCompletedAt(rs.getTimestamp("completed_at"));

        try {
            c.setResidentName(rs.getString("resident_name"));
            c.setApartmentNo(rs.getString("apartment_no"));
            c.setWorkerName(rs.getString("worker_name"));
        } catch (SQLException ignored) {}

        return c;
    }
}
