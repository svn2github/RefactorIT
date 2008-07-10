package CheckPossibleEjbMethods.a;

/**
 * @violations 1
 */
public class SBean implements javax.ejb.SessionBean {
  public String ejbHomeSome() {return null;}
  private void regularMethod() {}
}