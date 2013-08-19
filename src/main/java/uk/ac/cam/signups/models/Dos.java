package uk.ac.cam.signups.models;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import uk.ac.cam.signups.util.HibernateUtil;

import java.util.List;

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
	
	public Dos() {}
	
	public static Dos findByCrsid(String crsid) {
		Session session = HibernateUtil.getTransactionSession();
		Criteria q = session.createCriteria(Dos.class).add(Restrictions.eq("crsid", crsid));
		
		try {
			return (Dos) q.uniqueResult();
		} catch (NonUniqueResultException e) {
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
  public List<User> getPupils() {
		Session session = HibernateUtil.getTransactionSession();
		Criteria q = session.createCriteria(User.class).add(Restrictions.eq("instID", instID)).addOrder(Order.asc("crsid"));
		return (List<User>) q.list();
	}
	
	public boolean isMyPupil(User u) {
		return instID.equals(u.getInstID());
	}
}


