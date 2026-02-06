package utils;

public final class SalesContext {

    private static volatile LastSale lastSale;

    private SalesContext() {
    }

    public static void setLastSale(String plantName, int quantity) {
        lastSale = new LastSale(plantName, quantity);
    }

    public static LastSale getLastSale() {
        return lastSale;
    }

    public static void clearLastSale() {
        lastSale = null;
    }

    public static final class LastSale {
        private final String plantName;
        private final int quantity;

        private LastSale(String plantName, int quantity) {
            this.plantName = plantName;
            this.quantity = quantity;
        }

        public String getPlantName() {
            return plantName;
        }

        public int getQuantity() {
            return quantity;
        }
    }
}
