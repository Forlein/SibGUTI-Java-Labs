package task_08_comparable_max;

public class money implements Comparable<money> {
    private final int amount;

    public money(int amount) {
        this.amount = amount;
    }

    public int get_amount() {
        return amount;
    }

    @Override
    public int compareTo(money other_money) {
        /* Сравнение по величине; нужно, чтобы generic max мог упорядочивать money. */
        return Integer.compare(this.amount, other_money.amount);
    }

    @Override
    public String toString() {
        return "money(" + amount + ")";
    }
}
