// Dummy FIXME's have been entered to this class for testing purposes; TAB-size: 4.
package testPackage1;

/** Not a Javadoc-fixme. */
public class Sample2 {
	private int x;
	
	// Not a three-line TODO
	// ...
	// This is the third line of the three-line comment

	// TODO: this a three line todo
	// ...
	// This is the third line of the three-line comment
	
  /** FIXME: Javadoc-fixme. */
	public void x() {
        return x;   // Not a two-line fixme
                    // This is the second line of the two-line one
                    
                    // FIXME - a two-line fixme
                    // This is the second line of the two-line one
    }
    
    
  // FIXME start here
  /** Javadoc not added to the FIXME because it is multiline 
  */
	public void y() {

}

  // FIXME start here
  /** Javadoc IS added to the FIXME because it is single line */
	public void z() {

  }
    
    // FIXME: a one-line comment
    // FIXME: another one-line comment
    
    // FIXME: one-line comment
    /*
      This one is not appended to fixme because it is multiline.
    */
    
    // TODO: a two-line comment again
    	// This is a the last line of that two-line comment

	// This is not a FIXME
    private char c; // and not continued here
    
	// FIXME This is a fixme (still a two-line comment)
    private char d; // This *is* a continued comment here

 		// TODO TODO -- a tab-and-space test
        // This is supposed to be a two-line comment
        
        // FIXME: Comment near end-of-file
}