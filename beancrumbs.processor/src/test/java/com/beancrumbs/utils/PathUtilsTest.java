package com.beancrumbs.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

public class PathUtilsTest {
	@Test
	public void testFindCommonParentTwoNulls() throws IOException {
		testFindCommonParent(null, null, null);
	}
	
	@Test
	public void testFindCommonParentTwoRoots() throws IOException {
		File root = File.listRoots()[0];
		testFindCommonParent(root, root, null);
	}
	
	@Test
	public void testFindCommonParentTwoEqualDirs() throws IOException {
		File tmp = new File(System.getProperty("java.io.tmpdir"));
		testFindCommonParent(tmp, tmp, tmp.getParentFile());
	}

	@Test
	public void testFindCommonParentDirAndFile() throws IOException {
		File tmp = new File(System.getProperty("java.io.tmpdir"));
		File file = File.createTempFile("tmp", "tmp", tmp);
		file.deleteOnExit();
		testFindCommonParent(tmp, file, tmp);
		testFindCommonParent(file, tmp, tmp);
	}
	
	@Test
	public void testFindCommonParent2FilesInOneDir() throws IOException {
		File tmp = new File(System.getProperty("java.io.tmpdir"));
		File file1 = File.createTempFile("tmp", "tmp", tmp);
		File file2 = File.createTempFile("tmp", "tmp", tmp);
		file1.deleteOnExit();
		file2.deleteOnExit();
		testFindCommonParent(file1, file2, tmp);
		testFindCommonParent(file2, file1, tmp);
		testFindCommonParent(file1, file1, tmp);
		testFindCommonParent(file2, file2, tmp);
	}
	
	
	@Test
	public void testAntLikeProject() throws IOException {
		testProject("src", "classes");
	}

	@Test
	public void testMavnLikeProject() throws IOException {
		testProject("src/main/java", "target/classes");
	}
	
	
	private void testFindCommonParent(File one, File two, File expected) throws IOException {
		assertEquals(expected, PathUtils.findCommonParent(one, two));
	}
	
	private boolean delete(File f) throws IOException {
		boolean subRestult = true;
		if (f.isDirectory()) {
			for (File c : f.listFiles()) {
				if (!delete(c)) {
					subRestult = false;
				}
			}
		}
		return subRestult && f.delete();
	}
	
	public void testProject(String srcRoot, String classesRoot) throws IOException {
		File tmp = new File(System.getProperty("java.io.tmpdir"));
		File projectDir = new File(tmp, "project" + System.currentTimeMillis());
		
		try {
			assertTrue(projectDir.mkdir());
			
			File srcDir = new File(projectDir, srcRoot);
			assertTrue(srcDir.mkdirs());
			
			File classesDir = new File(projectDir, classesRoot);
			assertTrue(classesDir.mkdirs());
			
			final String packagePath = "com/company/app/";
			File packageDir = new File(srcDir, packagePath);
			assertTrue(packageDir.mkdirs());
			
			File javaSrc = new File(srcDir, packagePath + "Main.java");
			
			File someResource = new File(classesDir, "theresource.properties");
			
			testFindCommonParent(javaSrc, someResource, projectDir);
		} finally {
			assertTrue(delete(projectDir));
		}
	}
	
	
}
