package ParameterOrder;

/**
 *
 */
public class MixedParams {

/**
 *
 */
  public void rabbitFunction(Object rabbit, Object bear){
    
  }
/**
 *
 */  
  public void foxFunction(Object fox, Object owl){
    
  }
/**
 *
 */  
  public void owlFunction(Object mafox, Object powl2){
    
  }
  
/**
 *
 */
  public MixedParams() {
  }
  
/**
 *
 */  
  public void testRabbit1(){
    rabbitFunction(new String("zajka"), new Integer(4));
    
    rabbitFunction(new Float(4.5), new Integer(4));
  }

/**
 *@audit ParameterOrder
 */
  public void testRabbit2(){
    String rabbit = "zajka";
    String bear = "medved";
    
    rabbitFunction(rabbit, bear);
    
    rabbitFunction(bear, rabbit);
  }
  
  String fox = "lisa";
  String owl = "sova";

/**
 *@audit ParameterOrder
 */
  public void testFox(){
    
    
    foxFunction(fox, owl);
    
    foxFunction(owl, fox);
  }

/**
 *@audit ParameterOrder
 *@audit ParameterOrder
 */  
  public void testCrazyFox(){
    String myFox = "crazy fox";
    String sowl = "super owl";
    String powl2 = "power owl 2";
    String Foxy = "foxy lady";
    
    foxFunction(myFox, powl2);
    foxFunction(powl2, myFox);
    foxFunction(sowl, Foxy);
  }

/**
 *
 */  
  public void testCrazyOwl(){
    owlFunction(fox, owl);
  }
}
