class Test {
  public static void doSomething() {}
  public void doSomething2() {}

  private class Inner {
    private void doSomething2() {}

    {
      doSomething();
      Test.doSomething();
      Test.this.doSomething();

      doSomething2();
      new Test().doSomething2();
      Test.this.doSomething2();
    }
  }
}
