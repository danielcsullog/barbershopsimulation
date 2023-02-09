import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Barber implements Runnable {

    public AtomicBoolean needToWork;
    public BlockingQueue<Customer> queue;

    // Statistics
    public AtomicInteger servedCustomers;
    public AtomicInteger overCapacityCustomers;
    public AtomicInteger closedBarberCustomers;
    public AtomicLong allWaitingTime;
    public long averageWaitingTime;

    public Barber() {
        needToWork = new AtomicBoolean(false);
        queue = new LinkedBlockingQueue<>(BarberShopSimulation.MAX_QUEUE_CAPACITY);

        servedCustomers = new AtomicInteger(0);
        overCapacityCustomers = new AtomicInteger(0);
        closedBarberCustomers = new AtomicInteger(0);
        allWaitingTime = new AtomicLong(0);
    }

    @Override
    public void run() {
        while (!BarberShopSimulation.weekIsOver.get()) {
            try {
                if (needToWork.get()) {
                    if (queue.isEmpty())
                        synchronized (this) {
                            wait();
                        }
                    else
                        hairCut();
                } else {
                    if (!queue.isEmpty())
                        hairCut();
                    else
                        synchronized (this) {
                            wait();
                        }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        getStatistics();
    }

    private void hairCut() {
        int randomTime = ThreadLocalRandom.current()
                .nextInt(BarberShopSimulation.MIN_HAIRCUT_TIME,
                        BarberShopSimulation.MAX_HAIRCUT_TIME);
        Customer currentCustomer;
        try {
            long endWaitingTime = System.currentTimeMillis();
            currentCustomer = queue.poll(100, TimeUnit.MILLISECONDS);
            Thread.sleep(randomTime);
            System.out.println("[BARBER] Customer got a haircut in " + randomTime +
                    " ms, remaining customers: " + (queue.size()));
            allWaitingTime.addAndGet(endWaitingTime - currentCustomer.startWaitingInQueueTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void getStatistics() {
        averageWaitingTime = allWaitingTime.get() / servedCustomers.get();

        System.out.format("\r\n|| ---------- WEEKLY STATS ---------- ||\r\n" +
                "[%d] customers were served\r\n" +
                "[%d] customers left due to lack of chairs\r\n" +
                "[%d] customers left due to closing hours\r\n" +
                "[%d] ms was the average waiting time in the queue\r\n",
                servedCustomers.get(), overCapacityCustomers.get(),
                closedBarberCustomers.get(), averageWaitingTime);
    }
}
