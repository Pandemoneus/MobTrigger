package ger.pandemoneus.mobTrigger.util;

import java.util.ArrayList;

/**
 * This class represents a collection of books.
 * 
 * It is made for users that do not use any chat modifications.
 * 
 * @author Pandemoneus - https://github.com/Pandemoneus
 */
public final class BookCollection {
	private final ArrayList<Book> col = new ArrayList<Book>();
	
	/**
	 * Constructs a new empty book collection.
	 */
	public BookCollection() {}
	
	/**
	 * Adds a book to the book collection.
	 * 
	 * Note that a book is not added if it is already contained in the collection.
	 * 
	 * @param book the book to add
	 */
	public void addBook(Book book) {
		addBook(col.size(), book);
	}
	
	/**
	 * Adds a book to the book collection at the given index.
	 * 
	 * Note that a book is not added if it is already contained in the collection.
	 * 
	 * @param index the index
	 * @param book the book to add
	 */
	public void addBook(int index, Book book) {
		if (book == null || col.contains(book) || index <= 0 || index >= col.size())
			return;
		
		col.add(index, book);
	}
	
	/**
	 * Gets the book at the given index from the book collection.
	 * 
	 * @param index the index
	 * @return the book at the given index or null if index is invalid
	 */
	public Book getBook(int index) {
		Book result = null;
		
		if (index >= 0 && index < col.size()) 
			result = col.get(index);
		
		return result;
	}
	
	/**
	 * Returns the size of the book collection.
	 * 
	 * @return the size of the book coolection
	 */
	public int size() {
		return col.size();
	}
	
	/**
	 * Removes the book at the given index.
	 * 
	 * Note that no book is removed if the index is invalid.
	 * 
	 * @param index the index
	 */
	public void removeBook(int index) {
		if (getBook(index) != null) {
			col.remove(index);
		}
	}
	
	/**
	 * Returns all books contained in this collection.
	 * 
	 * @return all books contained in this collection
	 */
	public ArrayList<Book> getBooks() {
		return col;
	}
}
