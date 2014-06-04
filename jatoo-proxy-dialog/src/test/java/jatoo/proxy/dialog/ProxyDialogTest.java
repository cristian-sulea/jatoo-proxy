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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.UIManager;

/**
 * {@link ProxyDialog} test class.
 * 
 * @author <a href="http://cristian.sulea.net" rel="author">Cristian Sulea</a>
 * @version 3.1, June 4, 2014
 */
public class ProxyDialogTest {

  public static void main(String[] args) throws Throwable {

    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

    final JEditorPane pane = new JEditorPane();
    pane.setEditable(false);

    final JButton proxyButton = new JButton("Proxy Settings");
    proxyButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ev) {
        ProxyUtils.showDialog(proxyButton);
      }
    });

    final JButton testButton = new JButton("Test (https://www.google.com)");
    testButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ev) {

        try {
          pane.setContentType("text/html");
          pane.setText("<html><body bgcolor='#FFFFFF' color='#000000'>Loading...</body></html>");
          pane.setPage(new URL("https://www.google.com"));
        }

        catch (Exception e) {
          e.printStackTrace();
        }
      }
    });

    JToolBar toobar = new JToolBar();
    toobar.setFloatable(false);
    toobar.add(proxyButton);
    toobar.add(testButton);

    JFrame frame = new JFrame("Proxy Settings Dialog Tests");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    frame.getContentPane().setLayout(new BorderLayout());
    frame.getContentPane().add(toobar, BorderLayout.PAGE_START);
    frame.getContentPane().add(new JScrollPane(pane), BorderLayout.CENTER);

    frame.setSize(400, 300);
    frame.setLocationRelativeTo(null);

    frame.setVisible(true);

    ProxyUtils.showDialog(proxyButton);
  }

}
