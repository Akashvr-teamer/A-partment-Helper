package controller;

import model.*;
import model.dao.*;

import java.sql.Date;
import java.sql.SQLException;
import java.util.List;

public class AdminController {
    private final AdminDAO adminDAO = new AdminDAO();
    private final ComplaintDAO complaintDAO = new ComplaintDAO();
    private final PaymentDAO paymentDAO = new PaymentDAO();
    private final ResidentDAO residentDAO = new ResidentDAO();
    private final WorkerDAO workerDAO = new WorkerDAO();
    private final ParkingDAO parkingDAO = new ParkingDAO();

    // 1. Overview
    public SocietyMetrics getSocietyMetrics() throws SQLException {
        return adminDAO.getSocietyMetrics();
    }

    public List<Complaint> getRecentComplaints(int limit) throws SQLException {
        return complaintDAO.getRecent(limit);
    }

    public List<Payment> getRecentPayments(int limit) throws SQLException {
        return paymentDAO.getRecent(limit);
    }

    // 2. Complaints Management
    public List<Complaint> getAllComplaints() throws SQLException {
        return complaintDAO.getAll();
    }

    public String getComplaintDescription(int complaintId) throws SQLException {
        return complaintDAO.getDescription(complaintId);
    }

    public boolean assignComplaint(int complaintId, int workerId) throws SQLException {
        return complaintDAO.assignWorker(complaintId, workerId);
    }

    public Integer autoAssignComplaint(int complaintId, String category) throws SQLException {
        Integer bestWorkerId = workerDAO.findBestWorkerForCategory(category);
        if (bestWorkerId != null) {
            complaintDAO.assignWorker(complaintId, bestWorkerId);
        }
        return bestWorkerId;
    }

    // 3. Billing Management
    public List<Payment> getAllPayments() throws SQLException {
        return paymentDAO.getAll();
    }

    public boolean generateCustomBill(int residentId, String title, double amount, Date dueDate) throws SQLException {
        if (title != null && title.length() > 255) {
            title = title.substring(0, 255);
        }
        return paymentDAO.insertCustomBill(residentId, title, amount, dueDate) > 0;
    }

    // 4. Registration
    public boolean isEmailTaken(String email) throws SQLException {
        return residentDAO.emailExists(email);
    }

    public int registerResident(Resident resident) throws SQLException {
        if (isEmailTaken(resident.getEmail())) {
            throw new IllegalArgumentException("The email '" + resident.getEmail() + "' is already registered in the system.");
        }
        int genId = residentDAO.insert(resident);
        if (genId > 0 && resident.getParkingSlot() != null && !resident.getParkingSlot().isBlank()) {
            parkingDAO.syncResidentParking(resident.getParkingSlot(), genId);
        }
        return genId;
    }

    public int registerWorker(Worker worker) throws SQLException {
        if (isEmailTaken(worker.getEmail())) {
            throw new IllegalArgumentException("The email '" + worker.getEmail() + "' is already registered in the system.");
        }
        return workerDAO.insert(worker);
    }

    // 5. Directory Management
    public List<Resident> getAllResidents() throws SQLException {
        return residentDAO.getAll();
    }

    public Resident getResidentById(int residentId) throws SQLException {
        return residentDAO.getById(residentId);
    }

    public boolean updateResident(Resident resident, boolean updatePassword) throws SQLException {
        boolean ok = residentDAO.update(resident, updatePassword);
        if (ok && resident.getParkingSlot() != null && !resident.getParkingSlot().isBlank()) {
            parkingDAO.syncResidentParking(resident.getParkingSlot(), resident.getResidentId());
        }
        return ok;
    }

    public boolean deleteResident(int residentId) throws SQLException {
        return residentDAO.delete(residentId);
    }

    public List<Worker> getAllWorkers() throws SQLException {
        return workerDAO.getAll();
    }

    public List<Worker> getAllWorkersWithWorkload() throws SQLException {
        return workerDAO.getAllWithWorkload();
    }

    public List<String> getDistinctWorkerTypes() throws SQLException {
        return workerDAO.getDistinctWorkerTypes();
    }

    public Worker getWorkerById(int workerId) throws SQLException {
        return workerDAO.getById(workerId);
    }

    public boolean updateWorker(Worker worker, boolean updatePassword) throws SQLException {
        return workerDAO.update(worker, updatePassword);
    }

    public boolean deleteWorker(int workerId) throws SQLException {
        // Reset any complaints currently assigned to this worker so they are not orphaned
        complaintDAO.resetAssignedToWorker(workerId);
        return workerDAO.delete(workerId);
    }
}
