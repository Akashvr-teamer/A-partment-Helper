package model.dao;

import database.DBConnection;
import model.Complaint;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ComplaintDAO {

    public List<Complaint> getAll() throws SQLException {
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
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<Complaint> getRecent(int limit) throws SQLException {
        List<Complaint> list = new ArrayList<>();
        String sql =
            "SELECT c.complaint_id, c.resident_id, r.full_name AS resident_name, r.apartment_no, " +
            "       c.title, c.category, c.worker_id, COALESCE(w.full_name, 'Unassigned') AS worker_name, " +
            "       c.status, c.created_at, c.completed_at, c.description " +
            "FROM complaints c " +
            "JOIN residents r ON c.resident_id = r.resident_id " +
            "LEFT JOIN workers w ON c.worker_id = w.worker_id " +
            "ORDER BY c.complaint_id DESC LIMIT " + Math.max(1, limit);

        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<Complaint> getByResident(int residentId) throws SQLException {
        List<Complaint> list = new ArrayList<>();
        String sql =
            "SELECT c.complaint_id, c.resident_id, r.full_name AS resident_name, r.apartment_no, " +
            "       c.title, c.category, c.worker_id, COALESCE(w.full_name, 'Unassigned') AS worker_name, " +
            "       c.status, c.created_at, c.completed_at, c.description " +
            "FROM complaints c " +
            "JOIN residents r ON c.resident_id = r.resident_id " +
            "LEFT JOIN workers w ON c.worker_id = w.worker_id " +
            "WHERE c.resident_id = ? " +
            "ORDER BY c.complaint_id DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, residentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public Complaint getById(int complaintId) throws SQLException {
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
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public String getDescription(int complaintId) throws SQLException {
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

    public int insert(int residentId, String title, String category, String description, Integer workerId) throws SQLException {
        String status = (workerId != null && workerId > 0) ? "ASSIGNED" : "PENDING";
        String sql = "INSERT INTO complaints (resident_id, worker_id, title, description, category, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, residentId);
            if (workerId != null && workerId > 0) {
                ps.setInt(2, workerId);
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setString(3, title);
            ps.setString(4, description);
            ps.setString(5, category);
            ps.setString(6, status);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet gk = ps.getGeneratedKeys()) {
                    if (gk.next()) return gk.getInt(1);
                }
            }
        }
        return 0;
    }

    public int insert(Complaint c) throws SQLException {
        int id = insert(c.getResidentId(), c.getTitle(), c.getCategory(), c.getDescription(), c.getWorkerId());
        c.setComplaintId(id);
        return id;
    }

    public boolean assignWorker(int complaintId, int workerId) throws SQLException {
        String sql = "UPDATE complaints SET worker_id = ?, status = 'ASSIGNED' WHERE complaint_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, workerId);
            ps.setInt(2, complaintId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateStatus(int complaintId, String newStatus) throws SQLException {
        String sql = "COMPLETED".equalsIgnoreCase(newStatus)
                ? "UPDATE complaints SET status = 'COMPLETED', completed_at = CURRENT_TIMESTAMP WHERE complaint_id = ?"
                : "UPDATE complaints SET status = ? WHERE complaint_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if ("COMPLETED".equalsIgnoreCase(newStatus)) {
                ps.setInt(1, complaintId);
            } else {
                ps.setString(1, newStatus);
                ps.setInt(2, complaintId);
            }
            return ps.executeUpdate() > 0;
        }
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

    public boolean completeComplaint(int complaintId, int workerId, String completionNote) throws SQLException {
        String sql = "UPDATE complaints SET status = 'COMPLETED', completed_at = CURRENT_TIMESTAMP, " +
                     "description = CASE WHEN ? IS NOT NULL AND ? != '' THEN description || '\n\n[Worker Completion Note]: ' || ? ELSE description END " +
                     "WHERE complaint_id = ? AND worker_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            String note = completionNote != null ? completionNote.trim() : "";
            ps.setString(1, note);
            ps.setString(2, note);
            ps.setString(3, note);
            ps.setInt(4, complaintId);
            ps.setInt(5, workerId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean resetAssignedToWorker(int workerId) throws SQLException {
        String sql = "UPDATE complaints SET worker_id = NULL, status = 'PENDING' " +
                     "WHERE worker_id = ? AND status IN ('ASSIGNED', 'IN_PROGRESS', 'IN PROGRESS')";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, workerId);
            return ps.executeUpdate() >= 0;
        }
    }

    private Complaint mapRow(ResultSet rs) throws SQLException {
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
