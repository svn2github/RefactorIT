
class ZZZ {

        Object o = new ZZZ(){
            class Inner{}
            class XXX extends YYY {}
        }.new Inner(){};


  public void method() {
  }
}

class YYY {}
