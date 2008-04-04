/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.jbuilder;

import com.borland.jbuilder.build.JavaBuilder;
import com.borland.primetime.build.Builder;
import com.borland.primetime.build.BuilderManager;
import com.borland.primetime.node.Project;

import java.util.Arrays;
import java.util.HashSet;


/**
 * Fixes showing in the Explorer tree of our own registered java nodes for JB7.<p>
 *
 * Retrieves build extensions from nodes which are objects of JavaFileNode as
 * well as any subclasses of JavaFileNode.<p>
 * Original version doesn't check subclasses, so if we register our own subclass
 * of JavaFileNode, "java" is no longer associated with the original version
 * of JavaFileNode and therefore the original JavaBuilder does not tell
 * PackageNode to include any files with extension "java".
 *
 * @author Anton Safonov
 */
public class JavaBuilderRe extends JavaBuilder {
//  private static final NodeArrayProperty NO_COPY_EXTENSIONS =
//      new NodeArrayProperty("build", "NoCopyExtensions");
//
//  private static final NodeArrayProperty COPY_EXTENSIONS =
//      new NodeArrayProperty("build", "CopyExtensions");

  /**
   * Redefinition of the method which causes problems with subclassing
   * JavaFileNode and registering those subclasses within JBuilder 7.<p>
   *
   * Original version just takes care of extension mapped to JavaFileNode;
   * if you subclass JavaFileNode and register this class for the suffix
   * "java" using FileNode.registerFileNodeClass(...), original JavaBuilder
   * will return an empty extension list, because there are no extensions
   * mapped to JavaFileNode any more (but to your subclass).<p>
   *
   * @param project JBuilder project
   * @return array of extensions bound to JavaFileNode or any subclass
   */
  public String[] getBuildExtensions(Project project) {
//    HashSet copyext =
//        new HashSet(Arrays.asList(COPY_EXTENSIONS.getValues(project)));
//    HashSet nocopyext =
//        new HashSet(Arrays.asList(NO_COPY_EXTENSIONS.getValues(project)));
//
//    Map map = FileType.getFileTypes();
//    Iterator iterator = map.keySet().iterator();
//
//    while (iterator.hasNext()) {
//      String s = (String) iterator.next();
//      Class nodeType = ((FileType) map.get(s)).getNodeType();
//      boolean isJavaNode = JavaFileNode.class.isAssignableFrom(nodeType);
//      if (isJavaNode && !nocopyext.contains(s)) {
//        copyext.add(s);
//      }
//    }
//    return (String[]) copyext.toArray(new String[copyext.size()]);

    HashSet exts = new HashSet();
    String[] buildExts = super.getBuildExtensions(project);
    if (buildExts != null) {
      exts.addAll(Arrays.asList(buildExts));
    }

    String[] ourExts = Builder.getRegisteredExtensions(JavaFileNodeRe.class);
    if (ourExts != null) {
      exts.addAll(Arrays.asList(ourExts));
    }
    return (String[]) exts.toArray(new String[exts.size()]);
  }

  /**
   * Redefinition of the Open Tool initialization.
   *
   * @param majorVersion  version of the OpenToolsAPI
   * @param minorVersion  version of JBuilder (minus 3 ...)
   */
  public static void initOpenTool(byte majorVersion, byte minorVersion) {
    if (minorVersion == (7 - 3 /*magic*/)) {
      // register our own Builder only when running JBuilder 7
      BuilderManager.registerBuilder(new JavaBuilderRe());
    } else {
      // init original builder
      JavaBuilder.initOpenTool(majorVersion, minorVersion);
    }
  }
}
