# signups
=================
System to create and signup for events such as supervisions, ticking sessions<br/>
###Features:
* Event creation page
* Event signup page
* Deadlines
* Groups

###Database structure:
* Table USERS
* Table DEADLINES
* Table EVENTS
(TODO)

## models (uk.ac.cam.signups.models)
### User
### Event
### Group
### Deadline 
### Row
### Slot
### Type

## controllers (uk.ac.cam.signups.controllers)

### Home page controller
* Path: /
* Shows users info

### Event controller
* Path: 

### Group controller
* Path: groups/
* Gets users current groups and displays them 
* Controls creation of, editing and deleting groups

### Deadline controller

## helpers (uk.ac.cam.signups.helpers)
### LDAPQueryHelper

## util (uk.ac.cam.signups.util)
### UserLookupManager

### LDAPProvider

### HibernateSessionRequestFilter

