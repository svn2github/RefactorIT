import java.util.List;
import java.util.Properties;

class Main {
  void test(User u) {
	  Properties map=null;
	  map.put("test",HtmlUtil.useInteger(u.getStatus()));
	  List list=null;
	  
	  list.add(HtmlUtil.useInteger(u.getStatus()));
  }
  
}	  	
