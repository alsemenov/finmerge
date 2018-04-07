/*
  ~ Copyright (c) 2014-2015 Aleksei Semenov (aka xibodoh)
  ~ All rights reserved. This program and the accompanying materials
  ~ are made available under the terms of the GNU Public License v2.0
  ~ which accompanies this distribution, and is available at
  ~ http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
*/
package ru.xibodoh.finmerge.abilitycash;

import static ru.xibodoh.finmerge.Entity.TYPE_ACCOUNT;
import static ru.xibodoh.finmerge.Entity.TYPE_AMOUNT_DOUBLE;
import static ru.xibodoh.finmerge.Entity.TYPE_CURRENCY;
import static ru.xibodoh.finmerge.Entity.TYPE_CURRENCY_EXCHANGE_RATE;
import static ru.xibodoh.finmerge.Entity.TYPE_DATETIME_TEXT;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Constructor;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.xml.sax.SAXException;

import ru.xibodoh.finmerge.AbstractEntityManager;
import ru.xibodoh.finmerge.AbstractTreeEntity;
import ru.xibodoh.finmerge.Entity;
import ru.xibodoh.finmerge.EntityManager;
import ru.xibodoh.finmerge.MetaData;
import ru.xibodoh.finmerge.financisto.BackupFile;

public class AbilityCashXMLFile extends AbstractEntityManager implements EntityManager {

	private final static Logger logger = Logger.getLogger(AbilityCashXMLFile.class.getName());

//	private static final String METADATA_FILE_NAME_CODE = "FinMergeFileName";
//	private static final String METADATA_PARENTS_CODE = "FinMergeParents";
	
	private static final String DEFAULT_CONFIG_FILE = "abilitycash.properties";
	
	private static final Map<String, String[]> FP_DEFINITIONS = new HashMap<String, String[]>();
	private static final Map<String, String> REF_TYPES = new HashMap<String, String>();
	static {
		FP_DEFINITIONS.put(TYPE_CURRENCY, new String[]{"code"});
		
		FP_DEFINITIONS.put(TYPE_CURRENCY_EXCHANGE_RATE, new String[]{"date","currency-1","currency-2","amount-1","amount-2"});
		REF_TYPES.put("currency-1", TYPE_CURRENCY);
		REF_TYPES.put("currency-2", TYPE_CURRENCY);
		
		FP_DEFINITIONS.put(TYPE_ACCOUNT, new String[]{"name", "currency"});
		REF_TYPES.put("currency", TYPE_CURRENCY);
	
		REF_TYPES.put("expense-account", TYPE_ACCOUNT);
		REF_TYPES.put("income-account", TYPE_ACCOUNT);
		REF_TYPES.put("expense-amount", TYPE_AMOUNT_DOUBLE);
		REF_TYPES.put("income-amount", TYPE_AMOUNT_DOUBLE);
		REF_TYPES.put("date", TYPE_DATETIME_TEXT);
	}
	
	private final static String[] TRANSACTION_CONST_FP = {"date", "expense-account", "expense-amount", "income-account", "income-amount", "comment"};
	private final static String[] BF_TRANSACTION_CONST_FP = {"datetime", "from_account_id", "from_amount", "to_account_id", "to_amount", "note"};
	private final static String[] BACKUP_FILE_CLASSIFIERS = {"category", "project", "payee"};
	
	private static final SimpleDateFormat longDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss:SSS");

	
	private class MetaDataImpl implements MetaData {
		private String fileName = null;
		private String[] parents = null;
		
		
		@Override
		public String getFileName() {
			return fileName;
		}

		@Override
		public String[] getParents() {
			return parents;
		}

		public void setFileName(String fileName) {
			this.fileName = fileName;
		}

		public void setParents(String[] parents) {
			this.parents = parents;
		}

		@Override
		public Object get(String key) {
			return null;
		}

		@Override
		public Iterator<String> keys() {
			return Collections.<String>emptyIterator();
		}

		@Override
		public int getVersion() {
			return 0;
		}
	}
	
	private File file;
	private Entity metadataEntity;
	private Map<String, String[]> fpDefinitions;
	private Map<String, String> refTypes;
	private Account rootAccount;
	private LinkedHashMap<String, Classifier> rootClassifiers;
	private Properties config, reverseConfig;
	private boolean prettyOutput = false;
	private String classifierTreeTypeHint = null;

	private MetaDataImpl metaData;
	
	public AbilityCashXMLFile(File file) throws ParserConfigurationException, SAXException, IOException {
		this.file = file;
		fpDefinitions = new HashMap<>(FP_DEFINITIONS);
		refTypes = new HashMap<>(REF_TYPES);
		metaData = new MetaDataImpl();
		rootClassifiers = new LinkedHashMap<String, Classifier>();
		load(file);
	}
	
	@Override
	public String getFileName() {
		return file.getName();
	}

	private File getConfigFile(File file, boolean forSaving){
		// convert file.xml => file.properties
		String name = file.getName();
		int dotIndex = name.indexOf('.');
		if (dotIndex>=0){
			name = name.substring(0, dotIndex);
		}
		name = name + ".properties";
		File config = new File(file.getParentFile(), name);
		if (forSaving){
			return config;
		}
		if (config.exists() && config.isFile() && config.canRead()){
			return config;
		}
		// try default config in the same folder
		config = new File(file.getParentFile(), DEFAULT_CONFIG_FILE);
		if (config.exists() && config.isFile() && config.canRead()){
			return config;
		}
		// TODO try config file passed via system properties
		// try default config in the current folder
		config = new File(DEFAULT_CONFIG_FILE);
		if (config.exists() && config.isFile() && config.canRead()){
			return config;
		}
		// no luck 
		return null;		
	}
	
	private void loadConfig(File file) {
		config = new Properties();
		reverseConfig = new Properties();
		if (file!=null){
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
				try {
					config.load(reader);
					for (Map.Entry<Object, Object> me: config.entrySet()){
						reverseConfig.put(me.getValue(), me.getKey());
					}
				} finally {
					reader.close();
				}
			} catch (IOException e) {
				logger.log(Level.WARNING, "Failed to read AbilityCash config file "+file.getAbsolutePath()+" ", e);
			}
		}
	}
	
	private void saveConfig(File file) {
		
		try {
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
			try {
				config.store(writer, "");
			} finally {
				writer.close();
			}
		} catch (IOException e) {
			logger.log(Level.WARNING, "Failed to write AbilityCash config file "+file.getAbsolutePath()+" ", e);
		}
	}
	
	private void load(File file) throws ParserConfigurationException, SAXException, IOException {
		loadConfig(getConfigFile(file, false));
		SAXParserFactory parserFactory = SAXParserFactory.newInstance();
		SAXParser parser = parserFactory.newSAXParser();
		parser.parse(file, new EntityParser(this));
		
		if (fpDefinitions.get(Entity.TYPE_TRANSACTIONS)==null) {
			createTransactionsFPDefinitions();
		}

		for (Entity entity: idMap.values()){
			updateReferenceCounters(entity, 1);
		}
	}

	void loaded(Entity entity){
		String type = entity.getType();
		if (Entity.TYPE_FILE_METADATA.equals(type)){
			metadataEntity = entity;
			return;
		}
		// TODO read metadata
//		if (Entity.TYPE_CURRENCY.equals(type) && METADATA_FILE_NAME_CODE.equals(entity.getValue("code"))){
//			this.metaData.setFileName(entity.get("name"));
//			return;
//		}
//		if (Entity.TYPE_CURRENCY.equals(type) && METADATA_PARENTS_CODE.equals(entity.getValue("code"))){
//			String value = entity.get("name");
//			if (value!=null){
//				this.metaData.setParents(value.split(";"));
//			} else {
//				this.metaData.setParents(null);
//			}
//			return;
//		}
		if (fpDefinitions.get(Entity.TYPE_TRANSACTIONS)==null && Entity.TYPE_TRANSACTIONS.equals(entity.getType())){
			createTransactionsFPDefinitions();		
		}

		String fingerPrint = entity.getFingerPrint();
		// handle duplicate finger prints first
		// because entity id is based on finger print
		if (fingerPrintMap.containsKey(fingerPrint)){
			if (Entity.TYPE_TRANSACTIONS.equals(type)){
				String date = entity.get("date");
				char[] c = new char[3];
				int len = date.length();
				date.getChars(len-3, len, c, 0);
				String part1 = date.substring(0,len-3);
				while (fingerPrintMap.get(fingerPrint)!=null){
					c[2]++;
					if (c[2]>='9'){
						c[2] = '0';
						c[1]++;
						if (c[1]>=9){
							c[1] = '0';
							c[0]++;
							if (c[0]>'9'){
								throw new ArithmeticException("too many fingerprint duplicates");
							}
						}
					}					
					entity.set("date",  part1 + new String(c));
					fingerPrint = entity.getFingerPrint();
				}
				logger.log(Level.INFO, "adjusted date for transaction {0} to keep fingerPrint unique", fingerPrint);
			} else {
				logger.severe("FingerPrint collision: "+entity+" "+fingerPrintMap.get(fingerPrint));
			}
		}
				
		if (entity instanceof Classifier){
			String name = entity.get("singular-name");
			if (name!=null){
				type = reverseConfig.getProperty(name, Classifier.TYPE_CLASSIFIER);
				// assign type to whole tree
				Iterator<AbstractTreeEntity> it = ((Classifier) entity).tree();
				while (it.hasNext()){
					// root classifier is loaded last
					// changing type changes finger print and id of already registered entities					
					AbstractTreeEntity c = it.next();

					idMap.remove(c.getType()+c.getId());
					fingerPrintMap.remove(c.getFingerPrint());

					c.setType(type);

					idMap.put(type+c.getId(), c);
					fingerPrintMap.put(c.getFingerPrint(), c);
				}
				fingerPrint = entity.getFingerPrint();
				refTypes.put(name, type);				
				rootClassifiers.put(name, (Classifier) entity);
			}
		} else if ((entity instanceof Account) && "account-plan".equals(((Account) entity).getTag())){
			rootAccount = (Account) entity;
		}

		String id = entity.getId();
		idMap.put(type+id, entity);
		fingerPrintMap.put(fingerPrint, entity);
		
		// 
//		if (Entity.TYPE_ACCOUNT.equals(entity.getType()) || Entity.TYPE_TRANSACTIONS.equals(entity.getType())){
//		if (Classifier.TYPE_CLASSIFIER.equals(entity.getType())){
//		if (entity instanceof Account) {
//			System.out.println(entity);
//		}
	}

	private void createTransactionsFPDefinitions() {
		// explicitly create root classifiers for not configured Financisto types
		for (String financistoType: BACKUP_FILE_CLASSIFIERS){
			registerFinancistoType(financistoType);
		}		
		ArrayList<String> list = new ArrayList<String>();
		Collections.addAll(list, TRANSACTION_CONST_FP);
		list.addAll(rootClassifiers.keySet());
		String[] sa = new String[list.size()];
		list.toArray(sa);
		fpDefinitions.put(Entity.TYPE_TRANSACTIONS, sa);
	}

	public void save(File file) throws IOException, XMLStreamException, FactoryConfigurationError {
		this.file = file;
		metaData.setFileName(file.getName());
		// TODO write finmerge metadata
		XMLStreamWriter xmlWriter = XMLOutputFactory.newFactory().createXMLStreamWriter(new FileOutputStream(file), "UTF-8");
		if (prettyOutput) {
			// use reflection to instantiate the class, because it is Oracle/Sun internal
			try {
				Class<?> clazz = Class
						.forName("com.sun.xml.internal.txw2.output.IndentingXMLStreamWriter");
				Constructor<?> ctor = clazz
						.getConstructor(XMLStreamWriter.class);
				xmlWriter = (XMLStreamWriter) ctor.newInstance(xmlWriter);
			} catch (Exception e) {
				// just ignore
			}
		}
		xmlWriter.writeStartDocument();
		xmlWriter.writeStartElement("ability-cash");
		writeExportOptions(xmlWriter);
		List<Entity> currencies = new ArrayList<Entity>();
		List<Entity> rates = new ArrayList<Entity>();
		List<Entity> accounts = new ArrayList<Entity>();
		for (Entity entity: this){
			if (Entity.TYPE_CURRENCY.equals(entity.getType())){
				currencies.add(entity);
			} else if (Entity.TYPE_CURRENCY_EXCHANGE_RATE.equals(entity.getType())){
				rates.add(entity);
			} else if (Entity.TYPE_ACCOUNT.equals(entity.getType())){
				accounts.add(entity);
			}
		}
		writeCurrencies(xmlWriter, currencies);
		writeRates(xmlWriter, rates);
		writeAccounts(xmlWriter, accounts);
		if (rootAccount!=null){
			writeAccountPlans(xmlWriter, rootAccount);
		}
		writeClassifiers(xmlWriter, rootClassifiers.values());
		writeTransactions(xmlWriter);
		xmlWriter.writeEndElement(); // ability-cash
		xmlWriter.flush();
		xmlWriter.close();
		
		saveConfig(getConfigFile(file, true));
	}
	
	private void writeTransactions(XMLStreamWriter xml) throws XMLStreamException {
		xml.writeStartElement("transactions");
		for (Entity entity: this){
			if (Entity.TYPE_TRANSACTIONS.equals(entity.getType())){
				writeTransaction(xml, entity);
			}
		}
		xml.writeEndElement();
		xml.flush();
	}

	private void writeTransaction(XMLStreamWriter xml, Entity entity) throws XMLStreamException {
		xml.writeStartElement("transaction");
		writeKeyValue(xml, entity, "date");
		xml.writeStartElement(entity.get("cashflow"));
		Iterator<String> it = entity.keys();
		while (it.hasNext()){
			String key = it.next();
			if (!"date".equals(key) && !"comment".equals(key) && !"cashflow".equals(key)){
				Object value = entity.getValue(key);
				if ("expense-account".equals(key) || "income-account".equals(key)){
					writeAccountReference(xml, key, (Entity) value);
				} else if (value instanceof Classifier){
					writeClassifierReference(xml, (Classifier)value);
				} else {
					writeKeyValue(xml, entity, key);
				}
			}
		}
		
		xml.writeEndElement(); // cashflow
		if (entity.get("comment")!=null){
			writeKeyValue(xml, entity, "comment");
		}
		xml.writeEndElement(); // transaction
	}

	private void writeClassifierReference(XMLStreamWriter xml, Classifier classifier) throws XMLStreamException {
		AbstractTreeEntity[] path = classifier.getPath();
		for (int i=1; i<path.length; i++){
			xml.writeStartElement("category");
			if (i==1){
				xml.writeAttribute("classifier", path[0].get("singular-name"));
			}
			writeKeyValue(xml, path[i], "name");
		}
		for (int i=path.length; i>1; i--){
			xml.writeEndElement();
		}
	}

	private void writeClassifiers(XMLStreamWriter xml, Collection<Classifier> classifiers) throws XMLStreamException {
		xml.writeStartElement("classifiers");
		for (Classifier classifier: classifiers){
			writeClassifier(xml, "classifier", classifier);
		}
		xml.writeEndElement();
		xml.flush();
	}

	private void writeClassifier(XMLStreamWriter xml, String tag, Classifier classifier) throws XMLStreamException {
		xml.writeStartElement(tag);
		Iterator<String> it = classifier.keys();
		while (it.hasNext()){
			writeKeyValue(xml, classifier, it.next());
		}
		for (AbstractTreeEntity child: classifier){
			if (classifier.getParent()==null){
				xml.writeStartElement(((Classifier) child).getTreeType());
			}
			writeClassifier(xml, "category", (Classifier) child);
			if (classifier.getParent()==null){
				xml.writeEndElement();
			}
		}
		xml.writeEndElement();
	}



	private void writeAccountPlans(XMLStreamWriter xml, Account rootAccount) throws XMLStreamException {
		xml.writeStartElement("account-plans");
		writeAccountPlan(xml, rootAccount);	
		xml.writeEndElement();
		xml.flush();
	}

	private void writeAccountPlan(XMLStreamWriter xml, Account accountFolder) throws XMLStreamException {
		xml.writeStartElement(accountFolder.getTag());
		writeKeyValue(xml, accountFolder, "name");
		for (Entity entity: accountFolder){
			if (!Entity.TYPE_ACCOUNT.equals(entity.getType())) {
				writeAccountPlan(xml, (Account) entity);
			} else {
				writeAccountReference(xml, "account", entity);
			}
		}
		xml.writeEndElement();
	}

	private void writeAccountReference(XMLStreamWriter xml, String tag, Entity account) throws XMLStreamException{
		xml.writeStartElement(tag);
		writeKeyValue(xml, account, "name");
		writeKeyValue(xml, account, "currency");
		xml.writeEndElement();		
	}

	private void writeAccounts(XMLStreamWriter xml, List<Entity> accounts) throws XMLStreamException {
		xml.writeStartElement("accounts");
		for (Entity entity: accounts){
			writeEntity(xml, "account", entity);
		}		
		xml.writeEndElement();
		xml.flush();
	}

	private void writeRates(XMLStreamWriter xml, List<Entity> rates) throws XMLStreamException {
		xml.writeStartElement("rates");
		for (Entity entity: rates){
			writeEntity(xml, "rate", entity);
		}		
		xml.writeEndElement();
		xml.flush();
	}

	private void writeCurrencies(XMLStreamWriter xml,	List<Entity> currencies) throws XMLStreamException {
		xml.writeStartElement("currencies");
		for (Entity entity: currencies){
			writeEntity(xml, "currency", entity);
		}
		writeMetaData(xml, metaData);
		xml.writeEndElement();
		xml.flush();
	}

	private void writeMetaData(XMLStreamWriter xml, MetaData metaData) throws XMLStreamException {
		// TODO writeMetaData
	}

	private void writeExportOptions(XMLStreamWriter xml) throws XMLStreamException{
		writeEntity(xml, "export-options", metadataEntity);
		xml.flush();
	}
	
	private void writeEntity(XMLStreamWriter xml, String tag, Entity entity) throws XMLStreamException{
		xml.writeStartElement(tag);
		Iterator<String> it = entity.keys();
		while (it.hasNext()){
			writeKeyValue(xml, entity, it.next());
		}
		xml.writeEndElement();
	}
	
	private void writeKeyValue(XMLStreamWriter xml, Entity entity, String key) throws XMLStreamException{
		String value = entity.get(key);
		// TODO handle references
		if (value==null || "".equals(value)){
			xml.writeEmptyElement(key);
		} else {
			xml.writeStartElement(key);
			xml.writeCharacters(value);
			xml.writeEndElement(); // key
		}
	}
	
//	@Override
//	public String add(Entity entity) {
//		// TODO Auto-generated method stub
//		return null;
//	}

//	@Override
//	public Entity remove(Entity entity) {
//		// TODO Auto-generated method stub
//		return null;
//	}

	@Override
	public Map<String, String> getReferenceTypes() {
		return refTypes;
	}

	@Override
	public Map<String, String[]> getFingerPrintDefinitions() {
		return fpDefinitions;
	}

	@Override
	protected String getFingerPrint(Entity entity){
		Class<? extends EntityManager> thatEMClass = entity.getEntityManager().getClass();
		if (thatEMClass.equals(this.getClass())){
			return entity.getFingerPrint();
		} else if (BackupFile.class.equals(thatEMClass)){
			String type = entity.getType();
			String classifierName = config.getProperty(type);
			
			if (Entity.TYPE_CURRENCY.equals(type) || Entity.TYPE_ACCOUNT.equals(type)){
				return entity.getFingerPrint(); // TODO handle changes 							
			} else if (Entity.TYPE_CATEGORY.equals(type) || Entity.TYPE_PAYEE.equals(type) || Entity.TYPE_PROJECT.equals(type)){
				// for classifiers finger print is the following:
				// type\classifierName\tree\path
				// each root classifier contains either one child (for single-tree) or two (for expense/income trees) children.  
				Classifier classifier = null, defaultChild = null;
				if (classifierName!=null && rootClassifiers.containsKey(classifierName)) {
					// Financisto do not use expense/income flag for category, agent, project, etc, but AbilityCash use it
					// So for given Financisto entity there is no way to conclude its expense/income flag
					// Let's try to find matching entity in AbilityCash and use its flag
					// if there are more than one entity use first and warn
					// if there are no matching entities warn and assume that it is single tree (?)
					classifier = rootClassifiers.get(classifierName);
					defaultChild = (Classifier) classifier.getChild(classifierTreeTypeHint);
					List<AbstractTreeEntity> found = defaultChild.find("name", entity.get("title"));
					if (found.size()>0){
						int level = -1;
						for (Entity e = entity; e!=null && level<3; e=e.getParent()){
							level++;
						}
						// level is 0 for root or not category, 1 for default child 2 for direct children of default child						
						for (Entity e: found){
							switch (level){
							case 0:
								return e.getFingerPrint();
							case 1:
								if (e==defaultChild){
									return defaultChild.getFingerPrint();
								}
								break;
							case 2:
								if (e.getParent()==defaultChild) {
									return e.getFingerPrint();
								}
								break;
							default:
								if (e.getParent().get("name").equals(entity.getParent().getValue("title"))){
									return e.getFingerPrint();								
								}
							}
						}						
					}
				}
				StringBuilder result = new StringBuilder(type);
				result.append('\\').append(classifierName!=null?classifierName:type);
				result.append('\\').append(defaultChild!=null?defaultChild.get("name"):type);
				// TODO  add expense/income flag into finger print
				logger.finer("Assuming "+(defaultChild!=null?defaultChild.getTreeType():Classifier.TREE_TYPE_SINGLE)+" for Financisto entity "+entity);
				if (Entity.TYPE_CATEGORY.equals(type)){
					int index = result.length();
					// do not use root and its first child, there are artificial 
					for (Entity e=entity; e!=null && e.getParent()!=null && e.getParent()!=null; e=e.getParent()){
						result.insert(index, e.get("title"));
						result.insert(index, '\\');
					}
				} else { // payee and project are flat in Financisto
					assert entity.getParent()==null;
					result.append('\\').append(entity.get("title"));
				}
				return result.toString();
			} else if (Entity.TYPE_TRANSACTIONS.equals(type)){
				// order of fields: date, expense-account, expense-amount, income-account, income-amount, comment, classifiers			
				StringBuilder result = new StringBuilder(type);
				result.append(((Date)entity.getValue("datetime")).getTime());
				long fromAmount = (Long) entity.getValue("from_amount");
				Entity toAccount = (Entity) entity.getValue("to_account_id");
				Entity fromAccount = (Entity) entity.getValue("from_account_id");
				if (toAccount!=null){ // transfer
					result.append('{').append(getFingerPrint(fromAccount)).append('}');
					result.append(fromAmount);
					result.append('{').append(getFingerPrint(toAccount)).append('}');
					result.append(entity.getValue("to_amount"));
				} else if (fromAmount>=0){ // income
					result.append((Object)null).append(0L);
					result.append('{').append(getFingerPrint(fromAccount)).append('}');
					result.append(fromAmount);
					classifierTreeTypeHint = Classifier.TREE_TYPE_INCOME;
				} else { // expense
					result.append('{').append(getFingerPrint(fromAccount)).append('}');
					result.append(fromAmount);
					result.append((Object)null).append(0L);
				}
				result.append(entity.get("note"));
				// classifiers
				for (String key: rootClassifiers.keySet()){
					String k = reverseConfig.getProperty(key);
					Object value = null;
					if (k!=null && toAccount==null){ // no classifiers for transfer
						value = entity.getValue(k+"_id");
					}
					if (value instanceof Entity){
						result.append('{').append(getFingerPrint((Entity) value)).append('}');
					} else {
						result.append(value);
					}		
				}
				classifierTreeTypeHint = null;
				return result.toString();
			}
			logger.fine("Financisto entity "+entity+" can not be mapped to AbilityCash, skipping");
		} else {
			logger.warning("Entities managed by "+thatEMClass+" are not supported");
		}
		return null;
	}
	
	private static String[] getPath(Entity entity, String key){
		Deque<String> deque = new ArrayDeque<>();
		for (Entity e=entity; e!=null; e=e.getParent()){
			deque.addFirst(e.get(key));
		}
		String[] result = new String[deque.size()];
		deque.toArray(result);
		return result;
	}
	
	private Classifier registerFinancistoType(String type) {
		String classifierName = config.getProperty(type);
		Classifier root = rootClassifiers.get(classifierName);
		if (root==null){
			// create root classifier and its default child
			root = new Classifier(this, type, Classifier.TREE_TYPE_ROOT);
			root.set("singular-name", type);
			root.set("plural-name", type);
			config.put(type, type);
			reverseConfig.put(type, type);
			rootClassifiers.put(type, root);
			idMap.put(root.getType()+root.getId(), root);
			fingerPrintMap.put(root.getFingerPrint(), root);
			refTypes.put(type, type);
			
			Classifier c = new Classifier(this, type, Classifier.TREE_TYPE_SINGLE);
			c.set("name", type);
			root.addChild(c);
			idMap.put(c.getType()+c.getId(), c);
			fingerPrintMap.put(c.getFingerPrint(), c);
		}								
		return root;
	}
	
	@Override
	protected Entity createClone(Entity entity) {
		Class<? extends EntityManager> thatEMClass = entity.getEntityManager().getClass();
		if (thatEMClass.equals(this.getClass())){			
			Entity clone;
			// TODO think about abstract method newInstance in AbstractEntity  
			if (entity instanceof Account){
				clone = new Account(this, ((Account) entity).getTag());
			} else if (entity instanceof Classifier){
				clone = new Classifier(this, ((Classifier) entity).getTreeType());
			} else {
				clone = new EntityImpl(this, entity.getType());
			}

			if (entity instanceof AbstractTreeEntity && !(entity instanceof Account && rootAccount==null)){
				Entity parent = ((AbstractTreeEntity) entity).getParent();
				if (parent!=null){
					((AbstractTreeEntity) clone).setParent((AbstractTreeEntity) getById(parent.getType(), add(parent)));
				}
			}

			Iterator<String> it = entity.keys();
			while (it.hasNext()){
				String key = it.next();
				Object value = entity.getValue(key);
				if (value instanceof Entity){
					value = add((Entity) value);
				}
				clone.set(key, (String) entity.get(key));
			}
			return clone;
			
		} else if (BackupFile.class.equals(thatEMClass)){
			String type = entity.getType();
			Entity clone = null;
			if (Entity.TYPE_CURRENCY.equals(type)){
				clone = new EntityImpl(this, type);
				clone.set("code", entity.get("name"));
				clone.set("name", entity.get("title"));
				clone.set("precision", entity.get("decimals"));
			} else if (Entity.TYPE_ACCOUNT.equals(type)){
				clone = new Account(this);
				if (rootAccount!=null){
					((AbstractTreeEntity) clone).setParent(rootAccount);
				}
				clone.set("name", entity.get("title"));
				
				clone.set("currency", add((Entity) entity.getValue("currency_id"))); //((Entity) entity.getValue("currency_id")).get("name"));
				clone.set("init-balance", "0");				
			} else if (Entity.TYPE_CATEGORY.equals(type) || Entity.TYPE_PAYEE.equals(type) || Entity.TYPE_PROJECT.equals(type)){							
				Classifier root = rootClassifiers.get(config.getProperty(type)); // all financisto classifiers should be registered already
				Classifier rootDefaultChild = (Classifier) root.getChild(classifierTreeTypeHint);
				
				clone = new Classifier(this, type, rootDefaultChild.getTreeType());				
				clone.set("name", entity.get("title"));
				int level = -1;
				for (Entity e = entity; e!=null && level<3; e=e.getParent()){
					level++;
				}
				AbstractTreeEntity localParent;
				if (level<2){
					localParent = rootDefaultChild;
				} else {
					localParent = (AbstractTreeEntity) getById(clone.getType(), add(entity.getParent()));
				}
				((AbstractTreeEntity) clone).setParent(localParent);

			} else if (Entity.TYPE_TRANSACTIONS.equals(type)){
				clone = new EntityImpl(this, type);
				// accounts, date, comment
				clone.set("date", longDateFormat.format((Date)entity.getValue("datetime")));
				String note = entity.get("note");
				if (note!=null){
					clone.set("comment", note);
				}
				Entity fromAccount = (Entity) entity.getValue("from_account_id");
				String cashFlow = null;
				if (fromAccount!=null){
					long fromAmount = (Long)entity.getValue("from_amount");
					if (fromAmount>=0){
						cashFlow = "income";
					} else {
						cashFlow = "expense";
					}
					clone.set(cashFlow+"-account", add(fromAccount));
					clone.set(cashFlow+"-amount", convertFinancistoAmount(entity.get("from_amount")));
				}
				Entity toAccount = (Entity) entity.getValue("to_account_id");
				if (toAccount!=null){
					assert "expense".equals(cashFlow);
					cashFlow = "transfer";
					clone.set("income-account", add(toAccount));
					clone.set("income-amount", convertFinancistoAmount(entity.get("to_amount")));					
				}
				// classifiers
				if ("income".equals(cashFlow)){
					classifierTreeTypeHint = Classifier.TREE_TYPE_INCOME;
				}
				
				for (String bfType: BACKUP_FILE_CLASSIFIERS){
					// AbilityCash is may not use some classifiers in transfers
					// which is specified by user in classifiers configuration
					// so just do not use classifiers for imported transfers
					if (!"transfer".equals(cashFlow)){
						Entity value = (Entity) entity.getValue(bfType+"_id");
						if (value!=null){
							clone.set(config.getProperty(bfType), add(value));
						}
					}
				}
				classifierTreeTypeHint = null;
				clone.set("cashflow", cashFlow);
				clone.set("executed", "");
			}
			
			return clone;
		} else {
			logger.severe("converting entities from "+entity.getEntityManager().getClass().getName()+" to "+this.getClass().getName()+" is not supported");
		}
		return null;
	}

	private String convertFinancistoAmount(String a){
		String s = a,m = "";
		if (a.charAt(0)=='-'){
			m = "-";
			s = a.substring(1);
		}
		int length = s.length();
		if (length<2) {
			s = "0" + s;
			length++;
		}
		s = s.substring(0, length-2) + '.' + s.substring(length-2)+"00";
		if (s.charAt(0)=='.'){
			s = "0" + s;
		}
		return m+s;
	}
	
	@Override
	protected void added(Entity entity) {
		if (entity instanceof AbstractTreeEntity){
			Entity parent = ((AbstractTreeEntity) entity).getParent();
			if (parent!=null){
				((AbstractTreeEntity) parent).addChild((AbstractTreeEntity) entity);
				// TODO think about adding accounts and accounts folders in different cases
			} else if (Entity.TYPE_ACCOUNT.equals(entity.getType()) && rootAccount!=null){
				rootAccount.addChild((AbstractTreeEntity) entity);
			}
		}
			
	}

	@Override
	public MetaData getMetaData() {
		return metaData;
	}
	
	public Properties getConfig() {
		return new Properties(config); // protect from modifications
	}

	public boolean isPrettyOutput() {
		return prettyOutput;
	}

	public void setPrettyOutput(boolean prettyOutput) {
		this.prettyOutput = prettyOutput;
	}
	
}
