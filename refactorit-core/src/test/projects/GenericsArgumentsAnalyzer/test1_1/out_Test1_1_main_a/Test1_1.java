package genericsrefact.test1_1;

import java.util.HashMap;

public class Test1_1 {
  
  public static void main(String[] args){
    HashMap<String, Serializable> a;
    HashMap<String, Serializable> b;
    HashMap<String, Serializable> c;
    HashMap<String, Serializable> d;
    
    HashMap<String, Serializable> u;
    HashMap<String, Serializable> v;
    
    HashMap<String, Serializable> t;
    
    a = b;
    b = c;
    b = d;
    d = a;
    
    u = a;
    u = v;
    
    t = d;
    
    v.put("lalala", "upsja");
    c.put("lalala", new Float(5));
    d.put("lalala", new Double(5));
    t.put("lalala", new Double(5));
  }
  
}
