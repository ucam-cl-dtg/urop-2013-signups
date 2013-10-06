package uk.ac.cam.signups.models;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import uk.ac.cam.cl.dtg.ldap.LDAPObjectNotFoundException;
import uk.ac.cam.cl.dtg.ldap.LDAPPartialQuery;
import uk.ac.cam.cl.dtg.teaching.hibernate.HibernateUtil;
import uk.ac.cam.signups.util.ImmutableMappableExhaustedPair;

public class Dos {
	private List<String> instIDs;
	
	public Dos(List<String> instIDs) {
		this.instIDs = instIDs;
	}

	@SuppressWarnings("unchecked")
	public ImmutableMappableExhaustedPair<User> getPupils(int page) {
		int offset = 10 * page;

		Session session = HibernateUtil.getInstance().getSession();
		Criteria q = session.createCriteria(User.class)
		    .add(Restrictions.in("instID", instIDs)).addOrder(Order.asc("crsid"));

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
			List<HashMap<String,String>> crsids = new ArrayList<HashMap<String,String>>();
			for(String instID: instIDs)
				crsids.addAll(LDAPPartialQuery.partialUserByCrsidInInst(q, instID));

			return crsids;
		} catch (LDAPObjectNotFoundException e) {
			return new ArrayList<HashMap<String,String>>();
		}
	}

	public ImmutableMappableExhaustedPair<User> getPupils(int page, String partial) {
		int offset = 10 * page;

		Session session = HibernateUtil.getInstance().getSession();
		Criteria query = session
		    .createCriteria(User.class)
		    .add(
		        Restrictions.and(Restrictions.in("instID", instIDs),
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
		return instIDs.contains(u.getInstID());
	}
}
