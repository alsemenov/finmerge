/*
  ~ Copyright (c) 2014-2015 Aleksei Semenov (aka xibodoh)
  ~ All rights reserved. This program and the accompanying materials
  ~ are made available under the terms of the GNU Public License v2.0
  ~ which accompanies this distribution, and is available at
  ~ http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
*/
package ru.xibodoh.finmerge.financisto;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

class CategoryEntity extends EntityImpl {

	private CategoryEntity parent = null;
	private int left, right;
	private LinkedList<CategoryEntity> children = null;
	private boolean modified = false;
	
	public CategoryEntity(EntityManager entityManager) {
		super(entityManager);
	}
	
	public CategoryEntity(Entity entity){
		super(entity.getEntityManager());
		Iterator<String> it = entity.keys();
		while (it.hasNext()){
			String key = it.next();
			set(key, entity.get(key));
		}
	}

	@Override
	public String getFingerPrint() {
		LinkedList<CategoryEntity> path = new LinkedList<CategoryEntity>();
		CategoryEntity p = this.parent;
		while (p!=null){
			path.addFirst(p);
			p = p.parent;
		}
		StringBuilder result =  new StringBuilder(getType());
		for (CategoryEntity ce: path){
			result.append(ce.get("title")).append(ce.get("type")).append("\\");
		}
		result.append(get("title")).append(get("type"));
		return result.toString();
	}

	@Override
	public String get(String key) {
		if ("left".equals(key)){
			return Integer.toString(left);
		} else if ("right".equals(key)){
			return Integer.toString(right);
		}
		return super.get(key);
	}

	@Override
	public Object getValue(String key) {
		if ("left".equals(key)){
			return left;
		} else if ("right".equals(key)){
			return right;
		} else if ("parent".equals(key)){
			return parent;
		}
		return super.getValue(key);
	}

	@Override
	public void set(String key, String value) {
		if ("left".equals(key)){
			if (left!=Integer.MIN_VALUE){ // keep root min value
				left = Integer.parseInt(value);
			}
		} else if ("right".equals(key)){
			if (right!=Integer.MAX_VALUE){ // keep root max value
				right = Integer.parseInt(value);
			}
		} else if ("parent".equals(key)){
			parent = (CategoryEntity) entityManager.getById(TYPE_CATEGORY, value);
			parent.addChild(this);
			return;
		}
		super.set(key, value);
	}

	
	private final boolean isParent(CategoryEntity ce){
		return left<ce.left && ce.right<right;
	}
	
	
	public void addChild(CategoryEntity node){
		if (children==null){
			children = new LinkedList<CategoryEntity>();
		}
		children.add(node);
		node.parent = this;
		CategoryEntity p = this;
		while (p!=null && !p.modified){
			p.modified = true;
			p = p.parent;
		}
	}
	
	public void addDescendant(CategoryEntity node){
		assert(isParent(node));
		CategoryEntity n = this;		
		n.modified = true;
		if (n.children==null){
			n.addChild(node);
			return;
		}
		boolean added = false;
		ListIterator<CategoryEntity> it = n.children.listIterator();
		while (it.hasNext()){
			CategoryEntity c = it.next();
			if (c.isParent(node)){
				n = c;
				n.modified = true;
				if (n.children==null){
					n.addChild(node);
					return;
				}
				it = n.children.listIterator();
				added = false;
			
			} else if (node.isParent(c)){
				it.remove();
				node.addDescendant(c);
			
			} else if (!added && c.left > node.left){
				it.previous();				
				it.add(node);
				node.parent = n;
				added = true;
			}
		}
		if (!added){
			n.addChild(node);
		}
	}
	
	
	public Iterator<CategoryEntity> tree(int left){
		if (modified){
			rebuildTree(left);
		}
		return new CategoryTreeIterator(this);
		
	}
	
	public int rebuildTree(int left){
		set("left", Integer.toString(left));
		int right = left+1;
		this.modified = false;
		if (children!=null){
			for (CategoryEntity ce: children){
				right = ce.rebuildTree(right);
			}
		}
		set("right", Integer.toString(right));
		return right+1;
	}
	
	
	private static class CategoryTreeIterator implements Iterator<CategoryEntity>{
		
		private int x;
		private Deque<CategoryEntity> deque;
		
		
		CategoryTreeIterator(CategoryEntity root){
			x = -1;
			deque = new LinkedList<CategoryEntity>();
			deque.addLast(root);
		}
		
		public boolean hasNext() {
			return !deque.isEmpty();
		}
	
		public CategoryEntity next() {
			CategoryEntity ce = deque.pollLast();
			if (ce!=null && ce.children!=null){
				Iterator<CategoryEntity> it = ce.children.descendingIterator();
				while(it.hasNext()){
					deque.addLast(it.next());
				}
			}
			return ce;
		}
	
		public void remove() {
			throw new UnsupportedOperationException();
		}
		
	}


	@Override
	public Object clone() {
		CategoryEntity clone = new CategoryEntity(entityManager);
		clone.putAll(this);
		clone.left = 0;
		clone.right = 0;
		clone.parent = null;
		clone.children = null;
		clone.modified = false;
		return clone;
	}
	
	
}
