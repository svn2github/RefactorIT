class Top {
  public void method() { // super
    method(); // super

    new Top().method(); // super
    new Super().method(); // super
    new Main().method(); // any
    new Sub().method(); // sub
    new Bottom().method(); // !impl + sub
    new Another().method(); // !impl + super + sub
  }
}

class Super extends Top {
  {
    method(); // super
    super.method(); // super + !impl
  }
}

class Main extends Super {
  public void method() { // never
    method(); // any <-- we are searching for this!
    super.method(); // super + !impl
  }
}

class Sub extends Main {
  {
    method(); // sub
    super.method(); // any
  }
}

class Bottom extends Sub {
  public void method() { // sub
    method(); // !impl + sub
    super.method(); // any
  }
}

class Another extends Top {
  public void method() { // !impl + super + sub
    method(); // !impl + super + sub
    super.method(); // !impl + super + sub
  }
}
  