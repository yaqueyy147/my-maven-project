package com.syx.maven.page;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;

public class Page implements Serializable {

	private static final long serialVersionUID = 4700033191997928860L;
	public static final int DEFAULT_PAGE_SIZE = 15;//默认每页记录数15
	
	private int pageNo;//当前页
	private int pageSize = DEFAULT_PAGE_SIZE;//每页记录数
	private int start;//本页在数据集中的起始位置
	private int totalCount;//总记录数
	private int totalPage;//总页数
	private List data;//查询的结果集
	
	/**
	 * 构造函数 - 空页
	 */
	public Page() {
		this(new ArrayList(), 0, 0, DEFAULT_PAGE_SIZE);
	}

	/**
	 * 构造函数 - 默认
	 * 
	 * @param result
	 *            本页包含的数据
	 * @param start
	 *            本页在数据集中的起始位置
	 * @param totalCount
	 *            数据库记录数
	 * @param pageSize
	 *            页容量
	 */
	public Page(List results, int start, int totalCount, int pageSize) {
		if (results == null) {
			this.data = new ArrayList();
		} else if (results.size() <= pageSize) {
			this.data = results;
		} else {
			this.data = new ArrayList();
			for (int i = start; (i < start + pageSize) && (i < results.size()); i++)
				this.data.add(results.get(i));
		}
		this.start = start;
		this.pageNo = start / pageSize + 1;
		this.pageSize = (pageSize > 0) ? pageSize : DEFAULT_PAGE_SIZE;
		this.totalCount = totalCount;
		this.totalPage = (totalCount + pageSize - 1) / pageSize;
	}
	
	/**
	 * @param results
	 *            结果集
	 * @param pageNo
	 *            页数 从0开始
	 */
	public Page(List results, int pageNo) {
		this(results, pageNo, DEFAULT_PAGE_SIZE);
	}
	
	/**
	 * @param results
	 *            结果集
	 * @param pageNo
	 *            页数 从1开始
	 * @param pageSize
	 *            页容量
	 */
	public Page(List results, int pageNo, int pageSize) {
		this(results, getStartOfPage(pageNo, pageSize), results.size(),
				pageSize);
	}
	
	public int getPageNo() {
		return pageNo;
	}
	public void setPageNo(int pageNo) {
		this.pageNo = pageNo;
	}
	public int getPageSize() {
		return pageSize;
	}
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}
	
	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getTotalCount() {
		return totalCount;
	}
	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}
	public int getTotalPage() {
		return totalPage;
	}
	public void setTotalPage(int totalPage) {
		this.totalPage = totalPage;
	}
	public List getData() {
		return data;
	}
	public void setData(List data) {
		this.data = data;
	}
	
	
	/**
	 * 获取指定页在数据集中的起始位置(从0开始)
	 * 
	 * @param pageNo
	 * @param pageSize
	 * @return
	 */
	public static int getStartOfPage(int pageNo, int pageSize) {
		pageSize = (pageSize > 0) ? pageSize : DEFAULT_PAGE_SIZE;
		return Math.max((pageNo - 1) * pageSize, 0);
	}
	
//	/**
//	 * 获取最大页/总页数
//	 */
//	public int getLastPageNo() {
//		return (totalCount + pageSize - 1) / pageSize;
//	}
	
	/**
	 * 是否是首页
	 * 
	 * @return
	 */
	public boolean isFirstPage() {
		return this.getPageNo() <= 1;
	}

	/**
	 * 是否是末页
	 * 
	 * @return
	 */
	public boolean isLastPage() {
		return this.getPageNo() >= this.getTotalPage();
	}

	/**
	 * 下一页页码
	 * 
	 * @return
	 */
	public int getNextPage() {
		if (isLastPage()) {
			return this.getPageNo();
		}
		return this.getPageNo() + 1;
	}

	/**
	 * 上一页页码
	 * @return
	 */
	public int getPrePage() {
		if (isFirstPage()) {
			return this.getPageNo();
		}
		return this.getPageNo() - 1;
	}

	/**
	 * 判断是否有下一页
	 */
	public boolean hasNextPage() {
		return this.getPageNo() < this.getTotalPage();
	}
	
	/**
	 * 判断是否有前一页
	 */
	public boolean hasPrevPage() {
		return this.getPageNo() > 1;
	}
	
	/**
	 * @see Object#toString()
	 */
	public String toString() {
		return new ToStringBuilder(this).append("totalCount",
				this.getTotalCount()).append("currentPageNo",
				this.getPageNo()).append("lastPageNo",
				this.getTotalPage()).append("hasPrevPage", this.hasPrevPage())
				.append("result", this.getData()).toString();
	}
}
