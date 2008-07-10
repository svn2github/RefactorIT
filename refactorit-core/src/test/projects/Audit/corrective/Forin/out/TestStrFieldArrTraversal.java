package corrective.Forin.in;
/**
* @violations 1
*/
public class StrFieldArrTraversal {
    String strArr[]={"one","two","three"};
	public void methodA(){
		for(String str : strArr){ 
			System.out.println("blah"+str);  
		}
	}
}