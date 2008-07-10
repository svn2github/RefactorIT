interface IReference {
  IReference CONSTANT=null;

}
class StaticReference implements IReference {
public void m() {
  IReference val=IReference.CONSTANT;

}

}

class C {

 void m() {
   Object o=IReference.CONSTANT;
 }

}
