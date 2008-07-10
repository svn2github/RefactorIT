package corrective.Forin.in;
/**
* @violations 1
*/
public class IntArrTraversal {
	public void methodA(){
		int[]x = {1,2,3,4,5};
		for(int i=0;i<x.length;i++){ 
			System.out.println("blah"+x[i]);  
		}
	}
}