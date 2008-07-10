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
		Iterator iterator=list.iterator();
		System.out.println("");
		while(iterator.hasNext()){
			String item=(String)iterator.next();
			System.out.println(item);
			item="blah";
			System.out.println(""+item);
		}
	}
}