package checkAbstract.a;

public abstract class A implements checkAbstract.b.c.I {
  /**
  * @audit MinimizeAccessViolation
  */
  public abstract java.lang.String getId();
  /**
  * @audit MinimizeAccessViolation
  */
  public abstract void setId(java.lang.String args);
}