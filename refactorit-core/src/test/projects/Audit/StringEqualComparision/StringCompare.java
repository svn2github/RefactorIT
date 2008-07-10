package StringEqualComparision;

  /**
   *
   */
public class StringCompare {
  
  /**
   *@audit StringEqualComparision
   */
  public void test1 () {
    String a = "";
    String b = "";
    
    if (a == b){
      //bug
    }
    
    if (a.equals(b)){
      // not bug
    }
  }

  /**
   *@audit StringEqualComparision
   */  
  public void test2(){
    String a = "";
    String b = "";
   
    if (a != b){
      // bug
    }
    
    if (!a.equals(b)){
      // not bug
    }
  }
  
  /**
   *@audit StringEqualComparision
   *@audit StringEqualComparision
   */
  public void test3(){
    String a = "awera";
    String b = "dsfsf";
    
    if ((a + b) == "adsa"){
      // bug
    }
    
    if ((a) == "b"){
      // bug
    }
  }
  
}
