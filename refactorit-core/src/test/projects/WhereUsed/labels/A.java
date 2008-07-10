package WhereUsed.labels;

public class A {
  public static void meth1() {
    loop1:
    for(int i=0; i < 10; i++) {
      loop2:
      for(int j = 0; j<10; j++) {
        if(j == i*2) {
          break loop1;
        } else if( j < 5){
          continue loop2;
        } 
      }
  
      if(i < 100) {
        continue;
      }
      
      if(true) {
        continue loop1;
      } else if(false) {
        break loop1;
      }
      break;
    }
  }
}