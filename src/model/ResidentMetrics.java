package model;

public class ResidentMetrics {
    private double totalDues;
    private double overdueDues;
    private int paidCount;
    private int openComplaintsCount;
    private int activeVisitorsCount;

    public ResidentMetrics() {}

    public double getTotalDues() { return totalDues; }
    public void setTotalDues(double totalDues) { this.totalDues = totalDues; }

    public double getOverdueDues() { return overdueDues; }
    public void setOverdueDues(double overdueDues) { this.overdueDues = overdueDues; }

    public int getPaidCount() { return paidCount; }
    public void setPaidCount(int paidCount) { this.paidCount = paidCount; }

    public int getOpenComplaintsCount() { return openComplaintsCount; }
    public void setOpenComplaintsCount(int openComplaintsCount) { this.openComplaintsCount = openComplaintsCount; }

    public int getActiveVisitorsCount() { return activeVisitorsCount; }
    public void setActiveVisitorsCount(int activeVisitorsCount) { this.activeVisitorsCount = activeVisitorsCount; }
}
