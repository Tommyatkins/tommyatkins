package com.tommyatkins.j2ee.dao;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Id;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate4.HibernateCallback;
import org.springframework.orm.hibernate4.support.HibernateDaoSupport;
import org.springframework.stereotype.Repository;

import com.tommyatkins.j2ee.entity.Page;
import com.tommyatkins.j2ee.exception.BusinessException;

/**
 * @author xsd
 *
 */
@Repository
public class BaseDao<T> extends HibernateDaoSupport {

	@Autowired
	public void setLocalSessionFactory(SessionFactory sessionFactory) {
		super.setSessionFactory(sessionFactory);
	}

	public Serializable save(T t) {
		return getHibernateTemplate().save(t);
	}

	public void update(T t) {
		getHibernateTemplate().update(t);
	}

	public void delete(Class<T> entityClass, Serializable id) {
		getHibernateTemplate().delete(get(entityClass, id));
	}

	public void delete(T t) {
		getHibernateTemplate().delete(t);
	}

	/**
	 * Only used in simple primary key situation
	 * 
	 * @param entityClass
	 * @param id
	 */
	public void merge(T t) {
		getHibernateTemplate().merge(t);
	}

	public T get(Class<T> entityClass, Serializable id) throws BusinessException {
		T t = getHibernateTemplate().get(entityClass, id);
		// if (t == null) {
		// throw new
		// OpenPlatformException(String.format("Can not find id : \"%s\" in %s table",
		// id, entityClass.getSimpleName()));
		// }
		return t;
	}

	@SuppressWarnings("unchecked")
	public List<T> getAll(Class<T> entityClass) {
		StringBuilder hql = new StringBuilder("From ");
		hql.append(entityClass.getName());
		return (List<T>) findByHql(hql.toString(), new Object[] {});
	}

	/**
	 * this used hiberante's load-method , so all object are lazy initialized
	 * 
	 * @param entityClass
	 * @return
	 */
	public List<T> loadAll(Class<T> entityClass) {
		return getHibernateTemplate().loadAll(entityClass);
	}

	public List<?> findByHql(String hql, Object... objects) {
		return getHibernateTemplate().find(hql, objects);
	}

	public List<?> findBySql(String sql, final Object... objects) {
		List<?> results = getHibernateTemplate().execute(new HibernateCallback<List<?>>() {
			@Override
			public List<?> doInHibernate(Session session) throws HibernateException {
				Query query = session.createSQLQuery(sql);
				for (int i = 0; i < objects.length; i++) {
					query.setParameter(i, objects[i]);
				}
				List<?> result = query.list();
				return result;
			}
		});
		return results;
	}

	public int executeBySql(String sql, final Object... objects) {
		int results = getHibernateTemplate().execute(new HibernateCallback<Integer>() {
			@Override
			public Integer doInHibernate(Session session) throws HibernateException {
				Transaction transaction = session.getTransaction();
				transaction.begin();
				Query query = session.createSQLQuery(sql);
				for (int i = 0; i < objects.length; i++) {
					query.setParameter(i, objects[i]);
				}
				int result = query.executeUpdate();
				transaction.commit();
				return result;
			}
		});
		return results;
	}

	/**
	 * Just support simple POJO query , which fields are simple type like String
	 * ,int and so on. So far can not deal with Collection and other beans.
	 * 
	 * @param queryPOJO
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	@SuppressWarnings("unchecked")
	public List<T> intelligentQuery(T queryPOJO) throws IllegalArgumentException, IllegalAccessException {
		StringBuilder hql = new StringBuilder("FROM ");
		hql.append(queryPOJO.getClass().getName()).append(" o WHERE ");
		List<Object> objects = new ArrayList<Object>();
		Field[] fields = queryPOJO.getClass().getDeclaredFields();
		int count = 0;
		for (int i = 0; i < fields.length; i++) {
			if (fields[i].isAnnotationPresent(Column.class) || fields[i].isAnnotationPresent(Id.class)) {
				fields[i].setAccessible(true);
				Object value = fields[i].get(queryPOJO);
				if (value != null) {
					if (count != 0) {
						hql.append(" AND ");
					}
					hql.append(" o." + fields[i].getName() + " = ?");
					objects.add(value);
					count++;
				}
			}
		}
		return (List<T>) getHibernateTemplate().find(hql.toString(), objects.toArray());

	}

	public List<?> findByFields(Class<T> entityClass, List<?> params, String... fields) {
		StringBuilder hql = new StringBuilder("FROM " + entityClass.getName() + " WHERE ");
		for (int i = 0; i < fields.length; i++) {
			String prefix = "";
			if (i != 0) {
				prefix = " AND ";
			}
			hql.append(prefix + fields[i] + " = ? ");
		}
		return findByHql(hql.toString(), params.toArray());
	}

	public Page findByPageAndHql(Page page, String hql, List<?> params) {
		StringBuilder queryString = new StringBuilder(hql);
		if (StringUtils.isNotBlank(page.getOrderByField())) {
			queryString.append(" order by " + page.getOrderByField() + " ");
			queryString.append(page.getOrderType() == null ? " asc " : page.getOrderType());
		}
		Session session = getHibernateTemplate().getSessionFactory().getCurrentSession();
		Query query = session.createQuery(queryString.toString());
		query.setFirstResult((page.getPageNumber() - 1) * page.getPageSize());
		query.setMaxResults(page.getPageSize());
		if (params != null) {
			int index = 0;
			for (Object param : params) {
				query.setParameter(index++, param);
			}
		}
		int count = countByHql(hql, params);
		page.setTotalCount(count);
		page.setList(query.list());
		if (count > 0 && page.getList().size() == 0) {
			page.setPageNumber(1);
			page = findByPageAndHql(page, hql, params);
		}
		return page;
	}

	public Page findByPageAndSql(Page page, String sql, List<?> params) {
		if (page == null) {
			throw new BusinessException("QueryPage can not be null !");
		}
		StringBuilder queryString = new StringBuilder(sql);
		if (StringUtils.isNotBlank(page.getOrderByField())) {
			queryString.append(" order by " + page.getOrderByField() + " ");
			queryString.append(page.getOrderType() == null ? " asc " : page.getOrderType());
		}
		Session session = getHibernateTemplate().getSessionFactory().getCurrentSession();
		SQLQuery query = session.createSQLQuery(queryString.toString());
		if (params != null) {
			int i = 0;
			for (Object param : params) {
				query.setParameter(i++, param);
			}
		}
		query.setFirstResult((page.getPageNumber() - 1) * page.getPageSize());
		query.setMaxResults(page.getPageSize());
		@SuppressWarnings("unchecked")
		List<T> results = query.list();
		page.setList(results);
		page.setTotalCount(results.size());
		return page;
	}

	public Page findByConditions(Class<T> entityClass, Page page, List<String> conditions, List<?> params) {
		if (page == null) {
			throw new BusinessException("QueryPage can not be null !");
		}
		StringBuilder hql = new StringBuilder("from " + entityClass.getName());
		if (conditions != null && !conditions.isEmpty()) {
			hql.append(" where ");
			for (int i = 0; i < conditions.size(); i++) {
				if (i == 0) {
					hql.append(conditions.get(i));
				} else
					hql.append(" and " + conditions.get(i));
			}
		}
		if (StringUtils.isNotBlank(page.getOrderByField())) {
			hql.append(" order by " + page.getOrderByField() + " ");
			hql.append(page.getOrderType() == null ? " asc " : page.getOrderType());
		}
		Session session = getHibernateTemplate().getSessionFactory().getCurrentSession();
		Query query = session.createQuery(hql.toString());
		int count = countByConditions(entityClass, conditions, params);
		query.setFirstResult((page.getPageNumber() - 1) * page.getPageSize());
		query.setMaxResults(page.getPageSize());
		page.setTotalCount(count);
		if (params != null) {
			int index = 0;
			for (Object param : params) {
				query.setParameter(index++, param);
			}
		}
		page.setList(query.list());
		if (count > 0 && page.getList().size() == 0) {
			page.setPageNumber(1);
			page = findByConditions(entityClass, page, conditions, params);
		}
		return page;
	}

	@SuppressWarnings("unchecked")
	public List<T> findByConditionsWithOutPage(Class<T> entityClass, List<String> conditions, List<?> params) {
		StringBuilder hql = new StringBuilder("from " + entityClass.getName());
		if (conditions != null && !conditions.isEmpty()) {
			hql.append(" where ");
			for (int i = 0; i < conditions.size(); i++) {
				if (i == 0) {
					hql.append(conditions.get(i));
				} else
					hql.append(" and " + conditions.get(i));
			}
		}
		Session session = getHibernateTemplate().getSessionFactory().getCurrentSession();
		Query query = session.createQuery(hql.toString());
		if (params != null) {
			int index = 0;
			for (Object param : params) {
				query.setParameter(index++, param);
			}
		}
		return query.list();
	}

	public Integer countByHql(String hql, List<?> params) {
		int fromAt = hql.toLowerCase().indexOf("from");
		if (StringUtils.isNotBlank(hql.substring(0, fromAt))) {
			hql = hql.substring(fromAt);
		}
		StringBuilder countString = new StringBuilder("SELECT COUNT(*) ").append(hql);
		Session session = getHibernateTemplate().getSessionFactory().getCurrentSession();
		Query countQuery = session.createQuery(countString.toString());
		if (params != null) {
			int index = 0;
			for (Object param : params) {
				countQuery.setParameter(index++, param);
			}
		}
		return ((Long) countQuery.list().get(0)).intValue();
	}

	public Integer countByConditions(Class<T> entityClass, List<String> conditions, List<?> params) {
		StringBuilder countString = new StringBuilder("SELECT COUNT(*) FROM ").append(entityClass.getName());
		if (conditions != null && !conditions.isEmpty()) {
			countString.append(" where ");
			for (int i = 0; i < conditions.size(); i++) {
				if (i == 0) {
					countString.append(conditions.get(i));
				} else
					countString.append(" and " + conditions.get(i));
			}
		}
		Session session = getHibernateTemplate().getSessionFactory().getCurrentSession();
		Query countQuery = session.createQuery(countString.toString());
		if (params != null) {
			int index = 0;
			for (Object param : params) {
				countQuery.setParameter(index++, param);
			}
		}
		return ((Long) countQuery.list().get(0)).intValue();
	}


	public Object executeByHql(String hql, List<?> params) {
		Session session = getHibernateTemplate().getSessionFactory().getCurrentSession();
		Query countQuery = session.createQuery(hql);
		if (params != null) {
			int index = 0;
			for (Object param : params) {
				countQuery.setParameter(index++, param);
			}
		}
		return countQuery.executeUpdate();
	}


	public Session getCurrentSession() {
		return getHibernateTemplate().getSessionFactory().getCurrentSession();
	}

}
