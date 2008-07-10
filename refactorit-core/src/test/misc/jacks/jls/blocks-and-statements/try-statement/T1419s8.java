
public class T1419s8 {
    public static void main(String[] args) {
        
        try {
            throw new Exception();
        } catch (NullPointerException e) {
        } catch (RuntimeException e) {
        } catch (Throwable e) {
        }
    
    }
}
