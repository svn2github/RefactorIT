public class Bug {
  public static int m( int i ) throws Exception {

    /*]*/try {
      if( i != 0 ) {
        return( 1 );
      }
    } catch( Exception ex ) {
      throw ex;
    }/*[*/

    return 2;
  }

  public static void main(String[] args) throws Exception {
    int result = m(3);
  }
}
