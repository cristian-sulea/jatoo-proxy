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

import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;

import org.junit.Assert;
import org.junit.Test;

public class ProxyUtilsTest {

  @Test
  public void test1() throws Throwable {

    //
    // set proxy and everything should be ok

    ProxyUtils.setProxy("192.168.1.29", 8080, "csu", new String(new byte[] { 48, 48, 55, 46, 99, 115, 117, 46, 116, 53, 51, 48 }));

    checkConnection();

    //
    // remove proxy and only SocketTimeoutException is accepted

    ProxyUtils.removeProxy();

    try {
      checkConnection();
      Assert.fail("proxy was removed, so check connection should fail with SocketTimeoutException");
    } catch (SocketTimeoutException e) {}
  }

  @Test
  public void test2() throws Throwable {

    ProxyUtils.setAndStoreProxy("192.168.1.29", 8080, "csu", new String(new byte[] { 48, 48, 55, 46, 99, 115, 117, 46, 116, 53, 51, 48 }));
    ProxyUtils.removeProxy();

    ProxyUtils.setLastStoredProxy();

    checkConnection();
  }

  private void checkConnection() throws Throwable {
    URLConnection connection = new URL("https://www.google.com").openConnection();
    connection.setConnectTimeout(3000);
    connection.getInputStream().close();
  }

}
