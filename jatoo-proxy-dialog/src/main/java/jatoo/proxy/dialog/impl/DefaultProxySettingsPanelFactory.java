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

package jatoo.proxy.dialog.impl;

import jatoo.proxy.dialog.ProxyDialogPanel;
import jatoo.proxy.dialog.ProxyDialogPanelFactory;

/**
 * Default implementation for {@link ProxyDialogPanelFactory}.
 * 
 * @author <a href="http://cristian.sulea.net" rel="author">Cristian Sulea</a>
 * @version 2.0, June 4, 2014
 */
public class DefaultProxySettingsPanelFactory implements ProxyDialogPanelFactory {

  @Override
  public int getPriority() {
    return 0;
  }

  @Override
  public ProxyDialogPanel createDialogPanel() {
    return new DefaultProxySettingsPanel();
  }

}
