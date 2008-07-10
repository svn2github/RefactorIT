package RebuildArrayTypesTest;

public class WillBeChangedAndRebuilt {
  
  public WillBeChangedAndRebuilt() {
  }
 
  public void intDiv(){
    
    float c =  1 / 2;
    c++;
  }
  
  public void floatCompare(){
    float a = 0; float b = 2;
    if (a == b){
      System.out.println("lalala");
    }
  }
}
