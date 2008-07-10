import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Stack;

/**
 *  @violations 6
 */

public class Test04 {
  List list;
  ArrayList arrayList;
  HashMap hashSet;
  TreeMap map;
  Stack stack;
  private void method14() {
    int a = 0, b = a, c = b = a = 0, d = c = b = a = 1, e = d = c = b = a = 2;

    for (; a < list.size() && arrayList.size() < a || hashSet.size() >= b ||
         map.size() == new StringBuffer().length() && stack.size() == 1; a++) {
      break;
    }
  }

}
