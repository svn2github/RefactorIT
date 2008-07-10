package SwitchCaseFallthrough;

import java.io.*;
import java.util.*;


public class A {
  /**
   * @audit SwitchCaseFallthrough
   * @audit SwitchCaseFallthrough
   * @audit SwitchCaseFallthrough
   */
  public void test1(){
    int i = 0;
    
    //
    switch(RANDOM.nextInt(3)){
      case 0:
        i++; // !
      case 1:
        i++; // !
      case 2:
        i++; // !
      default:
        System.out.println(i);
        break;
    }
  }

  /**
   */
  public void test2(){
    
    //
    switch(RANDOM.nextInt(3)){
      case 0:
      case 1:
      case 2:
        break;
      default:
        break;
    }
  }

  /**
   * @audit SwitchCaseFallthrough
   * @audit SwitchCaseFallthrough
   * @audit SwitchCaseFallthrough
   */
  public void test3(){
    int i = 0;
    
    //
    switch(RANDOM.nextInt(3)){
      case 0:
        if(true){
          i++; // !
        } else {
          return;
        }
      case 1:
        if(true){
          i++; // !
        } else {
          i--; // !
        }
      case 2:
        if(true){
          return;
        } else {
          i--; // !
        }
      default:
        break;
    }
  }

  /**
   */
  public void test4(){
    int i = 0;
    
    //
    switch(RANDOM.nextInt(3)){
      case 0:
        if(true){
          i++;
        } else {
          i--;
        }
        break;
      case 1:
        if(true){
          i++;
        } else {
          return;
        }
        break;
      case 2:
        if(true){
          return;
        } else {
          i--;
        }
        break;
      default:
        if(true){
          return;
        } else {
          return;
        }
    }
  }

  /**
   * @audit SwitchCaseFallthrough
   * @audit SwitchCaseFallthrough
   * @audit SwitchCaseFallthrough
   */
  public void test5(){
    
    //
    switch(RANDOM.nextInt(3)){
      case 0:
        try {
          throw new EOFException();
        } catch(IOException ioe){
          // !
        }
      case 1:
        try {
          nullIO();
          throw new IllegalArgumentException();
        } catch(IOException ioe){
          // !
        }
      case 2:
        try {
          throw new EOFException();
        } catch(IOException ioe){
          // !
        } finally {
          return;
        }
      default:
        throw new InternalError();
    }
  }

  /**
   */
  private static void nullIO() throws IOException {
  }

  //
  //
  //
  
  private static final Random RANDOM = new Random();
}
