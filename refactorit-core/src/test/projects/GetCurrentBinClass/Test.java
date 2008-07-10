package com.acme;

import Test2;
import org.test.*;
import org.test.test.ImportTest;

/**
 * Just a pointless code that is used fro testing SourceUtil.getCurrentBinClass()
 */
public class Test extends AbstractTest implements java.io.Serializable {
  
  String name;
  
  public final int NULL_INT = 0;
  
  public Test() {
    
  }
  
  public String getName() throws Exception {
    return name;
  }
  
  public void moreComplexMethod(int param1, Object param2) throws IOException {
    int aa = param1;
    if (param2 instanceof String) {
      System.out.println(param2);
    }
    
    try {
      SomeObject obj = (SomeObject)param1;
    } catch (Exception e1) {
      throw new java.io.IOException("error");
    }
    String bli = getName();
    bli = this.getName();
    int i = bli.indexOf(" ");
  }
  
  public static void main(java.lang.String[] args) {
    Test test = new Test();
    Test test2 = new com.acme.Test();
    int i = test2.getName().indexOf(" ");
    oracle.ide.Ide.getLogManager().getMsgPage().log(msg + "\n");
    String ss = test.name;
    test.name = "bla";
    int l = args.length;
    for (int aa = 0, bb = 0; aa == bb; ) {
      aa++;
    }
  }
}
  
  