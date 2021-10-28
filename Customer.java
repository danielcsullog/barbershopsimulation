import java.util.concurrent.ThreadLocalRandom;

public class Customer implements Runnable {
    public long startWaitingTime;               //when customer get in the queue

    private static int customerCounter = 0;     //daily customer number, just for printing
    private final int randomArrivingTime;       //random time when customer arrives at the barbershop
    private final Barber barber;                //customers barber

    public Customer(int randomArrivingTime, Barber barber) {
        this.randomArrivingTime = randomArrivingTime;
        this.barber = barber;
    }

    public static void generateRandomCustomers(Barber barber) {             //generate customers between [min_customers_per_day, max_customers_per_day)
        customerCounter = 0;
        int randomCustomerNumber = ThreadLocalRandom.current().nextInt(BarberShopSimulation.MIN_CUSTOMERS_PER_DAY,
                                                                       BarberShopSimulation.MAX_CUSTOMERS_PER_DAY);
        Thread[] customers = new Thread[randomCustomerNumber];              //create threads for customers

        for(int i = 0; i < randomCustomerNumber; i++) {                     //generate random arriving time then create customers (0-24h)
            int randomArrivingTime = ThreadLocalRandom.current().nextInt(0,
                                                                        24 * BarberShopSimulation.ONE_HOUR_IN_MS);
            customers[i] = new Thread(new Customer(randomArrivingTime, barber));
            customers[i].start();
        }
    }

    @Override
    public void run() {
        try {
            Thread.sleep(randomArrivingTime);                           //wait until the customers arriving time
            customerCounter++;
            if(barber.needToWork.get() == false) {                      //if barber is out of working hours
                System.out.println("[Customer" + customerCounter + "] Barber shop is CLOSED!");
                barber.closedBarberCustomers.getAndIncrement(); //increase stat
            }
            else {                                                      //if barber is in working hours
                if(barber.queue.size() < 5) {                           //if barbershop queue is not full
                    barber.queue.put(this);
                    this.startWaitingTime = System.currentTimeMillis(); //waiting stat
                    System.out.println("[Customer" + customerCounter +"] I can go in!");
                    barber.servedCustomers.getAndIncrement();           //served stat
                    synchronized (barber) {                             //notify barber, if the barbershop is empty
                        barber.notify();
                    }
                }
                else {                                                  //if barbershop queue (chairs) is full
                    System.out.println("[Customer" + customerCounter +"] Barber shop is FULL!");
                    barber.overCapacityCustomers.getAndIncrement();     //over barbershop capacity stat
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
