package EqualsOnDiffTypesRule;

import java.util.*;

public class A {

  /**
   * @audit EqualsOnDiffTypes
   */
  public void test1(){
  
    String abcd = "efgh";
    Integer a5 = new Integer(5);
    
    if (abcd.equals(a5)){
    
    }
  }

  /**
   * 
   */
  public void test2(){
  
    String abcd = "efgh";
    String efgh = "abcd";
    
    if (efgh.equals(abcd)){
    
    }
  }

  /**
   * 
   */
  public void test3(){
  
    String abcd = "efgh";
  
    if ("efgh".equals(abcd)){
    
    }
  }

  /**
   *
   */
  public void test4(){
     String aString = "a";
     if (funct(aString)){
     
     }
  }
  
  /**
   * @audit EqualsOnDiffTypesSameBranch
   */
  public void test5(){
    List l = new ArrayList();
    if( l.get(0).equals("a")){
      
    }
  }

  /**
   * @audit EqualsOnDiffTypesSameBranch
   */
  public void test6(){
    List l = new ArrayList();
    if( "a".equals(l.get(0))){
      
    }
  }

  /**
   * 
   */
  public void test7(){
    List l = new ArrayList();
    if( l.get(1).equals(l.get(0))) {
      
    }
  }
  /**
   * @audit EqualsOnDiffTypesSameBranch
   */
  public boolean funct(Object mayBeString){
  
    String const = "some constant string";
    if (const.equals(mayBeString)){
      return true;
    }
    return false;
  }
  
}
