package uk.ac.cam.signups.forms;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.ws.rs.FormParam;
import javax.ws.rs.PathParam;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.cam.cl.dtg.teaching.api.FormValidationException;
import uk.ac.cam.cl.dtg.teaching.api.NotificationApi.NotificationApiWrapper;
import uk.ac.cam.cl.dtg.teaching.api.NotificationException;
import uk.ac.cam.cl.dtg.teaching.hibernate.HibernateUtil;
import uk.ac.cam.signups.models.Event;
import uk.ac.cam.signups.models.Row;
import uk.ac.cam.signups.models.Slot;
import uk.ac.cam.signups.models.User;
import uk.ac.cam.signups.util.Signapps;
import uk.ac.cam.signups.util.Util;

public class FillSlot {

	@PathParam("obfuscatedId")
	private String obfuscatedId;

	@FormParam("slot_crsids[]")
	private String[] crsids;

	@FormParam("slot_ids[]")
	private int[] slotIds;

	@FormParam("types[]")
	private int[] typeIds;

	@FormParam("original_types[]")
	private int[] original_typeIds;

	@FormParam("row_ids[]")
	private int[] rowIds;

	@FormParam("slot_original_crsids[]")
	private String[] original_crsids;

	private Event event;

	private Logger logger = LoggerFactory.getLogger(FillSlot.class);

	private static final int MODE_DO_NOTHING = 0;
	private static final int MODE_REMOVE_OWNER = 1;
	private static final int MODE_CREATE_OWNER = 2;
	private static final int MODE_REPLACE_OWNER = 3;

	private Query createQuery(Session session, int mode, int slotID,
			User newOwner, User previousOwner) {
		switch (mode) {
		case MODE_REMOVE_OWNER:
			return session
					.createQuery(
							"update Slot set user_crsid = null where id = :slotid and (user_crsid = :original_crsid or user_crsid is null)")
					.setInteger("slotid", slotID)
					.setString("original_crsid", previousOwner.getCrsid());
		case MODE_CREATE_OWNER:
			return session
					.createQuery(
							"update Slot set user_crsid = :crsid where id = :slotid and user_crsid is null")
					.setInteger("slotid", slotID)
					.setString("crsid", newOwner.getCrsid());
		case MODE_REPLACE_OWNER:
			return session
					.createQuery(
							"update Slot set user_crsid = :crsid where id = :slotid and (user_crsid = :original_crsid or user_crsid is null)")
					.setInteger("slotid", slotID)
					.setString("crsid", newOwner.getCrsid())
					.setString("original_crsid", previousOwner.getCrsid());
		}
		throw new Error("Invalid mode");
	}

	public List<String> handle(NotificationApiWrapper apiWrapper,
			User currentUser) throws FormValidationException {
		Session session = HibernateUtil.getInstance().getSession();

		List<String> errors = new LinkedList<String>();

		int length = slotIds.length;
		if (crsids.length != length)
			throw new FormValidationException(
					"Malformed submission.  Number of slots != number of submitted users");
		if (original_crsids.length != length)
			throw new FormValidationException(
					"Malformed submission.  Number of slots != number of cached original values");

		int rowLength = rowIds.length;
		if (typeIds.length != rowLength)
			throw new FormValidationException(
					"Malformed submission. Number of row ids != number of types chosen");
		if (original_typeIds.length != rowLength)
			throw new FormValidationException(
					"Malformed submission. Number of row ids != number of cached original types");

		
		this.event = (Event) session.createCriteria(Event.class)
				.add(Restrictions.eq("obfuscatedId", obfuscatedId))
				.uniqueResult();

		handleSlotChanges(apiWrapper, currentUser, session, errors);
		handleTypeChanges(apiWrapper, currentUser, session, errors);
		
		return errors;
	}

	private void handleTypeChanges(NotificationApiWrapper apiWrapper,
			User currentUser, Session session, List<String> errors) throws FormValidationException {
		for(int i=0;i<typeIds.length;++i) {
			int typeID = typeIds[i];
			int originalTypeID = original_typeIds[i];
			int rowID = rowIds[i];
			
			Row row = (Row)session.byId(Row.class).load(rowID);
			
			if (!row.getEvent().getObfuscatedId().equals(obfuscatedId)) {
				throw new FormValidationException("Row "+rowID+" is not associated with event "+obfuscatedId);
			}
			
			if (typeID != 0 && typeID != originalTypeID) {
				Query q = session
				.createQuery(
						"update Row set type_id = :type_id where id = :rowid and (type_id = :original_type_id or type_id = :type_id or type_id is null)")
				.setInteger("type_id",typeID)
				.setInteger("rowid",rowID)
				.setInteger("original_type_id", originalTypeID);
				int count = q.executeUpdate();
				if (count == 0) {
					errors.add("Not changing type for "+row.getTime()+" since it has already been changed to a different value.");
				}
				else if (count > 1) {
					throw new Error("Duplicate primary key in database (table rows)");
				}
			}
			
		}
	}

	private void handleSlotChanges(NotificationApiWrapper apiWrapper,
			User currentUser, Session session, List<String> errors)
			throws FormValidationException, Error {
		for (int i = 0; i < crsids.length; ++i) {
			User previousOwner = original_crsids[i].equals("") ? null : User
					.registerUser(original_crsids[i]);
			User newOwner = crsids[i].equals("") ? null : User
					.registerUser(crsids[i]);
			int slotID = slotIds[i];

			int mode = MODE_DO_NOTHING;
			if (previousOwner != null && newOwner == null) {
				mode = MODE_REMOVE_OWNER;
			} else if (previousOwner == null && newOwner != null) {
				mode = MODE_CREATE_OWNER;
			} else if (previousOwner == null && newOwner == null) {
				mode = MODE_DO_NOTHING;
			} else if (!newOwner.equals(previousOwner)) {
				mode = MODE_REPLACE_OWNER;
			}

			if (mode == MODE_DO_NOTHING) {
				continue;
			}

			Slot slot = (Slot) session.byId(Slot.class).load(slotID);
			Row row = slot.getRow();
			if (!row.getEvent().getObfuscatedId().equals(obfuscatedId)) {
				throw new FormValidationException("Slot " + slotID
						+ " is not associated with event " + obfuscatedId);
			}

			if (slot.isUpdateable(currentUser)) {
				Query q = createQuery(session, mode, slotID, newOwner,
						previousOwner);
				int count = q.executeUpdate();
				if (count == 0) {
					switch (mode) {
					case MODE_REMOVE_OWNER:
						errors.add("Not removing " + previousOwner.getCrsid()
								+ " because they are no longer in the slot at "
								+ row.getTime());
						break;
					case MODE_CREATE_OWNER:
						errors.add("Not allocating slot at " + row.getTime()
								+ " to " + newOwner.getCrsid()
								+ " because it is no longer empty");
						break;
					case MODE_REPLACE_OWNER:
						errors.add("Not replacing "
								+ previousOwner.getCrsid()
								+ " with "
								+ newOwner.getCrsid()
								+ " in slot at "
								+ row.getTime()
								+ " because it has now been assigned to someone else");
					}
				} else if (count > 1) {
					throw new Error(
							"Duplicate primary key in database (slots table)");
				} else {
					Set<String> relatedCrsids = new HashSet<String>();
					relatedCrsids.add(event.getOwner().getCrsid());
					if (previousOwner != null)
						relatedCrsids.add(previousOwner.getCrsid());
					if (newOwner != null)
						relatedCrsids.add(newOwner.getCrsid());
					relatedCrsids.add(currentUser.getCrsid());
					String message = null;
					switch (mode) {
					case MODE_REMOVE_OWNER:
						message = previousOwner.getNameCrsid()
								+ " was removed from event " + event.getTitle();
						if (!previousOwner.equals(currentUser)) {
							message += " by " + currentUser.getNameCrsid();
						}
						break;
					case MODE_CREATE_OWNER:
						message = newOwner.getNameCrsid()
								+ " was added to event " + event.getTitle();
						if (!newOwner.equals(currentUser)) {
							message += " by " + currentUser.getNameCrsid();
						}
						break;
					case MODE_REPLACE_OWNER:
						message = newOwner.getNameCrsid() + " replaced "
								+ previousOwner.getCrsid() + " at event "
								+ event.getTitle();
						if (!newOwner.equals(currentUser)) {
							message += " by " + currentUser.getNameCrsid();
						}
					}

					try {
						apiWrapper.createNotificationWithForeignId(message,
								Signapps.APPLICATION_NAME, "events/" + obfuscatedId,
								Util.join(relatedCrsids, ","), "signapp-"
										+ event.getId());
					} catch (NotificationException e) {
						logger.error("Notification could not be saved.");
						logger.error(e.getMessage());
					}
				}
			} else {
				errors.add("Not changing slot at " + row.getTime()
						+ " which you cannot update");
			}
		}
	}
}
