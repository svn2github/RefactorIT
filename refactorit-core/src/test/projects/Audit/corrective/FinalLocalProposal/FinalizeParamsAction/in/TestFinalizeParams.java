package corrective.FinalLocalProposal.FinalizeParamsAction.in;

/**
 * @violations 4
 * @author  ars
 */
public class TestFinalizeParams {
  
  public int a(){
    int a = 6;
    if (a > 4){
      return a;
    } else {
      return 7;
    }
  }
  
  public void b(int c, int d){
    System.out.println( (c + d) );
  }
  
  public void c1(int a){
    if (a == 5){
      return;
    } else {
      a++;
      System.out.println(a);
    }
  }
  
  public void c2(int a){
    if (a == 5){
      return;
    } else {
      a  = 7;
      System.out.println(a);
    }
  }
  
  public void d(){
    int a = 7;
    
    for (int i = 0; i < 5; i++){
      if (a == 7){
        a = 3;
        return;
      }
    }
  }
  
  public void e1(){
    final int a = 7;
    
    for (int i = 0; i < 5; i++){
      if (a == 7){
        return;
      }
    }
  }
  
  public void e2(){
    final int a = 7, b = 5;
    
    for (int i = 0; i < 5; i++){
      if (a == 7){
        return;
      }
    }
  }
  
  public void f(int a){
    for (int i = 0; i < 5; i++){
      if (a == 7){
        return;
      }
    }
  }
  
  public void g(){
    int a = 0;
    for (int i = 0; i < 5; i++){
      a = 5;
    }
  }
  
}
