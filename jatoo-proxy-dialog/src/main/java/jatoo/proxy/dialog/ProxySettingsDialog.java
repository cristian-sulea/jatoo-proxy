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

import jatoo.proxy.ProxyUtils;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.ServiceLoader;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.xml.bind.DatatypeConverter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Proxy Settings
 * 
 * @author Cristian Sulea ( http://cristian.sulea.net )
 * @version 1.1, May 8, 2014
 */
public class ProxySettingsDialog {

	/**
	 * The Logger
	 */
	private static final Log logger = LogFactory.getLog(ProxySettingsDialog.class);

	/**
	 * Settings File ( in user folder / .jatoo )
	 */
	private static final File PROXY_SETTINGS_FILE = new File(new File(new File(System.getProperty("user.home")), ".jatoo"), "proxy.settings");

	static {
		PROXY_SETTINGS_FILE.getParentFile().mkdirs();
	}

	/**
	 * Panel Factory loaded through {@link ServiceLoader}.
	 */
	private static ProxySettingsPanelFactory PROXY_SETTINGS_PANEL_FACTORY;

	static {

		ServiceLoader<ProxySettingsPanelFactory> settingsPanelFactoryLoader = ServiceLoader.load(ProxySettingsPanelFactory.class);
		Iterator<ProxySettingsPanelFactory> settingsPanelFactoryIterator = settingsPanelFactoryLoader.iterator();

		List<ProxySettingsPanelFactory> settingsPanelFactoryList = new ArrayList<>();

		while (settingsPanelFactoryIterator.hasNext()) {
			settingsPanelFactoryList.add(settingsPanelFactoryIterator.next());
		}

		Collections.sort(settingsPanelFactoryList, new Comparator<ProxySettingsPanelFactory>() {
			public int compare(ProxySettingsPanelFactory f1, ProxySettingsPanelFactory f2) {
				return f2.getPriority() - f1.getPriority();
			}
		});

		PROXY_SETTINGS_PANEL_FACTORY = settingsPanelFactoryList.get(0);
	}

	/**
	 * Shows the Proxy Settings dialog in the center of the screen.
	 */
	public static synchronized void show() {
		show(null);
	}

	/**
	 * Shows the Proxy Settings dialog relative to the specified owner.
	 */
	public static synchronized void show(Component owner) {

		JDialog dialogTmp;

		if (owner == null) {
			dialogTmp = new JDialog();
		} else {
			dialogTmp = new JDialog(SwingUtilities.getWindowAncestor(owner));
		}

		final JDialog dialog = dialogTmp;

		//
		// settings panel

		final ProxySettingsPanel settingsPanel = PROXY_SETTINGS_PANEL_FACTORY.createProxySettingsPanel();

		try {

			Properties p = new Properties();
			p.loadFromXML(new FileInputStream(PROXY_SETTINGS_FILE));

			settingsPanel.setProxyEnabled(Boolean.parseBoolean(p.getProperty("enabled", "true")));
			settingsPanel.setUsername(p.getProperty("username"));
			settingsPanel.setPassword(decryptString(p.getProperty("password")));
			settingsPanel.setProxyRequiringAuthentication(Boolean.parseBoolean(p.getProperty("authentication", "true")));
			settingsPanel.setHost(p.getProperty("host"));
			settingsPanel.setPort(Integer.parseInt(p.getProperty("port")));
		}

		catch (FileNotFoundException e) {
			// do nothing, maybe is the first time and the file is missing
		}

		catch (Exception e) {
			logger.error("Failed to load the properties.", e);
		}

		//
		// buttons

		JButton okButton = new JButton("Ok");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {

				try {

					if (settingsPanel.isProxyEnabled()) {

						if (settingsPanel.isProxyRequiringAuthentication()) {
							ProxyUtils.setProxy(settingsPanel.getHost(), settingsPanel.getPort(), settingsPanel.getUsername(), settingsPanel.getPassword());
						} else {
							ProxyUtils.setProxy(settingsPanel.getHost(), settingsPanel.getPort());
						}
					}

					else {
						ProxyUtils.removeProxy();
					}

					dialog.dispose();
				}

				catch (Exception e) {
					JOptionPane.showMessageDialog(dialog, "Failed to set the proxy:\n" + e.toString());
					return;
				}

				try {

					Properties p = new Properties();

					p.setProperty("enabled", Boolean.toString(settingsPanel.isProxyEnabled()));
					p.setProperty("username", settingsPanel.getUsername());
					p.setProperty("password", encryptString(settingsPanel.getPassword()));
					p.setProperty("authentication", Boolean.toString(settingsPanel.isProxyRequiringAuthentication()));
					p.setProperty("host", settingsPanel.getHost());
					p.setProperty("port", Integer.toString(settingsPanel.getPort()));

					p.storeToXML(new FileOutputStream(PROXY_SETTINGS_FILE), null);
				}

				catch (Exception e) {
					logger.error("Failed to save the properties.", e);
				}
			}
		});

		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dialog.dispose();
			}
		});

		//
		// layout dialog

		settingsPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		JPanel buttonsGroup = new JPanel(new GridLayout(1, 2, 5, 5));
		buttonsGroup.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		buttonsGroup.add(okButton);
		buttonsGroup.add(cancelButton);

		JPanel buttonsPanel = new JPanel(new BorderLayout());
		buttonsPanel.add(buttonsGroup, BorderLayout.LINE_END);

		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.add(settingsPanel, BorderLayout.CENTER);
		contentPane.add(buttonsPanel, BorderLayout.PAGE_END);

		//
		// setup dialog

		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setTitle("Proxy Settings");
		dialog.setContentPane(contentPane);
		dialog.pack();
		dialog.setLocationRelativeTo(dialog.getOwner());
		dialog.setModal(true);

		//
		// and show

		dialog.setVisible(true);
	}

	/**
	 * Encrypts the specified string using {@link #PROXY_SETTINGS_FILE} name as
	 * pass code.
	 */
	private static String encryptString(String string) throws GeneralSecurityException, UnsupportedEncodingException {

		MessageDigest digest = MessageDigest.getInstance("SHA");
		digest.update(PROXY_SETTINGS_FILE.getName().getBytes());

		Key key = new SecretKeySpec(digest.digest(), 0, 16, "AES");

		Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, key);

		byte[] encryptedData = cipher.doFinal(string.getBytes("UTF-8"));

		return DatatypeConverter.printBase64Binary(encryptedData);
	}

	/**
	 * Decrypts the specified string using {@link #PROXY_SETTINGS_FILE} name as
	 * pass code.
	 */
	private static String decryptString(String string) throws GeneralSecurityException, UnsupportedEncodingException {

		MessageDigest digest = MessageDigest.getInstance("SHA");
		digest.update(PROXY_SETTINGS_FILE.getName().getBytes());

		Key key = new SecretKeySpec(digest.digest(), 0, 16, "AES");

		Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		cipher.init(Cipher.DECRYPT_MODE, key);

		byte[] decryptedData = cipher.doFinal(DatatypeConverter.parseBase64Binary(string));

		return new String(decryptedData, "UTF-8");
	}

}
