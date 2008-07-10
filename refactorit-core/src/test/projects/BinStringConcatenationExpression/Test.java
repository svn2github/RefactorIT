public class Test {
  public static final void main(String[] params) {
    final Object tmp = new Integer(13);
    System.out.println("Hello" + tmp + (13 + 15));
    System.out.println(tmp + "Hello" + (13 + 15));
    System.out.println((String) null + null);
    System.out.println(null + (String) null);
    System.out.println((String) null + (String) null);
  }
}