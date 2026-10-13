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
        visitorDAO.insert(residentId, name, phone, hasVehicle, vehicleNo, passHash, validFrom, validUntil);
        return passCode;
    }

    public List<Complaint> getComplaints(int residentId) throws SQLException {
        return complaintDAO.getByResident(residentId);
    }

    public boolean fileComplaint(int residentId, String title, String category, String description) throws SQLException {
        Integer workerId = workerDAO.findBestWorkerForCategory(category);
        return complaintDAO.insert(residentId, title, category, description, workerId) > 0;
    }

    public List<Payment> getPayments(int residentId) throws SQLException {
        return paymentDAO.getByResident(residentId);
    }

    public boolean payBill(int paymentId, String paymentMethod) throws SQLException {
        return paymentDAO.markAsPaid(paymentId, paymentMethod);
    }
}
