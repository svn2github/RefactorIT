import java.util.ArrayList;

public class A{
  int i1 = 10;
  int i2 = 30;
  ArrayList<B.Inner> l = new ArrayList<B.Inner>();

  public void m(){
    ArrayList<B.Inner> l = new ArrayList<B.Inner>();
    newmethod(l);
  }

  void newmethod(final ArrayList<B.Inner> l) {
    /*[*/
    if(l.size() != 0)
      l.add(null);
    /*]*/
  }
}

class B{
  static class Inner{
  }
}