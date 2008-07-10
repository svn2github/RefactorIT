package CheckPossibleEjbMethods.a;

/**
 * Some parts are commented out here; these parts are
 * neccessary for real message beans, but for tests
 * they are not neccessary (for the current implementation at least)
 * and they would complicate the tests (more J2EE classes would need to be 
 * on classpath during testing).
 *
 * Perhaps later these things would need to be uncommented to test some later
 * implementation (there might be similar things in other classes in this package,
 * and some of such these things are not shown at all here -- 
 * the deployment descriptors are all missing in this package, for instance).
 */
/**
 * @violations 0
 */
public class MBean implements javax.ejb.MessageDrivenBean /*, javax.jms.MessageListener*/ {
  public void ejbCreate() {
		
	}
	
	/*public void onMessage(javax.jms.Message aMessage) {
    
  }*/
}