package StaticFieldAccessors;

public class StaticFieldAccessorsTest {

}

class A {
  private static final String description = "class A: description";

  public String getDescription() {
    return description;
  }
}

/**
 *@audit MissingFieldAccessViolation
 */

class B extends A {
  private static final String description = "class B: description";

  public String getDescription() {
    return "bred"; // overridden method does not access description variable
  }
}

class C extends B {
  private static final String description = "class C: description";

  public String getDescription() {
    return description;
  }
}

/**
 *@audit MissingAccessorMethodViolation
 */

class D extends A {
  private static final String description = "class D: description";
}

class AX {
  private static int count = 0;

  public static int getMyCount() {
    return count;
  }
}

class BX extends AX {
  private static int count = 0;

  public static int getMyCount() {
    return count;
  }
}

/**
 *@audit MissingFieldAccessViolation
 */

class CX extends BX {
  private static int count = 1;

  public static int getMyCount() {
    return 1;
  }
}
