public class Gold extends Customer {
    private Double discountPercentage;

    public Gold(String name, String lastName, String guestID, Double amountSpent, Double discount) {
        super(name, lastName, guestID, amountSpent);
        this.discountPercentage = discount;
    }

    public Double getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(Double discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

}
