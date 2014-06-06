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

import org.junit.Assert;
import org.junit.Test;

public class ProxyTest {

  @Test
  public void test() throws Exception {

    //
    // P1 (define)

    Proxy proxy1 = new Proxy("host", 8080, "username", "password");

    //
    // P2 (store)

    Proxy proxy2 = new Proxy();
    proxy2.setStoreFile(new File("target/proxy.properties"));

    proxy2.setEnabled(proxy1.isEnabled());

    proxy2.setHost(proxy1.getHost());
    proxy2.setPort(proxy1.getPort());

    proxy2.setRequiringAuthentication(proxy1.isRequiringAuthentication());
    proxy2.setUsername(proxy1.getUsername());
    proxy2.setPassword(proxy1.getPassword());

    proxy2.store();

    //
    // P3 (load)

    Proxy proxy3 = new Proxy();
    proxy3.setStoreFile(proxy2.getStoreFile());
    proxy3.load();

    //
    // asserts

    Assert.assertEquals(proxy1.getPassword(), proxy2.getPassword());
    Assert.assertEquals(proxy2.getPassword(), proxy3.getPassword());
    Assert.assertEquals(proxy3.getPassword(), proxy1.getPassword());
  }

}
