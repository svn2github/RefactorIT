package corrective.StringEqualComparision.ChangeToEqualsAction.in;
/**
 *@violations 4
 */
public class TestStringCompare {
  

  public TestStringCompare() {
  }
  
  public void test1 () {
    String a = "";
    String b = "";
    
    if (a.equals(b)){
      //bug
    }
    
    if (a.equals(b)){
      // not bug
    }
  }
  
  public void test2(){
    String a = "";
    String b = "";
   
    if (!a.equals(b)){
      // bug
    }
    
    if (!a.equals(b)){
      // not bug
    }
  }
  
  public void test3(){
    String a = "awera";
    String b = "dsfsf";
    
    if (("adsa").equals(a + b.replace('a','b'))){
      // bug
    }
    
    if ("b".equals(a)){
      // bug
    }
  }
  
}
