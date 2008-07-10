import java.lang.reflect.*;

public class X {
  public static void main(String[] args) {
    Method m = null;
    Object o = m.getAnnotation(/*]*/MyAnon.class/*[*/).c();
  }
}

@interface MyAnon {
  Class c();     
}     
