/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.vfs;


import net.sf.refactorit.test.TempFileCreator;
import net.sf.refactorit.vfs.AbstractSource;
import net.sf.refactorit.vfs.Source;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Date;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * SourceTest
 *
 * @author <a href="mailto:tonis.vaga@aqris.com>Tõnis Vaga</a>
 * @version $Revision: 1.3 $ $Date: 2005/01/06 16:07:39 $
 */
public class SourceTest extends TestCase {



  private Source rootSource;

  /**
   * @see junit.framework.TestCase#setUp()
   */
  protected void setUp() throws Exception {
    rootSource=TempFileCreator.getInstance().createRootDirectory();
  }

  /**
   * @see junit.framework.TestCase#tearDown()
   */
  protected void tearDown() throws Exception {
    ((AbstractSource)rootSource).emptyDirRecursively(null);
  }
  public void testMkdir() {
    Source newDir = rootSource.mkdir("test1");
    assertNotNull(newDir);
    assertTrue(newDir.exists());
  }

  public void testMkdirs() {
    Source newDir = rootSource.mkdirs("test2"+'/'+ "test1");
    assertNotNull(newDir);
    assertTrue(newDir.getFileOrNull().exists());
  }
  public void testCreateNewFile() throws IOException {
    String filename = "newFile";

    Source result=rootSource.createNewFile(filename);

    assertTrue(result.exists());
    assertEquals (0, result.length());

    assertTrue("source not found by parent",  Arrays.asList(rootSource.getChildren()).contains(result));
    assertEquals(" Wrong parent",rootSource,result.getParent());
  }
  public void testDelete() throws IOException {
    Source result=rootSource.createNewFile("newFile2");
    boolean bResult=result.delete();


    assertFalse("Deleting file failed",!bResult || result.exists());


    Source newDir=rootSource.mkdir("testN");
    bResult=newDir.delete();
    assertTrue ("Deleting directory failed", bResult && !newDir.exists());
  }
  public void testDeleteNotEmptyDir() throws Exception {
    Source dir=rootSource.mkdir("test");
    dir.createNewFile("test2");

    boolean result=dir.delete();

    assertFalse("Deleted not empty dir! ", result);
  }
  public void testEmptyDirRecursively() throws IOException {
    Source newDir=rootSource.mkdir(generateName(true));
    newDir.createNewFile("test1");
    newDir.mkdir("test2").createNewFile("test1");


    ((AbstractSource)rootSource).emptyDirRecursively(null);

    assertEquals(0, rootSource.getChildren().length);
  }
  private String generateName(boolean isDir) {

    return isDir
            ? TempFileCreator.TempNameGenerator.createDir()
            : TempFileCreator.TempNameGenerator.createFile();
  }

  public void testRename() throws Exception {
    Source newDir=rootSource.mkdir("test33");

    Source newFile=rootSource.createNewFile("test");

    Source result=newFile.renameTo(newDir,"test2");

    assertNotNull(result);
    assertEquals(newDir,result.getParent());
    assertEquals("test2",result.getName());

  }
  public void testOuputStream() throws Exception {
    Source newFile=rootSource.createNewFile(generateName(true));
    String contents="Jama jfsdafasdf";
    OutputStream output = newFile.getOutputStream();
    output.write(contents.getBytes());
    output.close();
    assertEquals(contents,newFile.getContentString());
  }
  public void testLastModified() throws IOException {
    Source newFile=rootSource.createNewFile(generateName(true));

    long currentTime = System.currentTimeMillis();

    assertBelongsToInterval( currentTime,newFile.lastModified(), 2*Source.MAX_TIMESTAMP_ERROR);

    final long newTime = newFile.lastModified()+2*Source.MAX_TIMESTAMP_ERROR;
    newFile.setLastModified(newTime);
    assertBelongsToInterval( newTime,newFile.lastModified(), Source.MAX_TIMESTAMP_ERROR);
  }
  public void testRelativePath() throws Exception {
    Source newDir=rootSource.mkdir(generateName(true));
    Source newFile=newDir.createNewFile("test");

    Source parent=rootSource;
    while( parent.getParent() != null ) {
     parent=parent.getParent();
    }

    assertEquals(newFile,parent.getChild(newFile.getRelativePath()));
  }

  public void testEmptyFileGetInputStream() throws Exception {
    Source newSrc=rootSource.createNewFile(generateFileName());
    InputStream input = newSrc.getInputStream();
    assertNotNull(input);
    input.close();
  }


  /**
   * @returns
   */
  private String generateFileName() {
    return generateName(false);
  }

  public void assertBelongsToInterval(long expected, long value,long interval) {
    assertTrue("Expected "+new Date(expected)+", was "+new Date(value), Math.abs(expected-value)< interval );
  }

  /**
   * @returns
   */
  public static Test suite() {
    return new TestSuite(SourceTest.class);
  }
}
