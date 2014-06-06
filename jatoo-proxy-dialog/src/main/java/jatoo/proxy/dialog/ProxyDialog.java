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

import jatoo.proxy.Proxy;
import jatoo.proxy.ProxyUtils;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Helper class to ease the Internet proxy configuration through a
 * {@link JDialog}.
 * 
 * @author <a href="http://cristian.sulea.net" rel="author">Cristian Sulea</a>
 * @version 2.1, June 6, 2014
 */
public class ProxyDialog {

  /** the logger */
  private static final Log logger = LogFactory.getLog(ProxyDialog.class);

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

      Proxy proxy = new Proxy();
      proxy.load();

      dialogPanel.setProxyEnabled(proxy.isEnabled());
      dialogPanel.setHost(proxy.getHost());
      dialogPanel.setPort(proxy.getPort());
      dialogPanel.setProxyRequiringAuthentication(proxy.isRequiringAuthentication());
      dialogPanel.setUsername(proxy.getUsername());
      dialogPanel.setPassword(proxy.getPassword());
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

          Proxy proxy = new Proxy();

          proxy.setEnabled(dialogPanel.isProxyEnabled());
          proxy.setUsername(dialogPanel.getUsername());
          proxy.setPassword(dialogPanel.getPassword());
          proxy.setRequiringAuthentication(dialogPanel.isProxyRequiringAuthentication());
          proxy.setHost(dialogPanel.getHost());
          proxy.setPort(dialogPanel.getPort());

          proxy.store();
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

}
