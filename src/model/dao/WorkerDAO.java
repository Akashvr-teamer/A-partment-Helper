package model.dao;

import database.DBConnection;
import model.Worker;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class WorkerDAO {

    public Worker authenticate(String email, String password) throws SQLException {
        String sql = "SELECT * FROM workers WHERE lower(email) = lower(?) AND password = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email.trim());
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public Worker getById(int workerId) throws SQLException {
        String sql = "SELECT * FROM workers WHERE worker_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, workerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public List<Worker> getAll() throws SQLException {
        List<Worker> list = new ArrayList<>();
        String sql = "SELECT * FROM workers ORDER BY full_name ASC";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<Worker> getAllWithWorkload() throws SQLException {
        List<Worker> list = new ArrayList<>();
        String sql =
            "SELECT w.worker_id, w.full_name, w.worker_type, w.email, w.phone, w.password, w.created_at, " +
            "       COUNT(CASE WHEN c.status IN ('ASSIGNED', 'IN_PROGRESS', 'IN PROGRESS') THEN 1 END) AS active_cnt, " +
            "       COUNT(CASE WHEN c.status IN ('COMPLETED', 'RESOLVED') THEN 1 END) AS resolved_cnt " +
            "FROM workers w " +
            "LEFT JOIN complaints c ON w.worker_id = c.worker_id " +
            "GROUP BY w.worker_id, w.full_name, w.worker_type, w.email, w.phone, w.password, w.created_at " +
            "ORDER BY w.worker_id ASC";

        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Worker w = mapRow(rs);
                w.setActiveTasks(rs.getInt("active_cnt"));
                w.setCompletedTasks(rs.getInt("resolved_cnt"));
                list.add(w);
            }
        }
        return list;
    }

    public List<String> getDistinctWorkerTypes() throws SQLException {
        List<String> types = new ArrayList<>();
        String sql = "SELECT DISTINCT worker_type FROM workers WHERE worker_type IS NOT NULL AND worker_type != '' ORDER BY worker_type";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                types.add(rs.getString(1));
            }
        }
        return types;
    }

    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM workers";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    public int insert(Worker w) throws SQLException {
        String sql = "INSERT INTO workers (full_name, email, phone, worker_type, password) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, w.getFullName());
            ps.setString(2, w.getEmail().trim().toLowerCase());
            ps.setString(3, w.getPhone());
            ps.setString(4, w.getWorkerType());
            ps.setString(5, w.getPassword());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet gk = ps.getGeneratedKeys()) {
                    if (gk.next()) {
                        int genId = gk.getInt(1);
                        w.setWorkerId(genId);
                        return genId;
                    }
                }
            }
        }
        return 0;
    }

    public boolean update(Worker w, boolean updatePassword) throws SQLException {
        String sql = updatePassword
                ? "UPDATE workers SET full_name = ?, phone = ?, worker_type = ?, password = ? WHERE worker_id = ?"
                : "UPDATE workers SET full_name = ?, phone = ?, worker_type = ? WHERE worker_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, w.getFullName());
            ps.setString(2, w.getPhone());
            ps.setString(3, w.getWorkerType());
            if (updatePassword) {
                ps.setString(4, w.getPassword());
                ps.setInt(5, w.getWorkerId());
            } else {
                ps.setInt(4, w.getWorkerId());
            }
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int workerId) throws SQLException {
        String sql = "DELETE FROM workers WHERE worker_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, workerId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Smart Auto-Assignment Algorithm:
     * 1. Look for workers with 0 active tasks (unassigned).
     * 2. ONLY look for assigned workers if NO workers are unassigned.
     */
    public Integer findBestWorkerForCategory(String category) throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) return null;

            // Step 1: Look for an unassigned worker (0 active complaints in PENDING, ASSIGNED, IN_PROGRESS)
            String unassignedSql =
                "SELECT w.worker_id " +
                "FROM workers w " +
                "WHERE NOT EXISTS (" +
                "    SELECT 1 FROM complaints c " +
                "    WHERE c.worker_id = w.worker_id " +
                "    AND c.status IN ('PENDING', 'ASSIGNED', 'IN_PROGRESS', 'IN PROGRESS')" +
                ") " +
                "ORDER BY CASE WHEN lower(w.worker_type) = lower(?) THEN 0 ELSE 1 END, w.worker_id ASC " +
                "LIMIT 1";

            try (PreparedStatement ps = con.prepareStatement(unassignedSql)) {
                ps.setString(1, category != null ? category : "");
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("worker_id");
                    }
                }
            }

            // Step 2: ONLY IF NO workers are unassigned, look among already-assigned workers (least busy)
            String busySql =
                "SELECT w.worker_id, COUNT(c.complaint_id) AS active_cnt " +
                "FROM workers w " +
                "LEFT JOIN complaints c ON w.worker_id = c.worker_id " +
                "    AND c.status IN ('PENDING', 'ASSIGNED', 'IN_PROGRESS', 'IN PROGRESS') " +
                "GROUP BY w.worker_id, w.worker_type " +
                "ORDER BY active_cnt ASC, CASE WHEN lower(w.worker_type) = lower(?) THEN 0 ELSE 1 END, w.worker_id ASC " +
                "LIMIT 1";

            try (PreparedStatement ps = con.prepareStatement(busySql)) {
                ps.setString(1, category != null ? category : "");
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("worker_id");
                    }
                }
            }
        }
        return null;
    }

    private Worker mapRow(ResultSet rs) throws SQLException {
        Worker w = new Worker();
        w.setWorkerId(rs.getInt("worker_id"));
        w.setFullName(rs.getString("full_name"));
        w.setEmail(rs.getString("email"));
        w.setPhone(rs.getString("phone"));
        w.setWorkerType(rs.getString("worker_type"));
        w.setPassword(rs.getString("password"));
        w.setCreatedAt(rs.getTimestamp("created_at"));
        return w;
    }
}
