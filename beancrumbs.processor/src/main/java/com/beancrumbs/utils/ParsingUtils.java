package com.beancrumbs.utils;

import java.util.regex.Pattern;

public class ParsingUtils {
	public static Pattern wildcardToPattern(String wildcard) {
		String regex = wildcard.replace("?", ".").replace("*", ".*");
		return Pattern.compile(regex);
	}
}
