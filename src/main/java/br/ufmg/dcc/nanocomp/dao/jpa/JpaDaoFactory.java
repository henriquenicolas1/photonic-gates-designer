package br.ufmg.dcc.nanocomp.dao.jpa;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.ufmg.dcc.nanocomp.dao.Dao;
import br.ufmg.dcc.nanocomp.dao.DaoFactory;
import br.ufmg.dcc.nanocomp.jsf.JpaFilter;
import br.ufmg.dcc.nanocomp.model.EntityInterface;

public class JpaDaoFactory extends DaoFactory implements AutoCloseable {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(JpaDaoFactory.class);
	
	/**
	 * A cache to store {@link Dao} objects, since {@link AbstractJpaDao} are thread safe,
	 * they can be stored and reused 
	 */
	private Map<Class<?>,AbstractJpaDao<?,?>> cache;
	private EntityManager em;
	
	public JpaDaoFactory() {
		em = JpaFilter.getEmf().createEntityManager();
		em.getTransaction().begin();
		cache = new HashMap<>();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <IdType extends Serializable,EntityType extends EntityInterface<IdType>,DaoType extends Dao<IdType,EntityType>> DaoType getDao(Class<DaoType> daoClass){
		try {
			AbstractJpaDao<?,?> dao = cache.get(daoClass);
			if(dao == null){
				String daoName = daoClass.getPackage().getName()+".jpa.Jpa"+daoClass.getSimpleName();
				dao = (AbstractJpaDao<?, ?>) Class.forName(daoName).newInstance();
				dao.setEntityManager(em);
				cache.put(daoClass, dao);
			}
			return (DaoType) dao;
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			LOGGER.warn("It was not possible to instanciate Dao",e);
			return null;
		}
	}
	
	@Override
	public void close() {
		em.getTransaction().commit();
		em.close();
	}

}
