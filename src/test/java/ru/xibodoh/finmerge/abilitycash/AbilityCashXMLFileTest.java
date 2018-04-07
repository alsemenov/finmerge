/*
  ~ Copyright (c) 2014-2015 Aleksei Semenov (aka xibodoh)
  ~ All rights reserved. This program and the accompanying materials
  ~ are made available under the terms of the GNU Public License v2.0
  ~ which accompanies this distribution, and is available at
  ~ http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
*/
package ru.xibodoh.finmerge.abilitycash;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

public class AbilityCashXMLFileTest extends TestCase {

	private static final String RESOURCES_FOLDER = "src/test/resources/";

	private String TEMP_FOLDER = null;
	static {
//		Log.configure("FINE");
	}
	
	protected void setUp() throws Exception {
		super.setUp();
		if (TEMP_FOLDER==null){
			try {
				TEMP_FOLDER = File.createTempFile("cccccc", "ddddd").getParent();
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
/*
	public void testAbilityCashXMLFile() throws Exception {
		AbilityCashXMLFile abilityCashXMLFile = new AbilityCashXMLFile(new File(RESOURCES_FOLDER, "2011nn.xml"));
		
		Entity rur = abilityCashXMLFile.getById(Entity.TYPE_CURRENCY, "RUR");
		assertNotNull(rur);
		assertEquals("рубли", rur.get("name"));
		assertEquals("2", rur.get("precision"));
		assertSame(rur, abilityCashXMLFile.getByFingerPrint(rur.getFingerPrint()));
		
		assertNotNull(abilityCashXMLFile.getById(Entity.TYPE_CURRENCY, "USD"));
		assertNotNull(abilityCashXMLFile.getById(Entity.TYPE_CURRENCY, "EUR"));
	
		Entity coshelekR = abilityCashXMLFile.getById(Entity.TYPE_ACCOUNT, "кошелекЯ{currencyRUR}");
		assertNotNull(coshelekR);
		assertEquals("RUR", coshelekR.get("currency"));
		assertEquals("кошелекЯ", coshelekR.get("name"));
		assertEquals("4715.6600", coshelekR.get("init-balance"));
		assertSame(coshelekR, abilityCashXMLFile.getByFingerPrint(coshelekR.getFingerPrint()));
		
		Entity categoryRoot = abilityCashXMLFile.getById(Classifier.TYPE_CLASSIFIER, "\\статья");
		assertNotNull(categoryRoot);
		assertEquals("статьи", categoryRoot.get("plural-name"));
		assertSame(categoryRoot, abilityCashXMLFile.getByFingerPrint(categoryRoot.getFingerPrint()));
		Entity salary = abilityCashXMLFile.getById(Classifier.TYPE_CLASSIFIER, "\\статья\\Все статьи прихода\\зарплата");
		assertNotNull(salary);
		assertSame(salary, abilityCashXMLFile.getByFingerPrint(salary.getFingerPrint()));
		
		Entity milk = abilityCashXMLFile.getById(Classifier.TYPE_CLASSIFIER, "\\статья\\Все статьи расхода\\еда\\молочное\\молоко");
		assertNotNull(milk);
		assertSame(milk, abilityCashXMLFile.getByFingerPrint(milk.getFingerPrint()));		
	}

	public void testAbilityCashXMLFile_Ref() throws Exception {
		AbilityCashXMLFile abilityCashXMLFile = new AbilityCashXMLFile(new File(RESOURCES_FOLDER, "2011nn.xml"));
		
		for (Entity entity: abilityCashXMLFile){
//			if (Entity.TYPE_TRANSACTIONS.equals(entity.getType())){
//				System.out.println(entity);
//				System.out.println(entity.getFingerPrint());
//			}
			Iterator<String> keys = entity.keys();
			while(keys.hasNext()){
				String key = keys.next();
				Object value = entity.getValue(key);
				if (value instanceof Entity){
					assertEquals(((Entity) value).getId(), entity.get(key));
				}
			}
		}
	}
	
	
	public void testAbilityCashXMLFileSave() throws Exception{
		AbilityCashXMLFile abilityCashXMLFile1 = new AbilityCashXMLFile(new File(RESOURCES_FOLDER, "2011nn.xml"));
		File file = File.createTempFile("aaaaa", ".xml"); // new File("test.xml"); //
		abilityCashXMLFile1.save(file);
		AbilityCashXMLFile abilityCashXMLFile2 = new AbilityCashXMLFile(file);
		assertEquals(abilityCashXMLFile1, abilityCashXMLFile2);
//		for (Entity e1: abilityCashXMLFile1){
//			assertNotNull(abilityCashXMLFile2.getByFingerPrint(e1.getFingerPrint()));
//		}
//		for (Entity e2: abilityCashXMLFile2){
//			assertNotNull(abilityCashXMLFile1.getByFingerPrint(e2.getFingerPrint()));
//		}
		file.delete();
	}
	
	public void testAbilityCashXMLFileAdd_sameEM() throws Exception {
		AbilityCashXMLFile abilityCashXMLFile1 = new AbilityCashXMLFile(new File(RESOURCES_FOLDER, "empty.xml"));
		EntityImpl euro = new EntityImpl(abilityCashXMLFile1, Entity.TYPE_CURRENCY);
		euro.set("name", "Euro");
		euro.set("code", "EUR");
		euro.set("precision", "2");
		abilityCashXMLFile1.add(euro);
		
		Account euroAccount = new Account(abilityCashXMLFile1);
		euroAccount.set("name", "Наличные EUR");
		euroAccount.set("currency", "EUR");
		euroAccount.set("init-balance", "0.0000");
		abilityCashXMLFile1.add(euroAccount);
		
		AbilityCashXMLFile abilityCashXMLFile2 = new AbilityCashXMLFile(new File(RESOURCES_FOLDER, "euro.xml"));
		
		Iterator<Entity> it1 = abilityCashXMLFile1.iterator();
		Iterator<Entity> it2 = abilityCashXMLFile2.iterator();
		while (it1.hasNext() && it2.hasNext()){
			Entity e1 = it1.next();
			Entity e2 = it2.next();
			assertEquals(e1, abilityCashXMLFile2.getByFingerPrint(e1.getFingerPrint()));
			assertEquals(e2, abilityCashXMLFile1.getByFingerPrint(e2.getFingerPrint()));
			
		}
		assertFalse(it1.hasNext());
		assertFalse(it2.hasNext());
	}

	public void testAbilityCashXMLFileAdd_otherEM() throws Exception {
		AbilityCashXMLFile abilityCashXMLFile1 = new AbilityCashXMLFile(new File(RESOURCES_FOLDER, "empty.xml"));
		AbilityCashXMLFile abilityCashXMLFile2 = new AbilityCashXMLFile(new File(RESOURCES_FOLDER, "euro.xml"));

		Entity euro = abilityCashXMLFile2.getById(Entity.TYPE_CURRENCY, "EUR"); 
		abilityCashXMLFile1.add(euro);
		
		
		Entity euroAccount = abilityCashXMLFile2.getById(Entity.TYPE_ACCOUNT, "Наличные EUR{currencyEUR}" );
		abilityCashXMLFile1.add(euroAccount);
		// TODO use this scheme for implementing AbstractEntityManager.equals()	
		Iterator<Entity> it1 = abilityCashXMLFile1.iterator();
		Iterator<Entity> it2 = abilityCashXMLFile2.iterator();
		while (it1.hasNext() && it2.hasNext()){
			Entity e1 = it1.next();
			Entity e2 = it2.next();
			assertEquals(e1, abilityCashXMLFile2.getByFingerPrint(e1.getFingerPrint()));
			assertEquals(e2, abilityCashXMLFile1.getByFingerPrint(e2.getFingerPrint()));
			
		}
		assertFalse(it1.hasNext());
		assertFalse(it2.hasNext());
	}

	public void testAbilityCashXMLFileAdd_relatedEntity() throws Exception {
		AbilityCashXMLFile abilityCashXMLFile1 = new AbilityCashXMLFile(new File(RESOURCES_FOLDER, "empty.xml"));
		AbilityCashXMLFile abilityCashXMLFile2 = new AbilityCashXMLFile(new File(RESOURCES_FOLDER, "euro.xml"));

		Entity euro = abilityCashXMLFile2.getById(Entity.TYPE_CURRENCY, "EUR"); 
		
		Entity euroAccount = abilityCashXMLFile2.getById(Entity.TYPE_ACCOUNT, "Наличные EUR{currencyEUR}" );
		abilityCashXMLFile1.add(euroAccount);
		// euro entity should be added along with account
		assertNotNull(abilityCashXMLFile1.getByFingerPrint(euro.getFingerPrint()));
		Iterator<Entity> it1 = abilityCashXMLFile1.iterator();
		Iterator<Entity> it2 = abilityCashXMLFile2.iterator();
		while (it1.hasNext() && it2.hasNext()){
			Entity e1 = it1.next();
			Entity e2 = it2.next();
			assertEquals(e1, abilityCashXMLFile2.getByFingerPrint(e1.getFingerPrint()));
			assertEquals(e2, abilityCashXMLFile1.getByFingerPrint(e2.getFingerPrint()));
			
		}
		assertFalse(it1.hasNext());
		assertFalse(it2.hasNext());
	}
	
	public void testAbilityCashXMLFileAdd_sameEntity() throws Exception {
		AbilityCashXMLFile abilityCashXMLFile1 = new AbilityCashXMLFile(new File(RESOURCES_FOLDER, "empty.xml"));
		AbilityCashXMLFile abilityCashXMLFile2 = new AbilityCashXMLFile(new File(RESOURCES_FOLDER, "euro.xml"));

//		Entity euro = abilityCashXMLFile2.getById(Entity.TYPE_CURRENCY, "EUR"); 
		
		Entity euroAccount = abilityCashXMLFile2.getById(Entity.TYPE_ACCOUNT, "Наличные EUR{currencyEUR}" );
		abilityCashXMLFile1.add(euroAccount);
		abilityCashXMLFile1.add(euroAccount);
		// there should be only one euroAccount and euro currency
		Iterator<Entity> it1 = abilityCashXMLFile1.iterator();
		Iterator<Entity> it2 = abilityCashXMLFile2.iterator();
		while (it1.hasNext() && it2.hasNext()){
			Entity e1 = it1.next();
			Entity e2 = it2.next();
			assertEquals(e1, abilityCashXMLFile2.getByFingerPrint(e1.getFingerPrint()));
			assertEquals(e2, abilityCashXMLFile1.getByFingerPrint(e2.getFingerPrint()));
			
		}
		assertFalse(it1.hasNext());
		assertFalse(it2.hasNext());
	}
	
	public void testAbilityCashXMLFileAdd_accountFNF()throws Exception {
		AbilityCashXMLFile abilityCashXMLFile1 = new AbilityCashXMLFile(new File(RESOURCES_FOLDER, "empty.xml"));
		AbilityCashXMLFile abilityCashXMLFile2 = new AbilityCashXMLFile(new File(RESOURCES_FOLDER, "2011nn.xml"));

		// coshelekR has parent account folder, while abilityCashXMLFile1 do not use account folders
		Entity coshelekR = abilityCashXMLFile2.getById(Entity.TYPE_ACCOUNT, "кошелекЯ{currencyRUR}");
		abilityCashXMLFile1.add(coshelekR);
		// expected result: only account is added
		Account a = (Account) abilityCashXMLFile1.getById(Entity.TYPE_ACCOUNT, "кошелекЯ{currencyRUR}");
		assertNull(a.getParent());
	}
	
	public void testAbilityCashXMLFileAdd_accountNFF()throws Exception {
		AbilityCashXMLFile abilityCashXMLFile1 = new AbilityCashXMLFile(new File(RESOURCES_FOLDER, "2011nn.xml"));
		AbilityCashXMLFile abilityCashXMLFile2 = new AbilityCashXMLFile(new File(RESOURCES_FOLDER, "euro.xml"));

		// a2 has no parent account folder, while abilityCashXMLFile1 use account folders
		Entity a2 = abilityCashXMLFile2.getById(Entity.TYPE_ACCOUNT, "Наличные EUR{currencyEUR}" );
		abilityCashXMLFile1.add(a2);
		// expected result: account is added into root folder
		Account a1 = (Account) abilityCashXMLFile1.getById(Entity.TYPE_ACCOUNT, "Наличные EUR{currencyEUR}");
		assertNotNull(a1.getParent());
		Field rootAccountField = abilityCashXMLFile1.getClass().getDeclaredField("rootAccount");
		rootAccountField.setAccessible(true);
		Account parent = (Account) rootAccountField.get(abilityCashXMLFile1);
		assertEquals(parent, a1.getParent());		
		assertEquals(a1, parent.getChild("name", "Наличные EUR"));
	}
	
	public void testAbilityCashXMLFileAdd_accountNFNF()throws Exception {
		AbilityCashXMLFile abilityCashXMLFile1 = new AbilityCashXMLFile(new File(RESOURCES_FOLDER, "2011nn.xml"));
//		AbilityCashXMLFile abilityCashXMLFile2 = new AbilityCashXMLFile(new File(RESOURCES_FOLDER, "euro.xml"));

		// a2 is assigned some parent account folder, while abilityCashXMLFile1 use account folders
		Account a2 = new Account(abilityCashXMLFile1);
		a2.set("name", "Наличные EUR");
		a2.set("currency", "EUR");
		a2.set("init-balance", "0.0000");
		AbstractTreeEntity parent = (AbstractTreeEntity) ((Account) abilityCashXMLFile1.getById(Entity.TYPE_ACCOUNT, "кошелекЯ{currencyRUR}")).getParent();
		a2.setParent(parent);
		abilityCashXMLFile1.add(a2);
		// expected result: account is added into specified folder
		Account a1 = (Account) abilityCashXMLFile1.getById(Entity.TYPE_ACCOUNT, "Наличные EUR{currencyEUR}");
		assertNotNull(a1.getParent());
		assertEquals(parent, a1.getParent());
		assertEquals(a1, parent.getChild("name", "Наличные EUR"));
	}
	
	public void testAbilityCashXMLFileRemove_noRefs() throws Exception {
		AbilityCashXMLFile abilityCashXMLFile1 = new AbilityCashXMLFile(new File(RESOURCES_FOLDER, "euro.xml"));
		Entity a1 = abilityCashXMLFile1.getById(Entity.TYPE_ACCOUNT, "Наличные EUR{currencyEUR}");
		Entity removed = abilityCashXMLFile1.remove(a1);
		assertNotNull(removed);
		assertSame(removed, a1);
		// referenced currency is not deleted 
		assertNotNull(abilityCashXMLFile1.getById(Entity.TYPE_CURRENCY, "EUR"));				
	}
	
	public void testAbilityCashXMLFileRemove_noRefsAnotherEM() throws Exception {
		AbilityCashXMLFile abilityCashXMLFile1 = new AbilityCashXMLFile(new File(RESOURCES_FOLDER, "euro.xml"));
		AbilityCashXMLFile abilityCashXMLFile2 = new AbilityCashXMLFile(new File(RESOURCES_FOLDER, "euro.xml"));

		Entity a1 = abilityCashXMLFile2.getById(Entity.TYPE_ACCOUNT, "Наличные EUR{currencyEUR}");
		Entity removed = abilityCashXMLFile1.remove(a1);
		assertNotNull(removed);
		assertNotSame(removed, a1);
		assertEquals(removed, a1);
		// referenced currency is not deleted 
		assertNotNull(abilityCashXMLFile1.getById(Entity.TYPE_CURRENCY, "EUR"));				
	}
	
	public void testAbilityCashXMLFileRemove_withRefs() throws Exception {
		AbilityCashXMLFile abilityCashXMLFile1 = new AbilityCashXMLFile(new File(RESOURCES_FOLDER, "euro.xml"));
		Entity c1 = abilityCashXMLFile1.getById(Entity.TYPE_CURRENCY, "EUR");
		Entity removed = abilityCashXMLFile1.remove(c1);
		assertNull(removed);
		
		AbilityCashXMLFile abilityCashXMLFile2 = new AbilityCashXMLFile(new File(RESOURCES_FOLDER, "euro.xml"));
		assertEquals(abilityCashXMLFile2, abilityCashXMLFile1);
	}
	
	public void testAbilityCashXMLFileRemove_withRefsAnotherEM() throws Exception {
		AbilityCashXMLFile abilityCashXMLFile1 = new AbilityCashXMLFile(new File(RESOURCES_FOLDER, "euro.xml"));
		AbilityCashXMLFile abilityCashXMLFile2 = new AbilityCashXMLFile(new File(RESOURCES_FOLDER, "euro.xml"));
		Entity c1 = abilityCashXMLFile2.getById(Entity.TYPE_CURRENCY, "EUR");
		Entity removed = abilityCashXMLFile1.remove(c1);
		assertNull(removed);
		
		assertEquals(abilityCashXMLFile2, abilityCashXMLFile1);
	}
	
	public void testAbilityCashXMLFileUnique()throws Exception {
		AbilityCashXMLFile abilityCashXMLFile1 = new AbilityCashXMLFile(new File(RESOURCES_FOLDER, "empty.xml"));
		AbilityCashXMLFile abilityCashXMLFile2 = new AbilityCashXMLFile(new File(RESOURCES_FOLDER, "euro.xml"));

		List<Entity> unique1 = abilityCashXMLFile1.unique(abilityCashXMLFile2);
		assertNotNull(unique1);
		assertTrue(unique1.isEmpty());
		
		List<Entity> unique2 = abilityCashXMLFile2.unique(abilityCashXMLFile1);
		assertNotNull(unique2);
		assertEquals(2, unique2.size());
		Entity e0 = unique2.get(0);
		Entity e1 = unique2.get(1);
		if (e0.get("code")==null){
			Entity e2 = e0;
			e0 = e1;
			e1 = e2;
		}
		assertEquals("EUR", e0.get("code"));
		assertEquals("Наличные EUR", e1.get("name"));		
	}
	
	public void testAbilityCashXMLFileCommon()throws Exception {
		AbilityCashXMLFile abilityCashXMLFile1 = new AbilityCashXMLFile(new File(RESOURCES_FOLDER, "empty.xml"));
		AbilityCashXMLFile abilityCashXMLFile2 = new AbilityCashXMLFile(new File(RESOURCES_FOLDER, "euro.xml"));

		List<Entity> common1 = abilityCashXMLFile1.common(abilityCashXMLFile2);
		assertNotNull(common1);
		assertTrue(!common1.isEmpty());
		
		List<Entity> common2 = abilityCashXMLFile2.common(abilityCashXMLFile1);
		assertNotNull(common2);
		assertEquals(common1.size(), common2.size());
		
		Comparator<Entity> entityComparator = new Comparator<Entity>() {			
			@Override
			public int compare(Entity o1, Entity o2) {
				return o1.getFingerPrint().compareTo(o2.getFingerPrint());
			}
		};
		Collections.sort(common1, entityComparator);
		Collections.sort(common2, entityComparator);
		
		assertEquals(common1, common2);
	}
	
	public void testAbilityCashXMLFile_DefaultClassifierType() throws Exception {
		AbilityCashXMLFile abilityCashXMLFile1 = new AbilityCashXMLFile(new File(RESOURCES_FOLDER, "empty.xml"));
		Entity categoryRoot = abilityCashXMLFile1.getById(Classifier.TYPE_CLASSIFIER, "\\Статья");
		assertNotNull(categoryRoot);
		ArrayList<AbstractTreeEntity> stack = new ArrayList<>();
		stack.add((AbstractTreeEntity) categoryRoot);
		while (!stack.isEmpty()){
			AbstractTreeEntity c = stack.remove(stack.size()-1);
			assertEquals(Classifier.TYPE_CLASSIFIER, c.getType());
			for (AbstractTreeEntity e: c){
				stack.add(e);
			}
		}
		
	}
	
	public void testAbilityCashXMLFile_ClassifierType() throws Exception {
		AbilityCashXMLFile abilityCashXMLFile1 = new AbilityCashXMLFile(new File(RESOURCES_FOLDER, "empty1.xml"));
		Entity categoryRoot = abilityCashXMLFile1.getById(Classifier.TYPE_CATEGORY, "\\Статья");
		assertNotNull(categoryRoot);
		ArrayList<AbstractTreeEntity> stack = new ArrayList<>();
		stack.add((AbstractTreeEntity) categoryRoot);
		while (!stack.isEmpty()){
			AbstractTreeEntity c = stack.remove(stack.size()-1);
			assertEquals(Entity.TYPE_CATEGORY, c.getType());
			for (AbstractTreeEntity e: c){
				stack.add(e);
			}
		}	
	}

	public void testAbilityCashXMLFile_AddFinancistoCurrency() throws Exception {
		AbilityCashXMLFile abilityCashXMLFile1 = new AbilityCashXMLFile(new File(RESOURCES_FOLDER, "empty1.xml"));
		BackupFile backupFile1 = new BackupFile(new File(RESOURCES_FOLDER,"20140126_185957_048.backup"));
		
		Entity euro = backupFile1.getById(Entity.TYPE_CURRENCY, "3"); // Euro
		
		String id = abilityCashXMLFile1.add(euro);
		assertNotNull(id);
		Entity c1 = abilityCashXMLFile1.getById(Entity.TYPE_CURRENCY, id);
		assertNotNull(c1);
		assertEquals(id, c1.getId());
		assertNotNull(c1.getFingerPrint());
		assertEquals(euro.get("name"), c1.get("code"));
		assertEquals(euro.get("title"), c1.get("name"));
		assertEquals(euro.get("decimals"), c1.get("precision"));
	}
	
	public void testAbilityCashXMLFile_AddFinancistoExistingCurrency() throws Exception {
		AbilityCashXMLFile abilityCashXMLFile1 = new AbilityCashXMLFile(new File(RESOURCES_FOLDER, "euro.xml"));
		BackupFile backupFile1 = new BackupFile(new File(RESOURCES_FOLDER,"20140126_185957_048.backup"));
		Entity feuro = backupFile1.getById(Entity.TYPE_CURRENCY, "3"); // Euro
		Entity aeuro = abilityCashXMLFile1.getById(Entity.TYPE_CURRENCY, "EUR");
		
		String id = abilityCashXMLFile1.add(feuro);
		assertNotNull(id);
		Entity c1 = abilityCashXMLFile1.getById(Entity.TYPE_CURRENCY, id);
		assertNotNull(c1);
		assertEquals(id, c1.getId());
		assertNotNull(c1.getFingerPrint());
		
		assertSame(aeuro, c1);
		
	}
	
	public void testAbilityCashXMLFile_AddFinancistoAccount() throws Exception {
		AbilityCashXMLFile abilityCashXMLFile1 = new AbilityCashXMLFile(new File(RESOURCES_FOLDER, "empty1.xml"));
		BackupFile backupFile1 = new BackupFile(new File(RESOURCES_FOLDER,"20140126_185957_048.backup"));
	
		Entity a = backupFile1.getById(Entity.TYPE_ACCOUNT, "5"); // Кошелек Euro
		String id = abilityCashXMLFile1.add(a);
		assertNotNull(id);
		Entity c1 = abilityCashXMLFile1.getById(Entity.TYPE_ACCOUNT, id);
		assertNotNull(c1);
		assertEquals(id, c1.getId());
		assertNotNull(c1.getFingerPrint());
		
		assertEquals(a.get("title"), c1.get("name"));
		assertEquals("0", c1.get("init-balance"));
		assertEquals(((Entity) a.getValue("currency_id")).get("name"), c1.get("currency") );
		
		// check that euro currency is added implicitly 
		Entity euro = backupFile1.getById(Entity.TYPE_CURRENCY, "3"); // Euro
		Entity c2 = (Entity) c1.getValue("currency");
		assertNotNull(c2);
		assertSame(abilityCashXMLFile1, c2.getEntityManager());
		assertNotNull(c2.getFingerPrint());
		assertEquals(euro.get("name"), c2.get("code"));
		assertEquals(euro.get("title"), c2.get("name"));
		assertEquals(euro.get("decimals"), c2.get("precision"));		
		
	}
	
	
	public void testAbilityCashXMLFile_AddFinancistoExistingAccount() throws Exception {
		AbilityCashXMLFile abilityCashXMLFile1 = new AbilityCashXMLFile(new File(RESOURCES_FOLDER, "2011nn.xml"));
		BackupFile backupFile1 = new BackupFile(new File(RESOURCES_FOLDER,"20140126_185957_048.backup"));
	
		Entity ba = backupFile1.getById(Entity.TYPE_ACCOUNT, "4"); // Кошелек $
		Entity fa = abilityCashXMLFile1.getById(Entity.TYPE_ACCOUNT, "кошелек ${currencyUSD}");
		Entity busd = (Entity) ba.getValue("currency_id");
		Entity fusd = (Entity) fa.getValue("currency");

		assertNotNull(fa);
		
		String id = abilityCashXMLFile1.add(ba);
		assertNotNull(id);
		Entity e1 = abilityCashXMLFile1.getById(Entity.TYPE_ACCOUNT, id);
		assertNotNull(e1);
		assertEquals(id, e1.getId());
		assertNotNull(e1.getFingerPrint());
		
		assertSame(fa, e1);
		
		assertEquals(ba.get("title"), e1.get("name"));
		assertEquals("600.0000", e1.get("init-balance"));
		assertEquals(((Entity) ba.getValue("currency_id")).get("name"), e1.get("currency") );
	
//		// check that USD currency is not added implicitly 
		Entity e2 = (Entity) e1.getValue("currency");
		assertNotNull(e2);
		assertSame(abilityCashXMLFile1, e2.getEntityManager());
		
		assertSame(fusd, e2);		
		assertNotNull(e2.getFingerPrint());
		assertEquals(busd.get("name"), e2.get("code"));
	}

	public void testAbilityCashXMLFile_AddFinancistoCategory() throws Exception {
		AbilityCashXMLFile abilityCashXMLFile1 = new AbilityCashXMLFile(new File(RESOURCES_FOLDER, "empty1.xml"));
		BackupFile backupFile1 = new BackupFile(new File(RESOURCES_FOLDER,"20140126_185957_048.backup"));
		
		Entity bc = backupFile1.getById(TYPE_CATEGORY, "2"); // \еда\обед на работе
		String fingerPrint = abilityCashXMLFile1.getFingerPrint(bc);
		
		String id = abilityCashXMLFile1.add(bc);
		Entity e1 = checkAddedEntity(abilityCashXMLFile1, TYPE_CATEGORY, id);
		assertEquals(fingerPrint, e1.getFingerPrint());
		assertEquals(bc.get("title"), e1.get("name"));
		assertEquals(Classifier.TREE_TYPE_EXPENSE, ((Classifier) e1).getTreeType());
		
		// parents should be added implicitly
		Entity e2 = e1.getParent();
		assertNotNull(e2);
		checkAddedEntity(abilityCashXMLFile1, TYPE_CATEGORY, e2.getId());
		assertEquals(bc.getParent().get("title"), e2.get("name"));
		assertEquals(Classifier.TREE_TYPE_EXPENSE, ((Classifier) e2).getTreeType());
//		System.out.println("e2: "+e2);
		
		Entity e3 = e2.getParent();
		assertNotNull(e3);
		//assertEquals(bc.getParent().getParent().get("title"), e3.get("name"));
//		System.out.println("e3: "+e3);
	}
	
	
	public void testAbilityCashXMLFile_AddFinancistoCategory2() throws Exception {
		AbilityCashXMLFile abilityCashXMLFile1 = new AbilityCashXMLFile(new File(RESOURCES_FOLDER, "empty1.xml"));
		BackupFile backupFile1 = new BackupFile(new File(RESOURCES_FOLDER,"20140126_185957_048.backup"));
		
		Entity bc1 = backupFile1.getById(TYPE_CATEGORY, "131"); // налог
		Entity bc2 = backupFile1.getById(TYPE_CATEGORY, "127"); // налог на машину
		
		String fingerPrint1 = abilityCashXMLFile1.getFingerPrint(bc1);
		String fingerPrint2 = abilityCashXMLFile1.getFingerPrint(bc2);
		assertFalse(fingerPrint1.equals(fingerPrint2));
				
	
		String id1 = abilityCashXMLFile1.add(bc1);
		Entity e1 = checkAddedEntity(abilityCashXMLFile1, TYPE_CATEGORY, id1);
		assertEquals(fingerPrint1, e1.getFingerPrint());
		
		String id2 = abilityCashXMLFile1.add(bc2);
		Entity e2 = checkAddedEntity(abilityCashXMLFile1, TYPE_CATEGORY, id2);
		assertEquals(fingerPrint2, e2.getFingerPrint());
		assertFalse(e1.getFingerPrint().equals(e2.getFingerPrint()));
		
//		assertEquals(bc.get("title"), e1.get("name"));
//		assertEquals(Classifier.TREE_TYPE_EXPENSE, ((Classifier) e1).getTreeType());
//		
//		// parents should be added implicitly
//		Entity e2 = e1.getParent();
//		assertNotNull(e2);
//		checkAddedEntity(abilityCashXMLFile1, TYPE_CATEGORY, e2.getId());
//		assertEquals(bc.getParent().get("title"), e2.get("name"));
//		assertEquals(Classifier.TREE_TYPE_EXPENSE, ((Classifier) e2).getTreeType());
////		System.out.println("e2: "+e2);
//		
//		Entity e3 = e2.getParent();
//		assertNotNull(e3);
		//assertEquals(bc.getParent().getParent().get("title"), e3.get("name"));
//		System.out.println("e3: "+e3);
	}
	
	public void testAbilityCashXMLFile_AddFinancistoRootCategory() throws Exception {
		AbilityCashXMLFile abilityCashXMLFile1 = new AbilityCashXMLFile(new File(RESOURCES_FOLDER, "empty1.xml"));
		BackupFile backupFile1 = new BackupFile(new File(RESOURCES_FOLDER,"20140126_185957_048.backup"));
		
		Entity bc = backupFile1.getById(TYPE_CATEGORY, "0"); // \Без категории
		String fingerPrint = abilityCashXMLFile1.getFingerPrint(bc);

		String id = abilityCashXMLFile1.add(bc);
		
		Entity e1 = checkAddedEntity(abilityCashXMLFile1, TYPE_CATEGORY, id);
		assertEquals(fingerPrint, e1.getFingerPrint());
		assertEquals("Все статьи расхода", e1.get("name"));
	}
	
	public void testAbilityCashXMLFile_AddFinancistoExistingCategory() throws Exception {
		AbilityCashXMLFile abilityCashXMLFile1 = new AbilityCashXMLFile(new File(RESOURCES_FOLDER, "2011np.xml"));
		BackupFile backupFile1 = new BackupFile(new File(RESOURCES_FOLDER,"20140126_185957_048.backup"));

		Entity bc = backupFile1.getById(Entity.TYPE_CATEGORY, "2"); // \еда\обед на работе
		Entity categoryRoot = abilityCashXMLFile1.getById(Classifier.TYPE_CATEGORY, "\\статья");
		Entity ac = ((Classifier) categoryRoot).find("name", "обед на работе").get(0);
		assertNotNull(ac);
//		assertNotNull(abilityCashXMLFile1.getByFingerPrint(ac.getFingerPrint()));
		
		String id = abilityCashXMLFile1.add(bc);
		assertNotNull(id);
		Entity e1 = abilityCashXMLFile1.getById(Entity.TYPE_CATEGORY, id);		
		assertNotNull(e1);
		assertSame(ac, e1);
		assertEquals(id, e1.getId());
		assertNotNull(e1.getFingerPrint());	
		assertEquals(bc.get("title"), e1.get("name"));
		assertEquals(Classifier.TREE_TYPE_EXPENSE, ((Classifier) e1).getTreeType());		
	}
	
	public void testAbilityCashXMLFile_AddFinancistoPayee() throws Exception {
		AbilityCashXMLFile abilityCashXMLFile1 = new AbilityCashXMLFile(new File(RESOURCES_FOLDER, "empty1.xml"));
		BackupFile backupFile1 = new BackupFile(new File(RESOURCES_FOLDER,"20140126_185957_048.backup"));
		
		Entity bp = backupFile1.getById(Entity.TYPE_PAYEE, "3"); // рынок
		
		String id = abilityCashXMLFile1.add(bp);
		Entity e1 = checkAddedEntity(abilityCashXMLFile1, TYPE_PAYEE, id);
		
		assertEquals(bp.get("title"), e1.get("name"));
		assertEquals(Classifier.TREE_TYPE_SINGLE, ((Classifier) e1).getTreeType());
		
		Entity e2 = e1.getParent();
		assertNotNull(e2);
		assertEquals(Entity.TYPE_PAYEE, e2.getType());
		assertEquals(Entity.TYPE_PAYEE, e2.get("name"));
		assertEquals(Classifier.TREE_TYPE_SINGLE, ((Classifier) e2).getTreeType());
		
		// parent should be root
		Entity e3 = e2.getParent();
		assertNotNull(e3);
		assertNull(e3.getParent());
		assertEquals(Entity.TYPE_PAYEE, e3.get("singular-name"));
		assertEquals(Entity.TYPE_PAYEE, e3.get("plural-name"));
		assertEquals(Classifier.TREE_TYPE_ROOT, ((Classifier) e3).getTreeType());
		assertEquals(Entity.TYPE_PAYEE, e3.getType());

	}
		
	
	public void testAbilityCashXMLFile_AddFinancistoExistingPayee() throws Exception {
		AbilityCashXMLFile abilityCashXMLFile1 = new AbilityCashXMLFile(new File(RESOURCES_FOLDER, "2011np.xml"));
		BackupFile backupFile1 = new BackupFile(new File(RESOURCES_FOLDER,"20140126_185957_048.backup"));

		Entity bp = backupFile1.getById(Entity.TYPE_PAYEE, "3"); // рынок
		
		Entity payeeRoot = abilityCashXMLFile1.getById(Entity.TYPE_PAYEE, "\\агент");
		Entity ap = ((AbstractTreeEntity) payeeRoot).find("name", "рынок").get(0);
		assertNotNull(ap);
		
		String id = abilityCashXMLFile1.add(bp);
		assertNotNull(id);
		Entity e1 = abilityCashXMLFile1.getById(Entity.TYPE_PAYEE, id);
		assertNotNull(e1);
		assertEquals(id, e1.getId());
		assertNotNull(e1.getFingerPrint());	
		
		assertSame(ap, e1);		
	}

	
	public void testAbilityCashXMLFile_AddFinancistoProject() throws Exception {
		AbilityCashXMLFile abilityCashXMLFile1 = new AbilityCashXMLFile(new File(RESOURCES_FOLDER, "empty1.xml"));
		BackupFile backupFile1 = new BackupFile(new File(RESOURCES_FOLDER,"20140126_185957_048.backup"));
		
		Entity bp = backupFile1.getById(Entity.TYPE_PROJECT, "1"); // Алексей
		String fingerPrint = abilityCashXMLFile1.getFingerPrint(bp);
		
		String id = abilityCashXMLFile1.add(bp);
		Entity e1 = checkAddedEntity(abilityCashXMLFile1, TYPE_PROJECT, id);
		assertEquals(fingerPrint, e1.getFingerPrint());
		
		assertEquals(bp.get("title"), e1.get("name"));
		assertEquals(Entity.TYPE_PROJECT, e1.getType());
		assertEquals(Classifier.TREE_TYPE_SINGLE, ((Classifier) e1).getTreeType());
		
		// parent should be first child of root
		Entity e2 = e1.getParent();
		assertNotNull(e2);
		assertEquals(Entity.TYPE_PROJECT, e2.getType());
		assertEquals(Entity.TYPE_PROJECT, e2.get("name"));
		assertEquals(Classifier.TREE_TYPE_SINGLE, ((Classifier) e2).getTreeType());
		
		Entity e3 = e2.getParent();
		assertNotNull(e3);
		assertNull(e3.getParent());
		assertEquals(Entity.TYPE_PROJECT, e3.get("singular-name"));
		assertEquals(Entity.TYPE_PROJECT, e3.get("plural-name"));
		assertEquals(Classifier.TREE_TYPE_ROOT, ((Classifier) e3).getTreeType());
		assertEquals(Entity.TYPE_PROJECT, e3.getType());

	}
	
	public void testAbilityCashXMLFile_AddFinancistoExistingProject() throws Exception {
		AbilityCashXMLFile abilityCashXMLFile1 = new AbilityCashXMLFile(new File(RESOURCES_FOLDER, "2011np.xml"));
		BackupFile backupFile1 = new BackupFile(new File(RESOURCES_FOLDER,"20140126_185957_048.backup"));

		Entity bp = backupFile1.getById(Entity.TYPE_PROJECT, "2"); // Федя
		
		Entity payeeRoot = abilityCashXMLFile1.getById(Entity.TYPE_PROJECT, "\\проект");
		Entity ap = ((AbstractTreeEntity) payeeRoot).find("name", "Федя").get(0);
		assertNotNull(ap);
		
		String id = abilityCashXMLFile1.add(bp);
		Entity e1 = checkAddedEntity(abilityCashXMLFile1, TYPE_PROJECT, id);
		
		assertSame(ap, e1);		
	}
	
	public void testAbilityCashXMLFile_AddFinancistoExpenseTransaction() throws Exception {
		AbilityCashXMLFile abilityCashXMLFile1 = new AbilityCashXMLFile(new File(RESOURCES_FOLDER, "empty1.xml"));
		BackupFile backupFile1 = new BackupFile(new File(RESOURCES_FOLDER,"20140126_185957_048.backup"));
		
		Entity bt = backupFile1.getById(TYPE_TRANSACTIONS, "3010");
		
		String id = abilityCashXMLFile1.add(bt);
		Entity e1 = checkAddedEntity(abilityCashXMLFile1, TYPE_TRANSACTIONS, id);

		assertEquals("expense", e1.get("cashflow"));
		assertNull(e1.get("income-account"));
		assertNull(e1.get("income-amount"));
		assertEquals(bt.getValue("from_amount"), e1.getValue("expense-amount"));
		assertNotNull(e1.get("expense-account"));
		Entity e2 = checkAddedEntity(abilityCashXMLFile1, TYPE_ACCOUNT, e1.get("expense-account"));
		assertEquals(((Entity) bt.getValue("from_account_id")).get("title"), e2.get("name"));
		
		Entity e3 = checkAddedEntity(abilityCashXMLFile1, TYPE_CATEGORY, e1.get("Статья"));
		assertEquals(((Entity) bt.getValue("category_id")).get("title"), e3.get("name"));
		
		Entity e4 = checkAddedEntity(abilityCashXMLFile1, TYPE_PROJECT, e1.get("project")); // auto added classifier project
		assertEquals(((Entity) bt.getValue("project_id")).get("title"), e4.get("name"));
		
		Entity e5 = checkAddedEntity(abilityCashXMLFile1, TYPE_PAYEE, e1.get("payee")); // auto added classifier payee
		assertEquals(((Entity) bt.getValue("payee_id")).get("title"), e5.get("name"));
		
//		System.out.println(e1);
		
	}
	
	
	public void testAbilityCashXMLFile_AddFinancistoIncomeTransaction1() throws Exception {
		AbilityCashXMLFile abilityCashXMLFile1 = new AbilityCashXMLFile(new File(RESOURCES_FOLDER, "empty1.xml"));
		BackupFile backupFile1 = new BackupFile(new File(RESOURCES_FOLDER,"20140126_185957_048.backup"));
		
		Entity bt = backupFile1.getById(TYPE_TRANSACTIONS, "3083");
		
		String id = abilityCashXMLFile1.add(bt);
		Entity e1 = checkAddedEntity(abilityCashXMLFile1, TYPE_TRANSACTIONS, id);

		assertEquals("income", e1.get("cashflow"));
		assertNull(e1.get("expense-account"));
		assertNull(e1.get("expense-amount"));
		assertEquals(bt.getValue("from_amount"), e1.getValue("income-amount"));
		assertNotNull(e1.get("income-account"));
		Entity e2 = checkAddedEntity(abilityCashXMLFile1, TYPE_ACCOUNT, e1.get("income-account"));
		assertEquals(((Entity) bt.getValue("from_account_id")).get("title"), e2.get("name"));
		
		Entity e3 = checkAddedEntity(abilityCashXMLFile1, TYPE_CATEGORY, e1.get("Статья"));
		assertEquals(((Entity) bt.getValue("category_id")).get("title"), e3.get("name"));
		assertEquals(Classifier.TREE_TYPE_INCOME, ((Classifier) e3).getTreeType());
		
		Entity e4 = checkAddedEntity(abilityCashXMLFile1, TYPE_PROJECT, e1.get("project")); // auto added classifier project
		assertEquals(((Entity) bt.getValue("project_id")).get("title"), e4.get("name"));
		assertEquals(Classifier.TREE_TYPE_SINGLE, ((Classifier) e4).getTreeType());
		
		Entity e5 = checkAddedEntity(abilityCashXMLFile1, TYPE_PAYEE, e1.get("payee")); // auto added classifier payee
		assertEquals(((Entity) bt.getValue("payee_id")).get("title"), e5.get("name"));
		assertEquals(Classifier.TREE_TYPE_SINGLE, ((Classifier) e5).getTreeType());

		
//		System.out.println(e1);
		
	}
	
	public void testAbilityCashXMLFile_AddFinancistoIncomeTransaction2() throws Exception {
		AbilityCashXMLFile abilityCashXMLFile1 = new AbilityCashXMLFile(new File(RESOURCES_FOLDER, "empty1.xml"));
		BackupFile backupFile1 = new BackupFile(new File(RESOURCES_FOLDER,"20140126_185957_048.backup"));
		
		abilityCashXMLFile1.add(backupFile1.getById(TYPE_CATEGORY, "15")); // will be added as expense
		
		Entity bt = backupFile1.getById(TYPE_TRANSACTIONS, "3083");
		
		String id = abilityCashXMLFile1.add(bt);
		Entity e1 = checkAddedEntity(abilityCashXMLFile1, TYPE_TRANSACTIONS, id);

		assertEquals("income", e1.get("cashflow"));
		assertNull(e1.get("expense-account"));
		assertNull(e1.get("expense-amount"));
		assertEquals(bt.getValue("from_amount"), e1.getValue("income-amount"));
		assertNotNull(e1.get("income-account"));
		Entity e2 = checkAddedEntity(abilityCashXMLFile1, TYPE_ACCOUNT, e1.get("income-account"));
		assertEquals(((Entity) bt.getValue("from_account_id")).get("title"), e2.get("name"));
		
		Entity e3 = checkAddedEntity(abilityCashXMLFile1, TYPE_CATEGORY, e1.get("Статья"));
		assertEquals(((Entity) bt.getValue("category_id")).get("title"), e3.get("name"));
		assertEquals(Classifier.TREE_TYPE_INCOME, ((Classifier) e3).getTreeType());
		
		Entity e4 = checkAddedEntity(abilityCashXMLFile1, TYPE_PROJECT, e1.get("project")); // auto added classifier project
		assertEquals(((Entity) bt.getValue("project_id")).get("title"), e4.get("name"));
		assertEquals(Classifier.TREE_TYPE_SINGLE, ((Classifier) e4).getTreeType());
		
		Entity e5 = checkAddedEntity(abilityCashXMLFile1, TYPE_PAYEE, e1.get("payee")); // auto added classifier payee
		assertEquals(((Entity) bt.getValue("payee_id")).get("title"), e5.get("name"));
		assertEquals(Classifier.TREE_TYPE_SINGLE, ((Classifier) e5).getTreeType());

		
//		System.out.println(e1);
		
	}	
	
	public void testAbilityCashXMLFile_AddFinancistoTransferTransaction() throws Exception {
		AbilityCashXMLFile abilityCashXMLFile1 = new AbilityCashXMLFile(new File(RESOURCES_FOLDER, "empty1.xml"));
		BackupFile backupFile1 = new BackupFile(new File(RESOURCES_FOLDER,"20140126_185957_048.backup"));
		
		Entity bt = backupFile1.getById(TYPE_TRANSACTIONS, "3087");
		
		String id = abilityCashXMLFile1.add(bt);
		Entity e1 = checkAddedEntity(abilityCashXMLFile1, TYPE_TRANSACTIONS, id);	
		
		assertEquals("transfer", e1.get("cashflow"));

		assertEquals(bt.getValue("from_amount"), e1.getValue("expense-amount"));
		assertNotNull(e1.get("expense-account"));
		Entity e2 = checkAddedEntity(abilityCashXMLFile1, TYPE_ACCOUNT, e1.get("expense-account"));
		assertEquals(((Entity) bt.getValue("from_account_id")).get("title"), e2.get("name"));
		
		assertEquals(bt.getValue("to_amount"), e1.getValue("income-amount"));
		assertNotNull(e1.get("income-account"));
		Entity e6 = checkAddedEntity(abilityCashXMLFile1, TYPE_ACCOUNT, e1.get("income-account"));
		assertEquals(((Entity) bt.getValue("to_account_id")).get("title"), e6.get("name"));

		// no classifiers for transfer transactions
		assertNull(e1.get("Статья"));
		assertNull(e1.get("project"));
		assertNull(e1.get("payee")); 
		
//		System.out.println(e1);

	}
	
	public void testAbilityCashXMLFile_AddFinancistoEntities1() throws Exception {
		AbilityCashXMLFile abilityCashXMLFile1 = new AbilityCashXMLFile(new File(RESOURCES_FOLDER, "empty1.xml"));
		BackupFile backupFile1 = new BackupFile(new File(RESOURCES_FOLDER,"20140126_185957_048.backup"));
		
		//abilityCashXMLFile1.merge(backupFile1);
		for (Entity entity: backupFile1){
//			if (TYPE_TRANSACTIONS.equals(entity.getType())){
//				System.out.println(entity);
//			}
			String type = entity.getType();
			String fingerPrint = abilityCashXMLFile1.getFingerPrint(entity);
			String id = abilityCashXMLFile1.add(entity);
			if (id!=null){
				Entity ae = checkAddedEntity(abilityCashXMLFile1, type, id);
				assertEquals(fingerPrint, ae.getFingerPrint());
			}
		}
		
		File file = new File(RESOURCES_FOLDER, "merged1.xml");
		abilityCashXMLFile1.setPrettyOutput(true);
		abilityCashXMLFile1.save(file);
		
	}
	
	public void testAbilityCashXMLFile_MergeFinancistoFile1() throws Exception {
		AbilityCashXMLFile abilityCashXMLFile1 = new AbilityCashXMLFile(new File(RESOURCES_FOLDER, "empty1.xml"));
		BackupFile backupFile1 = new BackupFile(new File(RESOURCES_FOLDER,"20140126_185957_048.backup"));
		
		abilityCashXMLFile1.merge(backupFile1);
		
		File file = new File(RESOURCES_FOLDER, "merged2.xml");
		abilityCashXMLFile1.setPrettyOutput(true);
		abilityCashXMLFile1.save(file);
		
	}
	
	public void testAbilityCashXMLFile_MergeFinancistoFile2() throws Exception {
		AbilityCashXMLFile abilityCashXMLFile1 = new AbilityCashXMLFile(new File(RESOURCES_FOLDER, "empty1.xml"));
		BackupFile backupFile1 = new BackupFile(new File(RESOURCES_FOLDER,"20140126_185957_048.backup"));
		
		abilityCashXMLFile1.merge(backupFile1);
		
		File file = new File(TEMP_FOLDER, "testAbilityCashXMLFile_MergeFinancistoFile2.xml");
		abilityCashXMLFile1.setPrettyOutput(true);
		abilityCashXMLFile1.save(file);
		
		abilityCashXMLFile1.merge(backupFile1); // should not change the file
		AbilityCashXMLFile abilityCashXMLFile2 = new AbilityCashXMLFile(new File(TEMP_FOLDER, "testAbilityCashXMLFile_MergeFinancistoFile2.xml"));
		assertEquals(abilityCashXMLFile1, abilityCashXMLFile2);
	}
	
//	public void testAbilityCashXMLFile_MergeFinancistoFile3() throws Exception {
//		AbilityCashXMLFile abilityCashXMLFile1 = new AbilityCashXMLFile(new File(RESOURCES_FOLDER, "empty1.xml"));
//		BackupFile backupFile1 = new BackupFile(new File(RESOURCES_FOLDER,"merged_20150108_193522_088.backup"));
//		
//		abilityCashXMLFile1.merge(backupFile1);
//		
//		File file = new File(RESOURCES_FOLDER, "merged3.xml");
//		abilityCashXMLFile1.setPrettyOutput(true);
//		abilityCashXMLFile1.save(file);
//		
//	}
	
	public void testAbilityCashXMLFile_MergeAbilityCashRates() throws Exception {
		AbilityCashXMLFile abilityCashXMLFile1 = new AbilityCashXMLFile(new File(RESOURCES_FOLDER, "empty1.xml"));
		AbilityCashXMLFile abilityCashXMLFile2 = new AbilityCashXMLFile(new File(RESOURCES_FOLDER, "rate.xml"));
		abilityCashXMLFile1.merge(abilityCashXMLFile2);
		
	}
	
	public void testConvertFinancistoAmount() throws Exception {
		AbilityCashXMLFile abilityCashXMLFile1 = new AbilityCashXMLFile(new File(RESOURCES_FOLDER, "empty1.xml"));
		Method convertFinancistoAmountMethod = AbilityCashXMLFile.class.getDeclaredMethod("convertFinancistoAmount", String.class);
		convertFinancistoAmountMethod.setAccessible(true);
		assertEquals("-0.2100", convertFinancistoAmountMethod.invoke(abilityCashXMLFile1, "-21"));
		assertEquals("-0.0100", convertFinancistoAmountMethod.invoke(abilityCashXMLFile1, "-1"));
	}
	
	private Entity checkAddedEntity(EntityManager em, String type, String id){
		assertNotNull(id);
		Entity e = em.getById(type, id);
		assertNotNull(e);
		assertEquals(id, e.getId());
		assertEquals(type, e.getType());
		assertNotNull(e.getFingerPrint());	
		assertSame(e, em.getByFingerPrint(e.getFingerPrint()));
		assertSame(em, e.getEntityManager());
		
		return e;
	}
*/	
}
