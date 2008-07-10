package genericsrefact.test4;

import java.util.ArrayList;
import java.util.List;

public class Test4 {
  
  public void a() {
    ProxyClass<String> proxy = new ProxyClass<String>();
    proxy.take("string");
    List<String> list = proxy.getObjectWithList().getList();
  }
  
}


class ProxyClass <PCT> {
  ObjectWithList<PCT> getObjectWithList(){
    return new ObjectWithList<PCT>();
  }
  
  public void take(PCT zju){
    
  }
}


class ObjectWithList <OBJT> {
  List<OBJT> getList(){
    return new ArrayList<OBJT>();
  }
}
