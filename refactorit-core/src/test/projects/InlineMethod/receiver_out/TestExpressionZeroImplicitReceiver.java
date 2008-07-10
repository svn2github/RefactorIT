package receiver_out;

public class TestExpressionZeroImplicitReceiver {
	private class ImplicitReceiver {
		public void foo() {
			System.out.println("Eclipse");
		}
	}

	public void main() {
		ImplicitReceiver implicitReceiver = getObject();
		System.out.println("Eclipse");
	}

	private ImplicitReceiver getObject() {
		return new ImplicitReceiver();
	}
}