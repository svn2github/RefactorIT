/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ejb;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * @author Juri Reinsalu
 */
public class EjbEntityResolver implements EntityResolver {

  public InputSource resolveEntity(String publicId, String systemId)
          throws IOException {
    if (systemId.equals("http://java.sun.com/dtd/ejb-jar_2_0.dtd")) {
      // return a special input source                          
      InputStream dtdIs=this.getClass().getResourceAsStream("dtds/ejb-jar_2_0.dtd");
      Reader dtdReader=new InputStreamReader(dtdIs,"UTF-8");
      return new InputSource(dtdReader);
    }
      // use the default behaviour
      return null;
  }

}
