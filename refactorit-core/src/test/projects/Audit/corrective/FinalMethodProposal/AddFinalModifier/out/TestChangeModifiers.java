package corrective.FinalMethodProposal.AddFinalModifier.in;

/**
 * @violations 4
 */
public class TestChangeModifiers {

  public final void method1(){

  }

  public void method2(){

  }

  public void method3(){

  }
}

class A extends TestChangeModifiers {
 
  public final void method2(){

  }
}

class B extends A {


  public final void method3(){

  }
}
