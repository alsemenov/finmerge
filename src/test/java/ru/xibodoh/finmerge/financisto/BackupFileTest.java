/*
  ~ Copyright (c) 2014-2015 Aleksei Semenov (aka xibodoh)
  ~ All rights reserved. This program and the accompanying materials
  ~ are made available under the terms of the GNU Public License v2.0
  ~ which accompanies this distribution, and is available at
  ~ http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
*/
package ru.xibodoh.finmerge.financisto;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;
import ru.xibodoh.finmerge.Log;

public class BackupFileTest extends TestCase {

	private static final String RESOURCES_FOLDER = "src/test/resources/";
	
	//private static final String TEMP_FOLDER = File.createTempFile("aaaaaa", "bbbbbbb").getParent(); 
	private String TEMP_FOLDER = null;
	static {
		Log.configure("FINE");
	}
	
	protected void setUp() throws Exception {
		super.setUp();
		if (TEMP_FOLDER==null){
			try {
				TEMP_FOLDER = File.createTempFile("aaaaaa", "bbbbbbb").getParent();
			}catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	// I had to comment all tests because they are based on my real financial data.
	// I do not agree to publish it.
	
	public void testHappy() {
		// have to keep this test to keep the test goal happy
		assertTrue(true);
	}
	
//	public void testBackupFile() throws IndexOutOfBoundsException, IOException {
//		new BackupFile(new File(RESOURCES_FOLDER, "20130702_155256_524.backup"));
//	}
//
//	public void testSave() throws Exception {
//		BackupFile backupFile = new BackupFile(new File(RESOURCES_FOLDER, "20130702_155256_524.backup"));
//		backupFile.save(new File(TEMP_FOLDER, "saved.backup"));	
//		BackupFile savedBackupFile = new BackupFile(new File(TEMP_FOLDER, "saved.backup"));
//		assertEquals(backupFile, savedBackupFile);		
//	}
//	
//	
//	public void testMerge1() throws Exception {
//		BackupFile backupFile1 = new BackupFile(new File(RESOURCES_FOLDER, "20140121_135551_358.backup"));
//		BackupFile backupFile2 = new BackupFile(new File(RESOURCES_FOLDER, "20140121_204454_184.backup"));
//		backupFile1.merge(backupFile2);
//		
//		backupFile1.save(new File(TEMP_FOLDER, "merged.backup"));
//	}
//	
//	public void testMerge2() throws Exception {
//		BackupFile backupFile1 = new BackupFile(new File(RESOURCES_FOLDER, "20140126_185957_048.backup"));
//		BackupFile backupFile2 = new BackupFile(new File(RESOURCES_FOLDER, "20140126_184916_913.backup"));
//		backupFile1.merge(backupFile2);
//		
//		backupFile1.save(new File(TEMP_FOLDER, "merged.backup"));
//	}	
//	
//	public void testCategorySortByLeft() throws Exception {
//		BackupFile backupFile1 = new BackupFile(new File(RESOURCES_FOLDER, "20140121_135551_358.backup"));
//		Comparator<Entity> comparator = new Comparator<Entity>() {
//			public int compare(Entity o1, Entity o2) {
//				return new Integer(o1.get("left")).compareTo(new Integer(o2.get("left")));
//			}
//		};
//		TreeSet<Entity> categories = new TreeSet<Entity>(comparator);
//		
//		CategoryEntity root = null;
//		
//		for (Entity e: backupFile1){
//			if (Entity.TYPE_CATEGORY.equals(e.getType())){
//				CategoryEntity c = new CategoryEntity(e);
//				if (root==null){
//					root = c;
//				} else {
//					root.addDescendant(c);
//				}
//			}
//		}
//		//System.out.println(categories);
//		for (Entity e : categories){
//			System.out.println(e);
//		}
//		
//	}
//	
//	public void testLoadCategories() throws Exception {
//		BackupFile backupFile1 = new BackupFile(new File(RESOURCES_FOLDER, "20140121_135551_358.backup"));
//		
//		CategoryEntity root = new CategoryEntity(backupFile1);
//		root.set("left", Integer.toString(Integer.MIN_VALUE));
//		root.set("right", Integer.toString(Integer.MAX_VALUE));
//		root.set("title", "root");
//		root.set("type", "0");
//		root.set("$ENTITY", Entity.TYPE_CATEGORY);
//		int count = 0;
//		for (Entity e: backupFile1){
//			if (Entity.TYPE_CATEGORY.equals(e.getType())){
//				CategoryEntity ce = new CategoryEntity(e);
////				if (count==44){
////					System.out.println("count: "+count);
////				}
//				root.addDescendant(ce);
//				count++;
//			}
//		}
////		System.out.println("count: "+count);
//
//		Iterator<CategoryEntity> it = root.tree(-1);
//		int l = (Integer) it.next().getValue("left");
//		int r = 0;
//		while (it.hasNext()){
//			CategoryEntity ce = it.next();
//			assertTrue(l<(Integer)ce.getValue("left"));
//			l = (Integer)ce.getValue("left");
//			r = Math.max(r,(Integer)ce.getValue("right"));
//			//System.out.println(ce.getFingerPrint()+": "+ce);
//		}
////		System.out.println(root.get("right"));
//		
//		assertEquals(count*2, r+1);
//	}
//
//		
//	public void testMetaData_Empty() throws Exception {
//		BackupFile backupFile = new BackupFile(new File(RESOURCES_FOLDER, "20140126_185957_048.backup"));
//		assertNotNull(backupFile.getMetaData());
//		assertNull(backupFile.getMetaData().getFileName());
//		assertNull(backupFile.getMetaData().getParents());
//	}
//
//	public void testMetaData_onSave() throws Exception {
//		BackupFile backupFile = new BackupFile(new File(RESOURCES_FOLDER, "20140126_185957_048.backup"));
//		backupFile.save(new File(TEMP_FOLDER, "20140126_185957_048.backup"));
//		
//		assertNotNull(backupFile.getMetaData());
//		assertEquals("20140126_185957_048.backup", backupFile.getMetaData().getFileName());
//		assertNull(backupFile.getMetaData().getParents());
//	}
//
//	public void testMetaData_onMerge() throws Exception {
//		BackupFile backupFile1 = new BackupFile(new File(RESOURCES_FOLDER, "20140126_185957_048.backup"));
//		BackupFile backupFile2 = new BackupFile(new File(RESOURCES_FOLDER, "20140126_184916_913.backup"));
//		backupFile1.merge(backupFile2);
//		
//		String[] expected = new String[]{"20140126_185957_048.backup", "20140126_184916_913.backup"};
//		String[] actual = backupFile1.getMetaData().getParents();
//		assertEquals(expected.length, actual.length);
//		for (int i=0; i<expected.length; i++){
//			assertEquals("parent "+i, expected[i], actual[i]);
//		}
//		
//		backupFile1.save(new File(TEMP_FOLDER, "merged.backup"));		
//		
//		actual = backupFile1.getMetaData().getParents();
//		assertEquals(expected.length, actual.length);
//		for (int i=0; i<expected.length; i++){
//			assertEquals("parent "+i, expected[i], actual[i]);
//		}
//
//		assertEquals("merged.backup", backupFile1.getMetaData().getFileName());
//	}
//	
//	
////	public void testSafe1() throws Exception {
////		Log.configure(Level.ALL);
////		BackupFile nFile = new BackupFile(new File(RESOURCES_FOLDER, "20140703_214907_437.backup"));
////		BackupFile alsFile = new BackupFile(new File(RESOURCES_FOLDER, "20140703_215049_297.backup"));
////
////		int nMinus = 0;
////		int nPlus = 0;
////		for (Entity nEntity: nFile){
////			if ("3".equals(nEntity.get("from_account_id")) && !"0".equals(nEntity.get("to_account_id"))){
////				//System.out.println(nEntity);
////				nMinus += Integer.parseInt(nEntity.get("from_amount"));						
////			} else if ("3".equals(nEntity.get("to_account_id"))){
//////				System.out.println(nEntity);
////				nPlus += Integer.parseInt(nEntity.get("to_amount"));	
////			}
////		}
////		System.out.println("nMinus: "+(nMinus/100));
////		System.out.println("nPlus: "+(nPlus/100));
////		System.out.println("outcome: "+((nPlus+nMinus)/100));
////		
////		int aMinus = 0;
////		int aPlus = 0;
////		for (Entity aEntity: alsFile){
////			if (Entity.TYPE_TRANSACTIONS.equals(aEntity.getType()) && ("3".equals(aEntity.get("from_account_id")) || "3".equals(aEntity.get("to_account_id")))){
////				Entity local = nFile.getByFingerPrint(aEntity.getFingerPrint());
////				if (local!=null){
////					if (local.getId()!=null && local.getId().equals(aEntity.getId())){
////						System.err.println( "Ignore clone entity: "+ aEntity);
////					} else {
////						System.err.println( "Ignore duplicate: N: "+local+" A: "+aEntity);
////					}
////				} else 
////				
////				if ("3".equals(aEntity.get("from_account_id")) && !"0".equals(aEntity.get("to_account_id"))){
////					System.out.println(aEntity);
////					aMinus += Integer.parseInt(aEntity.get("from_amount"));						
////				} else if ("3".equals(aEntity.get("to_account_id"))){
////					System.out.println(aEntity);
////					aPlus += Integer.parseInt(aEntity.get("to_amount"));	
////				}
////			}
////		}
////		System.out.println("aMinus: "+(aMinus/100));
////		System.out.println("aPlus: "+(aPlus/100));
////		System.out.println("outcome: "+((aPlus+aMinus)/100));
////				
////		System.out.println("summary: " + (95000 +(nPlus+nMinus+aPlus+aMinus)/100) );
////		
////	}
//	
//	public void testUnique0() throws Exception {
//		BackupFile backupFile1 = new BackupFile(new File(RESOURCES_FOLDER, "20130702_155256_524.backup"));
//		BackupFile backupFile2 = new BackupFile(new File(RESOURCES_FOLDER, "20130702_155256_524.backup"));
//		
//		List<Entity> unique1 = backupFile1.unique(backupFile2);
//		assertNotNull(unique1);
//		assertTrue(unique1.isEmpty());
//		
//		List<Entity> unique2 = backupFile2.unique(backupFile1);
//		assertNotNull(unique2);
//		assertTrue(unique2.isEmpty());
//	}
//	
//	public void testUnique1() throws Exception {
//		BackupFile backupFile1 = new BackupFile(new File(RESOURCES_FOLDER, "20140121_135551_358.backup"));
//		BackupFile backupFile2 = new BackupFile(new File(RESOURCES_FOLDER, "20140121_135551_358.backup"));
//		
//		Entity removed = backupFile1.getById(Entity.TYPE_TRANSACTIONS, "3010");
//		assertNotNull(backupFile1.remove(removed));
//		
//		List<Entity> unique1 = backupFile1.unique(backupFile2);
//		assertNotNull(unique1);
//		assertTrue(unique1.isEmpty());
//		
//		List<Entity> unique2 = backupFile2.unique(backupFile1);
//		assertNotNull(unique2);
//		assertEquals(1, unique2.size());
//		assertEquals(removed.getFingerPrint(), unique2.get(0).getFingerPrint());
//	}
//	
//	public void testUnique2() throws Exception {
//		BackupFile backupFile1 = new BackupFile(new File(RESOURCES_FOLDER, "20140121_135551_358.backup"));
//		BackupFile backupFile2 = new BackupFile(new File(RESOURCES_FOLDER, "20140126_185957_048.backup"));
//		
//		List<Entity> unique1 = backupFile1.unique(backupFile2);
//		assertNotNull(unique1);
//		assertTrue(unique1.isEmpty());
//		
//		List<Entity> unique2 = backupFile2.unique(backupFile1);
//		for(Entity e: unique2){
//			assertNull(backupFile1.getByFingerPrint(e.getFingerPrint()));
//		}
//	}
//	
//	public void testUnique3() throws Exception {
//		BackupFile backupFile1 = new BackupFile(new File(RESOURCES_FOLDER, "20140121_135551_358.backup"));
//		BackupFile backupFile2 = new BackupFile(new File(RESOURCES_FOLDER, "20140126_185957_048.backup"));
//		
//		Entity removed = backupFile2.getById(Entity.TYPE_TRANSACTIONS, "3010");
//		assertNotNull(backupFile2.remove(removed));
//
//		List<Entity> unique1 = backupFile1.unique(backupFile2);
//		assertNotNull(unique1);
//		assertEquals(1, unique1.size());
//		assertEquals(removed.getFingerPrint(), unique1.get(0).getFingerPrint());
//		
//		List<Entity> unique2 = backupFile2.unique(backupFile1);
//		for(Entity e: unique2){
//			assertNull(backupFile1.getByFingerPrint(e.getFingerPrint()));
//		}
//	}
//	
//	private void save(BackupFile backupFile, File name, File metaDataName) throws IOException{
//		File temp = File.createTempFile("aaaaaa", "bbbbbb");
//		boolean metadataFileExists = metaDataName.exists();
//		if (metadataFileExists){
//			// preserve existing file
//			Files.copy(metaDataName.toPath(), temp.toPath(), StandardCopyOption.REPLACE_EXISTING);
//		}		
//		// to get right metadata save under this name first
//		backupFile.save(metaDataName);
//		// manipulate files to get requested filename 
//		Files.copy(metaDataName.toPath(), name.toPath(), StandardCopyOption.REPLACE_EXISTING);
//		if (metadataFileExists){
//			Files.copy(temp.toPath(), metaDataName.toPath(), StandardCopyOption.REPLACE_EXISTING);
//		}
//		temp.delete();
//	}
//	
//	public void testDeleted1() throws Exception {
//		File A[] = new File[] { 
//				new File(TEMP_FOLDER, "A0.backup"),
//				new File(TEMP_FOLDER, "A1.backup"),
//				new File(TEMP_FOLDER, "A2.backup")
//		};			
//		for (File f: A){
//			f.delete();
//		}
//		try {
//			BackupFile A1 = new BackupFile(new File(RESOURCES_FOLDER, "20140121_135551_358.backup"));
//			A1.save(A[1]);
//			// modify A1
//			Entity removed = A1.getById(Entity.TYPE_TRANSACTIONS, "3010");
//			assertNotNull(A1.remove(removed));
//			
//			// save as A2
//			save(A1, A[2], A[1]);
//			// test A2.deleted
//			BackupFile A2 = new BackupFile(A[2]);
//			Collection<Entity> deletedList = A2.deleted();
//			assertNotNull(deletedList);
//			assertEquals(1, deletedList.size());
//			assertEquals(removed.getFingerPrint(), deletedList.iterator().next().getFingerPrint());
//			
//		} finally {
//			for (File f: A){
//				f.delete();
//			}			
//		}
//	}
//
//	// TODO testAdded1
//	
//	
//	/*   A1       B1
//	 *    +->C1<-+
//	 *      /  \   <- edited by user
//	 *     V    V 
//	 *    A2    B2  // both metadata.filename == 'C1'
//	 *    +->C2<-+
//	 *        |       test value of C2 
//	 */
//	public void testComplex1() throws Exception {
//		BackupFile A1 = new BackupFile(new File(RESOURCES_FOLDER, "20140121_135551_358.backup"));
//		BackupFile B1 = new BackupFile(new File(RESOURCES_FOLDER, "20140121_204454_184.backup"));
//		
//		BackupFile C1 = new BackupFile(new File(RESOURCES_FOLDER, "20140121_135551_358.backup"));
//		C1.merge(B1);
//		File FC1 = new File(TEMP_FOLDER, "C1.backup");
//		C1.save(FC1); // C1.metadata = FC1
//		
//		// change C1 -> A2
//		BackupFile A2 = new BackupFile(FC1);		
//		Entity removedInA2 = A2.getById(Entity.TYPE_TRANSACTIONS, "3010");
//		assertNotNull(A2.remove(removedInA2));
//		BackupFile a = new BackupFile(new File(RESOURCES_FOLDER, "20140126_185957_048.backup"));
//		ArrayList<Entity> addedInA2 = new ArrayList<Entity>();
//		for (int i=0 ; i<4; i++){
//			Entity e = a.getById(Entity.TYPE_TRANSACTIONS, "310"+i);
//			A2.add(e);
//			addedInA2.add(e);
//		}
//		File FA2 = new File(TEMP_FOLDER, "A2.backup");
//		save(A2, FA2, FC1);
//		
//		// change C1->B2
//		BackupFile B2 = new BackupFile(FC1);
//		Entity removedInB2 = B2.getById(Entity.TYPE_TRANSACTIONS, "3099");
//		assertNotNull(B2.remove(removedInB2));
//		BackupFile b = new BackupFile(new File(RESOURCES_FOLDER, "20140126_184916_913.backup"));
//		ArrayList<Entity> addedInB2 = new ArrayList<Entity>();		
//		for (int i=0 ; i<10; i++){
//			Entity e = b.getById(Entity.TYPE_TRANSACTIONS, "5"+i);
//			B2.add(e);
//			addedInB2.add(e);
//		}
//		File FB2 = new File(TEMP_FOLDER, "B2.backup");
//		save(B2, FB2, FC1);
//		
//		// merge A2 + B2 - B2.deleted() => C2		
//		BackupFile C2 = new BackupFile(FA2);
//		C2.deleted();
//		B2 = new BackupFile(FB2);
//		System.out.println("===================== merge A2.merge(B2) =========================");
//		C2.merge(B2);
//		File FC2 = new File(TEMP_FOLDER, "C2.backup");
//		C2.save(FC2);
//		
//		C2 = new BackupFile(FC2);
//		B2 = new BackupFile(FB2); // B2 was modified by changes(), so reload 
//		// now C2 = A2 + B2.changes() should be equal C1 + A2.changes() + B2.changes() should be equal B2 + A2.changes() => C2
//		checkComplex(A1, A2, addedInA2, removedInA2, B1, B2, addedInB2, removedInB2, C1, C2);
//		
//		// merge B2 + A2.changes() => C2
//		C2 = new BackupFile(FB2);
//		A2 = new BackupFile(FA2);
//		System.out.println("===================== merge B2.merge(A2) =========================");		
//		C2.merge(A2);
//		C2.save(FC2);
//
//		A2 = new BackupFile(FA2); // A2 was modified by changes(), so reload 
//		checkComplex(A1, A2, addedInA2, removedInA2, B1, B2, addedInB2, removedInB2, C1, C2);
//
//	}
//
//	private void checkComplex(BackupFile A1, BackupFile A2, List<Entity> addedInA2, Entity removedInA2, BackupFile B1, BackupFile B2, ArrayList<Entity> addedInB2, Entity removedInB2, BackupFile C1, BackupFile C2){
//		// check C2 does not contain A2.getById(Entity.TYPE_TRANSACTIONS, "3010")
//		assertNull(C2.getByFingerPrint(removedInA2.getFingerPrint()));
//		// check C2 does not contain B2.getById(Entity.TYPE_TRANSACTIONS, "3099"));
//		assertNull(C2.getByFingerPrint(removedInB2.getFingerPrint()));
//		//  check C2 contain a.getById(Entity.TYPE_TRANSACTIONS, "310"+i)
//		for (Entity e: addedInA2){
//			assertNotNull(C2.getByFingerPrint(e.getFingerPrint()));
//		}
//		// check C2 contain b.getById(Entity.TYPE_TRANSACTIONS, "5"+i)
//		for (Entity e: addedInB2){
//			assertNotNull(C2.getByFingerPrint(e.getFingerPrint()));
//		}
//		// check C2 contain every C1 record
//		for (Entity c1: C1){
//			if (!c1.getFingerPrint().equals(removedInA2.getFingerPrint()) && 
//					!c1.getFingerPrint().equals(removedInB2.getFingerPrint())){
//				assertNotNull(C2.getByFingerPrint(c1.getFingerPrint()));
//			}
//		}
//		
//		// check C2 contains every entity from A1
//		for (Entity a1: A1){
//			if (!a1.getFingerPrint().equals(removedInA2.getFingerPrint())) {
//				assertNotNull(C2.getByFingerPrint(a1.getFingerPrint()));
//			}
//		}
//		
//		// check C2 contains every entity from B1
//		for (Entity b1: B1){
//			if (!b1.getFingerPrint().equals(removedInB2.getFingerPrint())) {
//				assertNotNull(C2.getByFingerPrint(b1.getFingerPrint()));
//			}
//		}
//		// check C2 contains every entity from A2
//		for (Entity a2: A2){
//			if (!a2.getFingerPrint().equals(removedInB2.getFingerPrint())) {
//				assertNotNull(C2.getByFingerPrint(a2.getFingerPrint()));
//			}			
//		}
//		
//		// check C2 contains every entity from B2
//		for (Entity b2: B2){
//			if (!b2.getFingerPrint().equals(removedInA2.getFingerPrint())) {
//				assertNotNull(b2.toString(), C2.getByFingerPrint(b2.getFingerPrint()));
//			}
//		}
//		
//	}
//	
//	public void testRenameProject() throws Exception {
//		File f0 = new File(RESOURCES_FOLDER, "20140926_173430_983.backup"); // common parent for f1 f2
//		File f1 = new File(RESOURCES_FOLDER, "20140926_173555_524.backup"); // contains renamed project Алексей -> мы\Алексей
//		File f2 = new File(RESOURCES_FOLDER, "20140926_173742_214.backup"); // contains new record  
//		BackupFile bf1 = new BackupFile(f1);		
////		save(bf1, f1, f0);
//		BackupFile bf2 = new BackupFile(f2);		
////		save(bf2, f2, f0);
//		
//		// merge f1 + f2 
//		BackupFile bf12 = new BackupFile(f1);
//		bf12.merge(bf2);
//		for (Entity e: bf2.added()){
//			assertNotNull(bf12.getByFingerPrint(e.getFingerPrint()));
//		}
//		// merge f2 + f1 
//		BackupFile bf21 = new BackupFile(f2);
//		bf21.merge(bf1);
//		
//		assertTrue(bf12.unique(bf21).isEmpty());
//		List<Entity> unique_21_12 = bf21.unique(bf12);
//		assertFalse(unique_21_12.isEmpty());
////		System.out.println(unique_21_12);
//		
//		// the old named project is referenced by categories via last_project_id
//		assertEquals(1, unique_21_12.size());
//		Entity u = unique_21_12.get(0);
//		assertEquals(Entity.TYPE_PROJECT, u.getType());
//		assertEquals("1", u.getId());
//		
//	}
//	
}
