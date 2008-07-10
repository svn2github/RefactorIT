package corrective.Forin.in;
import java.util.*;
/**
* @violations 1
*/
public class WhileTraversalWithCast {
    public void methodA(){
		List list=new ArrayList();
		list.add("one");
		list.add("two");
		list.add("three");
		System.out.println("");
		for(Object listItem : list){
			String item=(String)listItem;
			System.out.println(item);
			item="blah";
			System.out.println(""+item);
		}
	}
}