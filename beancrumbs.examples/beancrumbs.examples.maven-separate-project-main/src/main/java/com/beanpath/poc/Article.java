package com.beanpath.poc;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement
@XmlType(propOrder = {"title", "magazine"})
public class Article {
	private String title;
	private String author;
	private Magazine magazine;
	
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public Magazine getMagazine() {
		return magazine;
	}
	public void setMagazine(Magazine magazine) {
		this.magazine = magazine;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	
	
}
