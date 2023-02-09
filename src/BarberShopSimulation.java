import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class BarberShopSimulation {

    //SIMULATION SETTINGS
    public static final int ONE_HOUR_IN_MS = 400;
    public static final int START_WORKING_HOUR = 9;         //9:00
    public static final int WORKING_HOURS = 8;              //9:00-17:00
    public static final int MIN_CUSTOMERS_PER_DAY = 50;
    public static final int MAX_CUSTOMERS_PER_DAY = 100;
    public static final int MIN_HAIRCUT_TIME = 20;
    public static final int MAX_HAIRCUT_TIME = 200;
    public static final int MAX_QUEUE_CAPACITY = 5;
    private static final int SIMULATION_DAYS = 5;

    public static AtomicBoolean weekIsOver;

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        weekIsOver = new AtomicBoolean(false);
        Barber barber = new Barber();

        final ScheduledExecutorService dailyScheduler = Executors.newScheduledThreadPool(1);

        new Thread(barber).start();

        final Runnable dailyTask = new Runnable() {
            int remainingDays = SIMULATION_DAYS;

            @Override
            public void run() {
                if (remainingDays > 0) {
                    System.out.println("[BARBERSHOP] NEW WORKDAY! Remaining days: " + remainingDays);
                    remainingDays--;
                    new Thread(() -> Customer.generateRandomCustomers(barber)).start();
                    try {
                        Thread.sleep(START_WORKING_HOUR * ONE_HOUR_IN_MS);        //wait until opening hour (9*400)
                        barber.needToWork.set(true);
                        synchronized (barber) {
                            barber.notify();
                        }
                        Thread.sleep(WORKING_HOURS * ONE_HOUR_IN_MS);             //wait for working hours (8*400)
                        barber.needToWork.set(false);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    weekIsOver.set(true);
                    synchronized (barber) {
                        barber.notify();
                    }
                    System.out.println("Week is over!");
                    dailyScheduler.shutdown();
                    long endTime = System.currentTimeMillis();
                    System.out.format("Execution time: %d ms [simulation days [%d]" +
                                      " * 24 * one hour in simulation[%d ms]]",
                                      endTime - startTime, SIMULATION_DAYS, ONE_HOUR_IN_MS);
                }

            }
        };
        dailyScheduler.scheduleAtFixedRate(dailyTask, 0, 24 * ONE_HOUR_IN_MS, TimeUnit.MILLISECONDS);
    }
}
