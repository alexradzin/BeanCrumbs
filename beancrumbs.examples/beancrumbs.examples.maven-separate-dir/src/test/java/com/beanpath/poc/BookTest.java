package com.beanpath.poc;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import org.apache.commons.beanutils.BeanUtils;
import org.junit.Test;

public class BookTest {
	@Test
	public void skeletonTest() throws ParseException, ReflectiveOperationException {
		Person levTolstoy = new Person();
		levTolstoy.setFirstName("Lev");
		levTolstoy.setLastName("Tolstoy");
		levTolstoy.setBithday(new SimpleDateFormat("MMMM DD, yyyy", Locale.US).parse("September 7, 1828"));
		
		Book book = new Book();
		book.setAuthor(levTolstoy);
		book.setTitle("War and Peace");
		book.setIsbn("12345");
		
		
		assertEquals(levTolstoy.getFirstName(), BeanUtils.getProperty(levTolstoy, PersonSkeleton.firstName));
		assertEquals(levTolstoy.getLastName(), BeanUtils.getProperty(levTolstoy, PersonSkeleton.lastName));
		assertEquals(String.valueOf(levTolstoy.getBithday()), BeanUtils.getProperty(levTolstoy, PersonSkeleton.bithday));

		assertEquals(levTolstoy.getFirstName(), BeanUtils.getProperty(book, BookSkeleton.author.firstName));
		assertEquals(levTolstoy.getLastName(), BeanUtils.getProperty(book, BookSkeleton.author.lastName));
		assertEquals(String.valueOf(levTolstoy.getBithday()), BeanUtils.getProperty(book, BookSkeleton.author.bithday));
		
		assertEquals(book.getTitle(), BeanUtils.getProperty(book, BookSkeleton.title));
		assertEquals(book.getIsbn(), BeanUtils.getProperty(book, BookSkeleton.isbn));
	}
}
