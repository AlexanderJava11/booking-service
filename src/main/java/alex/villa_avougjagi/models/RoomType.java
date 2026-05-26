package alex.villa_avougjagi.models;

public enum RoomType {

    SINGLE ("Enkelrum", 1),
    DOUBLE ("Dubbelrum", 2);

    private final String displayName;
    private final int baseCapacity;

    RoomType(String displayName, int baseCapacity) {
        this.displayName = displayName;
        this.baseCapacity = baseCapacity;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getBaseCapacity() {
        return baseCapacity;
    }
}
