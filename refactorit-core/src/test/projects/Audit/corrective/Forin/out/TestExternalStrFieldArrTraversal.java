package corrective.Forin.in;
/**
* @violations 1
*/
public class ExternalStrFieldArrTraversal {
    public void methodA(){
		for(String str : A.strArr){
			System.out.println(str);
		}
	}
}
public class A{
	String strArr[]={"one","two","three"};
}