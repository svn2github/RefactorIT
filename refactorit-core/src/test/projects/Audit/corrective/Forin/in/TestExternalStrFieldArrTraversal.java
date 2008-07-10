package corrective.Forin.in;
/**
* @violations 1
*/
public class ExternalStrFieldArrTraversal {
    public void methodA(){
		for(int i=0;i<A.strArr.length;i++){
			System.out.println(A.strArr[i]);
		}
	}
}
public class A{
	String strArr[]={"one","two","three"};
}