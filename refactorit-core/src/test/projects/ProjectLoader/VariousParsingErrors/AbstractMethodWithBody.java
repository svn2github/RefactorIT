public abstract class AbstractMethodWithBody { 
  public abstract void aMethodThatShouldNotHaveABodyHere () {
  	System.out.println( "This should not be in an abstract method" );
  }
}