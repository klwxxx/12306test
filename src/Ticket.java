public class Ticket {
    String sourceStation;
    String destStation;
    String seatType;
    String seatInfo;

    public Ticket() {

    }

    public Ticket(String sourceStation, String destStation, String seatType, String seatInfo) {
        this.sourceStation = sourceStation;
        this.destStation = destStation;
        this.seatType = seatType;
        this.seatInfo = seatInfo;
    }

    public String getSourceStation() {
        return sourceStation;
    }

    public String getSeatType() {
        return seatType;
    }

    public void setSeatType(String seatType) {
        this.seatType = seatType;
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

    public String getSeatInfo() {
        return seatInfo;
    }

    public void setSeatInfo(String seatInfo) {
        this.seatInfo = seatInfo;
    }

    @Override
    public String toString() {
        return "Ticket{" +
                "sourceStation='" + sourceStation + '\'' +
                ", destStation='" + destStation + '\'' +
                ", seatType='" + seatType + '\'' +
                ", seatInfo='" + seatInfo + '\'' +
                '}';
    }
}
