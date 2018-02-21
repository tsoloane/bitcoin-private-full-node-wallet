/************************************************************************************************
 *  _________          _     ____          _           __        __    _ _      _   _   _ ___
 * |__  / ___|__ _ ___| |__ / ___|_      _(_)_ __   __ \ \      / /_ _| | | ___| |_| | | |_ _|
 *   / / |   / _` / __| '_ \\___ \ \ /\ / / | '_ \ / _` \ \ /\ / / _` | | |/ _ \ __| | | || |
 *  / /| |__| (_| \__ \ | | |___) \ V  V /| | | | | (_| |\ V  V / (_| | | |  __/ |_| |_| || |
 * /____\____\__,_|___/_| |_|____/ \_/\_/ |_|_| |_|\__, | \_/\_/ \__,_|_|_|\___|\__|\___/|___|
 *                                                 |___/
 *
 * Copyright (c) 2016 Ivan Vaklinov <ivan@vaklinov.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 **********************************************************************************/
package com.vaklinov.zcashui;


import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JTextField;


/**
 * Dialog to enter a single private key to import
 *
 * @author Ivan Vaklinov <ivan@vaklinov.com>
 */
public class SingleKeyImportDialog
	extends JDialog
{
	protected boolean isOKPressed = false;
	protected String  key    = null;

	protected JLabel     keyLabel = null;
	protected JTextField keyField = null;

	protected JLabel upperLabel;
	protected JLabel lowerLabel;

	protected JProgressBar progress = null;

	protected ZCashClientCaller caller;

	JButton okButton;
	JButton cancelButton;

	public SingleKeyImportDialog(JFrame parent, ZCashClientCaller caller)
	{
		super(parent);
		this.caller = caller;

		this.setTitle("Import A Private Key");
	    this.setLocation(parent.getLocation().x + 50, parent.getLocation().y + 50);
		this.setModal(true);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		JPanel controlsPanel = new JPanel();
		controlsPanel.setLayout(new BoxLayout(controlsPanel, BoxLayout.Y_AXIS));
		controlsPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

		JPanel tempPanel = new JPanel(new BorderLayout(0, 0));
		tempPanel.add(this.upperLabel = new JLabel(
			"<html>Please enter a single private key to import." +
		    "</html>"), BorderLayout.CENTER);
		controlsPanel.add(tempPanel);

		JLabel dividerLabel = new JLabel("   ");
		dividerLabel.setFont(new Font("Helvetica", Font.PLAIN, 8));
		controlsPanel.add(dividerLabel);

		tempPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		tempPanel.add(keyLabel = new JLabel("Key: "));
		tempPanel.add(keyField = new JTextField(60));
		controlsPanel.add(tempPanel);

		dividerLabel = new JLabel("   ");
		dividerLabel.setFont(new Font("Helvetica", Font.PLAIN, 8));
		controlsPanel.add(dividerLabel);

		tempPanel = new JPanel(new BorderLayout(0, 0));
		tempPanel.add(this.lowerLabel = new JLabel(
			"<html><span style=\"font-weight:bold\">" +
		    "Warning:</span> Importing private keys can be a slow operation that " +
		    "requires blockchain rescanning (may take many minutes). <br/>The GUI " +
			"will not be usable for other functions during this time.</html>"),
			BorderLayout.CENTER);
		controlsPanel.add(tempPanel);

		dividerLabel = new JLabel("   ");
		dividerLabel.setFont(new Font("Helvetica", Font.PLAIN, 8));
		controlsPanel.add(dividerLabel);

		tempPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		tempPanel.add(progress = new JProgressBar());
		controlsPanel.add(tempPanel);

		this.getContentPane().setLayout(new BorderLayout(0, 0));
		this.getContentPane().add(controlsPanel, BorderLayout.NORTH);

		// Form buttons
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 3, 3));
		okButton = new JButton("Import");
		buttonPanel.add(okButton);
		buttonPanel.add(new JLabel("   "));
		cancelButton = new JButton("Cancel");
		buttonPanel.add(cancelButton);
		this.getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		okButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				SingleKeyImportDialog.this.processOK();
			}
		});

		cancelButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				SingleKeyImportDialog.this.setVisible(false);
				SingleKeyImportDialog.this.dispose();

				SingleKeyImportDialog.this.isOKPressed = false;
				SingleKeyImportDialog.this.key = null;
			}
		});

		this.setSize(740, 210);
		this.validate();
		this.repaint();

		this.pack();
	}


	protected void processOK()
	{
		final String key = SingleKeyImportDialog.this.keyField.getText();

		if ((key == null) || (key.trim().length() <= 0))
		{
			JOptionPane.showMessageDialog(
				SingleKeyImportDialog.this.getParent(),
				"Please enter a key.", "No Key Entered",
				JOptionPane.ERROR_MESSAGE);
			return;
		}

		SingleKeyImportDialog.this.isOKPressed = true;
		SingleKeyImportDialog.this.key = key;

		// Start import
		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		this.progress.setIndeterminate(true);
		this.progress.setValue(1);

		this.okButton.setEnabled(false);
		this.cancelButton.setEnabled(false);

		SingleKeyImportDialog.this.keyField.setEditable(false);

		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					String address = SingleKeyImportDialog.this.caller.importPrivateKey(key);
					String addition = "";

					if (!Util.stringIsEmpty(address))
					{
						addition = " It corresponds to address:\n" + address;
					}

					JOptionPane.showMessageDialog(
						SingleKeyImportDialog.this,
						"The private key:\n" +
						key + "\n" +
						"has been successfully imported." + addition,
						"Successfully Imported Private Key",
						JOptionPane.INFORMATION_MESSAGE);
				} catch (Exception e)
				{
					Log.error("An error occurred when importing private key", e);

					JOptionPane.showMessageDialog(
						SingleKeyImportDialog.this.getRootPane().getParent(),
						"Error occurred when importing private key:\n" +
						e.getClass().getName() + ":\n" + e.getMessage() + "\n\n" +
						"Please ensure that btcpd is running, and the key is in the correct \n" +
						"form. Try again later.\n",
						"Error Importing Private Key", JOptionPane.ERROR_MESSAGE);
				} finally
				{
					SingleKeyImportDialog.this.setVisible(false);
					SingleKeyImportDialog.this.dispose();
				}
			}
		}).start();
	}


	public boolean isOKPressed()
	{
		return this.isOKPressed;
	}


	public String getKey()
	{
		return this.key;
	}
}
