public class Test {
  
  public static final int public_field;
  private int private_field;
  
  public void public_methodA1() {
    public_methodA2();
  }
  public void public_methodA2() {
  }


  public void public_methodB1() {
    private_methodB2();
  }
  private void private_methodB2() {
  }


  private void private_methodC1() {
    public_methodC2();
  }
  public void public_methodC2() {
  }


  public void public_methodD1() {
    public_methodD2();
  }
  public void public_methodD2() {
    public_methodD1();
  }
  
  
  public void public_methodE() {
    public_field = 0;
  }


  private void private_methodF() {
    public_field = 0;
  }


  public void public_methodG() {
    private_field = 0;
  }


  private void private_methodH() {
    private_field = 0;
  }  
  
  public int public_field2 = public_methodX();
  public int public_field3 = private_methodY();  
  private int private_field2 = public_methodX();
  private int private_field3 = private_methodY();
  
  public int public_methodX() { 
    return 0; 
  }

  private int private_methodY() {
    return 0;
  }
  
}