/*
  ~ Copyright (c) 2014-2015 Aleksei Semenov (aka xibodoh)
  ~ All rights reserved. This program and the accompanying materials
  ~ are made available under the terms of the GNU Public License v2.0
  ~ which accompanies this distribution, and is available at
  ~ http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
*/
package ru.xibodoh.finmerge;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

public interface EntityManager extends Iterable<Entity> {

	public File getFile();
	
	public Entity getById(String type, String id);
	
	public Entity getByFingerPrint(String fingerPrint);
	
	public String add(Entity entity);
	
	public Entity remove(Entity entity);
	
	public MetaData getMetaData();
	
	public Map<String, String> getReferenceTypes();
	
	public Map<String, String[]> getFingerPrintDefinitions();

	public boolean contains(Entity entity);
	
	public List<Entity> unique(EntityManager entityManager);
	
	public List<Entity> common(EntityManager entityManager);
	
	public void merge(EntityManager backupFile);
	
	public Collection<Entity> added();
	
	public Collection<Entity> deleted();
	
	// TODO narrow exceptions list
	public void save(File file) throws IOException, XMLStreamException, FactoryConfigurationError;
}
