public class TestIssue1352 {
	static class Inner {
		void toInline() {
			new InnerInner() {
			};
		}

		void f() {
			/*[*/toInline()/*]*/;
		}

		static class InnerInner {
		}
	}
}
