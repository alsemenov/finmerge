/*
  ~ Copyright (c) 2014-2015 Aleksei Semenov (aka xibodoh)
  ~ All rights reserved. This program and the accompanying materials
  ~ are made available under the terms of the GNU Public License v2.0
  ~ which accompanies this distribution, and is available at
  ~ http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
*/
package ru.xibodoh.finmerge;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractEntityManager implements EntityManager {

	private static final Logger logger = Logger.getLogger(AbstractEntityManager.class.getName());
	
	/** map: type+id => entity */
	protected final Map<String, Entity> idMap = new LinkedHashMap<String, Entity>();
	/** map: fingerPrint => entity */
	protected final Map<String, Entity> fingerPrintMap = new HashMap<String, Entity>();

	protected EntityManager previousVersion = null;

	
	
	public Iterator<Entity> iterator() {
		return idMap.values().iterator();
	}

	@Override
	public Entity getById(String type, String id){
		return idMap.get(type+id);
	}
	
	public Entity getByFingerPrint(String fingerPrint) {
		return fingerPrintMap.get(fingerPrint);
	}

	protected void updateReferenceCounters(Entity entity, int increment) {
		Iterator<String> it = entity.keys();
		while (it.hasNext()){
			Object value = entity.getValue(it.next());
			if (value instanceof Entity ){
				((Entity) value).updateReferenceCount(increment);
			}
		}
	}
	
	@Override
	public int hashCode() {
		return idMap.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		return obj instanceof AbstractEntityManager && idMap.equals(((AbstractEntityManager) obj).idMap);
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
		String fingerPrint = getFingerPrint(entity);
		if (fingerPrint==null){
			return null;
		}
		Entity local = getByFingerPrint(fingerPrint);
		if (local!=null){
			if (local.getId()!=null && local.getId().equals(entity.getId())){
				logger.log(Level.FINEST, "Ignore clone entity: {0}", entity.getFingerPrint());
			} else {
				logger.log(Level.FINER, "Ignore duplicate: local: {0} entity: {1}", new Object[]{local, entity});
			}
			return local.getId();
		}
		// add entity
		if (entity.getEntityManager()!=this){
			// clone it first
			entity = createClone(entity);
			if (entity==null){
				return null;
			}
		}

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
			added(entity);
			logger.log(Level.FINE, "Added: {0}", entity);
		}
		return entity.getId(); 
	}

	public Entity remove(Entity entity){
		if (entity.getEntityManager()!=this){
			entity = getByFingerPrint(getFingerPrint(entity));
			if (entity==null){
				return null;
			}
		}
		assert(entity.getEntityManager()==this);
		if (entity.getReferenceCount()==0){ // remove only entities, that are not referenced by others
			String entityId = entity.getType()+entity.getId();
			idMap.remove(entityId);
			fingerPrintMap.remove(entity.getFingerPrint());
			updateReferenceCounters(entity, -1);
			logger.log(Level.FINE,"Removed: {0}", entity);
			return entity;
		} else {
			logger.warning("Can not remove entiry "+entity+" because its reference counter is not zero: "+entity.getReferenceCount());
		}
		return null;
	}
	
	
	/**
	 * create clone of the given entity, that will be added to this entity manager
	 * @param entity
	 * @return
	 */
	protected abstract Entity createClone(Entity entity);
	
	protected abstract void added(Entity entity);
	/**
	 * returns finger print of given entity, the entity may belong
	 * to different entity manager class
	 * @param entity
	 * @return
	 */
	protected abstract String getFingerPrint(Entity entity);
		
	
	@Override
	public boolean contains(Entity entity) {
		return getByFingerPrint(getFingerPrint(entity))!=null;
	}
	
	
	
	/** 
	 * returns entities unique for this backup file, i.e. entities absent in given backupFile
	 * @param entityManager
	 * @return
	 */
	public List<Entity> unique(EntityManager entityManager){
		ArrayList<Entity> result = new ArrayList<Entity>();
		for (Entity entity: this){
			if (!entityManager.contains(entity)){
				result.add(entity);
			}
		}
		return result;
	}
	/**
	 * returns entities common with given backup file 
	 * @param entityManager
	 * @return
	 */
	public List<Entity> common(EntityManager entityManager){
		ArrayList<Entity> result = new ArrayList<Entity>();
		for (Entity entity: this){
			if (entityManager.contains(entity)){
				result.add(entity);
			}
		}
		return result;
	}	
	
	protected EntityManager getPreviousVersion() {
		return null;
//		if (previousVersion ==null){
//			String previousFileName = getMetaData().getFileName();
//			if (previousFileName!=null){
//				File previousFile = new File(getFile().getParentFile(), previousFileName);
//				if (previousFile.exists() && previousFile.canRead()){
//					try {
//						Constructor<? extends AbstractEntityManager> ctor = getClass().getDeclaredConstructor(File.class);
//						previousVersion = ctor.newInstance(previousFile);
//					} catch (Exception e) {
//						logger.log(Level.FINE, "Failed to load previous backup file of "+getFile().getAbsolutePath()+" ", e);
//					}
//				}
//			}
//		}
//		return previousVersion;
	}
	
	
	public Collection<Entity> deleted() {
		EntityManager previousVersion = getPreviousVersion();
		if (previousVersion!=null){
			return previousVersion.unique(this);
		}
		return Collections.emptyList();
	}
	
	public Collection<Entity> added() {
		EntityManager previousVersion = getPreviousVersion();
		if (previousVersion!=null){
			return this.unique(previousVersion);
		}		
		return Collections.unmodifiableCollection(idMap.values());
	}
	
	
	@Override
	public void merge(EntityManager that) {
		
		Set<String> thisDeleted = new HashSet<String>();
		for (Entity entity: this.deleted()){
			thisDeleted.add(entity.getFingerPrint());
		}
		
		for (Entity entity: that){
			if (!thisDeleted.contains(getFingerPrint(entity))){
				this.add(entity);
			} else {
				logger.log(Level.FINER, "Ignore deleted: {0}", entity);
			}
		}
		// handle deleted entities
		try {
			ArrayList<Entity> localDelete = new ArrayList<Entity>();
			for (String fp: thisDeleted){ // entities, deleted in this file may be restored by merged file implicitly
				Entity local = getByFingerPrint(fp);
				if (local!=null && (local.getReferenceCount()!=0 || remove(local)==null)){
					localDelete.add(local);
				}				
			}
			for (Entity entity: that.deleted()){
				Entity local = getByFingerPrint(getFingerPrint(entity));
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
		
		getMetaData().setParents(new String[]{getFileName(), that.getFileName()});
	}


	@Override
	public void update(EntityManager otherFile){
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
}
