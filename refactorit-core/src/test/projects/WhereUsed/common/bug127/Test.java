class Test {
  public void test() {
    final Test test = new Test();
    System.out.println("Hello: " + test);
    System.out.println(test + "Hello: " + (13 + 15) + test);
    System.out.println(test + (String) null);
    System.out.println((String) null + test);
    System.out.println((String) null + test + null);
    System.out.println((String) null
                       + test);

    System.out.println((new Test[13])[3] + (String) null);
  }
}
