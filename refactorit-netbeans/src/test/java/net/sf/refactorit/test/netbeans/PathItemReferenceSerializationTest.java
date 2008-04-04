/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.netbeans;


import net.sf.refactorit.netbeans.common.RefactorItActions;
import net.sf.refactorit.netbeans.common.projectoptions.PathItemReference;
import net.sf.refactorit.test.netbeans.vfs.NbTestCase;

import org.openide.filesystems.FileObject;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Map;


/**
 * @author risto
 */
public class PathItemReferenceSerializationTest extends NbTestCase {
  public void testSerialization() {
    final File f = PathItemReferenceTest.createLocalFile();
    Map s = new PathItemReference(f).serialize();
    assertRepresents(f, new PathItemReference(s));
    
    final File folder = f.getParentFile();
    s = new PathItemReference(folder).serialize();
    assertRepresents(folder, new PathItemReference(s));
    
    final FileObject fo = PathItemReferenceTest.createFileObject();
    s = new PathItemReference(fo).serialize();
    assertRepresents(fo, new PathItemReference(s));
    
    final String str = "asd";
    s = new PathItemReference(str).serialize();
    assertRepresents(str, new PathItemReference(s));
  }
  
  public void testDeserializationFromRit20Format() {
    if(RefactorItActions.isNetBeansFour()) {
      return;
    }
    
    final File f = PathItemReferenceTest.createLocalFile();
    Map s = new PathItemReferenceVersion20Emulator(f).serialize();
    assertRepresents(f, new PathItemReference(s));
    
    final File folder = f.getParentFile();
    s = new PathItemReferenceVersion20Emulator(folder).serialize();
    assertRepresents(folder, new PathItemReference(s));
    
    final FileObject fo = PathItemReferenceTest.createFileObject();
    s = new PathItemReferenceVersion20Emulator(fo).serialize();
    assertRepresents(fo, new PathItemReference(s));
  }
  
  public void testDeserializationFromRit2491Format() {
    if(RefactorItActions.isNetBeansFour()) {
      return;
    }
    
    final File f = PathItemReferenceTest.createLocalFile();
    Map s = new PathItemReferenceVersion2491Emulator(f).serialize();
    assertRepresents(f, new PathItemReference(s));
    
    final File folder = f.getParentFile();
    s = new PathItemReferenceVersion2491Emulator(folder).serialize();
    assertRepresents(folder, new PathItemReference(s));
    
    final FileObject fo = PathItemReferenceTest.createFileObject();
    s = new PathItemReferenceVersion2491Emulator(fo).serialize();
    assertRepresents(fo, new PathItemReference(s));
  }
  
  public void testDeserializationFromRit250Format() {
    final File f = PathItemReferenceTest.createLocalFile();
    Map s = new PathItemReferenceVersionPre250BetaEmulator(f).serialize();
    assertRepresents(f, new PathItemReference(s));
    
    final File folder = f.getParentFile();
    s = new PathItemReferenceVersionPre250BetaEmulator(folder).serialize();
    assertRepresents(folder, new PathItemReference(s));
    
    final FileObject fo = PathItemReferenceTest.createFileObject();
    s = new PathItemReferenceVersionPre250BetaEmulator(fo).serialize();
    assertRepresents(fo, new PathItemReference(s));
    
    final String string = "someString";
    s = new PathItemReferenceVersionPre250BetaEmulator(string).serialize();
    assertRepresents(string, new PathItemReference(s));
  }
  
  public void testUrlsImportedFromOldFormats() throws MalformedURLException {
    String urlString = "http://www.aqris.com";
    String[] oldFormatSerialization = getOldFormatSerialization(urlString);
    
    PathItemReference[] converted = PathItemReference.deserializeMapArray(oldFormatSerialization);
    assertEquals(1, converted.length);
    assertTrue(converted[0].isFreeform());
    assertEquals(urlString, converted[0].getFreeform());
  }
  
  // Util methods
  
  private static String[] getOldFormatSerialization(String urlString) throws MalformedURLException {
    URL url = new URL(urlString);
    String[] oldFormatSerialization = new String[] {URLDecoder.decode(url.toExternalForm())};
    return oldFormatSerialization;
  }

  static void assertRepresents(final FileObject fo, PathItemReference reference) {
    assertEquals(fo, reference.getFileObject());
    assertEquals(fo.isFolder(), reference.isFolder());
    assertEquals(PathItemReferenceTest.getAbsolutePath(fo), reference.getAbsolutePath());
  }

  static void assertRepresents(final File f, PathItemReference reference) {
    assertEquals(f, reference.getFile());
    assertEquals(f.isDirectory(), reference.isFolder());
    assertEquals(f.getAbsolutePath(), reference.getAbsolutePath());
  }
  
  static void assertRepresents(String str, PathItemReference reference) {
    assertEquals(str, reference.getFreeform());
  }
}
