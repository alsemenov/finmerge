/*
  ~ Copyright (c) 2014-2015 Aleksei Semenov (aka xibodoh)
  ~ All rights reserved. This program and the accompanying materials
  ~ are made available under the terms of the GNU Public License v2.0
  ~ which accompanies this distribution, and is available at
  ~ http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
*/
package ru.xibodoh.finmerge;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


public abstract class AbstractTreeEntity extends AbstractEntity implements Iterable<AbstractTreeEntity>{

	public AbstractTreeEntity(EntityManager entityManager) {
		super(entityManager);
	}

	private static final long serialVersionUID = 4802347263163654026L;

	protected AbstractTreeEntity parent = null;
	protected LinkedList<AbstractTreeEntity> children = null;
	
	@Override
	public String getId() {
		return getFingerPrint("");
	}

	@Override
	public void setId(String id) {
		throw new UnsupportedOperationException("AbilityCash entities do not support external id");
	}
	
	public void addChild(AbstractTreeEntity child){
		if (child.getEntityManager()!=this.getEntityManager()){
			throw new IllegalArgumentException("child belongs to different entity manager");
		}
		if (children==null){
			children = new LinkedList<AbstractTreeEntity>();
		}
		children.add(child);
		child.parent = this;
	}

	@Override
	public Entity getParent() {
		return parent;
	}

	public void setParent(AbstractTreeEntity parent) {
		this.parent = parent;
	}
	
	public AbstractTreeEntity getChild(String key, String value){
		if (children!=null){
			for (AbstractTreeEntity entity: children){
				String entityValue = entity.get(key);
				if ((value==null && entityValue==null) ||
					(value!=null && value.equals(entityValue))){
					return entity;
				}
			}
		}
		return null;
	}

	@Override
	public Iterator<AbstractTreeEntity> iterator() {
		if (children!=null){
			return children.iterator();
		}
		return Collections.<AbstractTreeEntity>emptyIterator();
	}
	
	public AbstractTreeEntity[] getPath(){
		Deque<AbstractTreeEntity> deque = new ArrayDeque<>();
		AbstractTreeEntity e = this;
		while (e!=null){
			deque.addFirst(e);
			e = e.parent;
		}				
		AbstractTreeEntity[] result = new AbstractTreeEntity[deque.size()];
		deque.toArray(result);
		return result;
	}
	
	public List<AbstractTreeEntity> find(String key, String value){
		ArrayList<AbstractTreeEntity> result = new ArrayList<>();
		Iterator<AbstractTreeEntity> it = tree();
		while (it.hasNext()){
			AbstractTreeEntity entity = it.next();
			String entityValue = entity.get(key);
			if ((value==null && entityValue==null) ||
				(value!=null && value.equals(entityValue))){
				result.add(entity);
			}			
		}
		return result;
	}
	
	public List<AbstractTreeEntity> find(String key, String[] values){
		ArrayList<AbstractTreeEntity> result = new ArrayList<>();
		if (values==null || values.length==0){
			return result;
		}
		String value = values[0];
		Iterator<AbstractTreeEntity> it = tree();
		while (it.hasNext()){
			AbstractTreeEntity entity = it.next();
			String entityValue = entity.get(key);
			if ((value==null && entityValue==null) ||
				(value!=null && value.equals(entityValue))){

				int i = 1;
				while (i<values.length && (entity = entity.getChild(key, values[i]))!=null) {
					i++;
				}				
				if (i>=values.length){
					result.add(entity);
				}
			}			
		}
		return result;
	}
	
	
	public Iterator<AbstractTreeEntity> tree() {
		return new TreeIterator(this);
	}
	
	private static class TreeIterator implements Iterator<AbstractTreeEntity> {

		private Deque<AbstractTreeEntity> queue;
		
		TreeIterator(AbstractTreeEntity root){
			queue = new ArrayDeque<>();
			queue.addLast(root);
		}
		
		@Override
		public boolean hasNext() {
			return !queue.isEmpty();
		}

		@Override
		public AbstractTreeEntity next() {
			AbstractTreeEntity e = queue.pop(); // throws NoSuchElement if queue is empty
			if (e.children!=null){
				queue.addAll(e.children);
			}
			return e;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException(); 
		}
		
	}
	
}
