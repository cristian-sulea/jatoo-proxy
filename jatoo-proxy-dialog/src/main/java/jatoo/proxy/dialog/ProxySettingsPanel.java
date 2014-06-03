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

package jatoo.proxy.dialog;

import javax.swing.JPanel;

/**
 * {@link ProxySettingsDialog} panel.
 * 
 * @author Cristian Sulea ( http://cristian.sulea.net )
 * @version 2.0, May 8, 2014
 */
@SuppressWarnings("serial")
public abstract class ProxySettingsPanel extends JPanel {

	public abstract boolean isProxyEnabled();

	public abstract void setProxyEnabled(boolean enabled);

	public abstract String getHost();

	public abstract void setHost(String host);

	public abstract int getPort();

	public abstract void setPort(int port);

	public abstract boolean isProxyRequiringAuthentication();

	public abstract void setProxyRequiringAuthentication(boolean requiringAuthentication);

	public abstract String getUsername();

	public abstract void setUsername(String username);

	public abstract String getPassword();

	public abstract void setPassword(String password);

}
