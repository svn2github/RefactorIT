/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.query.text;


import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.WildcardPattern;
import net.sf.refactorit.vfs.Source;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.List;


/**
 *
 * @author  tanel
 */
public class NonJavaSourcesIndexer {
  private WildcardPattern[] patterns;

  public NonJavaSourcesIndexer(WildcardPattern[] patterns) {
    this.patterns = patterns;
  }

  public void visit(Project project) {
    List sources = project.getPaths().getSourcePath().getNonJavaSources(patterns);
    int size = sources.size();

    for (int i = 0; i < size; i++) {
      Source source = (Source) sources.get(i);

      try {
        visit(source);
      } catch (IOException e) {
        // TODO: figure out what to do with that
        System.err.println("Failed to visit " + source);
      }
    }
  }

  public void visit(Source source) throws IOException {
    LineNumberReader reader = new LineNumberReader(new InputStreamReader(source
        .getInputStream()));
    for (String lineContent = reader.readLine(); lineContent != null;
        lineContent = reader.readLine()) {
      Line line = new Line(source, reader.getLineNumber(), lineContent);
      visit(line);
    }

    reader.close();
  }

  public void visit(Line line) {
  }
}
