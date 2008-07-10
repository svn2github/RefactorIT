package CheckPossibleAccessRights1.com.p1;

/**
 * @violations 2
 */
public class Class10 extends Class1 {
	private void f100() {
		tmp1_4 = 1;
		tmp2_4 = 2;
		tmp3_4 = 3;
		tmp4_4 = 4;
	}
}

class Class11 {
        private void f101() {
                new Class1().tmp1_5 = 1;
                new Class1().tmp2_5 = 2;
                new Class1().tmp3_5 = 3;
                new Class1().tmp4_5 = 4;
        }

}
