// Класс для заказа на поездку
public class Order {
    public final int fromX, fromY;
    public final int toX, toY;
    public final long timestamp;
   
    public Order(int fromX, int fromY, int toX, int toY) {
        this.fromX = fromX;
        this.fromY = fromY;
        this.toX = toX;
        this.toY = toY;
        this.timestamp = System.currentTimeMillis();
    }
    // Вычисляем расстояние между точками отправления и назначения
    public int distance() {
        return Math.abs(fromX - toX) + Math.abs(fromY - toY);
    }
    
    @Override
    public String toString() {
        return String.format("(%d,%d) -> (%d,%d)", fromX, fromY, toX, toY);
    }
}