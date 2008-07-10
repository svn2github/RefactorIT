package p;

import java.io.File;
import java.net.MalformedURLException;

class F {
	public void foo() { 
		File file= null; // Is there a reason to *not* inline this? RefactorIT does not fail with this at the moment.
		 
		try { 
			file.toURL(); 
		} catch (MalformedURLException e) { 
		} 
	} 
}