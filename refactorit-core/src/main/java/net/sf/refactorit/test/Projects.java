/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.test;


import net.sf.refactorit.common.exception.SystemException;
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.exception.ErrorCodes;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * Allows accessing list of projects to test.
 */
public class Projects {

  /** Projects ({@link ProjectMetadata} instances). */
  private final List projects;

  /** Maps project ID to project metadata ({@link ProjectMetadata} instance). */
  private final Map idToProject = new HashMap();

  /**
   * Loads projects from file.
   *
   * @param file file.
   */
  public Projects(File file) {
    final DocumentBuilderFactory builderFactory =
        DocumentBuilderFactory.newInstance();

    final Document doc;
    try {
      final DocumentBuilder builder = builderFactory.newDocumentBuilder();
      doc = builder.parse(file);
    } catch (Exception e) {
      AppRegistry.getExceptionLogger().error(e,this);
      throw new SystemException(ErrorCodes.INTERNAL_ERROR, e);
    }

    this.projects = parseProjects(doc.getDocumentElement());

    for (final Iterator i = projects.iterator();
        i.hasNext(); ) {
      final ProjectMetadata project = (ProjectMetadata) i.next();
      if ((project.getId() == null) || (project.getId().length() == 0)) {
        continue; // Don't add this project to ID -> project map.
      }

      idToProject.put(project.getId(), project);
    }
  }

  /**
   * Parses <code>projects</code> element.
   *
   * @param projectsElement <code>projects</code> element.
   *
   * @return list of projects ({@link ProjectMetadata} instances).
   *         Never returns <code>null</code>.
   */
  private static List parseProjects(Element projectsElement) {
    final NodeList children = projectsElement.getChildNodes();
    final List projects = new LinkedList();
    if (children != null) {
      for (int i = 0, max = children.getLength(); i < max; i++) {
        final Node child = children.item(i);
        if (child.getNodeType() != Node.ELEMENT_NODE) {
          continue; // Not an element
        }
        final Element projectElement = (Element) child;
        if (!"project".equals(projectElement.getTagName())) {
          continue; // Not a project element
        }

        final ProjectMetadata project = parseProject((Element) child);
        projects.add(project);
      }
    }

    return projects;
  }

  /**
   * Parses <code>project</code> element.
   *
   * @param projectElement <code>project</code> element.
   * @return project metadata. Never returns <code>null</code>.
   */
  private static ProjectMetadata parseProject(Element projectElement) {

    Element copyPathElement = getFirstChild(projectElement, "copypath");
    String copyPath = null;
    if (copyPathElement != null) {
      List paths = parsePath(copyPathElement);
      if (paths != null && paths.size() > 0) {
        copyPath = (String) paths.get(0);
      }
    }

    Element ignoredPathElement = getFirstChild(projectElement, "ignoredpath");
    final List ignoredPaths =
        ((ignoredPathElement == null) ? null : parsePath(ignoredPathElement));

    Element sourcePathElement = getFirstChild(projectElement, "sourcepath");
    final List sourcePaths =
        ((sourcePathElement == null) ? null : parsePath(sourcePathElement));

    Element classPathElement = getFirstChild(projectElement, "classpath");
    final List classPaths =
        ((classPathElement == null) ? null : parsePath(classPathElement));

    String loadTest = projectElement.getAttribute("auto_load_test");
    boolean testForLoad = true;
    if (loadTest != null && loadTest.equals("false")) {
      testForLoad = false;
    }

    return new ProjectMetadata(
        projectElement.getAttribute("id"),
        projectElement.getAttribute("name"),
        copyPath,
        sourcePaths,
        classPaths,
        ignoredPaths,
        testForLoad);
  }

  /**
   * Parses path (<code>sourcepath</code> or <code>classpath</code>) element.
   *
   *
   * @return list of paths (<code>String</code> instances).
   *         Never returns <code>null</code>.
   */
  private static List parsePath(Element pathElement) {
    final List paths = new ArrayList();

    final NodeList children = pathElement.getElementsByTagName("pathelement");
    if (children != null) {
      for (int i = 0, max = children.getLength(); i < max; i++) {
        final Element child = (Element) children.item(i);
        String path = null;
        path = child.getAttribute("path");

        if (path.length() == 0) {
          path = child.getAttribute("location");
        }

        if (path != null) {
          paths.add(path);
        }
      }
    }

    return paths;
  }

  /**
   * Gets first child of the specified element which has specified name.
   *
   * @param element element.
   * @param name name.
   *
   * @return child or <code>null</code> if not found.
   */
  private static Element getFirstChild(Element element, String name) {
    final NodeList children = element.getElementsByTagName(name);
    if ((children == null) || (children.getLength() == 0)) {
      return null;
    }

    return (Element) children.item(0);
  }

  /**
   * Gets list of projects.
   *
   * @return projects ({@link ProjectMetadata} instances). Never returns
   *         <code>null</code>.
   */
  public List getProjects() {
    return projects;
  }

  /**
   * Get project.
   *
   * @param id project ID from projects.xml
   *
   * @return project or <code>null</code> if not found.
   */
  public ProjectMetadata getProject(String id) {
    ProjectMetadata result = (ProjectMetadata) idToProject.get(id);
    if (result == null) {
      throw new IllegalArgumentException("Cannot find project for id '" + id
          + "'");
    }

    return result;
  }
}
