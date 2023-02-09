import java.util.concurrent.ThreadLocalRandom;

public class Customer implements Runnable {

    private static int dailyCustomers = 0;

    public long startWaitingInQueueTime; 

    private final int randomArrivingTime;
    private final Barber barber;

    public Customer(int randomArrivingTime, Barber barber) {
        this.randomArrivingTime = randomArrivingTime;
        this.barber = barber;
    }

    public static void generateRandomCustomers(Barber barber) {
        dailyCustomers = 0;
        int randomCustomerNumber = ThreadLocalRandom.current()
                .nextInt(BarberShopSimulation.MIN_CUSTOMERS_PER_DAY,
                        BarberShopSimulation.MAX_CUSTOMERS_PER_DAY);
        Thread[] customers = new Thread[randomCustomerNumber];

        for (int i = 0; i < randomCustomerNumber; i++) {
            int randomArrivingTime = ThreadLocalRandom.current().nextInt(0,
                    24 * BarberShopSimulation.ONE_HOUR_IN_MS);
            customers[i] = new Thread(new Customer(randomArrivingTime, barber));
            customers[i].start();
        }
    }

    @Override
    public void run() {
        try {
            Thread.sleep(randomArrivingTime);
            dailyCustomers++;
            if (barber.needToWork.get() == false) {
                System.out.println("[ Customer" + dailyCustomers + " ] Barber shop is CLOSED!");
                barber.closedBarberCustomers.getAndIncrement(); // increase stat
            } else {
                if (barber.queue.size() < 5) {
                    barber.queue.put(this);
                    this.startWaitingInQueueTime = System.currentTimeMillis();
                    System.out.println("[ Customer" + dailyCustomers + " ] I can go in!");
                    barber.servedCustomers.getAndIncrement();
                    synchronized (barber) {
                        barber.notify();
                    }
                } else {
                    System.out.println("[ Customer" + dailyCustomers + " ] Barber shop is FULL!");
                    barber.overCapacityCustomers.getAndIncrement();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
