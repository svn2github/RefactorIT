package corrective.Forin.in;
/**
* @violations 1
*/
public class StrFieldArrTraversal {
    String strArr[]={"one","two","three"};
	public void methodA(){
		for(int i=0;i<strArr.length;i++){ 
			System.out.println("blah"+strArr[i]);  
		}
	}
}