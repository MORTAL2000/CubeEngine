package com.grillecube.common.resources;

import com.grillecube.common.Logger;
import com.grillecube.common.Logger.Level;
import com.grillecube.common.world.entity.WorldEntity;

public class EntityManager extends GenericManager<Class<? extends WorldEntity>> {

	public EntityManager(ResourceManager resource_manager) {
		super(resource_manager);
	}

	/** register a new entity from it class */
	public int registerEntity(Class<? extends WorldEntity> entityclass) {
		if (super.hasObject(entityclass)) {
			Logger.get().log(Level.WARNING,
					"Tried to register an already registered entity: " + entityclass.getSimpleName());
			return (-1);
		}
		Logger.get().log(Level.FINE, "Registered an entity: " + entityclass.getSimpleName());
		return (super.registerObject(entityclass));
	}

	/** create a new instance of the given entity by it ID */
	@SuppressWarnings("unchecked")
	public <T> T newInstance(int entityID) {
		try {
			Class<? extends WorldEntity> entityclass = super.getObjectByID(entityID);
			return (T) (entityclass.newInstance());
		} catch (Exception exception) {
			Logger.get().log(Level.ERROR, "Exception occured while creating new entity instance:");
			exception.printStackTrace(Logger.get().getPrintStream());
			return (null);
		}
	}

	@Override
	public void onInitialized() {
	}

	@Override
	public void onLoaded() {
	}

	@Override
	protected void onDeinitialized() {
	}

	@Override
	protected void onUnloaded() {
	}

	@Override
	protected void onObjectRegistered(Class<? extends WorldEntity> object) {
	}
}
