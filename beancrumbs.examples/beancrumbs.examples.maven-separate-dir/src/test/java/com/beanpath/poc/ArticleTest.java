package com.beanpath.poc;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;

import org.apache.commons.beanutils.BeanUtils;
import org.junit.Test;

//import com.beancrumbs.nullsafe.NullSafe;

public class ArticleTest {
	@Test
	public void skeletonTest() throws ParseException, ReflectiveOperationException {
		Magazine infoq = new Magazine();
		infoq.setName("InfoQ");
		
		Article article = new Article();
		article.setAuthor("Joshua Bloch");
		article.setTitle("Bumper-Sticker API Design");
		article.setMagazine(infoq);
		
		
		assertEquals(infoq.getName(), BeanUtils.getProperty(infoq, MagazineSkeleton.name));

		assertEquals(article.getTitle(), BeanUtils.getProperty(article, ArticleSkeleton.title));
		assertEquals(article.getAuthor(), BeanUtils.getProperty(article, ArticleSkeleton.author));
		assertEquals(article.getMagazine().getName(), BeanUtils.getProperty(article, ArticleSkeleton.magazine.name));
	}

	/*	
	@Test
	public void nullSafeTest() {
		Article article = new Article();
		Article nullSafeArticle = NullSafe.$(article);
		assertNull(nullSafeArticle.getMagazine().getName());
		assertNull(nullSafeArticle.getAuthor());
		assertNull(nullSafeArticle.getTitle());
		
		assertNull(article.getMagazine());
		assertNotNull(nullSafeArticle.getMagazine()); // returns wrapped object
	}
	*/
}
