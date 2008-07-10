package receiver_out;

public class TestExpressionTwoImplicitReceiver {
	private class ImplicitReceiver {
		public void foo() {
			bar();
			bar();
		}
		public void bar() {
		}
	}

	public void main() {
		ImplicitReceiver implicitReceiver = getObject();
		implicitReceiver.bar();
		implicitReceiver.bar();
	}

	private ImplicitReceiver getObject() {
		return new ImplicitReceiver();
	}
}