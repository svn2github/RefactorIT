package DebugCode;

import java.io.*;


public class A {
  /**
   * @audit SystemOutInvocation
   */
  public void test1(){
    System.out.println("Hello World!");
  }
  
  /**
   * @audit SystemOutInvocation
   */
  public void test2(){
    System.out.print(this);
  }
  
  /**
   * @audit SystemErrInvocation
   */
  public void test3(){
    System.err.println("Hello World!");
  }

  /**
   * @audit SystemErrInvocation 
   */
  public void test4(){
    System.err.print(this);
  }

  /**
   * @audit StackDumpToConsole
   */
  public void test5(){
    new Exception().printStackTrace();
  }

  /**
   * @audit StackDumpToConsole
   * @audit StackDumpToConsole
   */
  public void test6(){
    new Exception().printStackTrace(System.out);
    new Exception().printStackTrace(System.err);
  }

  /**
   */
  public void test(){
    new Exception().printStackTrace(new PrintWriter(new StringWriter()));
  }
}
