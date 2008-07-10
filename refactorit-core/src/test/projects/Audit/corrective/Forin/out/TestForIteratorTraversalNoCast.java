package corrective.Forin.in;
import java.util.*;
/**
* @violations 1
*/
public class WhileTraversalWithCast {
    public void methodA(){
		List list=Arrays.asList(new String[]{"one","two","three"});
		for(Object obj : list/* ah ah*/)
		{
			System.out.println(""+obj.toString());
			System.out.println(""+obj.toString());
		}
	}
}