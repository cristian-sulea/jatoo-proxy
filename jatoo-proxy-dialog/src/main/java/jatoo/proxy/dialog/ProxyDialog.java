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
 * Helper class to ease the internet proxy configuration through a
 * {@link JDialog}.
 * 
 * @author <a href="http://cristian.sulea.net" rel="author">Cristian Sulea</a>
 * @version 2.0, June 4, 2014
 */
public class ProxyDialog {

  /** the logger */
  private static final Log logger = LogFactory.getLog(ProxyDialog.class);

  /**
   * The file that will store the proxy properties.
   */
  private static final File FILE_PROXY_PROPERTIES = new File(new File(new File(System.getProperty("user.home")), ".jatoo"), "proxy.properties");

  static {
    FILE_PROXY_PROPERTIES.getParentFile().mkdirs();
  }

  /**
   * Panel factory loaded through {@link ServiceLoader}.
   */
  private static ProxyDialogPanelFactory PROXY_DIALOG_PANEL_FACTORY;

  static {

    ServiceLoader<ProxyDialogPanelFactory> dialogPanelFactoryLoader = ServiceLoader.load(ProxyDialogPanelFactory.class);
    Iterator<ProxyDialogPanelFactory> dialogPanelFactoryIterator = dialogPanelFactoryLoader.iterator();

    List<ProxyDialogPanelFactory> dialogPanelFactoryList = new ArrayList<>();

    while (dialogPanelFactoryIterator.hasNext()) {
      dialogPanelFactoryList.add(dialogPanelFactoryIterator.next());
    }

    Collections.sort(dialogPanelFactoryList, new Comparator<ProxyDialogPanelFactory>() {
      public int compare(ProxyDialogPanelFactory f1, ProxyDialogPanelFactory f2) {
        return f2.getPriority() - f1.getPriority();
      }
    });

    PROXY_DIALOG_PANEL_FACTORY = dialogPanelFactoryList.get(0);
  }

  /**
   * Shows the dialog in the center of the screen.
   */
  public static synchronized void show() {
    show(null);
  }

  /**
   * Shows the dialog relative to the specified owner.
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
    // the panel

    final ProxyDialogPanel dialogPanel = PROXY_DIALOG_PANEL_FACTORY.createDialogPanel();

    try {

      Properties p = new Properties();
      p.loadFromXML(new FileInputStream(FILE_PROXY_PROPERTIES));

      dialogPanel.setProxyEnabled(Boolean.parseBoolean(p.getProperty("enabled", "true")));
      dialogPanel.setUsername(p.getProperty("username"));
      dialogPanel.setPassword(decryptString(p.getProperty("password")));
      dialogPanel.setProxyRequiringAuthentication(Boolean.parseBoolean(p.getProperty("authentication", "true")));
      dialogPanel.setHost(p.getProperty("host"));
      dialogPanel.setPort(Integer.parseInt(p.getProperty("port")));
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

          if (dialogPanel.isProxyEnabled()) {

            if (dialogPanel.isProxyRequiringAuthentication()) {
              ProxyUtils.setProxy(dialogPanel.getHost(), dialogPanel.getPort(), dialogPanel.getUsername(), dialogPanel.getPassword());
            } else {
              ProxyUtils.setProxy(dialogPanel.getHost(), dialogPanel.getPort());
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

          p.setProperty("enabled", Boolean.toString(dialogPanel.isProxyEnabled()));
          p.setProperty("username", dialogPanel.getUsername());
          p.setProperty("password", encryptString(dialogPanel.getPassword()));
          p.setProperty("authentication", Boolean.toString(dialogPanel.isProxyRequiringAuthentication()));
          p.setProperty("host", dialogPanel.getHost());
          p.setProperty("port", Integer.toString(dialogPanel.getPort()));

          p.storeToXML(new FileOutputStream(FILE_PROXY_PROPERTIES), null);
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

    dialogPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    JPanel buttonsGroup = new JPanel(new GridLayout(1, 2, 5, 5));
    buttonsGroup.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    buttonsGroup.add(okButton);
    buttonsGroup.add(cancelButton);

    JPanel buttonsPanel = new JPanel(new BorderLayout());
    buttonsPanel.add(buttonsGroup, BorderLayout.LINE_END);

    JPanel contentPane = new JPanel(new BorderLayout());
    contentPane.add(dialogPanel, BorderLayout.CENTER);
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
   * Encrypts the specified string using {@link #FILE_PROXY_PROPERTIES} name as
   * pass code.
   */
  private static String encryptString(String string) throws GeneralSecurityException, UnsupportedEncodingException {

    MessageDigest digest = MessageDigest.getInstance("SHA");
    digest.update(FILE_PROXY_PROPERTIES.getName().getBytes());

    Key key = new SecretKeySpec(digest.digest(), 0, 16, "AES");

    Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
    cipher.init(Cipher.ENCRYPT_MODE, key);

    byte[] encryptedData = cipher.doFinal(string.getBytes("UTF-8"));

    return DatatypeConverter.printBase64Binary(encryptedData);
  }

  /**
   * Decrypts the specified string using {@link #FILE_PROXY_PROPERTIES} name as
   * pass code.
   */
  private static String decryptString(String string) throws GeneralSecurityException, UnsupportedEncodingException {

    MessageDigest digest = MessageDigest.getInstance("SHA");
    digest.update(FILE_PROXY_PROPERTIES.getName().getBytes());

    Key key = new SecretKeySpec(digest.digest(), 0, 16, "AES");

    Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
    cipher.init(Cipher.DECRYPT_MODE, key);

    byte[] decryptedData = cipher.doFinal(DatatypeConverter.parseBase64Binary(string));

    return new String(decryptedData, "UTF-8");
  }

}
