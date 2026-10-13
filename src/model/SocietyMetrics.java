package model;

public class SocietyMetrics {
    private int totalResidents;
    private int totalWorkers;
    private int openComplaints;
    private double totalUnpaidDues;

    // Complaints Breakdown
    private int totalComplaints;
    private int pendingComplaints;
    private int inProgressComplaints;
    private int completedComplaints;

    // Billing Breakdown
    private double totalBilled;
    private double totalPaid;
    private double totalPending;
    private double totalOverdue;

    public SocietyMetrics() {}

    public int getTotalResidents() { return totalResidents; }
    public void setTotalResidents(int totalResidents) { this.totalResidents = totalResidents; }

    public int getTotalWorkers() { return totalWorkers; }
    public void setTotalWorkers(int totalWorkers) { this.totalWorkers = totalWorkers; }

    public int getOpenComplaints() { return openComplaints; }
    public int getActiveComplaints() { return openComplaints; }
    public void setOpenComplaints(int openComplaints) { this.openComplaints = openComplaints; }

    public double getTotalUnpaidDues() { return totalUnpaidDues; }
    public void setTotalUnpaidDues(double totalUnpaidDues) { this.totalUnpaidDues = totalUnpaidDues; }

    public int getTotalComplaints() { return totalComplaints; }
    public void setTotalComplaints(int totalComplaints) { this.totalComplaints = totalComplaints; }

    public int getPendingComplaints() { return pendingComplaints; }
    public void setPendingComplaints(int pendingComplaints) { this.pendingComplaints = pendingComplaints; }

    public int getInProgressComplaints() { return inProgressComplaints; }
    public void setInProgressComplaints(int inProgressComplaints) { this.inProgressComplaints = inProgressComplaints; }

    public int getCompletedComplaints() { return completedComplaints; }
    public void setCompletedComplaints(int completedComplaints) { this.completedComplaints = completedComplaints; }

    public double getTotalBilled() { return totalBilled; }
    public void setTotalBilled(double totalBilled) { this.totalBilled = totalBilled; }

    public double getTotalPaid() { return totalPaid; }
    public void setTotalPaid(double totalPaid) { this.totalPaid = totalPaid; }

    public double getTotalPending() { return totalPending; }
    public void setTotalPending(double totalPending) { this.totalPending = totalPending; }

    public double getTotalOverdue() { return totalOverdue; }
    public void setTotalOverdue(double totalOverdue) { this.totalOverdue = totalOverdue; }
}
