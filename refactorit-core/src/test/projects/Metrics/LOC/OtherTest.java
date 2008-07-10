public class OtherTest {

  private String text;


  public void setText(String text) {
    this.text = text;
  }

  public String getText() {
    return text;
  }

  public void testBug1303() {
    if (true) {
      // test
      System.out.println("test");
    }
  }
  
}
