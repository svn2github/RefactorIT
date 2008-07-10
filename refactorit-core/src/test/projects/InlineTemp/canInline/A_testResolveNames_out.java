public class A{
  static int shadowed = 0;
  
  
	public static void main(String[] args){
		int shadowed = 1;
    System.out.println(A.shadowed);
	}
}