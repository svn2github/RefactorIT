package corrective.Forin.in;
import java.util.*;
/**
* @violations 1
*/
public class WhileTraversalWithCast {
    public void methodA(){
		List list=Arrays.asList(new String[]{"one","two","three"});
		for(Iterator i= list.iterator();   i.hasNext();/* ah ah*/)
		{
			Object obj =     i.next();
			System.out.println(""+obj.toString());
			System.out.println(""+obj.toString());
		}
	}
}