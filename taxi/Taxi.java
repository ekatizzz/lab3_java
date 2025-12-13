import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.BlockingQueue;

// Класс такси-потоков / Работает в отдельном потоке
public class Taxi extends Thread {
    private final int id;
    private int x = 0, y = 0;
    private final AtomicBoolean busy = new AtomicBoolean(false);
    private final ReentrantLock lock = new ReentrantLock();
    private Order currentOrder;
    private final BlockingQueue<Order> completedOrders;
    private final AtomicInteger completedCount;

    public Taxi(int id, BlockingQueue<Order> completedOrders, AtomicInteger completedCount) {
        this.id = id;
        this.completedOrders = completedOrders;
        this.completedCount = completedCount;
        setName("Taxi-" + id);
    }

    public int getTaxiId() {
        return id;
    }

    // Проверяем, свободно ли такси
    public boolean isFree() {
        return !busy.get();
    }
    // Вычисляем расстояние от текущего положения такси до точки отправления заказа
    public int distanceTo(Order o) {
        lock.lock();
        try {
            return Math.abs(x - o.fromX) + Math.abs(y - o.fromY);
        } finally {
            lock.unlock();
        }
    }
    // Назначаем заказ такси
    public boolean tryAssignOrder(Order order) {
        if (busy.compareAndSet(false, true)) {
            lock.lock();
            try {
                this.currentOrder = order;
                System.out.println("Такси " + id + " получило заказ " + order);
                return true;
            } finally {
                lock.unlock();
            }
        }
        return false;
    }
    // Бесконечно обрабатываем заказы, пока поток не прервут
    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            if (busy.get() && currentOrder != null) {
                try {
                    System.out.println("Такси " + id + " едет к клиенту " + currentOrder);
                    Thread.sleep(500);

                    System.out.println("Такси " + id + " подобрало клиента, едет к месту назначения");

                    int travelTime = currentOrder.distance() * 100;
                    Thread.sleep(travelTime);

                    lock.lock();
                    try {
                        x = currentOrder.toX;
                        y = currentOrder.toY;
                    } finally {
                        lock.unlock();
                    }

                    System.out.println("Такси " + id + " завершило поездку, координаты: (" + x + "," + y + ")");

                    // Уведомляем диспетчера
                    completedOrders.put(currentOrder);

                    // Увеличиваем счётчик выполненных заказов
                    completedCount.incrementAndGet();

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } finally {
                    busy.set(false);
                    currentOrder = null;
                }
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        System.out.println("Такси " + id + " остановлено");
    }
}