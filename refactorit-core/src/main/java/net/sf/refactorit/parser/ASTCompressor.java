/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.parser;

import net.sf.refactorit.vfs.Source;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * Experimental
 * Left a fixed length thing
 * Comparision data:
 * COMPRESSION: 5439394
 * FIXED LENGTH: 8637832
 */
public class ASTCompressor implements JavaTokenTypes, Serializable {
  protected static final byte CHILD = 1;
  protected static final byte SIBLING = 0;
  protected static final byte LASTSIBLING = 2;
  // FIRST and ONLY CHILD = CHILD | LASTSIBLING = 3

  static final int INITIAL_SIZE = 1500;

  // permanent fields
  public short[] startLines;
  public short[] endLines;
  public short[] startColumns;
  public short[] endColumns;
  public byte[] metaBytes;
  public byte[] astTypes;
  public String[] strings;

  public transient byte[] astToSourcePos = null;
  public transient List sources;

  /**
   * For serialization
   */
  public ASTCompressor() {
  }

  public ASTCompressor(final int structureSize) {
    startLines = new short[structureSize];
    endLines = new short[structureSize];
    startColumns = new short[structureSize];
    endColumns = new short[structureSize];
    metaBytes = new byte[structureSize];
    astTypes = new byte[structureSize];
    strings = new String[structureSize];
  }

  public static final int getStructureSize(final long fileLength) {
    if (fileLength < 1800) {
      return 500;
    }
    if (fileLength < 4000) {
      return 1000;
    }
    if (fileLength < 8000) {
      return 2000;
    }
    return 4000;

//    final int size = (int) (fileLength / 3.9) + 40;
//    final int size = (int) (((double) 45000) * Math.log(
//            ((double) ((double) fileLength) + ((double) 150000))
//            / ((double) 10000)))
//            - 121846;
//    final int size = INITIAL_SIZE;
//    return size;
  }

//  public ASTCompressor(final ASTImpl root) {
//    try {
//      for( ASTImpl cur = root ; cur != null ; cur = (ASTImpl)cur.getNextSibling() ) {
//        int structure = SIBLING;
//        if(cur == root) structure = CHILD;
//        if(cur.getNextSibling() == null) structure |= LASTSIBLING;
//        compress(cur, structure);
//      }
//    } finally {
//      // release memory
//      strings = (String[])t_strings.toArray(new String[t_strings.size()]); ; t_strings = null;
//      startLines = t_startLines.toShortArray(); t_startLines = null;
//      startColumns = t_startColumns.toShortArray(); t_startColumns = null;
//      endLines = t_endLines.toShortArray(); t_endLines = null;
//      endColumns = t_endColumns.toShortArray(); t_endColumns = null;
//      metaBytes = t_metaBytes.toByteArray(); t_metaBytes = null;
//      astTypes = t_astTypes.toByteArray(); t_astTypes = null;
//    }
//  }

//  public final int getMemorySize() {
//    return getASTCount() * 14;
//  }

  public final int getASTCount() {
    if (strings == null) {
      return 0;
    }
    return strings.length;
  }

//  private void compress(final ASTImpl root, final int structurePlace) {
//    final int type = root.getType();
//
//    t_astTypes.add( root.getTypeAsByte() );
//    t_metaBytes.add( (byte) structurePlace );
//
//    t_startColumns.add( root.getColumn() );
//    t_startLines.add( root.getLine() );
//
//    t_endColumns.add( root.getEndColumn() );
//    t_endLines.add( root.getEndLine() );
//
//    t_strings.add( root.getText() );
//
//    updateDebugStats(type);
//
//    int forwardStructure = -1;
//    for(ASTImpl c = (ASTImpl) root.getFirstChild() ; c != null ;
//        c = (ASTImpl) c.getNextSibling() ) {
//      if(forwardStructure == -1) {
//        forwardStructure = CHILD;
//      } else if( forwardStructure == CHILD) {
//        forwardStructure = SIBLING;
//      }
//
//      if( c.getNextSibling() == null) {
//        forwardStructure |= LASTSIBLING;
//      }
//
//      compress( c, forwardStructure );
//    }
//
//    // help the GC along
//    ASTImpl fc = (ASTImpl) root.getFirstChild();
//    if(fc != null) {
//      root.setFirstChild(null);
//      ASTImpl last;
//      for(; fc != null ;
//          fc = (ASTImpl) fc.getNextSibling(), last.setNextSibling(null)  ) {
//        last = fc;
//      }
//    }
//  }

//  private static final void updateDebugStats(final int type) {
//    typeCounts[ type ] += 14;
//    compressedCount++;
//  }

//  public static String getTypeCountReport() {
//    if( 1 == 1 ) return "";
//    final ArrayList report = new ArrayList(255);
//    for(int i = 0 ; i < 255 ; ++i) {
//      report.add( new CEntry(i, typeCounts[i] ) );
//    }
//    Collections.sort( report );
//
//    final StringBuffer result = new StringBuffer();
//    result.append( "AST TYPE FREQENCY REPORT\n\n");
//    for(int i = report.size() -1 ; i >= 0 ; --i) {
//      final CEntry entry = (CEntry)report.get(i);
//      if( entry.count == 0) break;
//      result.append( entry+"\n");
//    }
//
//    return result.toString();
//  }

  /*  private TreeASTImpl getSiblingCollector() {
    return (TreeASTImpl) siblingCollectorStack.get(
        siblingCollectorStack.size() - 1);
     }

     private void setSiblingCollector(final TreeASTImpl collector) {
    popSiblingCollector();
    pushSiblingCollector(collector);
     }

     private void pushSiblingCollector(final TreeASTImpl collector) {
    siblingCollectorStack.add(collector);
     }

     private void popSiblingCollector() {
    siblingCollectorStack.remove(siblingCollectorStack.size() - 1);
     }*/

  static final boolean isChild(final byte metabyte) {
    return (metabyte & ASTCompressor.CHILD) == ASTCompressor.CHILD;
  }

  static final boolean isLast(final byte metabyte) {
    return (metabyte & ASTCompressor.LASTSIBLING) == ASTCompressor.LASTSIBLING;
  }

  static final boolean isLastAndOnlyChild(final byte metabyte) {
    byte onlyChild = CHILD | LASTSIBLING;
    return (metabyte & onlyChild) == onlyChild;
  }

  static final String toString(final byte metabyte) {
    switch (metabyte) {
      case 0:
        return "SIBLING";
      case 1:
        return "CHILD";
      case 2:
        return "LASTSIBLING";
      case 3:
        return "LASTCHILD";
      default:
        return "UNKNOWN";
    }
  }

  public final void setSource(final Source source) {
    sources = new ArrayList(3);
    sources.add(source);
    //System.err.println("source: " + source + " - asts: " + getASTCount());
//    astToSourcePos = new byte[getASTCount()]; // inits with zero
  }

  public final void setSource(final int index, final Source source) {
    int exist = sources.indexOf(source);
    if (exist < 0) {
      if (sources == null) {
        sources = new ArrayList(3);
      }
      sources.add(source);
      exist = sources.size() - 1;
    }
    // NOTE: optimization - we don't need index in case of a single source
    if (sources.size() > 1) {
      if (astToSourcePos == null) {
        astToSourcePos = new byte[getASTCount()]; // inits with zero, i.e. first source
      }
      astToSourcePos[index] = (byte) exist;
    }
  }

  public final Source getSource(final int index) {
    if (sources == null || sources.size() == 0) {
      return null;
    }

    if (sources.size() == 1) {
      return (Source) sources.get(0);
    }

    return (Source) sources.get(astToSourcePos[index]);
  }

  // statistics variables
//  private static final int[] typeCounts = new int[255];
//  private static int compressedCount = 0;


  // statistics helper class
//  static final class CEntry implements Comparable {
//    public final int key;
//    public final int count;
//
//    private CEntry(final int key, final int count) {
//      this.key = key;
//      this.count = count;
//    }
//
//    public final int compareTo(final Object o) {
//      return new Integer(count).compareTo( new Integer( ((CEntry)o).count ) );
//    }
//
//    public final boolean equals(final Object o) {
//      return new Integer(count).equals( new Integer( ((CEntry)o).count ) );
//    }
//
//    public final String toString() {
//      return "Key: " + key + " Memory Size: " + count;
//    }
//  }
}
