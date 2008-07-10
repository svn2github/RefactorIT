package GenericsLoadFromBinary;

import generics.*;

public class UsesGenericClasses {
  
  public UsesGenericClasses() {
  }
  
  public void usage(){
    HasOneSimpleParameter a = new HasOneSimpleParameter();
    HasTwoParametersRecursion r = new HasTwoParametersRecursion();
    HasTwoParameters d = new HasTwoParameters();
    HasMultilevelArguments e = new HasMultilevelArguments();
    ExtendsParametrizedType p = new ExtendsParametrizedType();
  }
  
}
