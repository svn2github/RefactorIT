
import java.util.Arrays;


public class Test {
  private static char[] padBuffer = null;
  
  public static String pad2 (final String data, final int 
padLength) {
    /*]*/if ( padBuffer == null || (padLength > padBuffer.length) ) {
      padBuffer = new char[padLength];
      Arrays.fill (padBuffer, ' ');
    }/*[*/
  }
}
