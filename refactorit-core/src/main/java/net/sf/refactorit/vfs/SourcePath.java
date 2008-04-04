/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.vfs;


import net.sf.refactorit.common.util.WildcardPattern;

import java.util.List;


/**
 * Representation of source path.
 * Provides a set of root directories.
 *
 * @author  Igor Malinin
 */
public interface SourcePath {
    /**
     * List of root Sources.
     *
     * @return  list of root Source directories
     */
    Source[] getRootSources();

    List getAllSources();

    List getIgnoredSources();

    FileChangeMonitor getFileChangeMonitor();

    Source[] getAutodetectedElements();

    List getNonJavaSources(WildcardPattern [] patterns);

    /**
     * For customizing sourcepath. Returns toplevel sources which can be used
     *    for creating specified sourcepath
     */
    Source[] getPossibleRootSources();

    boolean isIgnoredPath(String absolutePath);
}
