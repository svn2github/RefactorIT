package EqualsHashcode;


/**
 * #hashCode() is overriden but #equals() is not.
 *
 */
public class C {
  /**
   * @audit EqualsNotDeclared
   */
  public int hashCode(){
    return 0;
  }
}
