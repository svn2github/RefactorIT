package p1;

class Class1 {
// old type reference
	public int b;

        public void func1() {
                b = 1;
        }
}

public class Class2 extends Class1 {

        public void func2(Class2 ref) {
                ref.b = 1;
        }
}
