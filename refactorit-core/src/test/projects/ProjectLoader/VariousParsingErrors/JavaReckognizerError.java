public class JavaReckognizerError { 
	public static class X {
		public void x() {
			return new Object() {
				public abstract x (); // The "abstract" keyword should cause an error here
			}
	}
}