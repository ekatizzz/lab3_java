import java.util.Random;
import java.util.concurrent.BlockingQueue;

// Генератор клиентов / Работает в отдельном потоке
public class ClientGenerator extends Thread {
    private final BlockingQueue<Order> orders; // Очередь для новых заказов
    private final Random rnd = new Random();   // Генератор случайных чисел

    public ClientGenerator(BlockingQueue<Order> orders) {
        this.orders = orders;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                // Создаём новый заказ со случайными координатами
                Order order = new Order(
                        rnd.nextInt(20),
                        rnd.nextInt(20),
                        rnd.nextInt(20),
                        rnd.nextInt(20)
                );
                orders.put(order);
                System.out.println("Генератор: создан новый заказ " + order);
                Thread.sleep(700);
            }
        } catch (InterruptedException e) {
            System.out.println("Генератор остановлен");
            Thread.currentThread().interrupt();
        }
    }
}