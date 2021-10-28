import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class BarberShopSimulation {
    //SIMULATION SETTINGS
    public static final int ONE_HOUR_IN_MS = 400;           //hour can be modified by this one
    public static final int START_WORKING_HOUR = 9;         //when the barber shop opens (9:00)
    public static final int WORKING_HOURS = 8;              //barber's working time (9:00-17:00)
    public static final int MIN_CUSTOMERS_PER_DAY = 50;
    public static final int MAX_CUSTOMERS_PER_DAY = 100;
    public static final int MIN_HAIRCUT_TIME = 20;
    public static final int MAX_HAIRCUT_TIME = 200;
    public static final int MAX_QUEUE_CAPACITY = 5;
    private static final int SIMULATION_DAYS = 5;

    public static AtomicBoolean weekIsOver;                 //if simulation is ended (stop the working while loop)

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();    //just for check the simulation time (24*400 ms ~ 48000 ms)
        Barber barber = new Barber();
        weekIsOver = new AtomicBoolean(false);

        final ScheduledExecutorService dailyScheduler = Executors.newScheduledThreadPool(1);

        new Thread(barber).start();

        final Runnable dailyTask = new Runnable() {
            int remainingDays = SIMULATION_DAYS;            //how many days on the week

            @Override
            public void run() {
                if (remainingDays > 0) {                    //if there are more days out of the week
                    System.out.println("[BARBERSHOP] NEW WORKDAY! Remaining days: " + remainingDays);
                    remainingDays--;
                    new Thread(() -> Customer.generateRandomCustomers(barber)).start(); //generating random customers
                    try {
                        Thread.sleep(START_WORKING_HOUR * ONE_HOUR_IN_MS);        //wait until opening hour (9*400)
                        barber.needToWork.set(true);
                        synchronized (barber) {                                         //wake up the barber to start work
                            barber.notify();
                        }
                        Thread.sleep(WORKING_HOURS * ONE_HOUR_IN_MS);             //wait for working hours (8*400)
                        barber.needToWork.set(false);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                else {                                          //if there are NO more days out of the week
                    weekIsOver.set(true);
                    synchronized (barber) {                     //wake up from it's waiting status to end it's task with stats
                        barber.notify();
                    }
                    System.out.println("Week is over!");
                    dailyScheduler.shutdown();                  //shutdown the scheduled timer
                    long endTime = System.currentTimeMillis();  //end of simulation
                    System.out.format("Execution time: %d ms [simulation days [%d]" +
                                      " * 24 * one hour in simulation[%d ms]]",
                                      endTime - startTime, SIMULATION_DAYS, ONE_HOUR_IN_MS);
                }

            }
        };      //schedules Runnable task on a new Thread, it delayed by 0 ms, run once every day (24*400)
        dailyScheduler.scheduleAtFixedRate(dailyTask, 0, 24 * ONE_HOUR_IN_MS, TimeUnit.MILLISECONDS);
    }
}
