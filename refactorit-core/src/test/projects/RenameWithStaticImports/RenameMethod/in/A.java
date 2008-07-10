import java.io.PrintStream;

public class A {
	
	public static PrintStream out = System.out;
	
	public static void out(String s) {
		out.println(s);
	}
}