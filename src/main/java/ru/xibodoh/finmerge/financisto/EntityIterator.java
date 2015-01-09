/*
  ~ Copyright (c) 2014-2015 Aleksei Semenov (aka xibodoh)
  ~ All rights reserved. This program and the accompanying materials
  ~ are made available under the terms of the GNU Public License v2.0
  ~ which accompanies this distribution, and is available at
  ~ http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
*/
package ru.xibodoh.finmerge.financisto;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.zip.GZIPInputStream;


class EntityIterator implements Iterator<Entity>{

		private BufferedReader reader = null;
		private String nextLine;
		private final EntityManager entityManager;
		
		
		public final String packageName;
		public final String versionCode;
		public final String versionName;
		public final String databaseVersion;
		
		EntityIterator(File file, EntityManager entityManager) throws IOException, IndexOutOfBoundsException{
//			this.entityName = entityName;
//			try {
				this.entityManager = entityManager;
				GZIPInputStream zin = new GZIPInputStream(new FileInputStream(file));
				reader = new BufferedReader(new InputStreamReader(zin, "UTF-8"));
				
				String packageLine = reader.readLine();
				packageName = packageLine.substring(packageLine.indexOf(':')+1);
				String versionCodeLine = reader.readLine();
				versionCode = versionCodeLine.substring(versionCodeLine.indexOf(':')+1);
				String versionNameLine = reader.readLine();
				versionName = versionNameLine.substring(versionNameLine.indexOf(':')+1);
				String databaseVersionLine = reader.readLine();
				databaseVersion = databaseVersionLine.substring(databaseVersionLine.indexOf(':')+1);
				String startLine = reader.readLine();
				if (!"#START".equals(startLine)){
					throw new RuntimeException("Unsupported file format");
				}
				nextLine = reader.readLine();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}
		
		
		public boolean hasNext() {
			boolean result = nextLine!=null && nextLine.startsWith("$ENTITY:");
			if (!result){
				try {
					reader.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}			
			return result; //!"#END".equals(nextLine);
		}

		public Entity next()  {
			if (!hasNext()){
				return null;
			}
			return readEntity();
		}

		private Entity readEntity() {
			try {			
				Entity entity = new EntityImpl(entityManager);
				while (nextLine!=null && !"$$".equals(nextLine)){
					int index = nextLine.indexOf(':');
					if (index>=0){
						String key = nextLine.substring(0,index);
						String value = nextLine.substring(index+1);
						entity.set(key, value);
					} else {
						throw new RuntimeException("Invalid line: "+nextLine);
					}
					nextLine = reader.readLine();
				}
				nextLine = reader.readLine();
				return entity;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
	
		public void remove() {
			throw new UnsupportedOperationException();
		}
		
	}