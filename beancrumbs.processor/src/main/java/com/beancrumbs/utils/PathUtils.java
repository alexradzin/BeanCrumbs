package com.beancrumbs.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class PathUtils {
	public static File findCommonParent(File one, File two) throws IOException {
		return 	findCommonParentImpl(normalize(one), normalize(two));
	}

	private static File findCommonParentImpl(File one, File two) {
		Collection<File> chain1 = split(one);
		Collection<File> chain2 = split(two);
		
		File parent = null;
		File lastEqual = null;
		
		Iterator<File> it1 = chain1.iterator();
		Iterator<File> it2 = chain2.iterator();
		
		while(it1.hasNext() && it2.hasNext()) {
			parent = lastEqual;
			File f1 = it1.next();
			File f2 = it2.next();
			
			if (!f1.equals(f2)) {
				break;
			}
			lastEqual = f1;
		}

		// If one of the iterators still has elements move parent down. 
		if (it1.hasNext() || it2.hasNext()) {
			parent = lastEqual;
		}
		
		return parent;
	}
	
	private static File normalize(File file) throws IOException {
		return file == null ? null : file.getAbsoluteFile().getCanonicalFile();
	}
	
	private static Collection<File> split(File file) {
		List<File> chain = new ArrayList<>();
		for (File f = file; f != null; f = f.getParentFile()) {
			chain.add(0, f);
		}
		return chain;
	}
}
