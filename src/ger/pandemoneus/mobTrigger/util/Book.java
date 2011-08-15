package ger.pandemoneus.mobTrigger.util;

import java.util.ArrayList;

/**
 * This class represents a book containing pages.
 * 
 * It is made for users that do not use any chat modifications.
 * 
 * @author Pandemoneus - https://github.com/Pandemoneus
 */
public final class Book {
	private final ArrayList<Page> pages = new ArrayList<Page>();
	
	/**
	 * Constructs a book containing the passed pages.
	 * 
	 * @param pages the pages the book should contain
	 */
	public Book(Page[] pages) {
		for (Page p : pages) {
			this.pages.add(p);
		}
	}
	
	/**
	 * Constructs a book with no pages.
	 */
	public Book() {}
	
	/**
	 * Adds a page to this book.
	 * 
	 * @param page the page to add
	 */
	public void addPage(Page page) {
		if (page != null) {
			pages.add(page);
		}
	}
	/**
	 * Gets the page at the given index.
	 * 
	 * @param index the index 
	 * @return the page at the given index or null if the index is invalid
	 */
	public Page getPage(int index) {
		Page result = null;
		
		if (index >= 0 && index <= size()) {
			result = pages.get(index);
		}
		
		return result; 
	}
	
	/**
	 * Returns the amount of pages the book contains.
	 * 
	 * @return the amount of pages the book contains
	 */
	public int size() {
		int result = 0;
		
		for (Page p : pages) {
			if (p != null) {
				result++;
			}
		}
		
		return result;
	}
}
