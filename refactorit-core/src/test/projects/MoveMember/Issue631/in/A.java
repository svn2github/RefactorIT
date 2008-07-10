public abstract class A {
  private int foo = 0;
  
  public void process(String xxx) {
      processInfo(xxx);
  }
  
  private void processInfo(String xxx) {
    foo++;
  }
  
}