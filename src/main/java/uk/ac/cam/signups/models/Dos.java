package uk.ac.cam.signups.models;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import uk.ac.cam.cl.dtg.ldap.LDAPObjectNotFoundException;
import uk.ac.cam.cl.dtg.ldap.LDAPPartialQuery;
import uk.ac.cam.signups.util.HibernateUtil;
import uk.ac.cam.signups.util.ImmutableMappableExhaustedPair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Table;

@Entity
@Table(name = "DOSES")
public class Dos {
	@Id
	private String crsid;
	private String instID;

	public Dos() {
	}

	public static Dos findByCrsid(String crsid) {
		Session session = HibernateUtil.getTransactionSession();
		Criteria q = session.createCriteria(Dos.class).add(
		    Restrictions.eq("crsid", crsid));

		try {
			return (Dos) q.uniqueResult();
		} catch (NonUniqueResultException e) {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public ImmutableMappableExhaustedPair<User> getPupils(int page) {
		int offset = 10 * page;

		Session session = HibernateUtil.getTransactionSession();
		Criteria q = session.createCriteria(User.class)
		    .add(Restrictions.eq("instID", instID)).addOrder(Order.asc("crsid"));

		Criteria mainq = q.setMaxResults(10).setFirstResult(offset);

		List<User> users = mainq.list();

		Boolean exhausted = false;
		if (users.size() % 10 != 0) {
			exhausted = true;
		} else if (q.setMaxResults(1).setFirstResult(offset + 10).list().size() < 1) {
			exhausted = true;
		}

		return new ImmutableMappableExhaustedPair<User>(users, exhausted);
	}
	
	public List<HashMap<String,String>> getPupilCRSIDs(String q) {
		try {
			return LDAPPartialQuery.partialUserByCrsidInInst(q, this.instID);
		} catch (LDAPObjectNotFoundException e) {
			return new ArrayList<HashMap<String,String>>();
		}
	}

	public ImmutableMappableExhaustedPair<User> getPupils(int page, String partial) {
		int offset = 10 * page;

		Session session = HibernateUtil.getTransactionSession();
		Criteria query = session
		    .createCriteria(User.class)
		    .add(
		        Restrictions.and(Restrictions.eq("instID", instID),
		            Restrictions.ilike("crsid", partial + "%")))
		    .addOrder(Order.asc("crsid"));
		
		@SuppressWarnings("unchecked")
    List<User> users = (List<User>) query.setMaxResults(10).setFirstResult(offset).list();
		
		Boolean exhausted = false;
		if (users.size() % 10 != 0) {
			exhausted = true;
		} else if (query.setMaxResults(1).setFirstResult(offset + 10).list().size() < 1) {
			exhausted = true;
		}
		
		return new ImmutableMappableExhaustedPair<User>(users, exhausted);
	}

	public boolean isMyPupil(User u) {
		return instID.equals(u.getInstID());
	}
}
