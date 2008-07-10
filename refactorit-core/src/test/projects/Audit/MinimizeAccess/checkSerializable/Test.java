package checkSerializable;

import java.io.*;

public class Test implements Serializable {
  public static final long serialVersionUID = 200502221212L;
}

class B implements Serializable {
  /**
   * @audit MinimizeAccessViolation
   */
  public static final int serialVersionUID = 20050222;
}

class C {
  /**
   * @audit MinimizeAccessViolation
   */
  public static final long serialVersionUID = 200502221212L;
}