
import java.util.Hashtable;

class Counts {
  Hashtable getTable(Integer i) { return null; }
}

public class Test {
  public Hashtable get(int id) {
    Counts counts = new Counts();
    return (Hashtable)counts.getTable(new Integer(id));
  }
}
