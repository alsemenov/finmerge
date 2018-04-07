package ru.xibodoh.finmerge.financisto;

import ru.xibodoh.finmerge.Entity;
import ru.xibodoh.finmerge.EntityManager;

import java.util.Iterator;
import java.util.Map;

import static ru.xibodoh.finmerge.financisto.BackupFile.VERSION_213;

public class AttributesEntity extends EntityImpl {

	public AttributesEntity(Entity entity) {
		super(entity.getEntityManager());
		Iterator<String> it = entity.keys();
		while (it.hasNext()){
			String key = it.next();
			set(key, entity.get(key));
		}
	}

	private String getTitleKey(){
		int version = getEntityManager().getMetaData().getVersion();
		return version>=VERSION_213 ? "title" : "name";
	}

	public String getFingerPrint() {
		String type = getType();
		return type + get("type") + get(getTitleKey());
	}

	@Override
	public void setEntityManager(EntityManager entityManager) {
		String titleValue = remove(getTitleKey());
		super.setEntityManager(entityManager); // possible changes title key
		set(getTitleKey(), titleValue);
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof AttributesEntity){
			AttributesEntity that = (AttributesEntity) o;
			String titleKey = getTitleKey();
			for (Map.Entry<String, String> kv: entrySet()){
				String key = kv.getKey();
				String thatValue = that.get(key);
				if (titleKey.equals(key)){
					thatValue = that.get(that.getTitleKey());
				}
				String value = kv.getValue();
				if ((value ==null && thatValue!=null) || (value !=null && !value.equals(thatValue))){
					return false;
				}
			}
			return true;
		}
		return super.equals(o);
	}

	@Override
	public int hashCode() {
		return getFingerPrint().hashCode();
	}

	@Override
	public Object clone() {
		return new AttributesEntity(this);
	}
}
