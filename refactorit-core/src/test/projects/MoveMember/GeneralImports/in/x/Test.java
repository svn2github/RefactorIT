package x;

import java.util.*;
import java.sql.SQLException;

public class Test {

  public static class Inner {
    public int i;
  }

  public List method(Map map) throws NoSuchElementException, SQLException, java.io.EOFException {
    if (map instanceof AbstractMap) {
      throw new NoSuchElementException();
    }
    new Inner().i = 5;
    java.io.File file;
    return new ArrayList(((HashMap) map).keySet());
  }
}
