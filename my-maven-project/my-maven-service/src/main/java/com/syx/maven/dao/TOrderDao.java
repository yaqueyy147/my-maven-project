package com.syx.maven.dao;

import org.springframework.stereotype.Repository;

import com.syx.maven.dao.hibernate.BaseHibernateDao;
import com.syx.maven.domain.TOrder;

@Repository("tOrderDao")
public class TOrderDao extends BaseHibernateDao<TOrder> {

}
