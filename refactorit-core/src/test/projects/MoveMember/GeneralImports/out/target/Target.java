package target;

import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import x.Test;


public class Target {

  public List method(Map map) throws NoSuchElementException, SQLException, java.io.EOFException {
    if (map instanceof AbstractMap) {
      throw new NoSuchElementException();
    }
    new Test.Inner().i = 5;
    java.io.File file;
    return new ArrayList(((HashMap) map).keySet());
  }
}
