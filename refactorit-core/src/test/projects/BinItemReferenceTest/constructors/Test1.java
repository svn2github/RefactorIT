/*
 * Test1.java
 *
 * Created on April 19, 2005, 12:53 PM
 */

package constructors;

import java.util.List;
import java.util.Map;

/**
 *
 * @author  Arseni Grigorjev
 */
public class Test1 {
  
  /** Creates a new instance of Test1 */
  public Test1(List list) {
  }
  
  public Test1(List<String> list){
    
  }
  
  public Test1(){
    
  }
  
  public Test1(){
    
    
    
  }
  
  public <T> Test1(List<T> listT){
    
  }
  
  public <T extends Map<String, Integer>> Test1(List<T> listT){
    
  }
  
}
