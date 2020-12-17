public class BuyTicketInfo {
    private String sourceStation;
    private String destStation;
    private String seatType;
    private int count;

    public BuyTicketInfo(String sourceStation, String destStation, String seatType, int count) {
        this.sourceStation = sourceStation;
        this.destStation = destStation;
        this.seatType = seatType;
        this.count = count;
    }

    public String getSourceStation() {
        return sourceStation;
    }

    public void setSourceStation(String sourceStation) {
        this.sourceStation = sourceStation;
    }

    public String getDestStation() {
        return destStation;
    }

    public void setDestStation(String destStation) {
        this.destStation = destStation;
    }

    public String getSeatType() {
        return seatType;
    }

    public void setSeatType(String seatType) {
        this.seatType = seatType;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return "BuyTicketInfo{" +
                "sourceStation='" + sourceStation + '\'' +
                ", destStation='" + destStation + '\'' +
                ", seatType='" + seatType + '\'' +
                ", ticketCount=" + count +
                '}';
    }
}
