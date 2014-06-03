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
import java.net.PasswordAuthentication;

/**
 * {@link Authenticator} implementation used by {@link ProxyUtils}.
 * 
 * @author Cristian Sulea ( http://cristian.sulea.net )
 * @version 1.1, June 3, 2014
 */
class ProxyAuthenticator extends Authenticator {

  /**
   * Username and password holder.
   */
  private PasswordAuthentication passwordAuthentication;

  /**
   * Creates a new {@link ProxyAuthenticator} object with the provided username
   * and password.
   * 
   * @param username
   *          proxy username
   * @param password
   *          proxy password
   */
  ProxyAuthenticator(final String username, final char[] password) {
    passwordAuthentication = new PasswordAuthentication(username, password);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.net.Authenticator#getPasswordAuthentication()
   */
  @Override
  protected PasswordAuthentication getPasswordAuthentication() {
    return passwordAuthentication;
  }

}
