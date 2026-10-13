package controller;

import model.Complaint;
import model.dao.ComplaintDAO;
import model.dao.PaymentDAO;

import java.sql.Date;
import java.sql.SQLException;
import java.util.List;

public class WorkerController {
    private final ComplaintDAO complaintDAO = new ComplaintDAO();
    private final PaymentDAO paymentDAO = new PaymentDAO();

    public List<Complaint> getAllComplaints() throws SQLException {
        return complaintDAO.getAll();
    }

    public String getComplaintDescription(int complaintId) throws SQLException {
        return complaintDAO.getDescription(complaintId);
    }

    public boolean claimComplaint(int complaintId, int workerId) throws SQLException {
        return complaintDAO.claimComplaint(complaintId, workerId);
    }

    public boolean markInProgress(int complaintId) throws SQLException {
        return complaintDAO.updateStatus(complaintId, "IN_PROGRESS");
    }

    public boolean completeComplaint(int complaintId, int workerId, String note, Double repairFee) throws SQLException {
        Complaint c = complaintDAO.getById(complaintId);
        if (c == null) return false;
        int residentId = c.getResidentId();
        String title = c.getTitle();
        Date dueDate = Date.valueOf(java.time.LocalDate.now().plusDays(7));
        return completeComplaint(complaintId, workerId, residentId, title, note, repairFee, dueDate);
    }

    public boolean completeComplaint(int complaintId, int workerId, int residentId,
                                     String complaintTitle, String note,
                                     Double repairFee, Date dueDate) throws SQLException {
        boolean ok = complaintDAO.completeComplaint(complaintId, workerId, note);
        if (ok && repairFee != null && repairFee > 0 && residentId > 0 && dueDate != null) {
            paymentDAO.insertWorkerCompletionBill(residentId, complaintId, complaintTitle, repairFee, dueDate);
        }
        return ok;
    }
}
