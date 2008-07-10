public class Temp2Field2 {

  public void method() {
    int price = 10; // Do convert temp to field on this variable and specify name "cost"
    System.out.println("Price: " + price);

    int cost;
    cost = 10;
    System.out.println("Cost: " + cost);
  }
}
