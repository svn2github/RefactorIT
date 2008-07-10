package ConstantFieldProposal;

/**
 *
 * @audit LowercaseConstant
 * @audit ConstantFieldProposal
 * @author  ars
 */
public class Source1 {
  
  public static final int isConstant = 4; // const, but name is not uppercase - violate
  public static final int IS_CONSTANT = 4; // const and correct name
  public int canBeConstant = 5; // can be constant - violate
  public int cantBeConstant1 = 3; // reassigned in this class - no viol
  public static int cantBeConstant2 = 2; // reassigned in other class - no viol
    
  public void testReassignValue(){
    cantBeConstant1 = 1;
  }
  
  public void testReadValue(){
    if (isConstant == 5 && IS_CONSTANT == 4){
      // do something
    }
  }
  
}
