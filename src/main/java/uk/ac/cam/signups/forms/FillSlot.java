package uk.ac.cam.signups.forms;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.cam.signups.models.Row;
import uk.ac.cam.signups.models.Slot;
import uk.ac.cam.signups.models.Type;
import uk.ac.cam.signups.models.User;
import uk.ac.cam.signups.util.HibernateUtil;
import uk.ac.cam.signups.util.Util;

import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.FormParam;
import javax.ws.rs.PathParam;

public class FillSlot {
	@PathParam("id")
	int eventId;
	@FormParam("slot_crsids[]")
	String[] crsids;
	@FormParam("slot_ids[]")
	int[] slotIds;
	@FormParam("types[]")
	int[] typeIds;

	Logger log = LoggerFactory.getLogger(FillSlot.class);

	@SuppressWarnings("unchecked")
  public void handle(int id) {
		Session session = HibernateUtil.getTransactionSession();

		Set<Integer> ids = new HashSet<Integer>();
		for (int slotId : slotIds) {
			ids.add(slotId);
		}
		
		List<Slot> slots = (List<Slot>) session
		    .createQuery(
		        "from Slot as slot where slot.row.event.id = :id and slot.row.calendar > :calendar")
		    .setParameter("id", id)
		    .setParameter("calendar", Calendar.getInstance()).list();

		int columnsSize = slots.get(0).getRow().getSlots().size();

		if (Util.getIds(slots).equals(ids)) {
			Slot slot;
			Row row;
			Type type;
			for (int i = 0; i < slotIds.length; i++) {
				User owner = User.registerUser(crsids[i]);
				if (owner == null && Util.findById(slots, slotIds[i]) == null)
					continue;
				slot = Util.findById(slots, slotIds[i]);
				slot.setOwner(owner);
				int typeId;
				session.update(slot);
				if ((slot.getRow().getEvent().getTypes().size() != 1)
				    && ((typeId = typeIds[i / columnsSize]) != 0)) {
					type = (Type) session
					    .createQuery(
					        "from Type as type where type.id = :type_id AND type.event.id = :id")
					    .setParameter("type_id", typeId).setParameter("id", id)
					    .uniqueResult();
					row = slot.getRow();
					row.setType(type);
					session.update(row);
				}
			}
		} else {
			// TODO unauthorized access
		}
	}
}
