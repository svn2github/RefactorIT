/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.loader;

import net.sf.refactorit.classmodel.Project;


/**
 * All classes that want to be notified on changes on Project must implement
 * this interface. Also, to be notified on changes the objects of these classes
 * must register themselves via Project.add<>Listener(ProjectChangedListener listener)
 * function. The objects are notified on events via interface functions when
 * project changes.
 *
 * @author Jaanek Oja
 */
public interface ProjectChangedListener {

  /**
   * This function is called when rebuild has been done (finished) to the whole
   * project.
   *
   * After rebuild to the project, all listeners are notified on that
   * event by this function. The rebuild means that source files that has
   * been changed are parsed by the Parser and CompilationUnit array (the Project
   * holds it) is rebuilt. So, any references you have queried from Project
   * object for CompilationUnit objects has been freed by Project itself.
   * So, you have to free these references to objects and redisplay any
   * references to these objects using new Project CompilationUnit objects.
   *
   * @param project the project on what the rebuild was performed.
   */
  void rebuildPerformed(Project project); //RebuildEvent <-- add this param if needed

  /**
   * This function is called when the project rebuild is in the start phase. No actions are
   * yet executed to rebuild a new project generation.
   *
   * Implement this function to release any resources that you hold in on the old project
   * generation.
   *
   * @param project the old project generation that is going to be replaced by a new one by
   * this function caller.
   */
  void rebuildStarted(Project project);
}
