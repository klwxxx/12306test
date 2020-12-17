import org.junit.Test;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class testBuyTicket {
    static ConcurrentHashMap<String,ConcurrentHashMap<Ticket,Long>> result = new ConcurrentHashMap<String,ConcurrentHashMap<Ticket,Long>>();
    static TrainTicket tt;
    static String[] stations;
    static String[] seatTypes;
    static Map<BuyTicketInfo,Ticket[]> resMap = new ConcurrentHashMap<>();
    public static void main(String[] args) {

    }

    @Test
    public void testRepeatTicket() throws InterruptedException {
        init();
        int requestNum = 20000;//requestNum为购票请求数量，一个请求包含一张或多张票；
        BuyTicketInfo[] buyTicketInfos = generateBuyRequest(requestNum);
        int NUMBER_OF_CORES =  Runtime.getRuntime().availableProcessors();

        ThreadPoolExecutor pool = new ThreadPoolExecutor(NUMBER_OF_CORES, NUMBER_OF_CORES + 2, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(requestNum));
        long startTime = System.currentTimeMillis();

        for(int i = 0 ; i < requestNum; i++){
            pool.execute(new Task(buyTicketInfos[i]));
        }
        pool.shutdown();
        pool.awaitTermination(20,TimeUnit.SECONDS);
        long endTime = System.currentTimeMillis();

        Map<String,Long> checkRepeat = new HashMap<>();
        Map<String, Integer> stationMap = tt.getStationMap();
        for(Ticket[] tickets : resMap.values()){
            for(Ticket ticket: tickets){
                Long seatValue = calSeatValue(stationMap.get(ticket.getSourceStation()),stationMap.get(ticket.getDestStation()));
                String key = ticket.getSeatType() + ticket.getSeatInfo();
                if(!checkRepeat.containsKey(key)){
                    checkRepeat.put(key,seatValue);
                }else{
                    Long oldValue = checkRepeat.get(key);
                    if((oldValue & seatValue) == 0){
                        checkRepeat.put(key,oldValue | seatValue);
                    }else{
                        System.out.println("Repeat Tikcet!!");
                        return;
                    }
                }
            }
        }

        //printResult(resMap);

        System.out.println("No Repeat Ticket");
        System.out.println("Request Number:" + requestNum + "  Total Execute Time(millis):" + (endTime - startTime));
    }


    @Test
    public void testBuyTicketUseThreadPool() throws InterruptedException {
        init();
        int requestNum = 20000;//requestNum为购票请求数量，一个请求包含一张或多张票；
        BuyTicketInfo[] buyTicketInfos = generateBuyRequest(requestNum);
        int NUMBER_OF_CORES =  Runtime.getRuntime().availableProcessors();

        ThreadPoolExecutor pool = new ThreadPoolExecutor(NUMBER_OF_CORES, NUMBER_OF_CORES + 2, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(requestNum));
        long startTime = System.currentTimeMillis();

        for(int i = 0 ; i < requestNum; i++){
            pool.execute(new Task(buyTicketInfos[i]));
        }
        pool.shutdown();
        pool.awaitTermination(20,TimeUnit.SECONDS);
        long endTime = System.currentTimeMillis();

        printResult(resMap);

        System.out.println("Request Number:" + requestNum + ";  Total Execute Time(millis):" + (endTime - startTime));
    }

    @Test
    public void testSingleRequestTime() throws InterruptedException {
        init();
        BuyTicketInfo[] buyTicketInfos = generateBuyRequest(2000);
        for(int i = 0 ; i < 2000 ; i++){
            new Thread(new Task(buyTicketInfos[i])).start();
        }
        Thread.sleep(5000);
        //先执行2000个请求将票池打乱

        Thread t = new Thread(new Task(buyTicketInfos[0]));
        long startTime = System.currentTimeMillis();
        t.start();
        t.join(10000);
        long endTime = System.currentTimeMillis();


        printResult(resMap);

        System.out.println("Single Request Execute Time(millis):" + (endTime - startTime));

    }

    public BuyTicketInfo[] generateBuyRequest(int requestNum){
        Random ran = new Random();
        int stationNum = stations.length;
        BuyTicketInfo[] bti = new BuyTicketInfo[requestNum];
        for(int i = 0 ; i < requestNum ; i++){
            int ticketCount = ran.nextInt(5) + 1;
            int seatType = ran.nextInt(3);
            int destStation = ran.nextInt(stationNum);
            if(destStation <= 1){
                destStation++;
            }
            int sourceStaion = ran.nextInt(destStation);

            bti[i] = new BuyTicketInfo(stations[sourceStaion],stations[destStation],seatTypes[seatType],ticketCount);
        }
        return bti;
    }

    public static void init(){
        String driver ="com.mysql.jdbc.Driver";
        String url ="jdbc:mysql://localhost:3306/12306_test";
        String user ="root";
        String password ="123456";
        Connection conn;
        Map<String,Integer> stationMap = new HashMap<>();
        try{
            Class.forName(driver);
            conn = DriverManager.getConnection(url,user,password);
            Statement statement = conn.createStatement();
            String sql = "select name, seq from trains,stop_infos,stations where trains.id = 8 and stop_infos.train_id = trains.id and stations.id = stop_infos.station_id ORDER BY seq;";
            ResultSet res = statement.executeQuery(sql);
            while(res.next()){
                String name = res.getString("name");
                int seq = res.getInt("seq");
                stationMap.put(name,seq);
            }
            res.close();
            conn.close();
        }catch (ClassNotFoundException e){

        } catch (SQLException e) {
            e.printStackTrace();
        }
        Map<String,Integer> seatTypeAndNum = new HashMap<>();
        seatTypeAndNum.put("A",500);
        seatTypeAndNum.put("B",500);
        seatTypeAndNum.put("C",500);
        tt = new TrainTicket("G01",stationMap.size(),seatTypeAndNum,stationMap);
        stations = new String[stationMap.size()];
        for(Map.Entry<String,Integer> entry : stationMap.entrySet()){
            testBuyTicket.stations[entry.getValue()] = entry.getKey();
        }
        seatTypes = new String[]{"A","B","C"};
    }

    public long calSeatValue(int sourceStation, int destStation){
        long seat = 1;
        seat <<= (destStation - sourceStation);
        seat -= 1;
        seat <<= sourceStation;
        return seat;
    }

    public void printResult(Map<BuyTicketInfo,Ticket[]> resMap){
        for(Map.Entry<BuyTicketInfo,Ticket[]> entry : resMap.entrySet()){//打印Ticket信息
            System.out.println("***************************************");
            System.out.println("Request Info: \n" + entry.getKey() + "\nTickets Info:");
            Ticket[] tickets = entry.getValue();
            for(Ticket ticket: tickets){
                System.out.println(ticket);
            }
            System.out.println("***************************************");
            System.out.println();
        }
    }
}

class Task implements Runnable{
    BuyTicketInfo bti;

    public Task(BuyTicketInfo bti){
        this.bti = bti;
    }
    public void run(){
        Ticket[] tickets = testBuyTicket.tt.buyTickets(bti);
        if(tickets != null)
            testBuyTicket.resMap.put(bti,tickets);
    }
}
//class TestTicket implements Runnable{
//    TrainTicket tt;
//    int sourceStation, destStation;
//    String seatType;
//
//    public TestTicket(TrainTicket tt, int sourceStation, int destStation, String seatType) {
//        this.tt = tt;
//        this.sourceStation = sourceStation;
//        this.destStation = destStation;
//        this.seatType = seatType;
//    }
//
//    @Override
//    public void run() {
//        Ticket ticket = tt.buyTicket(sourceStation,destStation,seatType);
//        System.out.println("Request{起点="+sourceStation+",终点="+ destStation +", 座位类型="+ seatType + "}    -----   " + ticket);
//        if(seatType == "A" && ticket != null){
//            testBuyTicket.result.get(ticket.seatInfo).put(ticket,System.currentTimeMillis());
//        }
//    }
//}