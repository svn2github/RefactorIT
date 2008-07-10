
public class B {

  public void method() {

    new A() {
      class LocalEntry extends A.AEntry {
      }

      public X.Entry getEntry() {
        return new LocalEntry();
      }
    };
  }
}
      