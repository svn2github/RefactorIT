interface IReference {
  StaticReference CONSTANT=null;

}
class StaticReference implements IReference {
public void m() {
  IReference val=StaticReference.CONSTANT;

}

}

class C {

 void m() {
   Object o=StaticReference.CONSTANT;
 }

}
