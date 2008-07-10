import java.awt.event.ActionEvent;


abstract class Abstract implements java.awt.event.ActionListener {
  Object obj=null;

  public void f() {
    obj.equals(null);

  }
  public abstract void doSomething();

}

class OverridesAbstract extends Abstract {


    public int hashCode() {
        // @todo: override this 
        return super.hashCode();
    }

    protected void finalize() throws Throwable {
        // @todo: override this 
        super.finalize();
    }

    public boolean equals(Object obj) {
        // @todo: override this 
        return super.equals(obj);
    }

    public void doSomething() {
         // FIXME 
        throw new java.lang.UnsupportedOperationException( "method doSomething not implemented yet");
    }

    protected Object clone() throws CloneNotSupportedException {
        // @todo: override this 
        return super.clone();
    }

    public void actionPerformed(ActionEvent event) {
         // FIXME 
        throw new java.lang.UnsupportedOperationException( "method actionPerformed not implemented yet");
    }

    public String toString() {
        // @todo: override this 
        return super.toString();
    }

    public void f() {
        // @todo: override this 
        super.f();
    }
}
