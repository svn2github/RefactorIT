class Test1 { // +1 RFC (extends Object constructor in default constructor)
  void test() {};
}

class Test2 { // +1 RFC (call to Object constructor in default constructor)
  static {
    (new Test1()).test(); // +2 RFC
  }
}

class Test3 { // +1 RFC (call to Object constructor in default constructor)
  void test() {
    test(); // +1 RFC
  }
}

class Test4 { // +1 RFC (call to Object constructor in default constructor)
  {
    System.out.println("Hello" + this); // +3 RFC
  }
}

class Test5 { // +1 RFC (call to Object constructor in default constructor)
  String test() {
    return super.toString(); // +1 RFC
  }
}

class Test6 { // +1 RFC (call to Object constructor in default constructor)
  {
    Object[] tmp = new Object[] {"a", "b"}; // +0 RFC (don't count default constructor of anonymous array here)
    if (tmp.equals(tmp)) { // +1 RFC
      tmp.equals(tmp); // +0 RFC (Object[].equals() already counted)
    }
  }
}

class Test7 { // +1 RFC (call to Object constructor in default constructor)
  private String a;
  String getA() {
    return a;
  }

  String getB() {
    return a;
  }

  public String toString() {
    return "[" + getA() + ", " + getB() + "]"; // +3 RFC
  }
}