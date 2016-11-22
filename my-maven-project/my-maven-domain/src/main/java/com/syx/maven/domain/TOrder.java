package com.syx.maven.domain;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "t_order")
public class TOrder implements Serializable {

	private static final long serialVersionUID = -1318250485814432438L;
	private String orderId;
	private String salesmanId;
	private String customerId;
	private Date createTime;
	private double totalPriceSalesman;
	private double totalPriceCustomer;
	private double invoicePrice;
	private String orderStatus;
	private String comment;
	private String spare1;
	private String spare2;
	private String spare3;
	private String spare4;
	private String clearingForm;
	
	public TOrder() {}

	public TOrder(String orderId, String salesmanId, String customerId, Date createTime, double totalPriceSalesman,
			double totalPriceCustomer, double invoicePrice, String orderStatus, String comment, String spare1,
			String spare2, String spare3, String spare4, String clearingForm) {
		this.orderId = orderId;
		this.salesmanId = salesmanId;
		this.customerId = customerId;
		this.createTime = createTime;
		this.totalPriceSalesman = totalPriceSalesman;
		this.totalPriceCustomer = totalPriceCustomer;
		this.invoicePrice = invoicePrice;
		this.orderStatus = orderStatus;
		this.comment = comment;
		this.spare1 = spare1;
		this.spare2 = spare2;
		this.spare3 = spare3;
		this.spare4 = spare4;
		this.clearingForm = clearingForm;
	}

	@Id
	@Column(name = "order_id",length = 32)
	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	@Column(name = "salesman_id")
	public String getSalesmanId() {
		return salesmanId;
	}

	public void setSalesmanId(String salesmanId) {
		this.salesmanId = salesmanId;
	}

	@Column(name = "customer_id")
	public String getCustomerId() {
		return customerId;
	}

	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "create_time")
	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	@Column(name = "total_price_salesman")
	public double getTotalPriceSalesman() {
		return totalPriceSalesman;
	}

	public void setTotalPriceSalesman(double totalPriceSalesman) {
		this.totalPriceSalesman = totalPriceSalesman;
	}


	@Column(name = "total_price_customer")
	public double getTotalPriceCustomer() {
		return totalPriceCustomer;
	}

	public void setTotalPriceCustomer(double totalPriceCustomer) {
		this.totalPriceCustomer = totalPriceCustomer;
	}

	@Column(name = "invoice_price")
	public double getInvoicePrice() {
		return invoicePrice;
	}

	public void setInvoicePrice(double invoicePrice) {
		this.invoicePrice = invoicePrice;
	}

	@Column(name = "order_status")
	public String getOrderStatus() {
		return orderStatus;
	}

	public void setOrderStatus(String orderStatus) {
		this.orderStatus = orderStatus;
	}

	@Column(name = "comment_")
	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	@Column(name = "spare1")
	public String getSpare1() {
		return spare1;
	}

	public void setSpare1(String spare1) {
		this.spare1 = spare1;
	}

	@Column(name = "spare2")
	public String getSpare2() {
		return spare2;
	}

	public void setSpare2(String spare2) {
		this.spare2 = spare2;
	}

	@Column(name = "spare3")
	public String getSpare3() {
		return spare3;
	}

	public void setSpare3(String spare3) {
		this.spare3 = spare3;
	}

	@Column(name = "spare4")
	public String getSpare4() {
		return spare4;
	}

	public void setSpare4(String spare4) {
		this.spare4 = spare4;
	}

	@Column(name = "结算方式")
	public String getClearingForm() {
		return clearingForm;
	}

	public void setClearingForm(String clearingForm) {
		this.clearingForm = clearingForm;
	}
	
}
