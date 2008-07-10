import java.net.URL;
import java.io.IOException;

public class Test {
	public void m(boolean b) throws IOException{
		/*[*/
		String result = "foo";
		if (b) {
			return;
		}
		// the bug only seems to appear if there is a throwing statement after return
		URL url = new URL("http://aqris.com"); 
		/*]*/
		System.out.println(result);
	}
}