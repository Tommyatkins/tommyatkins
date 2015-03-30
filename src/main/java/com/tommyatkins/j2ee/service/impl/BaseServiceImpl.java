package com.tommyatkins.j2ee.service.impl;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;

import com.tommyatkins.j2ee.dao.BaseDao;
import com.tommyatkins.j2ee.entity.Page;
import com.tommyatkins.j2ee.exception.BusinessException;
import com.tommyatkins.j2ee.service.BaseService;

public class BaseServiceImpl<T> implements BaseService<T> {
	@Autowired
	protected BaseDao<T> baseDao;
	protected Class<T> entityClass;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public BaseServiceImpl() {
		Class clazz = getClass();
		Type type = clazz.getGenericSuperclass();
		if (type instanceof ParameterizedType) {
			ParameterizedType pType = (ParameterizedType) type;
			entityClass = (Class<T>) pType.getActualTypeArguments()[0];
		}
	}

	@Override
	public Serializable save(T t) {
		return baseDao.save(t);

	}

	@Override
	public void update(T t) {
		baseDao.update(t);

	}

	@Override
	public void delete(Serializable id) {
		baseDao.delete(entityClass, id);

	}

	@Override
	public T get(Serializable id) throws BusinessException {
		return baseDao.get(entityClass, id);
	}

	@Override
	public List<T> getAll() {
		return baseDao.getAll(entityClass);
	}

	@Override
	public List<?> findByHql(String hql, Object... objects) {
		return baseDao.findByHql(hql, objects);
	}

	@Override
	public List<?> findBySql(String sql, Object... objects) {
		return baseDao.findBySql(sql, objects);
	}

	@Override
	public int executeBySql(String sql, Object... objects) {
		return baseDao.executeBySql(sql, objects);
	}
	@Override
	public void executeByHql(String hql,List<?> params) {
		baseDao.executeByHql(hql,params);
	}
	
	@Override
	public void merge(T t) {
		baseDao.merge(t);
	}

	@Override
	public List<T> intelligentQuery(T queryPOJO) throws IllegalArgumentException, IllegalAccessException {
		return baseDao.intelligentQuery(queryPOJO);
	}

	@Override
	public List<?> findByFields(List<?> params, String... fields) {
		return baseDao.findByFields(entityClass, params, fields);
	}

	@Override
	public Page findByPageAndHql(Page page, String hql, List<?> params) {
		return baseDao.findByPageAndHql(page, hql, params);
	}

	@Override
	public Page findByPageAndSql(Page page, String sql, List<?> params) {
		return baseDao.findByPageAndSql(page, sql, params);
	}

	@Override
	public List<T> loadAll() {
		return baseDao.loadAll(entityClass);
	}

	@Override
	public Page findByConditions(Page page, List<String> conditions, List<?> params) {
		return baseDao.findByConditions(entityClass, page, conditions, params);
	}

	@Override
	public List<T> findByConditionsWithOutPage(List<String> conditions, List<?> params) {
		// TODO Auto-generated method stub
		return baseDao.findByConditionsWithOutPage(entityClass, conditions, params);
	}

	@Override
	public Integer countByHql(String hql, List<?> params) {
		// TODO Auto-generated method stub
		return baseDao.countByHql(hql, params);
	}

	@Override
	public Integer countByConditions(List<String> conditions, List<?> params) {
		return baseDao.countByConditions(entityClass, conditions, params);
	}

	@Override
	public Session getCurrentSession() {
		return baseDao.getCurrentSession();
	}

}
