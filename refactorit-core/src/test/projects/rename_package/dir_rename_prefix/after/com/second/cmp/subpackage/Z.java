package com.second.cmp.subpackage;

/** Here we make sure RIT can also move a bit more complicated types -- inners, anonymous classes, etc */
public class Z {
  public void m() {
    class Local {};
    Object anonymous = new Object() {};
    System.out.println(anonymous);
  }

  public static class Inner1 {}
  public class Inner2 {}
}