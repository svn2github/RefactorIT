
public class T1420catch21 {
    public static void main(String[] args) {
        
        final Exception e = new java.io.IOException();
        try {
            throw e;
        } catch (java.io.IOException io) {
            // this one will be called
        } catch (Exception ex) {
            // this one is reachable, but will never be executed,
            // since analysis does not evaluate variable contents
        }
    
    }
}
