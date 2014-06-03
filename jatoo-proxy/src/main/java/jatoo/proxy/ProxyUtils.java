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

import java.net.Authenticator;
import java.net.InetSocketAddress;

/**
 * A collection of utility methods to ease the work with proxies.
 * 
 * @author Cristian Sulea ( http://cristian.sulea.net )
 * @version 3.1, June 3, 2014
 */
public final class ProxyUtils {

  private static final String SYSTEM_PROPERTY_PROXY_SET = "proxySet";
  private static final String SYSTEM_PROPERTY_PROXY_HOST = "proxyHost";
  private static final String SYSTEM_PROPERTY_PROXY_PORT = "proxyPort";

  private static final String SYSTEM_PROPERTY_PROXY_SET_VALUE_TRUE = "true";

  private ProxyUtils() {}

  public static void setProxy(final String host, final int port, final Authenticator authenticator) {

    System.setProperty(SYSTEM_PROPERTY_PROXY_SET, SYSTEM_PROPERTY_PROXY_SET_VALUE_TRUE);
    System.setProperty(SYSTEM_PROPERTY_PROXY_HOST, host);
    System.setProperty(SYSTEM_PROPERTY_PROXY_PORT, Integer.toString(port));

    Authenticator.setDefault(authenticator);
  }

  public static void setProxy(final String host, final int port, final String username, final char[] password) {
    setProxy(host, port, new ProxyAuthenticator(username, password));
  }

  public static void setProxy(final String host, final int port, final String username, final String password) {
    setProxy(host, port, new ProxyAuthenticator(username, password.toCharArray()));
  }

  public static void setProxy(final String host, final int port) {
    setProxy(host, port, null);
  }

  public static void setProxy(final InetSocketAddress proxy, final String username, final String password) {
    setProxy(proxy.getHostName(), proxy.getPort(), new ProxyAuthenticator(username, password.toCharArray()));
  }

  public static void setProxy(final InetSocketAddress proxy) {
    setProxy(proxy.getHostName(), proxy.getPort(), null);
  }

  /**
   * Checks if a proxy have been set.
   * 
   * @return <code>true</code> if a proxy have been set, <code>false</code>
   *         otherwise (or if {@link #removeProxy()} have been used)
   */
  public static boolean isProxySet() {

    boolean isProxySet = true;

    isProxySet = isProxySet && System.getProperty(SYSTEM_PROPERTY_PROXY_SET) != null;
    isProxySet = isProxySet && System.getProperty(SYSTEM_PROPERTY_PROXY_HOST) != null;
    isProxySet = isProxySet && System.getProperty(SYSTEM_PROPERTY_PROXY_PORT) != null;

    return isProxySet;
  }

  /**
   * Removes any previously proxy set.
   */
  public static void removeProxy() {

    System.getProperties().remove(SYSTEM_PROPERTY_PROXY_SET);
    System.getProperties().remove(SYSTEM_PROPERTY_PROXY_HOST);
    System.getProperties().remove(SYSTEM_PROPERTY_PROXY_PORT);

    Authenticator.setDefault(null);
  }

}
