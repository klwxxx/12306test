import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

public class TrainTicket {
    private String trainNo;
    private int stationNum;
    private long fullTicket;
    private Map<String,FullTicket> fullTicketMap;
    private Map<String,Integer> stationMap;
    private Map<String, ConcurrentSkipListMap<KeyNode, LinkedList<String>>> seatMap;
    /*    seatMap:  key   ---> 座位类型 （一等，二等....）
                    value ---> 跳表  (与key对应的座位类型)
          skipList：key   ---> 座位数和最大连续座位数量(用于比较器)
                    value ---> 座位数量，座位集合（LinkedList）
    */

    public TrainTicket(String trainNo, int stationNum, Map<String,Integer> seatTypeAndNum, Map<String,Integer> stationMap) {
        // seatTypeAndNum ： key是座位类型，value是座位数量。
        // stationMap ： key是站点名称， value是站点序号sequence（0...n）
        this.trainNo = trainNo;
        this.stationNum = stationNum;

        fullTicket = 1;
        fullTicket <<= stationNum - 1;
        fullTicket -= 1;

        this.stationMap = stationMap;
        init(seatTypeAndNum);
    }


    public Map<String, ConcurrentSkipListMap<KeyNode, LinkedList<String>>> getSeatMap() {
        return seatMap;
    }

    public Map<String, Integer> getStationMap() {
        return stationMap;
    }

    private void init(Map<String,Integer> seatTypeAndNum){
        Comparator<KeyNode> comp = (o1, o2) -> {
            if(o1.getContinueCount() > o2.getContinueCount()){
                return 1;
            }else if(o1.getContinueCount() < o2.getContinueCount()) {
                return -1;
            }else{
                if(o1.getValue() > o2.getValue()){
                    return 1;
                }else if (o1.getValue() < o2.getValue()){
                    return -1;
                }else {
                    return 0;
                }
            }
        };
        Comparator<KeyNode> comp2 = (o1, o2) -> {
            if(o1.getContinueCount() > o2.getContinueCount()){
                return 1;
            }else{
                if(o1.getValue() > o2.getValue()){
                    return 1;
                }else if (o1.getValue() < o2.getValue()){
                    if((o1.getValue()&o2.getValue()) == o1.getValue())
                        return -1;
                    else
                        return 1;
                }else {
                    return 0;
                }
            }
        };

        fullTicketMap = new HashMap<>();
        seatMap = new HashMap<>();
        for(Map.Entry<String,Integer> entry : seatTypeAndNum.entrySet()){
            fullTicketMap.put(entry.getKey(),new FullTicket(entry.getValue()));
            seatMap.put(entry.getKey(),new ConcurrentSkipListMap<>(comp,comp2));
        }
    }

    public Ticket[] buyMultiTickets(BuyTicketInfo[] bti){
        int count = 0;
        for(BuyTicketInfo b : bti)
            count += b.getCount();
        if(count > 5){
            return null;
        }
        Map<KeyNode, String[]>[] seats = new HashMap[bti.length];
        long[] ticketValues = new long[bti.length];
        for(int i = 0 ; i< bti.length; i++){
            ticketValues[i] = calTicketValue(stationMap.get(bti[i].getSourceStation()),stationMap.get(bti[i].getDestStation()));
            Map<KeyNode,String[]> res = getSeats(ticketValues[i], calContinueCount(ticketValues[i]), bti[i].getSeatType(), bti[i].getCount());
            if(res == null){
                for(int j = 0 ; j < i ; j++){
                    ConcurrentSkipListMap<KeyNode, LinkedList<String>> skipList = seatMap.get(bti[i].getSeatType());
                    for(Map.Entry<KeyNode,String[]> entry : seats[i].entrySet())
                        skipList.insertRemainTicket(entry.getKey(),entry.getValue());
                }
                return null;
            }
            seats[i] = res;
        }
        Ticket[] tickets = new Ticket[count];
        for(int i = 0 ; i < seats.length ; i++){
            String seatType = bti[i].getSeatType();
            String sourceStation = bti[i].getSourceStation();
            String destStation = bti[i].getDestStation();
            ConcurrentSkipListMap<KeyNode, LinkedList<String>> skipList = seatMap.get(seatType);
            for(Map.Entry<KeyNode,String[]> entry : seats[i].entrySet()){
                String[] seatInfo = entry.getValue();
                for(String s : seatInfo){
                    tickets[--count] = new Ticket(sourceStation,destStation,seatType,s);
                }
                long remainTicket = ticketValues[i] ^ (entry.getKey().getValue());
                skipList.insertRemainTicket(new KeyNode(remainTicket, calContinueCount(remainTicket)), seatInfo);
            }
        }
        return tickets;

    }

    public Ticket[] buyTickets(BuyTicketInfo bti){
        int count = bti.getCount();
        if(count > 5){
            return null;
        }
        Map<KeyNode, String[]> seats = new HashMap<>();
        long ticketValue =  calTicketValue(stationMap.get(bti.getSourceStation()),stationMap.get(bti.getDestStation()));
        Map<KeyNode,String[]> res = getSeats(ticketValue, calContinueCount(ticketValue), bti.getSeatType(), count);
        if(res == null){
            ConcurrentSkipListMap<KeyNode, LinkedList<String>> skipList = seatMap.get(bti.getSeatType());
            for(Map.Entry<KeyNode,String[]> entry : seats.entrySet())
                skipList.insertRemainTicket(entry.getKey(),entry.getValue());
            return null;
        }
        seats = res;

        Ticket[] tickets = new Ticket[count];
        String seatType = bti.getSeatType();
        String sourceStation = bti.getSourceStation();
        String destStation = bti.getDestStation();
        ConcurrentSkipListMap<KeyNode, LinkedList<String>> skipList = seatMap.get(seatType);
        for(Map.Entry<KeyNode,String[]> entry : seats.entrySet()){
            String[] seatInfo = entry.getValue();
            for(String s : seatInfo){
                tickets[--count] = new Ticket(sourceStation,destStation,seatType,s);
            }
            long remainTicket = ticketValue ^ (entry.getKey().getValue());
            skipList.insertRemainTicket(new KeyNode(remainTicket, calContinueCount(remainTicket)), seatInfo);
        }

        return tickets;
    }

    private Map<KeyNode, String[]> getSeats(long ticketValue, int continueCount, String seatType, int count){//列车的起点到终点所有站点依次编号为0……n

        ConcurrentSkipListMap<KeyNode, LinkedList<String>> skipList = seatMap.get(seatType);
        Map<KeyNode, String[]> seats = null;

        seats = skipList.searchAndAllocateSeat(new KeyNode(ticketValue, continueCount), count);

        for(String[] s : seats.values())
            count -= s.length;
        if(count > 0){
            String[] fullSeats = divideFullTicket(seatType, count);
            if(fullSeats == null) {
                //rollback
                for(Map.Entry<KeyNode,String[]> entry : seats.entrySet()){
                    skipList.insertRemainTicket(entry.getKey(),entry.getValue());
                }
                return null;
            }
            seats.put(new KeyNode(fullTicket,stationNum - 1), fullSeats);
        }
        return seats;
    }


    public String getRemainTicketCount(int sourceStation, int destStation, String seatType){
        FullTicket fullTicket = fullTicketMap.get(seatType);
        if(fullTicket.hasTicket()){
            return "余票充足";
        }else{
            return String.valueOf(getTicketCount(sourceStation,destStation,seatType));
        }
    }


    private int getTicketCount(int sourceStation, int destStation, String seatType){
        ConcurrentSkipListMap<KeyNode, LinkedList<String>> skipList = seatMap.get(seatType);
        long ticket = calTicketValue(sourceStation,destStation);
        return skipList.searchTicketCount(new KeyNode(ticket, calContinueCount(ticket)));
    }

    private String[] divideFullTicket(String seatType, int count){
        return fullTicketMap.get(seatType).getSeats(count);
    }

    private long calTicketValue(int sourceStation, int destStation){
        long ticket = 1;
        ticket <<= (destStation - sourceStation);
        ticket -= 1;
        ticket <<= sourceStation;
        return ticket;
    }

    private int calContinueCount(long ticketValue){
        long f = 1;
        int continueCount = 0;
        for(int i = 0,temp = (f&ticketValue) == 1 ? 1 : 0; i < stationNum - 1; i++){
            ticketValue >>= 1;
            if((f&ticketValue) == 1)
                temp++;
            else{
                continueCount = continueCount > temp ? continueCount : temp;
                temp = 0;
            }
        }
        return continueCount;
    }

}