package possiblecallnpe;

public class PossibleCallNPE {
  DemoClass src = new DemoClass();

  Interfacable ax = new A();

  A az = new A();

  /**
   * @audit PossibleNPEViolation
   */
  public void m1() {
    src.meth1().substring(1);
  }

  /**
   * @audit PossibleNPEViolation
   */
  public void m2() {
    src.meth2(1).substring(1);
  }

  /**
   * @audit PossibleNPEViolation
   */
  public void m3() {
    src.meth3(1).substring(1);
  }

  /**
   * @audit PossibleNPEViolation
   */
  public void m4() {
    src.meth4(1).substring(1);
  }

  /**
   * @audit PossibleNPEViolation
   */
  public void m5() {
    src.meth5(1).substring(1);
  }

  /**
   * @audit PossibleNPEViolation
   */
  public void m6() {
    src.meth6("").substring(1);
  }

  /**
   * @audit PossibleNPEViolation
   */
  public void m7() {
    src.meth7(1).substring(1);
  }

  /**
   * @audit PossibleNPEViolation
   */
  public void m8() {
    src.meth8(1).substring(1);
  }

  /**
   * @audit PossibleNPEViolation
   */
  public void m9() {
    src.meth9(1).substring(1);
  }

  /**
   * @audit PossibleNPEViolation
   */
  public void m10() {
    src.meth10(1).substring(1);
  }

  /**
   * @audit PossibleNPEViolation
   */
  public void m11() {
    src.meth11(1).substring(1);
  }

  /**
   * @audit PossibleNPEViolation
   */
  public void m12() {
    src.meth12(1).substring(1);
  }

  /**
   * @audit PossibleNPEViolation
   */
  public void m13() {
    ax.test1().substring(1);
  }

  /**
   * @audit PossibleNPEViolation
   */
  public void m14() {
    az.test2().substring(1);
  }

  /**
   * 
   */
  public void m15() {
    src.methodGood1().substring(1);
  }

  /**
   * 
   */
  public void m16() {
    src.methodGood2().substring(1);
  }

  /**
   * 
   */
  public void m17() {
    src.methodGood3().substring(1);
  }
}

class DemoClass {

  public String meth1() {
    return null;
  }

  public String meth2(int k) {
    if (k > 0) {
      return null;
    }
    return "";
  }

  public String meth3(int k) {
    String f;
    try {
      f = "";
    } catch (Exception e) {
    }
    return f;
  }

  public String meth4(int k) {
    String f = "";
    if (k > 4) {
      f = null;
      return f;
    }
    return "";
  }

  public String meth5(int k) {
    switch (k) {
    case 0:
      return "0";
      break;

    case 1:
      return "1";
      break;

    default:
      return null;
    }
  }

  public String meth6(String s) {
    if (s.length() > 3) {
      return s;
    }
    return "demo";
  }

  public String meth7(int k) {
    String a;
    String b = a;
    Object c = b;
    Object d;
    d = c;
    return (String) d;
  }

  public String meth8(int k) {
    String s = meth7(k);
    return s;
  }

  public String meth9(int k) {
    String a = "";
    String b = a;
    Object c = b;
    Object d;
    if (k > 10) {
      c = null;
    }
    d = c;
    return (String) d;
  }

  public String meth10(int k) {
    String s = "a";
    String b = meth9(1);
    if (k < 10) {
      s = b;
    }
    return s;
  }

  public String meth11(int k) {
    String s = "abc";
    String r;

    if (k > 10) {
      s = r;
    }
    return s;
  }

  public String meth12(int k) {
    String s;
    // won`t check all ifelse statement lists
    // for correct assignments, consider bad style
    if (k > 10) {
      s = "a";
    } else {
      s = "b";
    }
    return s;
  }

  // good methods

  public String methodGood1() {
    String s;
    s = "mystring";
    return s;
  }

  public String methodGood2() {
    String a = "";
    String b = a;
    Object c = b;
    Object d;
    d = c;
    return (String) d;
  }

  public String methodGood3() {
    return methodGood2();
  }
}

interface Interfacable {
  public String test1();
}

class A implements Interfacable {
  public String test1() {
    return "";
  }

  public String test2() {
    return null;
  }
}

class B implements Interfacable {
  public String test1() {
    return null;
  }
}

class C extends A {
  public String test2() {
    return "";
  }
}