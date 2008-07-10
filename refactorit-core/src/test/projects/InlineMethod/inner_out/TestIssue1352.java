public class TestIssue1352 {
	static class Inner {
		void toInline() {
			new InnerInner() {
			};
		}

		void f() {
			new TestIssue1352.Inner.InnerInner() {
			};
		}

		static class InnerInner {
		}
	}
}
