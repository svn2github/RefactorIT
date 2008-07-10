package EqualsHashcode;


/**
 * Both #equals() and #hashCode() are overriden.
 */
public class B {
  /**
   */
  public boolean equals(Object that){
    return (this == that);
  }

  /**
   */
  public int hashCode(){
    return 0;
  }
}
