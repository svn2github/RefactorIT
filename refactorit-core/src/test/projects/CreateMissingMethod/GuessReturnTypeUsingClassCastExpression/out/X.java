import java.util.List;
import java.util.ArrayList;

public class X {

 Class2 instanceField = new Class2();

 public List method() {
   switch (1) {
     case 1:
       return (ArrayList) instanceField.method2();
     case 2:

     default:
       return null;
   }
 }
}

class Class2 {

  public ArrayList method2() {
    //FIXME: implement this
    throw new java.lang.UnsupportedOperationException("Method method2() not implemented");
  }
}
 
