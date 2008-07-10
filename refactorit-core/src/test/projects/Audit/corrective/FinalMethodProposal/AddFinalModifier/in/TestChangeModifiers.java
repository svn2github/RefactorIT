package corrective.FinalMethodProposal.AddFinalModifier.in;

/**
 * @violations 4
 */
public class TestChangeModifiers {

  public void method1(){

  }

  public void method2(){

  }

  public void method3(){

  }
}

class A extends TestChangeModifiers {
 
  public void method2(){

  }
}

class B extends A {


  public void method3(){

  }
}
