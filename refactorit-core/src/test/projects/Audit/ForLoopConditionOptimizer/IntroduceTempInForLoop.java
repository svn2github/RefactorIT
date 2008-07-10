package ForLoopConditionOptimizer;

import java.util.*;

public class IntroduceTempInForLoop {

  public List list;
  public ArrayList arrayList;
  Set hashSet = new HashSet();
  HashMap map = new HashMap();
  Stack stack = new Stack();

  /**
   * @audit ForLoopPerformance
   * @audit ForLoopPerformance
   * @audit ForLoopPerformance
   * @audit ForLoopPerformance
   * @audit ForLoopPerformance
   * @audit ForLoopPerformance
   */
  private void method14() {
    int a = 0, b = a, c = b = a = 0, d = c = b = a = 1, e = d = c = b = a = 2;

    for (int i = 0;
         i < list.size() && arrayList.size() < a || hashSet.size() >= b ||
         map.size() == new StringBuffer().length() && stack.size() == 1; i++) {
      break;
    }
  }

  /**
   * @audit ForLoopPerformance
   * @audit ForLoopPerformance
   * @audit ForLoopPerformance
   * @audit ForLoopPerformance
   * 
   * @audit ForLoopPerformance
   *
   */
  private void method13() {
    for (int i = 0; i < list.size(); i++)
      for (int i1 = 0; i1 < arrayList.size(); i++)
        for (boolean b = true; i1 < hashSet.size(); i++)
          for (int i3 = 0; i3 < map.size(); i++)
            for (int i4 = 0; i4 < stack.size(); i++) {
            }
  }

  /**
   * @audit ForLoopPerformance
   * @audit ForLoopPerformance
   * @audit ForLoopPerformance
   * 
   * @audit ForLoopPerformance
   */
  private void method12() {
    {
      CollectionClass cc = new CollectionClass();

      for (int i = 0; i < cc.size(); i++) {

      }
      int i = 0;
      for (; i < cc.size(); i++) {

      }

      for (; 0 < cc.size(); ) {
        break;
      }

      for (float f = 1f; 0 < cc.size(); ) {
        break;
      }
    }
  }

  /**
   * @audit ForLoopPerformance
   */
  private void method11() {
    for (int i = 0; i < new CollectionClass().size(); i++) {

    }
  }

  private void method10() {
    for (; ; ) {
      break;
    }
  }

  private void method9() {
    for (int i = 0; ; i++) {
      break;
    }
  }

  private void method8() {
    for (int i = 0; i < 10; i++);
  }

  /**
   * @audit ForLoopPerformance
   */
  private void method7() {
    {
      int i = 0;
      for (list = new ArrayList(), i = 9; i < list.size(); i++);
    }
  }

  /**
   * @audit ForLoopPerformance
   */
  private void method6() {
    {
      int i = 0;
      for (; i < list.size(); i++);
    }
  }

  /**
   * @audit ForLoopPerformance
   */
  private void method5() {
    for (int i = 0; i < stack.size(); i++);
  }

  /**
   * @audit ForLoopPerformance
   */
  private void method4() {
    for (int i = 0; i < map.size(); i++);
  }

  /**
   * @audit ForLoopPerformance
   */
  private void method3() {
    for (int i = 0; i < hashSet.size(); i++);
  }

  /**
   * @audit ForLoopPerformance
   */
  private void method2() {
    for (int i = 0; i < arrayList.size(); i++);
  }

  /**
   * @audit ForLoopPerformance
   */
  private void method1() {
    for (int i = 0; i < list.size(); i++);
  }

  private void method0() {
    class SomeClass {
      public int size() {
        return 0;
      }
      public int length() {
        return 0;
      }
    }

    SomeClass sc = new SomeClass();

    for(int i=0; i < sc.size(); i++) {
      break;
    }

    for (int i = 0; i < sc.length(); i++) {
      break;
    }

  }

  private void method_1() {
    class SomeClass {
      public boolean size() {
        return true;
      }
      public SomeClass length() {
        return null;
      }
    }

    SomeClass sc = new SomeClass();

    for(int i=0; (2 == 2) || sc.size(); i++) {
      break;
    }

    for (int i = 0; new SomeClass() == sc.length(); i++) {
      break;
    }

    for(; (2 == 2) || sc.size(); ) {
      break;
    }

    for ( ;new SomeClass() == sc.length();) {
      break;
    }

  }


  class CollectionClass
      implements Collection {
    public int hashCode() {
      // FIXME
      throw new java.lang.UnsupportedOperationException(
          "method hashCode not implemented yet");
    }

    public Iterator iterator() {
      // FIXME
      throw new java.lang.UnsupportedOperationException(
          "method iterator not implemented yet");
    }

    public boolean isEmpty() {
      // FIXME
      throw new java.lang.UnsupportedOperationException(
          "method isEmpty not implemented yet");
    }

    public boolean add(Object obj) {
      // FIXME
      throw new java.lang.UnsupportedOperationException(
          "method add not implemented yet");
    }

    public boolean contains(Object obj) {
      // FIXME
      throw new java.lang.UnsupportedOperationException(
          "method contains not implemented yet");
    }

    public boolean removeAll(Collection collection) {
      // FIXME
      throw new java.lang.UnsupportedOperationException(
          "method removeAll not implemented yet");
    }

    public boolean addAll(Collection collection) {
      // FIXME
      throw new java.lang.UnsupportedOperationException(
          "method addAll not implemented yet");
    }

    public void clear() {
      // FIXME
      throw new java.lang.UnsupportedOperationException(
          "method clear not implemented yet");
    }

    public boolean equals(Object obj) {
      // FIXME
      throw new java.lang.UnsupportedOperationException(
          "method equals not implemented yet");
    }

    public Object[] toArray(Object[] arrobj) {
      // FIXME
      throw new java.lang.UnsupportedOperationException(
          "method toArray not implemented yet");
    }

    public Object[] toArray() {
      // FIXME
      throw new java.lang.UnsupportedOperationException(
          "method toArray not implemented yet");
    }

    public int size() {
      // FIXME
      throw new java.lang.UnsupportedOperationException(
          "method size not implemented yet");
    }

    public boolean retainAll(Collection collection) {
      // FIXME
      throw new java.lang.UnsupportedOperationException(
          "method retainAll not implemented yet");
    }

    public boolean containsAll(Collection collection) {
      // FIXME
      throw new java.lang.UnsupportedOperationException(
          "method containsAll not implemented yet");
    }

    public boolean remove(Object obj) {
      // FIXME
      throw new java.lang.UnsupportedOperationException(
          "method remove not implemented yet");
    }
  }

}
