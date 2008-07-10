public class A{
  static int shadowed = 0;
  
  static int x = shadowed;
  
	public static void main(String[] args){
		int shadowed = 1;
    System.out.println(x);
	}
}