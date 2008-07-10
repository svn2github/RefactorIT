package simple_out;

public class TestPrimitiveParam {
    public void foo() {
        int a = 1;
		int a1 = a;
        int b = a1++;
        System.out.println(a);
    }
    
    public int bar(int a) {
        return a++;
    }
}