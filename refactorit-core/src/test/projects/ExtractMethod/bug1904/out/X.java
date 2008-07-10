
public class X {
  public void m() {
    Object listener = /*]*/newmethod();/*[*/
  }

  Object newmethod() {
    Object listener;

    listener = new Object() {};

    return listener;
  }
}
