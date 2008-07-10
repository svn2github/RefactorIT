package b;

import a.A;

class B {

  {
    new Object() {
      public void method() {
        new A().d = 4;
      }

      class Some {
        public void some() {
          new A().e = 5;
        }
      }
    };

    new A() {
      int field = f;

      class Another {
        {
          g = 7;
        }
      }
    };

    new Object() {
      class Third extends A {
        int field = h;
      }
    };
  }
}

class C extends A {
  {
    new Object() {
      int field = i;

      class Forth {
        int jjj = j;
      }
    };
  }
}
