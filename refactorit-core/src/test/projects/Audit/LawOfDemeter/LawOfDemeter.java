public class LawOfDemeter {
  private interface Interface {
    public void interfaceMethod();
  }

  private class Implementor implements Interface {
    public void interfaceMethod() {
      aMethod();
    }
  }

  private String str1;

  private String str2 = "just a string";

  private String str3 = new String();

  private String str4 = getAString();

  public LawOfDemeter(String str1) {
    this.str1 = str1;
  }

  public LawOfDemeter() {
    this("");
  }

  /**
   * @audit LawOfDemeterViolation
   * @audit LawOfDemeterViolation
   * @audit LawOfDemeterViolation
   */
  public void aMethod() {
    String str, lower;

    str = getAString();
    System.out.println(str);
    System.out.println(str.toUpperCase());
    lower = str.toLowerCase();
    str2 = str1;

    str2 = str1.toUpperCase().toLowerCase();
  }

  private String getAString() {
    aMethod();
    return 0;
  }

  private Class1 c1 = new Class1();

  /**
   * @audit LawOfDemeterViolation
   */
  private String field1 = c1.class2.field;

  /**
   * @audit LawOfDemeterViolation
   */
  private String field2 = c1.getClass2().getField();

  /**
   * @audit LawOfDemeterViolation
   */
  private void tooLongFieldInvocationChain(Class1 c1) {
    c1.class2.field.toString();
  }

  /**
   * @audit LawOfDemeterViolation
   * @audit LawOfDemeterViolation
   */
  private void tooLongMethodInvocationChain(Class1 c1) {
    c1.getClass2().getField();
    c1.getClass2().field.toUpperCase();
  }
}

class Class1{
  Class2 class2 = new Class2();

  public Class2 getClass2() {
    return class2;
  }
}

class Class2 {
  String field = new String();

  public String getField() {
    return field;
  }
}

class LocalsTest {
  private javax.swing.JButton button = new javax.swing.JButton("Button");

  private void aMethod() {
  }

  private void listenerAdder() {
    button.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        aMethod();
      }
    });
  }
}

class TestManyMethodsOfThisClassInvoked {
  public TestManyMethodsOfThisClassInvoked testField = this;

  public TestManyMethodsOfThisClassInvoked method1() {
    return this;
  }

  public TestManyMethodsOfThisClassInvoked method2() {
    return this;
  }

  public TestManyMethodsOfThisClassInvoked method3() {
    return this;
  }

  public String getString() {
    return "";
  }

  // Not violative
  public TestManyMethodsOfThisClassInvoked testMethod1() {
    method1().method2().method3().getString();
    return this;
  }

  // Not violative
  public void testMethod2() {
    testField.method1().method2().method3().getString();
  }

  // Not violative
  public void testMethod3() {
    testField.testField.testField.testField.testMethod1().testMethod1().testField
        .getString();
  }
}

class TestManyMethodsOfOuterClassInvoked {
  private TestManyMethodsOfThisClassInvoked testField = new TestManyMethodsOfThisClassInvoked();

  /**
   * @audit LawOfDemeterViolation
   */
  private void testMethod1() {
    testField.method1().method2().method3().getString();
  }

  /**
   * @audit LawOfDemeterViolation
   */
  private void testMethod2() {
    testField.testField.method1();
  }

  // Not violative
  private void testMethod3() {
    testField.method1();
  }

  /**
   * @audit LawOfDemeterViolation
   */
  private void testMethod4() {
    testField.testField.testMethod1();
  }
}

class TestIterators {
  /**
   * @audit LawOfDemeterViolation
   */
  public void testMethod1(java.util.ArrayList list) {
    for (java.util.Iterator a = list.iterator(); a.hasNext();) {
      Class1 c1 = (Class1) a.next();
      c1.getClass2().getField();
    }
  }

  /**
   * @audit LawOfDemeterViolation
   */
  public void testMethod2(java.util.ArrayList list) {
    for (java.util.Iterator a = list.iterator(); a.hasNext();) {
      ((Class1) a.next()).getClass2().getField();
    }
  }

  // Not violative
  public void testMethod3(java.util.ArrayList list) {
    for (java.util.Iterator a = list.iterator(); a.hasNext();) {
      ((Class1) a.next()).getClass2();
    }
  }
  
  public void testMethod4(java.util.ArrayList list) {
    for (java.util.Iterator a = list.iterator(); a.hasNext();) {
      Class1 c1 = (Class1) a.next();
      c1.getClass2();
    }
  }

  /**
   * @audit LawOfDemeterViolation
   */
  public void testMethod5(java.util.HashMap map) {
    for (java.util.Iterator a = map.keySet().iterator(); a.hasNext();) {
      Class1 c1 = (Class1) a.next();
      c1.getClass2();
    }
  }
}

class TestInnerUsage {
  private static class Inner {
    public Inner field = this;

    public Inner method1() {
      return this;
    }

    public Inner method2() {
      return this;
    }

    public String getString() {
      return "";
    }

    public TestInnerUsage testMethodInner1() {
      return method1();
    }

    public Inner testMethodInner2() {
      return method1().method2().field;
    }
  }

  private Inner field = new Inner();

  private TestInnerUsage method1() {
    return this;
  }

  private TestInnerUsage method2() {
    return this;
  }

  // Not violative
  private void testMethod1() {
    method1().method2().field.method1();
  }

  private void testMethod2() {
    method1().method2().method1().field.method1().method2().method1()
        .getString();
  }
}

class StaticMethodHolder {
  public static StaticMethodHolder field = new StaticMethodHolder();

  public static StaticMethodHolder method() {
    return field;
  }
}

class StaticsTest {
  // Not violative
  public void testMethod1() {
    StaticMethodHolder.method();
  }

  // Not violative
  public void testMethod2(){
		StaticMethodHolder.field;
  }

  /**
   * @audit LawOfDemeterViolation
   */
  public void testMethod3() {
    StaticMethodHolder.field.method();
  }

  /**
   * @audit LawOfDemeterViolation
   */
  public void testMethod3(){
		StaticMethodHolder.method().field;
  }
}

class HashMapTest {
  /**
   * @audit LawOfDemeterViolation
   * @audit LawOfDemeterViolation
   */
  public static void main(String[] args) {
    java.util.HashMap m = new java.util.HashMap();

    for (java.util.Iterator a = m.keySet().iterator(); a.hasNext();)
      System.out.println(a.next());
  }
}