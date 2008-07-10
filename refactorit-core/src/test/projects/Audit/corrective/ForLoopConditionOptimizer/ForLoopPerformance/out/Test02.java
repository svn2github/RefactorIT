import java.util.List;

/**
*  @violations 6
*/

public class Test02 {
  List list, arrayList, hashSet, map, stack;
  private void method14() {
    int a = 0, b = a, c = b = a = 0, d = c = b = a = 1, e = d = c = b = a = 2;

    for (int i = 0, max = list.size(), max0 = arrayList.size(), max1 = hashSet.size(), max2 = map.size(), max3 = new StringBuffer().length(), max4 = stack.size();
         i < max && max0 < a || max1 >= b ||
         max2 == max3 && max4 == 1; i++) {
      break;
    }
  }

}
