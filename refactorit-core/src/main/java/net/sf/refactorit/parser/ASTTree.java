/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.parser;

import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.loader.ProjectLoader;

import java.io.IOException;
import java.io.Serializable;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;


/**
 * @author Sander Magi
 * @author Anton Safonov
 */
public final class ASTTree extends ASTCompressor implements Serializable {
  private transient ASTImpl[] astIndex = null;
  private transient Reference astIndexReference = null;
  private transient BackReference backReference = null;

  private transient int astsNum = 0;
//  public transient TreeASTImpl rootNode = null;

//  private transient static final byte BYTE = (byte) 0;
//  private transient static final short SHORT = (short) 0;

  /** When any AST node is changed, then the whole tree will be made strongly
   *  reachable again by changedLifeline variable */
  private transient Object changedLifeline = null;

  private transient ArrayList siblingCollectorStack = null;

  private transient String packageName = null;
  private transient String lastPackageName = null;
  private transient HashSet typeNames = null;
  private transient HashSet typeFullNames = null;
  private transient HashSet superTypeNames = null;
  private transient HashSet idents = null;

  private transient boolean waking = false;

//  private transient long fileLength;

  /**
   * For serialization
   */
  public ASTTree() {
    super();
  }

  public ASTTree(final long fileLength) {
    super(getStructureSize(fileLength));
//System.err.println("ASTTree: " + fileLength + " - " + getStructureSize(fileLength));
//    this.fileLength = fileLength;

    this.astIndex = new TreeASTImpl[getStructureSize(fileLength)];
    if (ProjectLoader.isLowMemoryMode()) {
      this.astIndexReference = new WeakReference(this.astIndex);
    } else {
      this.astIndexReference = new SoftReference(this.astIndex);
    }
    this.backReference = new BackReference(this.astIndex, this);
  }

  private ASTImpl[] getAstIndex() {
    if (this.astIndex != null) { // even hardlink is available
      return this.astIndex;
    }
    if (astIndexReference == null) {
      return null;
    }
    return (ASTImpl[]) astIndexReference.get();
  }

  public final ASTImpl getAstAt(final int index) {
    wakeTree();

    if (getASTCount() == 0) {
      return null;
    }

    final ASTImpl[] asts = getAstIndex();
    if (index >= asts.length) {
      new ArrayIndexOutOfBoundsException("asts.length: " + asts.length
          + ", index: " + index).printStackTrace();
      return null;
    }
    final ASTImpl result = asts[index];

    this.astIndex = null; // no need to hold astIndex again

//    ASTImplImpl ast = new ASTImplImpl();
//    ast.setFirstChild(result);
//    new rantlr.debug.misc.ASTFrame("Root", ast).setVisible(true);

    return result;

  }

  public final int getAstType(final int index) {
    try {
      final byte ttype = this.astTypes[index];
      if (ttype >= 0) {
        return ttype;
      } else {
        return 127 - ttype;
      }
    } catch (ArrayIndexOutOfBoundsException e) {
      return -1;
    }
  }

  public final String getAstText(final int index) {
    try {
      return this.strings[index];
    } catch (ArrayIndexOutOfBoundsException e) {
      return null;
    }
  }

  public final int getAstStartLine(final int index) {
    try {
      return (int)this.startLines[index];
    } catch (ArrayIndexOutOfBoundsException e) {
      return 0;
    }
  }

  public final int getAstStartColumn(final int index) {
    try {
      return (int)this.startColumns[index];
    } catch (ArrayIndexOutOfBoundsException e) {
      return 0;
    }
  }

  public final int getAstEndLine(final int index) {
    try {
      return (int)this.endLines[index];
    } catch (ArrayIndexOutOfBoundsException e) {
      return 0;
    }
  }

  public final int getAstEndColumn(final int index) {
    try {
      return (int)this.endColumns[index];
    } catch (ArrayIndexOutOfBoundsException e) {
      return 0;
    }
  }

//  private int reloadCount = 0;
  /**
   * N.B! Call to wakeTree makes astIndex strongly reachable.
   * Caller must ensure it sets astIndex = null later
   */
  private void wakeTree() {
    if (!waking && getAstIndex() != null) {
      return;
    }

  //    System.err.println("Reloading count :" + (++reloadCount));
    synchronized (this) {
      if (getAstIndex() != null) {
        return;
      }
      waking = true;

      try {
        this.astIndex = new TreeASTImpl[getASTCount()];
        if (ProjectLoader.isLowMemoryMode()) {
          this.astIndexReference = new WeakReference(this.astIndex);
        } else {
          this.astIndexReference = new SoftReference(this.astIndex);
        }

        if (this.siblingCollectorStack == null) {
          this.siblingCollectorStack = new ArrayList(256);
        } else {
          this.siblingCollectorStack.clear(); // to be sure
        }
        this.backReference = new BackReference(astIndex, this);

        TreeASTImpl lastAstRead = null;

        for (int index = 0, max = getASTCount(); index < max; ++index) {
          lastAstRead = readAst(index, lastAstRead, backReference);
          this.astIndex[index] = lastAstRead;
        }

      } catch (RuntimeException e) {
        e.printStackTrace();
        throw e;
      } finally {
        // clean up temps
        if (this.siblingCollectorStack != null) {
          this.siblingCollectorStack.clear();
        }
        this.backReference = null;
        waking = false;
      }
    }
  }
  private TreeASTImpl readAst(final int index, final TreeASTImpl lastAstRead,
      final BackReference backReference) {

    final byte metabyte = metaBytes[index];
    final TreeASTImpl ast = new TreeASTImpl(index, backReference);
//System.err.println("TreeASTImpl.getType(): " + ast.getType() + " - " + ast.getText());

    if (isChild(metabyte)) {
      if (lastAstRead != null) {
        lastAstRead.setFirstChildInternal(ast);
        //System.err.println("Adding child to " + lastAstRead.index);
      } else {
        // in this case we are the root node
        if (index != 0) {
          throw new RuntimeException("index = " + index);
        }
      }
      siblingCollectorStack.add(ast);
    } else {
      // it was a sibling
      ((TreeASTImpl) siblingCollectorStack.get(
          siblingCollectorStack.size() - 1)).setNextSiblingInternal(ast);
      //System.err.println("Adding sibling to " + getSiblingCollector().index);
      siblingCollectorStack.set(siblingCollectorStack.size() - 1, ast);
    }

    if (isLast(metabyte)) {
      siblingCollectorStack.remove(siblingCollectorStack.size() - 1);
    }

    return ast;
  }

  public TreeASTImpl createNewAst() {

    final TreeASTImpl node
        = new TreeASTImpl(this.astsNum, this.backReference);

    try {
      this.astIndex[this.astsNum] = node;
    } catch (ArrayIndexOutOfBoundsException e) {
      this.astIndex = ensureSize(this.astIndex, this.astsNum);
      this.backReference.astIndex = this.astIndex;

      this.astTypes = ensureSize(this.astTypes, this.astsNum);
      this.metaBytes = ensureSize(this.metaBytes, this.astsNum);
      this.startLines = ensureSize(this.startLines, this.astsNum);
      this.startColumns = ensureSize(this.startColumns, this.astsNum);
      this.endLines = ensureSize(this.endLines, this.astsNum);
      this.endColumns = ensureSize(this.endColumns, this.astsNum);
      this.strings = ensureSize(this.strings, this.astsNum);

      this.astIndex[this.astsNum] = node;
    }
    this.astsNum++;

//    if (this.rootNode == null) {
//      this.rootNode = node;
//    }

    return node;
  }

  public static final byte[] ensureSize(final byte[] array, final int num) {
    if (num >= array.length) {
      final byte[] newArray = new byte[array.length << 1];
      System.arraycopy(array, 0, newArray, 0, array.length);
      return newArray;
    }

    return array;
  }

  public static final short[] ensureSize(final short[] array, final int num) {
    if (num >= array.length) {
      final short[] newArray = new short[array.length << 1];
      System.arraycopy(array, 0, newArray, 0, array.length);
      return newArray;
    }

    return array;
  }

  public static final ASTImpl[] ensureSize(final ASTImpl[] array, final int num) {
    if (num >= array.length) {
      final ASTImpl[] newArray = new ASTImpl[array.length << 1];
      System.arraycopy(array, 0, newArray, 0, array.length);
      return newArray;
    }

    return array;
  }

  public static final String[] ensureSize(final String[] array, final int num) {
    if (num >= array.length) {
      final String[] newArray = new String[array.length << 1];
      System.arraycopy(array, 0, newArray, 0, array.length);
      return newArray;
    }

    return array;
  }

//public static int totalAsts = 0;
//public static int excessiveAsts = 0;
//public static int totalBytes = 0;
//public static int zeroBytes = 0;

  // TODO should be possible to just sort arrays - not copy from one to another
  public final void recompress(TreeASTImpl rootNode) {
//    int astsCreated = this.astsNum;

//    final int oldNodes = this.astsNum;
    final int totalNodes = rootNode == null ? 0 : countReachableNodes(rootNode);
//totalAsts += totalNodes;
//excessiveAsts += oldNodes - totalNodes;

    final String[] newstrings = new String[totalNodes];
    final short[] newstartLines = new short[totalNodes];
    final short[] newstartColumns = new short[totalNodes];
    final short[] newendLines = new short[totalNodes];
    final short[] newendColumns = new short[totalNodes];
    final byte[] newmetaBytes = new byte[totalNodes];
    final byte[] newastTypes = new byte[totalNodes];

//    ASTImpl[] oldAstIndex = new TreeASTImpl[oldNodes];
//    System.arraycopy(this.astIndex, 0, oldAstIndex, 0, oldNodes);

    try {
      this.astsNum = 0;

      for (TreeASTImpl cur = (TreeASTImpl) rootNode; cur != null;
          cur = (TreeASTImpl) cur.getNextSibling()) {
        byte structure = SIBLING;
        if (cur == rootNode) {
          structure = CHILD;
        }
        if (cur.getNextSibling() == null) {
          structure |= LASTSIBLING;
        }
        recompress(cur, structure, newstrings, newstartLines, newstartColumns,
            newendLines, newendColumns, newmetaBytes, newastTypes);
      }
    } finally {
//      System.err.println("index: " + this.astIndex.length
//                + " - created: " + t_strings.size()
//                + " - actual: " + totalNodes);

//      stats[((int)t_strings.size() / 100) * 100]++;

//      for (int i = 0; i < oldAstIndex.length; i++) {
//        ASTImpl node = oldAstIndex[i];
//        if (node.getParent() == null) {
//  System.err.println("orphane node: " + node);
//        }
//      }

      this.strings = newstrings;
      this.startLines = newstartLines;
      this.startColumns = newstartColumns;
      this.endLines = newendLines;
      this.endColumns = newendColumns;
      this.metaBytes = newmetaBytes;
      this.astTypes = newastTypes;

//      for (int i = 0; i < this.strings.length; i++) {
//        totalBytes += 9;
//        if (this.startLines[i] < 256) {
//          zeroBytes++;
//          if (this.startLines[i] == 0) {
//            zeroBytes++;
//          }
//        }
//        if (this.startColumns[i] < 256) {
//          zeroBytes++;
//          if (this.startColumns[i] == 0) {
//            zeroBytes++;
//          }
//        }
//        if (this.endLines[i] < 256) {
//          zeroBytes++;
//          if (this.endLines[i] == 0) {
//            zeroBytes++;
//          }
//        }
//        if (this.endColumns[i] < 256) {
//          zeroBytes++;
//          if (this.endColumns[i] == 0) {
//            zeroBytes++;
//          }
//        }
//        if (this.metaBytes[i] == 0) {
//          zeroBytes++;
//        }
//      }

//GlobalOptions.logUsage(fileLength + "\t" + astsCreated + "\t" + this.astIndex.length + "\t" + this.astsNum);

      // parser generates more ASTs, but doesn't add all of them to the tree
      // so we trim extra nodes
      ASTImpl[] newAstIndex = new TreeASTImpl[this.astsNum];
      System.arraycopy(this.astIndex, 0, newAstIndex, 0, this.astsNum);
      if (ProjectLoader.isLowMemoryMode()) {
        this.astIndexReference = new WeakReference(newAstIndex);
      } else {
        this.astIndexReference = new SoftReference(newAstIndex);
      }
      this.astIndex = null; // releasing hard link
      this.backReference.astIndex = newAstIndex;

      // everything created - we no longer need it here
      this.backReference = null;
      this.changedLifeline = null; // all changes were legal, doesn't need to force reload
//      this.rootNode = null;
      // now it should hang on soft reference only
    }
  }

  private final void findTypes() {
    this.typeNames = new HashSet();
    this.typeFullNames = new HashSet();
    this.superTypeNames = new HashSet();
    this.packageName = "";

    for (int index = 0; index < this.astTypes.length; index++) {
      if (this.astTypes[index] == JavaTokenTypes.PACKAGE_DEF) {
//new rantlr.debug.misc.ASTFrame("pack", getAstAt(index)).setVisible(true);
        ++index;

        if (this.astTypes[index] == JavaTokenTypes.ANNOTATIONS) {
          if (index + 1 < this.astTypes.length
              && (this.astTypes[index + 1] == JavaTokenTypes.IDENT
              || this.astTypes[index + 1] == JavaTokenTypes.DOT)) {
            ++index;
          } else { // trying to skip the whole ANNOTATIONS section
            index = ((TreeASTImpl) getAstAt(index).getNextSibling()).getIndex();
          }
        }

        while (this.astTypes[index] == JavaTokenTypes.IDENT
            || this.astTypes[index] == JavaTokenTypes.DOT) {
          if (this.astTypes[index] == JavaTokenTypes.IDENT) {
            if (this.packageName.length() > 0) {
              this.packageName += '.';
            }
            this.packageName += this.strings[index];
            this.lastPackageName = this.strings[index];
          }
          ++index;
        }
      }

      // JAVA5: what about annotations here? doesn't it mess up the name?
//System.err.println("this.astTypes[index]: " + this.astTypes[index]);
      if (isTypeStart(this.astTypes[index])) {
        index = goDeeper(index, true, false, null); // visit type
      }
    }

    if (this.lastPackageName == null) {
      this.lastPackageName = "";
    }
  }

  private final static boolean isTypeStart(int astType) {
    switch (astType) {
      case JavaTokenTypes.CLASS_DEF:
      case JavaTokenTypes.INTERFACE_DEF:
      case JavaTokenTypes.ENUM_DEF:
      case JavaTokenTypes.ANNOTATION_DEF:

//        ////////////////////////
//        ArrayList types = new ArrayList();
//        for (int k = index > 0 ? index - 1 : index;
//             k < index + 20 && k < this.astTypes.length; k++) {
//          types.add(Byte.toString(this.astTypes[k]) + " - " + this.strings[k]
//              + " - " + this.metaBytes[k]);
//        }
//        System.err.println("nodes: " + types);
//        ////////////////////////

        return true;

      default:
        return false;
    }
  }

  /** @return index of last analyzed AST, caller should ++ itself when needed */
  private final int goDeeper(final int parent,
      boolean trackTypes, boolean trackSupers, String typeName) {
//System.err.println("goDeeper: " + parent + " - " + this.astTypes[parent] + " " + this.strings[parent] + ", types: " + trackTypes + ", supers: " + trackSupers + ", meta: " + this.metaBytes[parent]);
    int index = parent + 1;
    boolean scanningBody = this.astTypes[parent] == JavaTokenTypes.OBJBLOCK;
    int level = 0;

    while (index < this.astTypes.length) {
      if (isChild(this.metaBytes[index])) {
        ++level;
      }
      if (isLast(this.metaBytes[index])) {
        --level;
      }
//System.err.println("check: " + index + " - " + this.astTypes[index] + " " + this.strings[index] + ", types: " + trackTypes + ", supers: " + trackSupers + ", meta: " + this.metaBytes[index] + ", level: " + level);

      if (trackTypes && this.astTypes[index] == JavaTokenTypes.IDENT) {
        typeName = addTypeName(index, typeName);
        trackTypes = false; // no more type names in a single type
      }

      if (trackSupers) {
        if (this.astTypes[index] == JavaTokenTypes.IDENT
            && (this.astTypes[parent] != JavaTokenTypes.DOT // simple name
            || !isChild(this.metaBytes[index]))) { // not a package in complex name
          this.superTypeNames.add(this.strings[index]);
        }
      }

      switch (this.astTypes[index]) {
        case JavaTokenTypes.OBJBLOCK:
          index = goDeeper(index, false, false, typeName);
          break;

        case JavaTokenTypes.CLASS_DEF:
        case JavaTokenTypes.INTERFACE_DEF:
        case JavaTokenTypes.ENUM_DEF:
        case JavaTokenTypes.ANNOTATION_DEF:
          index = goDeeper(index, true, false, typeName);
          break;

        case JavaTokenTypes.EXTENDS_CLAUSE:
        case JavaTokenTypes.IMPLEMENTS_CLAUSE:
          trackSupers = true;
          // fallthrough
        default:
          if (!scanningBody && index + 1 < this.astTypes.length
              && isChild(this.metaBytes[index + 1])) {
            boolean notParams
                = this.astTypes[index] != JavaTokenTypes.TYPE_ARGUMENTS
                && this.astTypes[index] != JavaTokenTypes.TYPE_PARAMETERS;
            boolean aPackage = this.astTypes[parent] == DOT
                && this.astTypes[index] == DOT;
            index = goDeeper(index, trackTypes && notParams,
                trackSupers && notParams && !aPackage, typeName);
          }
          break;
      }

      if (level == 0) { // last sibling, go up now
        break;
      }

      ++index;
    }

//System.err.println("leaving: " + parent + " - " + this.astTypes[parent] + " " + this.strings[parent] + ", types: " + trackTypes + ", supers: " + trackSupers + ", meta: " + this.metaBytes[parent]);
    return index;
  }

  private String addTypeName(int index, String parentName) {
    String aName = this.strings[index];
    this.typeNames.add(aName);
    if (parentName == null) {
      parentName = aName;
    } else {
      parentName += '$' + aName;
    }
    this.typeFullNames.add(parentName);
    return parentName;
  }

  private static final int countReachableNodes(final ASTImpl node) {
    int result = 1;

    ASTImpl child = (ASTImpl) node.getFirstChild();
    if (child != null) {
      result += countReachableNodes(child);
    }
    ASTImpl sibling = (ASTImpl) node.getNextSibling();
    if (sibling != null) {
      result += countReachableNodes(sibling);
    }

    return result;
  }

  private void recompress(final TreeASTImpl node, final byte structurePlace,
      final String[] newstrings,
      final short[] newstartLines,
      final short[] newstartColumns,
      final short[] newendLines,
      final short[] newendColumns,
      final byte[] newmetaBytes,
      final byte[] newastTypes
      ) {
    final int oldIndex = node.getIndex();
//System.err.println("recomp: " + t_astTypes.get(ind) + " " + t_strings.get(ind)
//        + ", ind: " + node.backRef.astIndex
//        + ", hex: " + Integer.toHexString(System.identityHashCode(node)));

    final int newIndex = this.astsNum;
    newstrings[newIndex] = strings[oldIndex];
    newastTypes[newIndex] = astTypes[oldIndex];
    newmetaBytes[newIndex] = structurePlace;
    newstartLines[newIndex] = startLines[oldIndex];
    newstartColumns[newIndex] = startColumns[oldIndex];
    newendLines[newIndex] = endLines[oldIndex];
    newendColumns[newIndex] = endColumns[oldIndex];

    this.astIndex[newIndex] = node;
    node.setIndex(newIndex);
    ++this.astsNum;

    int forwardStructure = -1;
    for (TreeASTImpl c = (TreeASTImpl) node.getFirstChild(); c != null;
        c = (TreeASTImpl) c.getNextSibling()) {
      if (forwardStructure == -1) {
        forwardStructure = CHILD;
      } else if (forwardStructure == CHILD) {
        forwardStructure = SIBLING;
      }

      c.setParent(node);
      if (c.getNextSibling() == null) {
        forwardStructure |= LASTSIBLING;
      }

      recompress(c, (byte) forwardStructure,
          newstrings, newstartLines, newstartColumns, newendLines,
          newendColumns, newmetaBytes, newastTypes);
    }
  }

  public String toString() {
    return super.toString() + " " + this.strings.length + " nodes";
  }

  public static final class BackReference {
    BackReference(ASTImpl[] astIndex, final ASTTree ownerTree) {
      this.astIndex = astIndex;
      this.ownerTree = ownerTree;
    }

    /** NB! Will hold the reference to astIndex array so it won't be garbage
     * collected when any of the TreeASTImpl nodes is alive */
    public ASTImpl[] astIndex;

    public final ASTTree ownerTree;
  }


  public final void markChanged() {
    if (this.changedLifeline == null) {
      this.changedLifeline = getAstIndex();
      if (Assert.enabled) {
        Assert.must(this.changedLifeline != null, "Impossible thing happened");
      }
    }
  }

  public final boolean isChanged() {
    return this.changedLifeline != null;
  }

  private void readObject(final java.io.ObjectInputStream in) throws
      IOException, ClassNotFoundException {
    in.defaultReadObject();
    byte[] types = this.astTypes;
    String[] strs = this.strings;
    for (int i = strs.length; --i >= 0; ) {
       strs[i] = ASTUtil.intern(types[i], strs[i]);
    }
  }

  /** @return single word type names, e.g. A, B, no owners or package name */
  public final HashSet getTypeNames() {
    if (this.typeNames == null) {
      findTypes();
    }
    return this.typeNames;
  }

  /** @return names with all owners, e.g. A$B$C, but no package name */
  public final HashSet getTypeFullNames() {
    if (this.typeFullNames == null) {
      findTypes();
    }
    return this.typeFullNames;
  }

  public final HashSet getSuperTypeNames() {
    if (this.superTypeNames == null) {
      findTypes();
    }
    return this.superTypeNames;
  }

  public final String getPackageName() {
    if (this.packageName == null) {
      findTypes();
    }
    return this.packageName;
  }

  public final String getLastPackageName() {
    if (this.lastPackageName == null) {
      findTypes();
    }
    return this.lastPackageName;
  }

  public final HashSet getIdents() {
    if (this.idents == null) {
      int len = this.astTypes.length;
      HashSet ids = new HashSet((int) (len * 0.3f), 0.93f);
      for (int i = len; --i >= 0; ) {
        if (this.astTypes[i] == JavaTokenTypes.IDENT) {
          ids.add(strings[i]);
        }
      }
      this.idents = ids;
    }

    return this.idents;
  }
}
