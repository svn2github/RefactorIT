/*
 * Holder.java
 *
 * Created on April 15, 2005, 3:44 PM
 */

package methods;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author  ars
 */
public class Test2 {

//  public <MT extends java.util.List> MT method(){
   //System.out.println("method1");
//   return null;
//  }

  public <MT extends java.util.Map> MT method(){
    //System.out.println("method2");
  //  return null;
  }
   
  public <MT> MT method(){
    //System.out.println("method3");
//    return null;
  }
  
//  public static void main(String[] args){
//    Holder instance = new Holder();
//    java.util.List aaa = instance.method();
//  }
}
