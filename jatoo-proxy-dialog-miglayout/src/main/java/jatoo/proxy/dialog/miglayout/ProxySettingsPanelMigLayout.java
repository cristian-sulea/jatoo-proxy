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

package jatoo.proxy.dialog.miglayout;

import jatoo.proxy.dialog.ProxySettingsPanel;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

/**
 * MigLayout implementation for {@link ProxySettingsPanel}.
 * 
 * @author Cristian Sulea ( http://cristian.sulea.net )
 * @version 1.0, May 7, 2014
 */
@SuppressWarnings("serial")
public class ProxySettingsPanelMigLayout extends ProxySettingsPanel {

	private JCheckBox enabledCheckBox;

	private JLabel hostLabel;
	private JTextField hostField;

	private JLabel portLabel;
	private JTextField portField;

	private JCheckBox requiringAuthenticationCheckBox;

	private JLabel usernameLabel;
	private JTextField usernameField;

	private JLabel passwordLabel;
	private JPasswordField passwordField;

	public ProxySettingsPanelMigLayout() {

		enabledCheckBox = new JCheckBox("Enabled", true);

		hostLabel = new JLabel("Host:");
		hostField = new JTextField(15);

		portLabel = new JLabel("Port:");
		portField = new JTextField(4);

		requiringAuthenticationCheckBox = new JCheckBox("Requires authentication", true);

		usernameLabel = new JLabel("Username:");
		usernameField = new JTextField(10);

		passwordLabel = new JLabel("Password:");
		passwordField = new JPasswordField(20);

		setLayout(new MigLayout("wrap 2, insets 0", "[right][grow]", ""));
		add(enabledCheckBox, "left, spanx");
		add(hostLabel);
		add(hostField);
		add(portLabel);
		add(portField);
		add(requiringAuthenticationCheckBox, "left, spanx");
		add(usernameLabel);
		add(usernameField);
		add(passwordLabel);
		add(passwordField);

		enabledCheckBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {

				if (enabledCheckBox.isSelected()) {

					hostLabel.setEnabled(true);
					hostField.setEnabled(true);

					portLabel.setEnabled(true);
					portField.setEnabled(true);

					requiringAuthenticationCheckBox.setEnabled(true);

					if (requiringAuthenticationCheckBox.isSelected()) {

						usernameLabel.setEnabled(true);
						usernameField.setEnabled(true);

						passwordLabel.setEnabled(true);
						passwordField.setEnabled(true);
					}

					else {

						usernameLabel.setEnabled(false);
						usernameField.setEnabled(false);

						passwordLabel.setEnabled(false);
						passwordField.setEnabled(false);
					}
				}

				else {

					hostLabel.setEnabled(false);
					hostField.setEnabled(false);

					portLabel.setEnabled(false);
					portField.setEnabled(false);

					requiringAuthenticationCheckBox.setEnabled(false);

					usernameLabel.setEnabled(false);
					usernameField.setEnabled(false);

					passwordLabel.setEnabled(false);
					passwordField.setEnabled(false);
				}
			}
		});

		requiringAuthenticationCheckBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				usernameField.setEnabled(requiringAuthenticationCheckBox.isSelected());
				passwordField.setEnabled(requiringAuthenticationCheckBox.isSelected());
			}
		});
	}

	@Override
	public boolean isProxyEnabled() {
		return enabledCheckBox.isSelected();
	}

	@Override
	public void setProxyEnabled(boolean enabled) {
		enabledCheckBox.setSelected(enabled);
	}

	@Override
	public String getHost() {
		return hostField.getText();
	}

	@Override
	public void setHost(String host) {
		hostField.setText(host);
	}

	@Override
	public int getPort() {
		return Integer.parseInt(portField.getText());
	}

	@Override
	public void setPort(int port) {
		portField.setText(Integer.toString(port));
	}

	@Override
	public boolean isProxyRequiringAuthentication() {
		return requiringAuthenticationCheckBox.isSelected();
	}

	@Override
	public void setProxyRequiringAuthentication(boolean requiringAuthentication) {
		requiringAuthenticationCheckBox.setSelected(requiringAuthentication);
	}

	@Override
	public String getUsername() {
		return usernameField.getText();
	}

	@Override
	public void setUsername(String username) {
		usernameField.setText(username);
	}

	@Override
	public String getPassword() {
		return new String(passwordField.getPassword());
	}

	@Override
	public void setPassword(String password) {
		passwordField.setText(password);
	}

}
