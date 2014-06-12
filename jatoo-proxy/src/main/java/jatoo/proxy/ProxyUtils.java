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

import java.awt.Component;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.security.GeneralSecurityException;

/**
 * A collection of utility methods to ease the work with proxies.
 * 
 * @author <a href="http://cristian.sulea.net" rel="author">Cristian Sulea</a>
 * @version 6.0, June 10, 2014
 */
public final class ProxyUtils {

  /**
   * The host name, or address, of the proxy server. See: <a href=
   * "http://docs.oracle.com/javase/7/docs/api/java/net/doc-files/net-properties.html"
   * >http://docs.oracle.com/javase/7/docs/api/java/net/doc-files/net-properties
   * . html</a>
   */
  private static final String SYSTEM_PROPERTY_PROXY_HOST = "proxyHost";

  /**
   * The port number of the proxy server. See: <a href=
   * "http://docs.oracle.com/javase/7/docs/api/java/net/doc-files/net-properties.html"
   * >http://docs.oracle.com/javase/7/docs/api/java/net/doc-files/net-properties
   * . html</a>
   */
  private static final String SYSTEM_PROPERTY_PROXY_PORT = "proxyPort";

  /**
   * Utility classes (classes that contain only static methods or fields in
   * their API) do not have a public constructor.
   */
  private ProxyUtils() {}

  /**
   * Configures the proxy with the provided host, port and authenticator.
   * 
   * @param host
   *          the host name, or address, of the proxy server
   * @param port
   *          the port number of the proxy server
   * @param authenticator
   *          the authentication object
   */
  public static void setProxy(final String host, final int port, final Authenticator authenticator) {

    System.setProperty(SYSTEM_PROPERTY_PROXY_HOST, host);
    System.setProperty(SYSTEM_PROPERTY_PROXY_PORT, Integer.toString(port));

    Authenticator.setDefault(authenticator);
  }

  /**
   * Configures the proxy with the provided host, port, username and password.
   * 
   * @param host
   *          the host name, or address, of the proxy server
   * @param port
   *          the port number of the proxy server
   * @param username
   *          the user name
   * @param password
   *          the user's password (as a {@link String})
   */
  public static void setProxy(final String host, final int port, final String username, final String password) {
    setProxy(host, port, new ProxyAuthenticator(username, password.toCharArray()));
  }

  /**
   * Configures the proxy with the provided host, port, username and password.
   * 
   * @param host
   *          the host name, or address, of the proxy server
   * @param port
   *          the port number of the proxy server
   * @param username
   *          the user name
   * @param password
   *          the user's password (as a char array)
   */
  public static void setProxy(final String host, final int port, final String username, final char[] password) {
    setProxy(host, port, new ProxyAuthenticator(username, password));
  }

  /**
   * Configures the proxy (no authentication) with the provided host and port.
   * 
   * @param host
   *          the host name, or address, of the proxy server
   * @param port
   *          the port number of the proxy server
   */
  public static void setProxy(final String host, final int port) {
    setProxy(host, port, null);
  }

  /**
   * Configures the proxy with the provided host and port (as
   * {@link InetSocketAddress} object), username and password.
   * 
   * @param proxy
   *          the host and port (as {@link InetSocketAddress} object
   * @param username
   *          the user name
   * @param password
   *          the user's password (as a {@link String})
   */
  public static void setProxy(final InetSocketAddress proxy, final String username, final String password) {
    setProxy(proxy.getHostName(), proxy.getPort(), new ProxyAuthenticator(username, password.toCharArray()));
  }

  /**
   * Configures the proxy (no authentication) with the provided host and port
   * (as {@link InetSocketAddress} object).
   * 
   * @param proxy
   *          the host and port (as {@link InetSocketAddress} object
   */
  public static void setProxy(final InetSocketAddress proxy) {
    setProxy(proxy.getHostName(), proxy.getPort(), null);
  }

  /**
   * Configures the proxy with the provided host, port, username and password.
   * The provided data is stored into a file for later use (for example with the
   * method {@link #setStoredProxy()}).
   * 
   * @param host
   *          the host name, or address, of the proxy server
   * @param port
   *          the port number of the proxy server
   * @param username
   *          the user name
   * @param password
   *          the user's password (as a {@link String})
   * 
   * @throws GeneralSecurityException
   *           if encryption of the store file fails
   * @throws IOException
   *           if writing to the store file fails
   */
  public static void setAndStoreProxy(final String host, final int port, final String username, final String password) throws GeneralSecurityException, IOException {
    setProxy(host, port, username, password);
    new Proxy(host, port, username, password).store();
  }

  /**
   * Configures the proxy with the provided host, port, username and password.
   * The provided data is stored into a file for later use (for example with the
   * method {@link #setStoredProxy()}).
   * 
   * @param host
   *          the host name, or address, of the proxy server
   * @param port
   *          the port number of the proxy server
   * @param username
   *          the user name
   * @param password
   *          the user's password (as a char array)
   * 
   * @throws GeneralSecurityException
   *           if encryption of the store file fails
   * @throws IOException
   *           if writing to the store file fails
   */
  public static void setAndStoreProxy(final String host, final int port, final String username, final char[] password) throws GeneralSecurityException, IOException {
    setAndStoreProxy(host, port, username, new String(password));
  }

  /**
   * Configures the proxy with the previously stored data.
   * 
   * @throws GeneralSecurityException
   *           if decryption of the store file fails
   * @throws IOException
   *           if reading from the store file fails
   */
  public static void setStoredProxy() throws GeneralSecurityException, IOException {

    Proxy proxy = new Proxy();
    proxy.load();

    if (proxy.isEnabled()) {

      if (proxy.isRequiringAuthentication()) {
        setProxy(proxy.getHost(), proxy.getPort(), proxy.getUsername(), proxy.getPassword());
      }

      else {
        setProxy(proxy.getHost(), proxy.getPort());
      }
    }
  }

  /**
   * Checks if a proxy have been set.
   * 
   * @return <code>true</code> if a proxy have been set, <code>false</code>
   *         otherwise (or if {@link #removeProxy()} have been used)
   */
  public static boolean isProxySet() {

    boolean isProxySet = true;

    isProxySet = isProxySet && System.getProperty(SYSTEM_PROPERTY_PROXY_HOST) != null;
    isProxySet = isProxySet && System.getProperty(SYSTEM_PROPERTY_PROXY_PORT) != null;

    return isProxySet;
  }

  /**
   * Removes any previously proxy set.
   */
  public static void removeProxy() {

    System.getProperties().remove(SYSTEM_PROPERTY_PROXY_HOST);
    System.getProperties().remove(SYSTEM_PROPERTY_PROXY_PORT);

    Authenticator.setDefault(null);
  }

  /**
   * A shortcut for <code>jatoo.proxy.dialog.ProxyDialog#show()</code> method.
   * If <code>jatoo-proxy-dialog</code> is not in classpath then a
   * {@link RuntimeException} is thrown.
   * 
   * @throws UnsupportedOperationException
   *           if <code>jatoo-proxy-dialog</code> is not in classpath or if the
   *           invocation fails
   */
  public static void showProxyDialog() throws UnsupportedOperationException {

    try {
      Class.forName("jatoo.proxy.dialog.ProxyDialog").getMethod("show").invoke(null);
    }

    catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
      throw new UnsupportedOperationException("failed to invoke #show() method", e);
    }

    catch (ClassNotFoundException e) {
      throw new UnsupportedOperationException("jatoo-proxy-dialog is not in classpath", e);
    }
  }

  /**
   * A shortcut for <code>jatoo.proxy.dialog.ProxyDialog#show(Component)</code>
   * method. If <code>jatoo-proxy-dialog</code> is not in classpath then a
   * {@link RuntimeException} is thrown.
   * 
   * @param owner
   *          the {@code Component} from which the dialog is displayed
   * 
   * @throws UnsupportedOperationException
   *           if <code>jatoo-proxy-dialog</code> is not in classpath or if the
   *           invocation fails
   */
  public static void showProxyDialog(final Component owner) throws UnsupportedOperationException {

    try {
      Class.forName("jatoo.proxy.dialog.ProxyDialog").getMethod("show", Component.class).invoke(null, owner);
    }

    catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
      throw new UnsupportedOperationException("failed to invoke #show(owner) method", e);
    }

    catch (ClassNotFoundException e) {
      throw new UnsupportedOperationException("jatoo-proxy-dialog is not in classpath", e);
    }
  }

}
