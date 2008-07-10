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

    for (int max = list.size(), max0 = arrayList.size(), max1 = hashSet.size(), max2 = map.size(), max3 = new StringBuffer().length(), max4 = stack.size(); a < max && max0 < a || max1 >= b ||
         max2 == max3 && max4 == 1; a++) {
      break;
    }
  }

}
