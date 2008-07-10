package CheckPossibleEjbMethods.a;

public class SBean implements javax.ejb.SessionBean {
  public String ejbHomeSome() {return null;}
  /**
   * @audit MinimizeAccessViolation
   */
  public void regularMethod() {}
}