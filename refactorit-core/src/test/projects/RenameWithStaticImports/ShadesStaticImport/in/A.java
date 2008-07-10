import java.io.PrintStream;
import static java.lang.System.load; 

public class A {
	
	public static PrintStream out = System.out;
	
	public static void out(String s) {
		out.print(s);
	}
	
	public static void main(String[] args) throws Exception {
		load("foo");
	}
}