package com.tommyatkins.j2ee.entity;

import java.util.List;

import com.alibaba.fastjson.JSON;

public class Page {
	public enum OrderType {
		asc, desc
	}

	private Integer pageNumber = 1;// 当前页码
	private Integer pageSize = 15;// 每页记录数
	private Integer totalCount = 0;// 总记录数
	private Integer pageCount = 0;// 总页数
	private String orderByField;// 排序字段
	private OrderType orderType = OrderType.asc;// 排序方式
	private List<?> list;// 数据List

	public Integer getPageNumber() {
		return pageNumber;
	}

	public Page setPageNumber(Integer pageNumber) {
		this.pageNumber = pageNumber;
		return this;
	}

	public Integer getPageSize() {
		return pageSize;
	}

	public Page setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
		return this;
	}

	public Integer getTotalCount() {
		return totalCount;
	}

	public Page setTotalCount(Integer totalCount) {
		this.totalCount = totalCount;
		return this;
	}

	public Integer getPageCount() {
		return pageCount;
	}

	public Page setPageCount(Integer pageCount) {
		this.pageCount = pageCount;
		return this;
	}

	public String getOrderByField() {
		return orderByField;
	}

	public Page setOrderByField(String orderByField) {
		this.orderByField = orderByField;
		return this;
	}

	public OrderType getOrderType() {
		return orderType;
	}

	public Page setOrderType(OrderType orderType) {
		this.orderType = orderType;
		return this;
	}

	public List<?> getList() {
		return list;
	}

	public Page setList(List<?> list) {
		this.list = list;
		return this;
	}

	@Override
	public String toString() {
		return JSON.toJSONString(this);
	}

}
