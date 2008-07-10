package ConstantFieldProposal;

/**
 *
 * @author  ars
 */
public class Source2 {
  private static int cantBeConstant = 2; // reassigned, no violation
  
  public void testReassignValue() {
    // field from other class
    Source1.cantBeConstant2 = 1;
    // field from this class
    cantBeConstant = 2;
  }
  
}
