import java.util.List;
import java.util.concurrent.BlockingQueue;


// Диспетчер распределяет заказы между такси / Работает в отдельном потоке 
public class Dispatcher extends Thread {
    private final BlockingQueue<Order> orders;
    private final List<Taxi> taxis;
    private final BlockingQueue<Order> completedOrders;

    public Dispatcher(BlockingQueue<Order> orders, List<Taxi> taxis, BlockingQueue<Order> completedOrders) {
        this.orders = orders;
        this.taxis = taxis;
        this.completedOrders = completedOrders;
        setName("Dispatcher");
    }

    @Override
    public void run() {
        // Запускаем обработчик завершённых заказов
        Thread completionHandler = new Thread(this::handleCompletions);
        completionHandler.setName("CompletionHandler");
        completionHandler.setDaemon(true);
        completionHandler.start();

        try {
            while (!Thread.currentThread().isInterrupted()) {
                // Берём заказ из очереди
                Order order = orders.take();
                System.out.println("Диспетчер: получен заказ " + order);
                
                boolean orderAssigned = false;
                int attempts = 0;
                final int MAX_ATTEMPTS = 3; // Макс количество попыток
                // Пытаемся назначить заказ
                while (!orderAssigned && attempts < MAX_ATTEMPTS) {
                    Taxi bestTaxi = null;
                    int minDistance = Integer.MAX_VALUE;
                    // Ищем ближайшее свободное такси
                    for (Taxi taxi : taxis) {
                        if (taxi.isFree()) {
                            int distance = taxi.distanceTo(order);
                            if (distance < minDistance) {
                                minDistance = distance;
                                bestTaxi = taxi;
                            }
                        }
                    }

                    if (bestTaxi != null && bestTaxi.tryAssignOrder(order)) {
                        System.out.println("Диспетчер: заказ " + order + " назначен такси " + bestTaxi.getTaxiId());
                        orderAssigned = true;
                    } else {
                        attempts++;
                        if (attempts < MAX_ATTEMPTS) {
                            System.out.println("Диспетчер: не удалось назначить заказ, попытка " + attempts);
                            Thread.sleep(200);
                        }
                    }
                }

                if (!orderAssigned) {
                    System.out.println("Диспетчер: все такси заняты, заказ " + order + " возвращается в очередь");
                    orders.put(order);
                    Thread.sleep(500);
                }
            }
        } catch (InterruptedException e) {
            System.out.println("Диспетчер остановлен");
            Thread.currentThread().interrupt();
        }
    }
    // Обрабатываем завершённые заказы / Вычисляем и выводим время выполнения каждого заказа
    private void handleCompletions() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                Order completed = completedOrders.take();
                long tripTime = System.currentTimeMillis() - completed.timestamp;
                System.out.println("Диспетчер: завершен заказ " + completed + 
                                 " (время выполнения: " + tripTime + " мс)");
            }
        } catch (InterruptedException e) {
            System.out.println("Обработчик завершённых заказов остановлен");
            Thread.currentThread().interrupt();
        }
    }
}