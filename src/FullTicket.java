import java.util.concurrent.atomic.AtomicInteger;

public class FullTicket {
    private int fullTicketNum;
    private AtomicInteger currentSeatNum;


    public FullTicket(int fullTicketNum) {
        this.fullTicketNum = fullTicketNum;
        currentSeatNum = new AtomicInteger();
    }

    public int getFullTicketNum() {
        return fullTicketNum;
    }

    public void setFullTicketNum(int fullTicketNum) {
        this.fullTicketNum = fullTicketNum;
    }

    public AtomicInteger getCurrentSeatNum() {
        return currentSeatNum;
    }

    public void setCurrentSeatNum(AtomicInteger currentSeatNum) {
        this.currentSeatNum = currentSeatNum;
    }

    public String[] getSeats(int count){
        int num;
        for(;;){
            if((num = currentSeatNum.get()) + count > fullTicketNum){
                return null;
            }
            if(!currentSeatNum.compareAndSet(num, num + count)){
                continue;
            }
            String[] seats = new String[count];
            for(int i = 0 ; i < count; i++)
                seats[i] = String.valueOf(++num);
            return seats;
        }
    }

    public boolean hasTicket(){
        if(fullTicketNum > currentSeatNum.get()){
            return true;
        }
        return false;
    }

    public int fullTicketCount(){
        return fullTicketNum - currentSeatNum.get();
    }
}
