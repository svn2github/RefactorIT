package p1;

import p2.TestInnerClasses.InnerClass;
import p2.X;
import java.lang.String;
import p2.Z;
import p1.C;
import p2.Y;
import p1.A.Inner1;
import p1.A.Inner2;
import p1.A.Inner3.Inner3_1.Inner3_2;
import p1.A.Inner3.Inner3_1;
import p1.A.Inner4;

public class A {
	int i = InnerClass.m();
  X x = new X();
  String s = "";
  p2.Z z = new p2.Z();
  C c = new C();

  public static class Inner1 { }
  public static class Inner2 { }

  public static class Inner3 {
    Inner3_2 i1;

    public static class Inner3_1 {
      public static class Inner3_2 {
        Inner3_1 i2;
      }
    }
  }

  public static class Inner4 { }
}

class B {
  Inner1 i = new Inner1();
  A.Inner4 i2 = new A.Inner4();
}
