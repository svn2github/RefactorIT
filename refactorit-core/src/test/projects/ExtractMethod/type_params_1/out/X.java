import java.lang.reflect.*;

public class X {
  public static void main(String[] args) {
    Method m = null;
    Object o = m.getAnnotation(/*]*/newmethod()/*[*/).c();
  }

  static Class<MyAnon> newmethod() {
    return MyAnon.class;
  }
}

@interface MyAnon {
  Class c();     
}     
