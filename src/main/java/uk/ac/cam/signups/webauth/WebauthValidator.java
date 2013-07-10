/* This file is part of the University of Cambridge Web Authentication
 * System Java Toolkit
 *
 * Copyright 2005 University of Cambridge
 *
 * This toolkit is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * The toolkit is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this toolkit; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA
 *
 * $Id: WebauthValidator.java,v 1.12 2005/03/31 15:10:07 jw35 Exp $
 *
 */

package uk.ac.cam.signups.webauth;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

/**
 * Implements a validator for authentication response message.
 * 
 * @version $Revision: 1.12 $ $Date: 2005/03/31 15:10:07 $
 */

public class WebauthValidator {

	private static final int MAX_VER = 2;
	private static final String SIGNATURE_SCHEME = "SHA1withRSA";

	private static final int DEFAULT_TIMEOUT = 30000;
	private static final int DEFAULT_MAX_SKEW = 500;
	private static final String DEFAULT_KEY_PREFIX = "webauth-pubkey";

	private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS z";

	private KeyStore keyStore;
	private int timeout = DEFAULT_TIMEOUT;
	private int maxSkew = DEFAULT_MAX_SKEW;
	private String keyPrefix = DEFAULT_KEY_PREFIX;

	/**
	 * Default constructor. The timeout for the resulting object is set to 30
	 * sec, the clock skew to 0.5s, and the key prefix to "webauth-pubkey".
	 * 
	 * <p>
	 * 
	 * The validator needs access to the public key (or occasionally keys)
	 * corresponding to the private key(s) being used by the WLS to sign
	 * responses. These keys must be available in certificates contained in the
	 * {@link java.security.KeyStore} passed to the constructor. These should be
	 * stored under aliases formed by concatenating the key prefix and the KeyID
	 * used to identify a key in a response - for example by default the key
	 * identified in a response by '2' should be stored under the alias
	 * 'webauth-pubkey2'. Note that for the correct operation of the system it
	 * is important that exactly the right set of keys are provided to the
	 * validator - new keys should be added before they start to be used by the
	 * WLS, and old keys MUST be removed once they are no longer in use since
	 * otherwise if compromised they would represent a security vulnerability.
	 * 
	 * <p>
	 * 
	 * Any KeyStore implementing the standard Java Crypto API can be used for
	 * this. The most common is the "JKS" store implemented in the "SUN" crypto
	 * provider. Such a KeyStore can either be initialised from a keystore file
	 * on disk maintained by the <tt>keytool</tt> utility, or can be initialised
	 * at run-time with certificates stored elsewhere. For example:
	 * 
	 * <pre>
	 * import java.security.KeyStore;
	 * import java.security.cert.Certificate;
	 * import java.security.cert.CertificateFactory;
	 * 
	 * KeyStore ks = KeyStore.getInstance("JKS");
	 * ks.load(null, new char[] {}); // Null InputStream, no password
	 * 
	 * CertificateFactory factory =
	 * CertificateFactory.getInstance("X.509");
	 * Certificate cert =
	 * factory.generateCertificate(new FileInputStream("pubkey2.crt"));
	 * ks.setCertificateEntry("webauth-pubkey2",cert);
	 * </pre>
	 * 
	 * @param k
	 *            A {@link java.security.KeyStore} containing the
	 *            currently-valid public keys for the authentication system.
	 */

	public WebauthValidator(KeyStore k) {
		keyStore = k;
	}

	/**
	 * Perform validation tests on a WebauthResponse. This involves:
	 * <ol>
	 * <li>Checking that an acceptable combination of parameters are present in
	 * the response.
	 * 
	 * <li>Checking that 'kid', if present, corresponds to a key currently being
	 * used by the WAA.
	 * 
	 * <li>Checking that the signature, if provided, matches the data supplied.
	 * 
	 * <li>Checking that the response is recent by comparing 'issue' with the
	 * supplied date. If the supplied date is not from a clock synchronised by
	 * NTP or a similar mechanism then an allowance must be made for the maximum
	 * expected clock skew.
	 * 
	 * <li>Checking that 'url' is consistent with that in the corresponding
	 * Request.
	 * 
	 * <li>Checking that 'auth' and/or 'sso' contain values that are consistent
	 * with those in the corresponding Requestg.
	 * </ol>
	 * 
	 * @param request
	 *            The {@link WebauthRequest WebauthRequest} object, the
	 *            submission of which to the login server resulted in the
	 *            WebauthResponse being validated. This is not required to be
	 *            the identical object, but it should contain the same values
	 *            for the following parameters as the object which did (or at
	 *            least could) have caused this response: ver, url, iact, aauth.
	 *            Note that the url in the request is only required to be a
	 *            prefix of the url in the response.
	 * 
	 * @param response
	 *            The {@link WebauthResponse WebauthResponse} object to be
	 *            validated
	 * 
	 * @throws WebauthException
	 *             if the response fails to validate
	 */

	public void validate(WebauthRequest request, WebauthResponse response)
			throws WebauthException {

		validate(request, response, new Date().getTime());

	}

	/**
	 * Alternate version of {@link #validate(WebauthRequest, WebauthResponse)
	 * validate} in which date on which validation is based can be specified.
	 * 
	 * @param request
	 *            See {@link #validate(WebauthRequest, WebauthResponse)}
	 * @param response
	 *            See {@link #validate(WebauthRequest, WebauthResponse)}
	 * @param date
	 *            The date on which validation should be based, expressed as the
	 *            number of milliseconds since January 1, 1970 GMT..
	 * 
	 * @throws WebauthException
	 *             if the response fails to validate
	 */

	public void validate(WebauthRequest request, WebauthResponse response,
			long date) throws WebauthException {

		check_parameters(response);
		check_protocol(request, response);
		check_url(request, response);
		check_status(response);
		check_time(response, date);
		check_iact(request, response);
		check_aauth(request, response);
		check_sig(response);

	}

	private void check_parameters(WebauthResponse response)
			throws WebauthException {

		if (response.get("ver").equals(""))
			throw new WebauthException("Protocol version number missing "
					+ "from response");

		if (response.get("status").equals(""))
			throw new WebauthException("Status code missing from response");

		if (response.get("issue").equals(""))
			throw new WebauthException("Issue date missing from response");

		if (response.get("id").equals(""))
			throw new WebauthException("Response ID missing");

		if (response.get("url").equals(""))
			throw new WebauthException("URL missing from response");

		if (response.get("principal").equals("")
				&& response.getInt("status") == WebauthResponse.SUCCESS)
			throw new WebauthException("Principal missing from "
					+ "status 200 response");

		if (response.get("auth").equals("") && response.get("sso").equals("")
				&& response.getInt("status") == WebauthResponse.SUCCESS)
			throw new WebauthException("No authentication type "
					+ "found in status 200 response");

		if (!response.get("auth").equals("") && !response.get("sso").equals(""))
			throw new WebauthException("Both first-hand and SSO auth tokens "
					+ "found in response");

		if ((response.get("kid").equals("") || response.get("sig").equals(""))
				&& response.getInt("status") == WebauthResponse.SUCCESS)
			throw new WebauthException("KeyID and/or signature missing from "
					+ "status 200 response");
	}

	private void check_protocol(WebauthRequest request, WebauthResponse response)
			throws WebauthException {

		if ((response.getInt("ver") > MAX_VER)
				|| (response.getInt("ver") > request.getInt("ver"))) {
			throw new WebauthException("Unacceptable protocol version ("
					+ response.get("ver") + ") in response");
		}
	}

	private void check_url(WebauthRequest request, WebauthResponse response)
			throws WebauthException {

		String requestURL = request.get("url");
		String responseURL = response.get("url");
		if (!responseURL.startsWith(requestURL)) {
			throw new WebauthException("URL in response (" + responseURL
					+ ") does not match expected URL (" + requestURL + ")");
		}

	}

	private void check_status(WebauthResponse response) throws WebauthException {

		int status = response.getInt("status");
		if (status != WebauthResponse.SUCCESS) {
			String msg = WebauthResponse.statusString(status);
			if (!response.get("msg").equals("")) {
				msg = msg + ": " + response.get("msg");
			}
			throw new WebauthException(msg);
		}

	}

	private void check_time(WebauthResponse response, long now)
			throws WebauthException {

		long issue = response.getDate("issue");

		// We require Issue <= Now + Skew

		if (issue > now + maxSkew) {
			SimpleDateFormat formater = new SimpleDateFormat(DATE_FORMAT);
			throw new WebauthException("Response apparently issued "
					+ "in the future; " + "issue time "
					+ formater.format(new Date(issue)) + " compared with "
					+ formater.format(new Date(now)));
		}

		// ... and that Now - Skew <= Issue + Timeout

		// Note that the actual moment of issue could have been up to
		// 1s after the issue time from the response message because
		// the latter only has second resolution and is (probably)
		// rounded down to the nearest second. As a result the timeout
		// applied could be upto 1s less than the value of
		// timeout. Since timeouts of less than about 30s are a bad
		// idea for other reasons it does not seem worth allowig for
		// this here.

		if (now - maxSkew > issue + timeout) {
			SimpleDateFormat formater = new SimpleDateFormat(DATE_FORMAT);
			throw new WebauthException("Response issued too long ago; "
					+ "issue time " + formater.format(new Date(issue))
					+ " compared with " + formater.format(new Date(now)));
		}

	}

	private void check_iact(WebauthRequest request, WebauthResponse response)
			throws WebauthException {

		if (request.get("iact").equalsIgnoreCase("yes")
				&& response.get("auth").equals("")) {
			throw new WebauthException("First-hand authentication required "
					+ "but not supplied");
		}

	}

	private void check_aauth(WebauthRequest request, WebauthResponse response)
			throws WebauthException {

		Collection<String> aauth = request.getColl("aauth");

		if (aauth == null || aauth.isEmpty())
			return;

		String auth = response.get("auth");
		Collection<String> sso = response.getColl("sso");
		for (String next : aauth) {
			if (auth.equals(next) || sso.contains(next)) {
				return;
			}
		}
		throw new WebauthException("No acceptable authentication types used");
	}

	private void check_sig(WebauthResponse response) throws WebauthException {

		try {
			Certificate cert = keyStore.getCertificate(keyPrefix
					+ response.get("kid"));
			if (cert == null) {
				throw new WebauthException("Failed to retrieve a key with "
						+ "alias " + keyPrefix + response.get("kid")
						+ " from the key store");
			}

			WebauthDecoder decoder = new WebauthDecoder();
			byte[] sigBytes = decoder.decodeBuffer(response.get("sig"));

			Signature signature = Signature.getInstance(SIGNATURE_SCHEME);
			signature.initVerify(cert);
			signature.update(response.getRawData().getBytes());

			if (!signature.verify(sigBytes)) {
				throw new WebauthException(
						"Unable to verify response signature");
			}
		} catch (KeyStoreException e) {
			throw new WebauthException("Validator keyStore object "
					+ "not correctly initialized");
		} catch (IOException e) {
			throw new WebauthException("Failed to decode signature string");
		} catch (NoSuchAlgorithmException e) {
			throw new WebauthException("No security provider implementing "
					+ "signature scheme " + SIGNATURE_SCHEME
					+ " available in this VM");
		} catch (InvalidKeyException e) {
			throw new WebauthException("Key with alias " + keyPrefix
					+ response.get("kid") + " in the key store is invalid");
		} catch (SignatureException e) {
			throw new WebauthException("Failed to verify signature - "
					+ "signature object is not initialized");
		}

	}

	/**
	 * Set the maximum expected transmission time for response messages, in
	 * milliseconds. A response recieved more than this time after it was issued
	 * will be considered invalid. The default is 30s, and since in some
	 * situations response transmition may require user intervention (for
	 * example to confirm a redirect) it should probably not be set much lower
	 * than this. However in an environment subject to network snooping this
	 * should be kept as short as realistically possible.
	 * 
	 * @param timeout
	 *            the integer value to which timeout should be set.
	 */

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	/**
	 * Get the maximum expected transmission time for response messages, in
	 * milliseconds. See {@link #setTimeout setTimeout} for details.
	 * 
	 * @return the integer value representing the timeout in milliseconds
	 */

	public int getTimeout() {
		return timeout;
	}

	/**
	 * Set the maximum expected difference between the clock supplying the date
	 * parameter for {@link #validate validate} and correct time, in
	 * milliseconds. The default is 0.5s. Given that exact clock synchronisation
	 * is near to impossible it should probably not be set significantly lower
	 * than this. In particular, at very low values there is likelyhood that
	 * even minor clock skew and/or rounding error will result in responses
	 * being rejected becasue the y appear to have been issued in the future.
	 * 
	 * @param maxSkew
	 *            the integer value of the maximum expected clock skew.
	 */

	public void setMaxSkew(int maxSkew) {
		this.maxSkew = maxSkew;
	}

	/**
	 * Get the maximum expected clock skew. See {@link #setMaxSkew setMaxSkew}
	 * for details
	 * 
	 * @return the integer value representing the maximum expected clock skew.
	 */

	public int getMaxSkew() {
		return maxSkew;
	}

	/**
	 * Set the string prefix used to identify the relevant public key in the key
	 * store. Keys must be available in the key store under an alias formed from
	 * this prefix and the key-id carried in the response message being
	 * validated.
	 * 
	 * @param keyPrefix
	 *            the prefix string
	 */

	public void setKeyPrefix(String keyPrefix) {
		this.keyPrefix = keyPrefix;
	}

	/**
	 * Get the maximum expected clock skew. See {@link #setKeyPrefix
	 * setKeyPrefix} for details
	 * 
	 * @return the prefix string
	 */

	public String getKeyPrefix() {
		return keyPrefix;
	}

}