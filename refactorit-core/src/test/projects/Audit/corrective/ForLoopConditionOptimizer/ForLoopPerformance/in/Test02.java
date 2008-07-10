import java.util.List;

/**
*  @violations 6
*/

public class Test02 {
  List list, arrayList, hashSet, map, stack;
  private void method14() {
    int a = 0, b = a, c = b = a = 0, d = c = b = a = 1, e = d = c = b = a = 2;

    for (int i = 0;
         i < list.size() && arrayList.size() < a || hashSet.size() >= b ||
         map.size() == new StringBuffer().length() && stack.size() == 1; i++) {
      break;
    }
  }

}
