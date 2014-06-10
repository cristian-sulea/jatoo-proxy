/*
 * Copyright (C) 2014 Cristian Sulea ( http://cristian.sulea.net )
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package jatoo.proxy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.MessageDigest;
import java.util.Properties;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

/**
 * A business object with all the properties representing a proxy, and
 * functionality to {@link #store()} and {@link #load()} to/from a file. The
 * password, if there is authentication required, will be encrypted before
 * storing and decrypted after loading.
 * 
 * @author <a href="http://cristian.sulea.net" rel="author">Cristian Sulea</a>
 * @version 1.1, June 6, 2014
 */
public class Proxy {

  /** The default value for the store file. */
  private static final File STORE_FILE = new File(new File(new File(System.getProperty("user.home")), ".jatoo"), "proxy.properties");
  static {
    STORE_FILE.getParentFile().mkdirs();
  }

  /** The keys of the properties. */
  private static final String STORE_FILE_ENABLED = "enabled";
  private static final String STORE_FILE_HOST = "host";
  private static final String STORE_FILE_PORT = "port";
  private static final String STORE_FILE_AUTHENTICATION = "authentication";
  private static final String STORE_FILE_USERNAME = "username";
  private static final String STORE_FILE_PASSWORD = "password";

  /** The properties for the encryption/decryption configuration. */
  private static final String CRYPTO_DIGEST_ALGORITHM = "SHA";
  private static final byte[] CRYPTO_DIGEST_UPDATE_INPUT = STORE_FILE.getName().getBytes();
  private static final String CRYPTO_KEY_ALGORITHM = "AES";
  private static final int CRYPTO_KEY_LEN = 16;
  private static final String CRYPTO_CIPHER_TRANSFORMATION = "AES/ECB/PKCS5Padding";
  private static final String CRYPTO_CHARSET = "UTF-8";

  /** The file where this BO will be stored. */
  private File storeFile = STORE_FILE;

  /** <code>True</code> if the proxy should be enabled. */
  private boolean enabled;

  /** The host name, or address, of the proxy server. */
  private String host;

  /** The port number of the proxy server. */
  private int port;

  /** <code>True</code> if the server requires authentication. */
  private boolean requiringAuthentication;

  /** The user name for the proxy server. */
  private String username;

  /** The user's password for the proxy server. */
  private String password;

  /**
   * Creates an empty proxy BO.
   */
  public Proxy() {}

  /**
   * Creates a new proxy BO with default values for all properties.
   * 
   * @param enabled
   *          <code>true</code> if the proxy should be enabled
   * @param host
   *          the host name, or address, of the proxy server
   * @param port
   *          the port number of the proxy server
   * @param requiringAuthentication
   *          <code>true</code> if the server requires authentication
   * @param username
   *          the user name
   * @param password
   *          the user's password
   */
  public Proxy(final boolean enabled, final String host, final int port, final boolean requiringAuthentication, final String username, final String password) {
    this.enabled = enabled;
    this.host = host;
    this.port = port;
    this.requiringAuthentication = requiringAuthentication;
    this.username = username;
    this.password = password;
  }

  /**
   * Creates a new proxy BO with default values for all properties. The proxy is
   * enabled (using the provided host and port) and requires authentication
   * (using the provided user name and password).
   * 
   * @param host
   *          the host name, or address, of the proxy server
   * @param port
   *          the port number of the proxy server
   * @param username
   *          the user name
   * @param password
   *          the user's password
   */
  public Proxy(final String host, final int port, final String username, final String password) {
    this(true, host, port, true, username, password);
  }

  /**
   * Creates a new proxy BO with default values for all properties. The proxy is
   * enabled (using the provided host and port) and does NOT requires
   * authentication.
   * 
   * @param host
   *          the host name, or address, of the proxy server
   * @param port
   *          the port number of the proxy server
   */
  public Proxy(final String host, final int port) {
    this(true, host, port, false, null, null);
  }

  /**
   * Stores the properties of this business object into the store file.
   * 
   * @throws GeneralSecurityException
   *           if the encryption fails
   * @throws IOException
   *           if writing to the store file fails
   */
  public final synchronized void store() throws GeneralSecurityException, IOException {

    Properties p = new Properties();

    p.setProperty(STORE_FILE_ENABLED, Boolean.toString(enabled));

    p.setProperty(STORE_FILE_HOST, host);
    p.setProperty(STORE_FILE_PORT, Integer.toString(port));

    p.setProperty(STORE_FILE_AUTHENTICATION, Boolean.toString(requiringAuthentication));

    if (username != null) {
      p.setProperty(STORE_FILE_USERNAME, username);
    }
    if (password != null) {
      p.setProperty(STORE_FILE_PASSWORD, encrypt(password));
    }

    p.storeToXML(new FileOutputStream(storeFile), null);
  }

  /**
   * Loads the stored properties into this business object.
   * 
   * @throws GeneralSecurityException
   *           if the decryption fails
   * @throws IOException
   *           if reading from the store file fails
   */
  public final synchronized void load() throws GeneralSecurityException, IOException {

    Properties p = new Properties();
    p.loadFromXML(new FileInputStream(storeFile));

    enabled = Boolean.parseBoolean(p.getProperty(STORE_FILE_ENABLED, "true"));

    host = p.getProperty(STORE_FILE_HOST);
    port = Integer.parseInt(p.getProperty(STORE_FILE_PORT));

    requiringAuthentication = Boolean.parseBoolean(p.getProperty(STORE_FILE_AUTHENTICATION, "true"));

    username = p.getProperty(STORE_FILE_USERNAME);
    password = p.getProperty(STORE_FILE_PASSWORD);
    if (password != null) {
      password = decrypt(password);
    }
  }

  /**
   * Encrypts the specified string.
   * 
   * @param string
   *          a {@link String} to be encrypted
   * 
   * @return encrypted value of the parameter as a {@link String}
   * 
   * @throws GeneralSecurityException
   *           if the encryption fails
   */
  private String encrypt(final String string) throws GeneralSecurityException {

    MessageDigest digest = MessageDigest.getInstance(CRYPTO_DIGEST_ALGORITHM);
    digest.update(CRYPTO_DIGEST_UPDATE_INPUT);

    Key key = new SecretKeySpec(digest.digest(), 0, CRYPTO_KEY_LEN, CRYPTO_KEY_ALGORITHM);

    Cipher cipher = Cipher.getInstance(CRYPTO_CIPHER_TRANSFORMATION);
    cipher.init(Cipher.ENCRYPT_MODE, key);

    byte[] input;

    try {
      input = string.getBytes(CRYPTO_CHARSET);
    } catch (UnsupportedEncodingException e) {
      throw new GeneralSecurityException("the " + CRYPTO_CHARSET + " charset is not supported", e);
    }

    byte[] encryptedData = cipher.doFinal(input);

    return DatatypeConverter.printBase64Binary(encryptedData);
  }

  /**
   * Decrypts the specified string.
   * 
   * @param string
   *          a {@link String} to be decrypted
   * 
   * @return decrypted value of the parameter as a {@link String}
   * 
   * @throws GeneralSecurityException
   *           if the decryption fails
   */
  private String decrypt(final String string) throws GeneralSecurityException {

    MessageDigest digest = MessageDigest.getInstance(CRYPTO_DIGEST_ALGORITHM);
    digest.update(CRYPTO_DIGEST_UPDATE_INPUT);

    Key key = new SecretKeySpec(digest.digest(), 0, CRYPTO_KEY_LEN, CRYPTO_KEY_ALGORITHM);

    Cipher cipher = Cipher.getInstance(CRYPTO_CIPHER_TRANSFORMATION);
    cipher.init(Cipher.DECRYPT_MODE, key);

    byte[] decryptedData = cipher.doFinal(DatatypeConverter.parseBase64Binary(string));

    try {
      return new String(decryptedData, CRYPTO_CHARSET);
    } catch (UnsupportedEncodingException e) {
      throw new GeneralSecurityException("the " + CRYPTO_CHARSET + " charset is not supported", e);
    }
  }

  /**
   * @return the storeFile
   */
  public final File getStoreFile() {
    return storeFile;
  }

  /**
   * @param storeFile
   *          the storeFile to set
   */
  public final void setStoreFile(final File storeFile) {
    this.storeFile = storeFile;
  }

  /**
   * @return the enabled
   */
  public final boolean isEnabled() {
    return enabled;
  }

  /**
   * @param enabled
   *          the enabled to set
   */
  public final void setEnabled(final boolean enabled) {
    this.enabled = enabled;
  }

  /**
   * @return the host
   */
  public final String getHost() {
    return host;
  }

  /**
   * @param host
   *          the host to set
   */
  public final void setHost(final String host) {
    this.host = host;
  }

  /**
   * @return the port
   */
  public final int getPort() {
    return port;
  }

  /**
   * @param port
   *          the port to set
   */
  public final void setPort(final int port) {
    this.port = port;
  }

  /**
   * @return the requiringAuthentication
   */
  public final boolean isRequiringAuthentication() {
    return requiringAuthentication;
  }

  /**
   * @param requiringAuthentication
   *          the requiringAuthentication to set
   */
  public final void setRequiringAuthentication(final boolean requiringAuthentication) {
    this.requiringAuthentication = requiringAuthentication;
  }

  /**
   * @return the username
   */
  public final String getUsername() {
    return username;
  }

  /**
   * @param username
   *          the username to set
   */
  public final void setUsername(final String username) {
    this.username = username;
  }

  /**
   * @return the password
   */
  public final String getPassword() {
    return password;
  }

  /**
   * @param password
   *          the password to set
   */
  public final void setPassword(final String password) {
    this.password = password;
  }

}
