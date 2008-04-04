/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.vfs.local;


import net.sf.refactorit.vfs.ClassPath;
import net.sf.refactorit.vfs.ClassPathElement;
import net.sf.refactorit.vfs.Source;
import net.sf.refactorit.vfs.SourcePath;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;


/**
 * Test local filesystem.
 */
public final class TestLocalFS {
  /*
   original test files
   private static final String JAR1 = "C:\\Work\\RefactorITLib\\openide.jar";
   private static final String JAR2 = "C:\\Work\\RefactorITLib\\jbuilder.jar";

   private static final String SRC1 = "C:\\Work\\RefactorIT";
   private static final String SRC2 = "C:\\Work\\RefactorITRes";
   private static final String SRC3 = "C:\\Work\\Test";
   */

  /*
   test files for erikb.
   */
  private static final String JAR1 = "..//dist//openide.jar";
  private static final String JAR2 = "..//dist//jbuilder.jar";

  private static final String SRC1 = "//home//erikb//work//swing";
  private static final String SRC2 = "//home//erikb//work//jbtest";
  //private static final String SRC3 = "//home//erikb//work//test";

  public static void main(String[] args) {
    System.out.println("Class Path Test");
    String curDir = (new File(".")).getAbsolutePath();

    System.out.println("Curent Directory: " + curDir);

    try {
      ClassPath cp = new LocalClassPath(JAR1 + File.pathSeparator + JAR2);
      printClassPath(cp);
      // TODO: delete this - testClassPath(cp, "org/openide/TopManager.class");
      testClassPath(cp, "com/borland/jbuilder/JBuilder.class");
    } catch (IOException e) {
      System.out.println("LocalClassPath: error: " + e);
    }

    System.out.println("\nSource Path Test");
    try {
      SourcePath sp = new LocalSourcePath(SRC1 + File.pathSeparator + SRC2);
      Source[] sources = sp.getRootSources();
      for (int i = 0; i < sources.length; i++) {
        testSource(sources[i]);
      }
    } catch (IOException e) {
      System.out.println("LocalSourcePath: error: " + e);
    }

    System.out.println("\nSource Path Test: Collect Sources");

    try {
      SourcePath sp = new LocalSourcePath(SRC1 + File.pathSeparator + SRC2);
      System.out.println("Collect All:");

      {
        List sourceList = sp.getAllSources();
        Iterator it = sourceList.iterator();
        while (it.hasNext()) {
          Source s = (Source) it.next();
          System.out.println("\tName: " + s.getName());
        }
      }
      System.out.println("Collect by extension:");

      ((LocalSourcePath) sp).setValidExtensions(new String[] {".class", ".jpx"}); {
        List sourceList = sp.getAllSources();
        Iterator it = sourceList.iterator();
        while (it.hasNext()) {
          Source s = (Source) it.next();
          System.out.println("\tName: " + s.getName());
        }
      }

      ((LocalSourcePath) sp).setValidExtensions(null);

      System.out.println("Collect All:");

      {
        List sourceList = sp.getAllSources();
        Iterator it = sourceList.iterator();
        while (it.hasNext()) {
          Source s = (Source) it.next();
          System.out.println("\tName: " + s.getName());
        }
      }

      System.out.println("Walk:");

      Source[] sources = sp.getRootSources();
      for (int i = 0; i < sources.length; i++) {
        testSource(sources[i]);
      }
    } catch (IOException e) {
      System.out.println("LocalSourcePath: error: " + e);
    }
    /*
            try {
                SourcePath sp = new LocalSourcePath( SRC3 );
                Source source = sp.getRootSources()[0];
                Source file = source.getChildren()[ 0 ];
                System.out.println( "file: " + file.getAbsolutePath() );
                file.renameTo( source, "NewName.java" );
                System.out.println( "file: " + file.getAbsolutePath() );

            } catch ( IOException e ) {
                    System.out.println( "LocalSourcePath: error: " + e );
            }
     */
  }

  private static void testClassPath(ClassPath cp,
      String name) throws IOException {
    InputStream in = cp.getInputStream(name);
    if (in == null) {
      return;
    }
    try {
      int size = 0;
      while (in.read() >= 0) {
        size++;
      }
      System.out.println("LocalClassPath: " + name + "; size: " + size);
    } finally {
      in.close();
    }
  }

  private static void testSource(Source src) throws IOException {
    System.out.println("Source: " + src.getAbsolutePath());
    System.out.println("\tName: " + src.getName());
    System.out.println("\tisDirectory: " + src.isDirectory() + "; isFile: "
        + src.isFile());

    if (src.isDirectory()) {
      Source[] children = src.getChildren();
      for (int i = 0; i < children.length; i++) {
        testSource(children[i]);
      }
    }
  }

  private static void printClassPath(ClassPath cp) {
    System.out.println("LocalClassPath: " + cp);
    System.out.println("LocalClassPath stringForm: " + cp.getStringForm());
    ClassPathElement[] cpe = ((LocalClassPath) cp).createElements();
    System.out.println("Number of elements:" + cpe.length);

    for (int i = 0; i < cpe.length; i++) {
      System.out.println("  element:" + cpe[i]);
    }
  }
}
