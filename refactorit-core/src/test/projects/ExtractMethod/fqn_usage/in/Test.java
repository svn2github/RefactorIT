
import java.awt.List;
import java.util.EmptyStackException;
import java.util.Map;
import java.util.Set;


public class Test {

  public Test() throws java.util.EmptyStackException {
    throw new java.util.EmptyStackException();
  }

  private void method(java.util.Date date) throws Exception {

    java.util.List list = null;
    Map map = null;
    Set set = null;

    /*]*/
    System.out.println("" + map + list + date);
    list = null;
    date = null;
    map = null;
    set = null;
    new Test();
    if (true) throw new java.util.NoSuchElementException();
    if (true) throw new NullPointerException();
    /*[*/

    System.out.println(list);
  }

}
