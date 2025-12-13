import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

// Главный класс системы
public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Система управления беспилотными такси в городе ===\n");
        // Создаём потокобезопасные очереди
        BlockingQueue<Order> orderQueue = new LinkedBlockingQueue<>();
        BlockingQueue<Order> completedOrders = new LinkedBlockingQueue<>();
        AtomicInteger completedCount = new AtomicInteger(0);

        List<Taxi> taxis = new ArrayList<>();
        List<Thread> allThreads = new ArrayList<>();

        // Создаем такси
        for (int i = 0; i < 5; i++) {
            Taxi taxi = new Taxi(i, completedOrders, completedCount);
            taxis.add(taxi);
            allThreads.add(taxi);
        }

        // Создаем диспетчера
        Dispatcher dispatcher = new Dispatcher(orderQueue, taxis, completedOrders);
        allThreads.add(dispatcher);
        
        // Создаем генератор клиентов
        ClientGenerator generator = new ClientGenerator(orderQueue);
        allThreads.add(generator);

        // Запускаем все потоки
        for (Thread thread : allThreads) {
            thread.start();
        }

        System.out.println("Система запущена. 5 такси готовы к работе.");
        System.out.println("Работа системы будет автоматически остановлена через 30 секунд...\n");

        // Работаем 30 сек
        TimeUnit.SECONDS.sleep(30);

        // Останавливаем систему
        System.out.println("\n=== Остановка системы ===");
        
        // Прерываем все потоки
        for (Thread thread : allThreads) {
            thread.interrupt();
        }

        // Ждем завершения
        for (Thread thread : allThreads) {
            thread.join(2000);
        }
        // Выводим статистику
        System.out.println("\n=== Система остановлена ===");
        System.out.println("Осталось заказов в очереди: " + orderQueue.size());
        System.out.println("Всего выполненных заказов: " + completedCount.get());
    }
}