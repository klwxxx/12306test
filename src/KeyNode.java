public class KeyNode {
    private long value;
    private int continueCount;

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    public int getContinueCount() {
        return continueCount;
    }

    public void setContinueCount(int continueCount) {
        this.continueCount = continueCount;
    }

    public KeyNode(long value, int continueCount) {
        this.value = value;
        this.continueCount = continueCount;
    }

}
