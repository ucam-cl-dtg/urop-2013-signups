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

## controllers (uk.ac.cam.signups.controllers)

## util (uk.ac.cam.signups.util)
### UserLookupManager
* Deals with all lookups through LDAP with assistance from LDAPProvider
* Looks up all user data and converts into correct format
* Stores all data looked up for future use
* Various return methods for whatever data is required 
* Uses singleton design pattern, only created once for the raven user session
### LDAPProvider
* Deals with raw LDAP queries
### HibernateSessionRequestFilter
* Deals with hibernate sessions
