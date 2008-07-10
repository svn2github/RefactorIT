class Test {
  Test() {
    Client c = null;
  }
}

class Client {
  {
    Test t = new Test();
  }
}