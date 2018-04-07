/*
  ~ Copyright (c) 2014-2015 Aleksei Semenov (aka xibodoh)
  ~ All rights reserved. This program and the accompanying materials
  ~ are made available under the terms of the GNU Public License v2.0
  ~ which accompanies this distribution, and is available at
  ~ http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
*/
package ru.xibodoh.finmerge.financisto;

import ru.xibodoh.finmerge.Entity;
import ru.xibodoh.finmerge.EntityManager;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.zip.GZIPOutputStream;

import static ru.xibodoh.finmerge.Entity.*;

public class BackupFile implements EntityManager, Iterable<Entity>{ // TODO extends AbstractEntityManager

	private static final String FINMERGE_FILE_ATTRIBUTE = "finmerge_file";

	private final static Logger logger = Logger.getLogger(BackupFile.class.getName());
	
	private final static String PACKAGE = "ru.orangesoftware.financisto";
	
	private final static int MIN_VERSION_CODE = 82;
	private final static int MAX_VERSION_CODE = 109;
	private final static int MIN_DATABASE_VERSION = 197;
	private final static int MAX_DATABASE_VERSION = 214;

	private final static Set<String> ENTITIES_WITH_SYSTEM_VALUES = new HashSet<String>(
			Arrays.asList(Entity.TYPE_CATEGORY, Entity.TYPE_ATTRIBUTES, Entity.TYPE_PROJECT, Entity.TYPE_LOCATIONS)
	);
	public static final int VERSION_213 = 213;


	public static class MetaData implements ru.xibodoh.finmerge.MetaData {

		public static final String PACKAGE_NAME = "package name";
		public static final String VERSION_NAME = "version name";
		public static final String VERSION_CODE = "version code";
		public static final String DATABASE_VERSION = "database version";

		private String fileName;
		private String[] parents;
		private final Map<String, Object> data;
		private int version = -1;

		public MetaData() {
			data = new HashMap<>();
		}
		
		public Entity getEntity(EntityManager entityManager){
			if (fileName!=null || parents!=null) {
				EntityImpl entity = new EntityImpl(entityManager);
				entity.set("$ENTITY", Entity.TYPE_ATTRIBUTES);
				entity.set(getVersion()>=VERSION_213 ? "title" : "name", FINMERGE_FILE_ATTRIBUTE);
				entity.set("type", "3"); // == type list				
				entity.set("default_value", fileName);
				if (parents!=null){
					StringBuilder sb = new StringBuilder();
					for (String p: parents){
						sb.append(p).append(';');
					}
					sb.deleteCharAt(sb.length()-1);
					entity.set("list_values", sb.toString());
				}				
				return entity;
			}
			return null;
		}

		public String getFileName() {
			return fileName;
		}

		public void setFileName(String fileName) {
			this.fileName = fileName;
		}

		public String[] getParents() {
			return parents;
		}

		public void setParents(String[] parents) {
			this.parents = parents;
		}
		
		public Iterator<String> keys() {
			return data.keySet().iterator();
		}
		
		public Object get(String key){
			return data.get(key);
		}

		void set(String key, String value){
			data.put(key, value);
			if (DATABASE_VERSION.equals(key)){
				try {
					version = Integer.parseInt(value);
				} catch (NumberFormatException e){
					logger.warning("Invalid database version: '"+value+"'");
				}
			}
		}

		void set(String packageName, String versionName, String versionCode, String databaseVersion){
			set(PACKAGE_NAME, packageName);
			set(VERSION_NAME, versionName);
			set(VERSION_CODE, versionCode);
			set(DATABASE_VERSION, databaseVersion);
		}

		void setEntity(Entity entity){
			this.fileName = entity.get("default_value");
			String list = entity.get("list_values");
			if (list!=null){
				this.parents = list.split(";");
			}
		}

		public int getVersion(){
			return version;
		}
	}
	

	private File file;
	private String fileName;
	private String packageName;
	private String versionCode;
	private String versionName;
	private String databaseVersion;
	private CategoryEntity rootCategory;
	private final MetaData metadata = new MetaData();
	private BackupFile previousBackupFile = null;

	/** map: type+id => entity */
	private final Map<String, Entity> idMap = new LinkedHashMap<String, Entity>();
	/** map: fingerPrint => entity */
	private final Map<String, Entity> fingerPrintMap = new HashMap<String, Entity>();
	/** map: type => nextId */
	private final Map<String, Integer> nextIdMap = new HashMap<String, Integer>();

	public BackupFile(File file) throws IndexOutOfBoundsException, IOException {
		this(file, true);
	}

	public BackupFile(File file, boolean loadSystemEntities) throws IndexOutOfBoundsException, IOException {
		this.file = file;
		this.fileName = file.getName();
		load(file, loadSystemEntities);
	}

	public BackupFile(String fileName, InputStream inputStream) throws IndexOutOfBoundsException, IOException {
		this(fileName, inputStream, true);
	}

	public BackupFile(String fileName, InputStream inputStream, boolean loadSystemEntities) throws IndexOutOfBoundsException, IOException {
		this.fileName = fileName;
		load(fileName, inputStream, loadSystemEntities);
	}

	@Override
	public String getFileName() {
		return fileName;
	}
	
	private CategoryEntity createRootCategory(){
		CategoryEntity ce = new CategoryEntity(this);
		ce.set("$ENTITY", Entity.TYPE_CATEGORY);
		ce.set("title", "root");
		ce.set("type", "0");
		ce.set("_id", "0");
		ce.set("left", Integer.toString(Integer.MIN_VALUE));
		ce.set("right", Integer.toString(Integer.MAX_VALUE));
		return ce;
	}

	
	private void checkVersion(String packageName, String versionCodeString, String versionName, String databaseVersionString){
		if (!PACKAGE.equals(packageName)){
			throw new RuntimeException("Incompatible package: '"+packageName+"' should be '"+PACKAGE+"'");
		}
		try {
			int versionCode = Integer.parseInt(versionCodeString);
			if (versionCode<MIN_VERSION_CODE || versionCode>MAX_VERSION_CODE){
				throw new RuntimeException("Incompatible version code: "+versionCode+" should be in range "+MIN_VERSION_CODE+"-"+MAX_VERSION_CODE);
			}
		} catch (NumberFormatException e){
			throw new RuntimeException("Invalid version code: "+versionCodeString, e);
		}
		try {
			int databaseVersion = Integer.parseInt(databaseVersionString);
			if (databaseVersion<MIN_DATABASE_VERSION || databaseVersion>MAX_DATABASE_VERSION){
				throw new RuntimeException("Incompatible database version: "+databaseVersion+" should be in range"+MIN_DATABASE_VERSION+"-"+MAX_DATABASE_VERSION);
			}
		} catch (NumberFormatException e){
			throw new RuntimeException("Invalid database version: "+databaseVersionString, e);
		}
	}

	private void load(File file, boolean loadSystemEntities) throws IndexOutOfBoundsException, IOException {
		try (InputStream stream = new FileInputStream(file)) {
			load(file.getAbsolutePath(), stream, loadSystemEntities);
		}
	}
	private void load(String fileName, InputStream stream, boolean loadSystemEntities) throws IndexOutOfBoundsException, IOException {
		rootCategory = createRootCategory();
		
		EntityIterator it = new EntityIterator(stream, this);
		packageName = it.packageName;
		versionCode = it.versionCode;
		versionName = it.versionName;
		databaseVersion = it.databaseVersion;

		metadata.set(packageName, versionName, versionCode, databaseVersion);

		logger.log(Level.FINER, "Loading file {0}", fileName);
		logger.log(Level.FINER, "\tpackage: {0}",packageName);
		logger.log(Level.FINER, "\tversionCode: {0}", versionCode);
		logger.log(Level.FINER, "\tversionName: {0}",versionName);
		logger.log(Level.FINER, "\tdatabaseVersion: {0}",databaseVersion);
		
		checkVersion(packageName, versionCode, versionName, databaseVersion);

		while (it.hasNext()){
			Entity entity = it.next();
			
			String id = entity.getId();
			String type = entity.getType();

			if (Entity.TYPE_ATTRIBUTES.equals(type) &&
					(FINMERGE_FILE_ATTRIBUTE.equals(entity.get("name")) || FINMERGE_FILE_ATTRIBUTE.equals(entity.get("title")))){
				metadata.setEntity(entity);
				continue; // avoid further processing of metadata
			}
			
			if (TYPE_CATEGORY.equals(entity.getType())){
				entity = new CategoryEntity(entity);
				if (!"0".equals(entity.getId())){
					rootCategory.addDescendant((CategoryEntity) entity);
				}
			}

			if (TYPE_ATTRIBUTES.equals(entity.getType())){
				entity = new AttributesEntity(entity);
			}
			
			if (id==null){
				id = getNextId(type);
			}
			if (loadSystemEntities || !ENTITIES_WITH_SYSTEM_VALUES.contains(entity.getType()) || !"0".equals(entity.getId())) {
				idMap.put(type + id, entity);
			}
			
			int i = Integer.parseInt(id);
			Integer n = nextIdMap.get(type);
			if (n==null || i+1>n){
				nextIdMap.put(type, i+1);
			}
		}

		idMap.put(rootCategory.getType()+rootCategory.getId(), rootCategory);

		for (Entity entity: idMap.values()){			
			String fingerPrint = entity.getFingerPrint();
			if (TYPE_TRANSACTIONS.equals(entity.getType()) && fingerPrintMap.get(fingerPrint)!=null){
				long date = Long.parseLong(entity.get("datetime"));				
				while (fingerPrintMap.get(fingerPrint)!=null){
					date++;
					entity.set("datetime", Long.toString(date));
					fingerPrint = entity.getFingerPrint();
				}
				logger.log(Level.INFO, "adjusted date for transaction {0} to keep fingerPrint unique", fingerPrint);
			}
			fingerPrintMap.put(fingerPrint, entity);
			updateReferenceCounters(entity, 1);
		}
		// add root category
		fingerPrintMap.put(rootCategory.getFingerPrint(), rootCategory);
		logger.log(Level.FINER, "Loaded file {0}", fileName);
	}
		
	
	private void updateReferenceCounters(Entity entity, int increment) {
		Iterator<String> it = entity.keys();
		while (it.hasNext()){
			Object value = entity.getValue(it.next());
			if (value instanceof Entity ){
				((Entity) value).updateReferenceCount(increment);
			}
		}
	}

	public void save(File file) throws IOException{
		rootCategory.rebuildTree(-1);
		metadata.setFileName(file.getName());
		GZIPOutputStream out = new GZIPOutputStream(new FileOutputStream(file));		
		OutputStreamWriter osw = new OutputStreamWriter(out, "UTF-8");
		BufferedWriter bw = new BufferedWriter(osw, 65536);
		try {
			writeHeader(bw);
			writeMetaData(bw);
			writeBody(bw);
			writeFooter(bw);
		} finally {
			bw.close();
		}
		this.file = file;
	}
	
	private void writeHeader(BufferedWriter bw) throws IOException{
		bw.write("PACKAGE:"); bw.write(packageName);bw.write("\n");
		bw.write("VERSION_CODE:");bw.write(versionCode);bw.write("\n");
		bw.write("VERSION_NAME:");bw.write(versionName);bw.write("\n");
		bw.write("DATABASE_VERSION:");bw.write(databaseVersion);bw.write("\n");
		bw.write("#START\n");
	}
	
	private void writeMetaData(BufferedWriter bw) throws IOException{
		Entity entity = metadata.getEntity(this);
		if (entity!=null){
			entity.setId(getNextId(entity.getType()));
			Iterator<String> it = entity.keys();
			while (it.hasNext()){
				String key = it.next();
				bw.write(key);
				bw.write(":");
				bw.write(entity.get(key));
				bw.write("\n");				
			}
			bw.write("$$\n");
		}		
	}
	
	private void writeBody(BufferedWriter bw) throws IOException{		
		for (Map.Entry<String, Entity> me: idMap.entrySet()){
			Entity entity = me.getValue();
			if (entity==rootCategory && metadata.getVersion()>=VERSION_213){
				continue;
			}
			Iterator<String> it = entity.keys();
			while (it.hasNext()){
				String key = it.next();
				bw.write(key);
				bw.write(":");
				bw.write(entity.get(key));
				bw.write("\n");				
			}
			bw.write("$$\n");			
		}
	}
	
	private void writeFooter(BufferedWriter bw) throws IOException {
		bw.write("#END");
	}
	
	public Entity getById(String type, String id){
		return idMap.get(type+id);
	}
	
	public Entity getByFingerPrint(String fingerPrint) {
		return fingerPrintMap.get(fingerPrint);
	}
	
	
	private String getNextId(String type){
		Integer k = nextIdMap.get(type);
		if (k==null){
			k = 1;
		} else {
			k++;
		}
		nextIdMap.put(type, k);
		return k.toString();
	}
	
	@Override
	public void merge(EntityManager backupFile) {
		// TODO move versions to interface
		if (backupFile instanceof BackupFile){
			checkVersion(
					((BackupFile)backupFile).packageName, 
					((BackupFile)backupFile).versionCode, 
					((BackupFile)backupFile).versionName, 
					((BackupFile)backupFile).databaseVersion
			);
		}
		
		Set<String> thisDeleted = new HashSet<String>();
		for (Entity entity: this.deleted()){
			thisDeleted.add(entity.getFingerPrint());
		}
		
		for (Entity entity: backupFile){
			if (!thisDeleted.contains(entity.getFingerPrint())){
				this.add(entity);
			} else {
				logger.log(Level.FINER, "Ignore deleted: {0}", entity);
			}
		}
		// handle deleted entities
		try {
			ArrayList<Entity> localDelete = new ArrayList<Entity>();
			for (String fp: thisDeleted){ // entities, deleted in this file may be restored by merged file
				Entity local = getByFingerPrint(fp);
				if (local!=null && (local.getReferenceCount()!=0 || remove(local)==null)){
					localDelete.add(local);
				}				
			}
			for (Entity entity: backupFile.deleted()){
				Entity local = getByFingerPrint(entity.getFingerPrint());
				if (local!=null && (local.getReferenceCount()!=0 || remove(local)==null)){
					localDelete.add(local);
				}
			}
			boolean deleted = true;
			while (deleted && !localDelete.isEmpty()){
				deleted = false;
				Iterator<Entity> it = localDelete.iterator();
				while(it.hasNext()){
					Entity e = it.next();
					if (e.getReferenceCount()==0 && remove(e)!=null){
						deleted = true;
						it.remove();
					}
				}
			}
			for (Entity e: localDelete){
				logger.log(Level.FINE, "Entity {0} is not deleted, because it is used", e);
			}
			
		} catch (Exception e) {
			logger.log(Level.WARNING, "Error while detecting deleted entities ", e);
		}
		
		metadata.setParents(new String[]{fileName, backupFile.getFileName()});
	}
	
	/** 
	 * returns entities unique for this backup file, i.e. entities absent in given backupFile
	 * @param backupFile
	 * @return
	 */
	@Override
	public List<Entity> unique(EntityManager backupFile){
		ArrayList<Entity> result = new ArrayList<Entity>();
		for (Entity entity: this){
			if (backupFile.getByFingerPrint(entity.getFingerPrint())==null){
				result.add(entity);
			}
		}
		return result;
	}
	/**
	 * returns entities common with given backup file 
	 * @param backupFile
	 * @return
	 */
	@Override
	public List<Entity> common(EntityManager backupFile){
		ArrayList<Entity> result = new ArrayList<Entity>();
		for (Entity entity: this){
			if (backupFile.getByFingerPrint(entity.getFingerPrint())!=null){
				result.add(entity);
			}
		}
		return result;
	}
	
	public Collection<Entity> deleted() {
		BackupFile pbf = getPreviousBackupFile();
		if (pbf!=null){
			return pbf.unique(this);
		}
		return Collections.emptyList();
	}
	
	public Collection<Entity> added() {
		BackupFile pbf = getPreviousBackupFile();
		if (pbf!=null){
			return this.unique(pbf);
		}		
		return Collections.unmodifiableCollection(idMap.values());
	}
	
	/**
	 * adds entity and return its new id
	 * 
	 * @param entity
	 * @return
	 */
	public String add(Entity entity){
		// sanity check
		if (entity==null){
			return null;
		}	
		// fingerPrint check
		Entity local = getByFingerPrint(entity.getFingerPrint());
		if (local!=null){
			if (local.getId()!=null && local.getId().equals(entity.getId())){
				logger.log(Level.FINEST, "Ignore clone entity: {0}", entity.getFingerPrint());
			} else {
				logger.log(Level.FINER, "Ignore duplicate: local: {0} entity: {1}", new Object[]{local, entity});
			}
			return local.getId();
		}
		// same EntityManager case
		if (entity.getEntityManager()==this){
			String entityId = entity.getType()+entity.getId();
			local = idMap.get(entityId);
			if (!entity.equals(local)){
				if (local!=null){
					fingerPrintMap.remove(local.getFingerPrint());
					entity.setReferenceCount(local.getReferenceCount());
				}
				idMap.put(entityId, entity);
				fingerPrintMap.put(entity.getFingerPrint(), entity);
				updateReferenceCounters(entity, 1);
				logger.log(Level.FINE, "Added: {0}", entity);
			}
			return entity.getId(); 
		}
		// clone to keep entity unchanged, because it belong to another entity manager
		Entity clone = (Entity) entity.clone();
		clone.setEntityManager(this);
		clone.setId(getNextId(clone.getType()));		
		// check  for sub entities
		Iterator<String> keys = entity.keys();
		while (keys.hasNext()){
			String key = keys.next();
			Object value = entity.getValue(key);
			if (value instanceof Entity){
				// assign new ids
				clone.set(key, add((Entity) value));
			}
		}
		if (clone instanceof CategoryEntity){
			clone.set("parent", add((Entity) entity.getValue("parent")));
		}
		return add(clone);
	}

	@Override
	public void update(EntityManager otherFile){
		if (otherFile instanceof BackupFile){
			checkVersion(
					((BackupFile)otherFile).packageName,
					((BackupFile)otherFile).versionCode,
					((BackupFile)otherFile).versionName,
					((BackupFile)otherFile).databaseVersion
			);
		}

		for (Entity entity: otherFile.added()){
			this.add(entity);
		}

		remove(otherFile.deleted());
	}

	@Override
	public int remove(Collection<Entity> entities){
		ArrayList<Entity> toDelete = new ArrayList<Entity>(entities);
		int count = toDelete.size();
		boolean deleted = true;
		while (deleted && !toDelete.isEmpty()){
			deleted = false;
			Iterator<Entity> it = toDelete.iterator();
			while(it.hasNext()){
				if (remove(it.next())!=null){
					deleted = true;
					it.remove();
				}
			}
		}
		for (Entity e: toDelete){
			logger.log(Level.FINE, "Entity {0} is not deleted, because it is used", e);
		}
		return count - toDelete.size();
	}

	public Entity remove(Entity entity){
		return remove(entity, true);
	}
	private Entity remove(Entity entity, boolean updateReferenceCounters) {
		if (entity.getEntityManager()==this){
			if (entity.getReferenceCount()==0){ // remove only entities, that are not referenced by others
				String entityId = entity.getType()+entity.getId();
				idMap.remove(entityId);
				fingerPrintMap.remove(entity.getFingerPrint());
				if (updateReferenceCounters){
					updateReferenceCounters(entity, -1);
				}
				logger.log(Level.FINE,"Removed: {0}", entity);
				return entity;
			} else {
				logger.warning("Can not remove entiry "+entity+" because its reference counter is not zero: "+entity.getReferenceCount());
			}
		} else {
			Entity local = getByFingerPrint(entity.getFingerPrint());
			if (local!=null){
				return remove(local);
			}
		}
		return null;
	}
	
	
	@Override
	public int hashCode() {
		return idMap.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof BackupFile && idMap.equals(((BackupFile) obj).idMap);
	}

	public Iterator<Entity> iterator() {
		return idMap.values().iterator();
	}
	
	public MetaData getMetaData(){
		return metadata;
	}
	
	public BackupFile getPreviousBackupFile() {
		if (previousBackupFile==null){
			String previousFileName = metadata.getFileName();
			if (previousFileName!=null && file!=null){
				File previousFile = new File(file.getParentFile(), previousFileName);
				if (previousFile.exists() && previousFile.canRead()){
					try {
						previousBackupFile = new BackupFile(previousFile, getMetaData().getVersion()< VERSION_213);
					} catch (Exception e) {
						logger.log(Level.FINE, "Failed to load previous backup file of "+fileName+" ", e);
					}
				}
			}
		}
		return previousBackupFile;
	}

	public void setPreviousBackupFile(BackupFile previousBackupFile) {
		this.previousBackupFile = previousBackupFile;
	}

	public String getPackageName() {
		return packageName;
	}

	public String getVersionCode() {
		return versionCode;
	}

	public String getVersionName() {
		return versionName;
	}

	public String getDatabaseVersion() {
		return databaseVersion;
	}

	@Override
	public Map<String, String> getReferenceTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, String[]> getFingerPrintDefinitions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean contains(Entity entity) {
		return getByFingerPrint(entity.getFingerPrint())!=null;
	}

	public Stream<Entity> stream() {
		return StreamSupport.stream(spliterator(), false);
	}

}
