public class Test {

  public static void main(String[] args) throws Exception {
    try {
      exceptionalMethod();
    } catch(NullPointerException npe){
    }
  }

  private static void exceptionalMethod() throws Exception {
    throw null;
  }
}
