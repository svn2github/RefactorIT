public class Temp2Field2 {
  private int cost;


  public void method() {
    cost = 10; // Do convert temp to field on this variable and specify name "cost"
    System.out.println("Price: " + cost);

    int cost;
    cost = 10;
    System.out.println("Cost: " + cost);
  }
}
