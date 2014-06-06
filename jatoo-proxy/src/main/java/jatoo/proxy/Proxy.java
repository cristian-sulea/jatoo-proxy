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
 * @version 1.0, June 5, 2014
 */
public class Proxy {

  private static final File STORE_FILE = new File(new File(new File(System.getProperty("user.home")), ".jatoo"), "proxy.properties");
  private static final String STORE_FILE_ENABLED = "enabled";
  private static final String STORE_FILE_HOST = "host";
  private static final String STORE_FILE_PORT = "port";
  private static final String STORE_FILE_AUTHENTICATION = "authentication";
  private static final String STORE_FILE_USERNAME = "username";
  private static final String STORE_FILE_PASSWORD = "password";

  private static final String CRYPTO_DIGEST_ALGORITHM = "SHA";
  private static final byte[] CRYPTO_DIGEST_UPDATE_INPUT = STORE_FILE.getName().getBytes();
  private static final String CRYPTO_KEY_ALGORITHM = "AES";
  private static final int CRYPTO_KEY_LEN = 16;
  private static final String CRYPTO_CIPHER_TRANSFORMATION = "AES/ECB/PKCS5Padding";
  private static final String CRYPTO_CHARSET = "UTF-8";

  private File storeFile = STORE_FILE;

  private boolean enabled;
  private String host;
  private int port;
  private boolean requiringAuthentication;
  private String username;
  private String password;

  public Proxy() {}

  public Proxy(boolean enabled, String host, int port, boolean requiringAuthentication, String username, String password) {
    this.enabled = enabled;
    this.host = host;
    this.port = port;
    this.requiringAuthentication = requiringAuthentication;
    this.username = username;
    this.password = password;
  }

  public Proxy(String host, int port, String username, String password) {
    this(true, host, port, true, username, password);
  }

  public Proxy(String host, int port) {
    this(true, host, port, false, null, null);
  }

  public synchronized void store() throws GeneralSecurityException, UnsupportedEncodingException, IOException {

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

  public synchronized void load() throws GeneralSecurityException, UnsupportedEncodingException, IOException {

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
   * @throws UnsupportedEncodingException
   *           if the {@link #CRYPTO_CHARSET} is not supported
   */
  private String encrypt(String string) throws GeneralSecurityException, UnsupportedEncodingException {

    MessageDigest digest = MessageDigest.getInstance(CRYPTO_DIGEST_ALGORITHM);
    digest.update(CRYPTO_DIGEST_UPDATE_INPUT);

    Key key = new SecretKeySpec(digest.digest(), 0, CRYPTO_KEY_LEN, CRYPTO_KEY_ALGORITHM);

    Cipher cipher = Cipher.getInstance(CRYPTO_CIPHER_TRANSFORMATION);
    cipher.init(Cipher.ENCRYPT_MODE, key);

    byte[] encryptedData = cipher.doFinal(string.getBytes(CRYPTO_CHARSET));

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
   * @throws UnsupportedEncodingException
   *           if the {@link #CRYPTO_CHARSET} is not supported
   */
  private String decrypt(String string) throws GeneralSecurityException, UnsupportedEncodingException {

    MessageDigest digest = MessageDigest.getInstance(CRYPTO_DIGEST_ALGORITHM);
    digest.update(CRYPTO_DIGEST_UPDATE_INPUT);

    Key key = new SecretKeySpec(digest.digest(), 0, CRYPTO_KEY_LEN, CRYPTO_KEY_ALGORITHM);

    Cipher cipher = Cipher.getInstance(CRYPTO_CIPHER_TRANSFORMATION);
    cipher.init(Cipher.DECRYPT_MODE, key);

    byte[] decryptedData = cipher.doFinal(DatatypeConverter.parseBase64Binary(string));

    return new String(decryptedData, CRYPTO_CHARSET);
  }

  /**
   * @return the storeFile
   */
  public File getStoreFile() {
    return storeFile;
  }

  /**
   * @param storeFile
   *          the storeFile to set
   */
  public void setStoreFile(File storeFile) {
    this.storeFile = storeFile;
  }

  /**
   * @return the enabled
   */
  public boolean isEnabled() {
    return enabled;
  }

  /**
   * @param enabled
   *          the enabled to set
   */
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  /**
   * @return the host
   */
  public String getHost() {
    return host;
  }

  /**
   * @param host
   *          the host to set
   */
  public void setHost(String host) {
    this.host = host;
  }

  /**
   * @return the port
   */
  public int getPort() {
    return port;
  }

  /**
   * @param port
   *          the port to set
   */
  public void setPort(int port) {
    this.port = port;
  }

  /**
   * @return the requiringAuthentication
   */
  public boolean isRequiringAuthentication() {
    return requiringAuthentication;
  }

  /**
   * @param requiringAuthentication
   *          the requiringAuthentication to set
   */
  public void setRequiringAuthentication(boolean requiringAuthentication) {
    this.requiringAuthentication = requiringAuthentication;
  }

  /**
   * @return the username
   */
  public String getUsername() {
    return username;
  }

  /**
   * @param username
   *          the username to set
   */
  public void setUsername(String username) {
    this.username = username;
  }

  /**
   * @return the password
   */
  public String getPassword() {
    return password;
  }

  /**
   * @param password
   *          the password to set
   */
  public void setPassword(String password) {
    this.password = password;
  }

}
