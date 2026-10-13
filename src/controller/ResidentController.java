package controller;

import model.*;
import model.dao.*;
import util.CodeGenerator;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

public class ResidentController {
    private final ResidentDAO residentDAO = new ResidentDAO();
    private final VisitorDAO visitorDAO = new VisitorDAO();
    private final ComplaintDAO complaintDAO = new ComplaintDAO();
    private final PaymentDAO paymentDAO = new PaymentDAO();
    private final WorkerDAO workerDAO = new WorkerDAO();

    public Resident getProfile(int residentId) throws SQLException {
        return residentDAO.getById(residentId);
    }

    public ResidentMetrics getMetrics(int residentId) throws SQLException {
        return residentDAO.getMetrics(residentId);
    }

    public List<Visitor> getRecentVisitors(int residentId) throws SQLException {
        return visitorDAO.getByResident(residentId);
    }

    public String createVisitorPass(int residentId, String name, String phone,
                                    boolean hasVehicle, String vehicleNo,
                                    Timestamp validFrom, Timestamp validUntil) throws SQLException {
        String passCode = CodeGenerator.generateCode();
        String passHash = CodeGenerator.sha256(passCode);

        Visitor v = new Visitor();
        v.setResidentId(residentId);
        v.setVisitorName(name);
        v.setPhone(phone);
        v.setHasVehicle(hasVehicle);
        v.setVehicleNo(hasVehicle ? vehicleNo : null);
        v.setAccessHash(passHash);
        v.setValidFrom(validFrom);
        v.setValidUntil(validUntil);

        visitorDAO.insert(v);
        return passCode;
    }

    public List<Complaint> getComplaints(int residentId) throws SQLException {
        return complaintDAO.getByResident(residentId);
    }

    public boolean fileComplaint(int residentId, String title, String category, String description) throws SQLException {
        Complaint c = new Complaint();
        c.setResidentId(residentId);
        c.setTitle(title);
        c.setCategory(category);
        c.setDescription(description);
        c.setStatus("PENDING");

        // Try smart auto-assigning matching worker right away if available
        Integer workerId = workerDAO.findBestWorkerForCategory(category);
        if (workerId != null) {
            c.setWorkerId(workerId);
            c.setStatus("ASSIGNED");
        }

        return complaintDAO.insert(c) > 0;
    }

    public List<Payment> getPayments(int residentId) throws SQLException {
        return paymentDAO.getByResident(residentId);
    }

    public boolean payBill(int paymentId, String paymentMethod) throws SQLException {
        return paymentDAO.markAsPaid(paymentId, paymentMethod);
    }
}
