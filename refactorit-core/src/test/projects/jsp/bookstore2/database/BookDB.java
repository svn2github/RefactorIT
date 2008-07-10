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
import exception.*;

public class BookDB {

  private String bookId = "0";
  private BookDBEJB database = null;

  public BookDB () {
  }

  public void setBookId(String bookId) {
    this.bookId = bookId;
  }
    
  public void setDatabase(BookDBEJB database) {
    this.database = database;
  }

  public BookDetails getBookDetails() throws Exception {
    try {
        return (BookDetails)database.getBookDetails(bookId);				
    } catch (BookNotFoundException ex) {
        throw ex;
    } 
  }

  public Collection getBooks() throws Exception {
    try {
        return database.getBooks();
    } catch (BooksNotFoundException ex) {
        throw ex;
    } 

  }

  public int getNumberOfBooks() throws Exception {
    try {
        return database.getNumberOfBooks();
    } catch (BooksNotFoundException ex) {
        throw ex;
    } 		
  }
}
