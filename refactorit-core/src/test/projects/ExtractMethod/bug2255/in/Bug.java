public class Bug {
  
  public void m() {
    for (int i = 0; i < 10; i++) {
      for (int j = 0; j < 10; j++) {
        /*]*/String a = "";
        String b = "";/*[*/
      }
    }
  }
}
