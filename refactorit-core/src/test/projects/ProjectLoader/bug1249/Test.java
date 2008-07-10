import java.io.File;

public class Test {
	public Test(File f) {
  }

  public Test(String s) {
    this((Object) s);
  }
  
  public Test(int i) {
    super(i);
  }

	public static void makeTest() {
		new Test(new Object());
		
		new Test(new File("")) {
		  public void method() {}
		};
	}
}