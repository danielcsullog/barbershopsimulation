import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Barber implements Runnable {

    public AtomicBoolean needToWork;                    //Barber is in working hours
    public BlockingQueue<Customer> queue;               //Waiting Queue

    //Statistics
    public AtomicInteger servedCustomers;
    public AtomicInteger overCapacityCustomers;
    public AtomicInteger closedBarberCustomers;
    public AtomicLong allWaitingTime;                   //all ms customers have been waiting for
    public long averageWaitingTime;                     //all waiting time divided by num of all customers (average)

    public Barber(){
        needToWork = new AtomicBoolean(false);
        queue = new LinkedBlockingQueue<>(BarberShopSimulation.MAX_QUEUE_CAPACITY);

        servedCustomers = new AtomicInteger(0);
        overCapacityCustomers = new AtomicInteger(0);
        closedBarberCustomers = new AtomicInteger(0);
        allWaitingTime = new AtomicLong(0);
    }

    @Override
    public void run() {
        while(!BarberShopSimulation.weekIsOver.get()) {  //run until simulation finishes
            try {
                if(needToWork.get()) {                  //if working hours...
                    if (queue.isEmpty()) {                  //...but no customers, just wait for customer's signal
                        synchronized (this) {
                            wait();
                        }
                    } else {                                //...and there are customers in queue, haircut
                        hairCut();
                    }
                }
                else {                                  //if not working hours...
                    if(!queue.isEmpty())                    //..but there are customers in queue
                        hairCut();                               //who is in queue, cut their hair off
                    else                                    //..and no customers in queue
                        synchronized (this) {                    //just wait until working hours and first signal
                            wait();
                        }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        getStatistics();                                //after the simulation, calculate and print stats
    }

    private void hairCut() {
        int randomTime = ThreadLocalRandom.current().nextInt(BarberShopSimulation.MIN_HAIRCUT_TIME,
                                                             BarberShopSimulation.MAX_HAIRCUT_TIME);  //generates a random num between [x...y)
        Customer currentCustomer;                                                   //current customer need for statistics (queue in time)
        try {
            long endWaitingTime = System.currentTimeMillis();                       //end time of waiting (before cutting)
            currentCustomer = queue.poll(100, TimeUnit.MILLISECONDS);        //get out the queue's first customer
            Thread.sleep(randomTime);                                               //simulate time of the haircut
            System.out.println("[BARBER]Customer get a haircut (" + randomTime +
                                "ms) from the queue, remaining: " + (queue.size()));
            allWaitingTime.addAndGet(endWaitingTime - currentCustomer.startWaitingTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void getStatistics() {                       //calculate average waiting time, and print all of them
        averageWaitingTime = allWaitingTime.get() /
                (servedCustomers.get() + overCapacityCustomers.get() + closedBarberCustomers.get());

        System.out.format("\r\n|| ---------- WEEKLY STATS ---------- ||\r\n" +
                        "[%d] customers were served\r\n" +
                        "[%d] customers left due to lack of chairs\r\n" +
                        "[%d] customers left due to closing hours\r\n" +
                        "[%d] ms was the average waiting time\r\n",
                        servedCustomers.get(), overCapacityCustomers.get(),
                        closedBarberCustomers.get(), averageWaitingTime);
    }
}
