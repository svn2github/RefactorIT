/*
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */

package database;

import java.util.*;
import javax.ejb.*;
import java.rmi.RemoteException;
import exception.*;

public interface BookDBEJB extends EJBObject {
   public BookDetails getBookDetails(String bookId) throws RemoteException, BookNotFoundException;
   public int getNumberOfBooks() throws RemoteException, BooksNotFoundException;
   public Collection getBooks() throws RemoteException, BooksNotFoundException;
}
