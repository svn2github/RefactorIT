class Anonym {
  public void anonym1() { }
  public void anonym2() { }
}

public class A {
  public void f1() {

    Anonym o = new Anonym() {
      public void anonym1() {
      }
    };
  }
}

class B {
}


