class Test {
  public void method() {
    // Variables declaration
    A a;      // A
    B b;      // B
    Object o; // neither A nor B

    // New
    new A(); // A
    new B(); // B

    // Members invocation
    E e            // E
        = new E(); // E
    e
        .method(); // no sub - usage of E; super - E, F (since we don't know runtime type)
                   // sub - shouldn't affect

    new F();       // F
    F f            // F
        = new G(); // G
    f
        .method(); // sub/super - F
                   // sub       - F
                   // super     - F
                   // -----     - F

    G gg           // G
        = (G)f;    // G

    G g            // G
        = new G(); // G
    g
        .method(); // sub - F; G

    ((Object)g)
        .toString(); // super - D

    g
        .toString(); // G


    System.err.println(""
        + g);        // G
  }

  // Members declaration
  A aa;          // A
  B bb;          // B
  Object oo      // neither A nor B
      = new A(); // A
}

interface X {}

class A implements X {} // X

class B extends A {} // two usages of A: inheritance and default constructor


class C extends Exception {}

class D {
  void method() throws C { // C
    C c            // C
        = new C(); // C
    throw c;
  }

  void test1() throws Throwable {
    Throwable thr
        = new C(); // C
    throw thr;
  }

  public String toString() {
    return "D";
  }
}


class E {
  public void method() {}

  public static void static_method() {}

  public int field;

  public static int static_field;
}

class F extends E { // two usages of E
  public void method() {
    method(); // no sub - F; sub - F and G

    this.method(); // no sub - F; sub - F and G
    F.this.method(); // no sub - F; sub - F and G, F - class

    super.method();   // E
    F.super.method(); // E - method, F - class


    static_method(); // E

    E.static_method(); // E - method, E - class

    F.static_method(); // E - method, F - class

    G.static_method(); // E - method, G - class


    field = 0; // F

    this.field = 0; // F
    F.this.field = 0; // F, F - class

    super.field = 0; // E
    F.super.field = 0; // E - field, F - class


    static_field = 0; // E - field; F

    this.static_field = 0; // E - field; F
    F.this.static_field = 0; // E - field, F - class

    super.static_field = 0; // E
    F.super.static_field = 0; // E - field, F - class
  }

  public void overridable_method1(Object o) {
    overridable_method1(o); // no super - F only; super - F, G

    this.overridable_method1(o); // no super - F only; super - F, G

    F.this.overridable_method1(o); // no super - F only; super - F, G; F - class
  }

  public void overridable_method2(Object o) {
    overridable_method2(o); // F only

    this.overridable_method2(o); // F only

    F.this.overridable_method2(o); // F only; F - class
  }

  public int field;
}

class G extends F { // two usages of F
  void supermethod() {
    method(); // sub - F, G; no sub - G only

    this.method();  // sub - F, G; no sub - G only
    G.this.method(); // sub - F, G; no sub - G only -- method; G - class

    super.method(); // F
    G.super.method(); // F - method, G - class


    static_method(); // E

    E.static_method(); // E - method, E - class

    F.static_method(); // E - method, F - class

    G.static_method(); // E - method, G - class


    field = 0; // sub - F

    this.field = 0; // sub - F
    G.this.field = 0; // sub - F, G - class

    super.field = 0; // F
    G.super.field = 0; // F - field, G - class


    static_field = 0; // sub - E - field

    this.static_field = 0; // sub - E - field
    G.this.static_field = 0; // sub - E - field; G - class

    super.static_field = 0; // sub - E - field
    G.super.static_field = 0; // sub - E - field; G - class
  }

  F getF() { // F
    return new G(); // G
  }

  void parameter_test(F f) {} // F only, not G or E

  public void overridable_method1(Object o) {}

  public void overridable_method2(String s) {} // changed parameter type
}

class Test2 {
  static {
    Object o = new Object();
    System.err.println(""
        + o);        // super - D

    String s = A.class.getName(); // A
    Class a = A.class; // A

    try {
      throw new C(); // C
    } catch (C c) {} // C
  }
}