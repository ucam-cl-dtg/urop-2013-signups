package uk.ac.cam.signups.forms;

import org.hibernate.Session;

import java.util.List;
import java.util.Set;
import java.util.HashSet;

import uk.ac.cam.signups.models.Slot;
import uk.ac.cam.signups.models.User;
import uk.ac.cam.signups.util.HibernateUtil;
import uk.ac.cam.signups.util.Util;

import javax.ws.rs.PathParam;
import javax.ws.rs.FormParam;

public class FillSlot {
	@PathParam("id") int eventId;
	@FormParam("slot_crsids[]") String[] crsids;
	@FormParam("slot_ids[]") int[] slotIds;
	@FormParam("type_ids[]") int[] typeIds;
	
	public void handle(int id) {
		Session session = HibernateUtil.getTransactionSession();
		
		Set<Integer> ids = new HashSet<Integer>();
		for(int slotId: slotIds)
			ids.add(slotId);
		
		List<Slot> slots = (List<Slot>) session.createQuery("from Slot as slot where slot.row.event.id = :id").setParameter("id", id).list();
		
		if (ids.equals(Util.getIds(slots))) {
			for(int i = 0; i < slotIds.length; i++) {
				User owner = User.registerUser(crsids[i]);
				if (owner == null) continue;
				Slot slot = Util.findById(slots, slotIds[i]);
				slot.setOwner(owner);
				session.update(slot);
			}
		}
	}
}
