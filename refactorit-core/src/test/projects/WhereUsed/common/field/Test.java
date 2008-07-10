class A {
  static int field = 13;

  class Inner {
    {
      field = 14;

      class Local {
        {
          field = 15;
          A.this.field = 18;
          A.field = 19;
        }
      }
    }
  }
  {
    (new B()).field = 14;
    field = 16;
    this.field = 16;
    A.field = 20;
  }
}

class B extends A {
  {
    System.out.println(field);
    super.field = 15;
  }

  class Inner {
    {
      B.super.field = 17;
      B.this.field = 18;
    }
  }
}

class C extends B {
  {
    System.out.println(field);
    System.out.println(B.field);

    class Local {
      {
        field = 17;
        C.this.field = 18;
        C.super.field = 19;
        B.field = 20;
        A.field = 21;
      }
    }
  }
}

class D extends C {
  private int field = 18;
  {
    System.out.println(field);
    super.field = 19;
  }

  class Inner {
    {
      field = 20;
      D.this.field = 21;
      D.super.field = 22;
      B.field = 23;
      A.field = 24;
    }
  }

  class Inner2 {
    int field = 25;
    {
      field = 26;
      this.field = 28;
      D.this.field = 27;
      D.super.field = 28;
      C.field = 29;
      A.field = 28;
    }
  }
}