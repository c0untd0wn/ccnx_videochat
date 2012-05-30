/*
 * A CCNx chat program.
 *
 * Copyright (C) 2008, 2009, 2010, 2011 Palo Alto Research Center, Inc.
 *
 * This work is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License version 2 as published by the
 * Free Software Foundation.
 * This work is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details. You should have received a copy of the GNU General Public
 * License along with this program; if not, write to the
 * Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA 02110-1301, USA.
 */

package org.ccnx.ccn.apps.ccnchat;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.ccnx.ccn.apps.ccnchat.CCNChatNet.CCNChatCallback;
import org.ccnx.ccn.config.ConfigurationException;
import org.ccnx.ccn.protocol.MalformedContentNameStringException;


/**
 * Based on a client/server chat example in Robert Sedgewick's Algorithms
 * in Java.
 * 
 * Refactored to be just the JFrame UI.
 */
public class CCNReceiver extends JFrame implements ActionListener, CCNChatCallback {
	private static final long serialVersionUID = -8779269133035264361L;

    // Chat window
    protected Canvas _canvas = new Canvas();
    protected JLabel imageLabel = new JLabel();
    //protected JTextArea  _messagePane = new JTextArea(10, 32);
    private final CCNChatNet _chat;
    
    public CCNReceiver(String namespace) throws MalformedContentNameStringException {

    	_chat = new CCNChatNet(this, namespace);
    	
    	// close output stream  - this will cause listen() to stop and exit
        addWindowListener(
            new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    try {
						stop();
					} catch (IOException e1) {
						System.out.println("IOException shutting down listener: " + e1);
						e1.printStackTrace();
					}
                }
            }
        );
        
        
        // Make window
        //_messagePane.setEditable(false);
        //_messagePane.setBackground(Color.LIGHT_GRAY);
        //_messagePane.setLineWrap(true);

        Container content = getContentPane();
        imageLabel.setSize(300,300);
        content.add(imageLabel);
        setSize(300,300);
        //content.add(_canvas, BorderLayout.CENTER);
        //content.add(new JScrollPane(_messagePane), BorderLayout.CENTER);
        
        // display the window, with focus on typing box
        setTitle("CCNChat 1.2: [" + namespace + "]");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setVisible(true);
    }
	
	/**
	 * Process input to TextField after user hits enter.
	 * (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
	}

	
	/**
	 * Add a message to the output.
	 * @param message
	 */
	public void recvMessage(byte[] message) {
    try {
        BufferedImage image = new BufferedImage(300,300,BufferedImage.TYPE_INT_ARGB);
        for (int i=0;i<100;i++)
          for(int j=0;j<100;j++)
          {
            int color = 0x7f000000+((message[i*300+j*3]+128)<<16)+((message[i*300+j*3+1]+128)<<8)+message[i*300+j*3+2]+128;
            for(int k=0;k<3;k++)
              for(int l=0;l<3;l++)
                image.setRGB(i*3+k,j*3+l,color);
          }
        ImageIcon ii = new ImageIcon(image);
        imageLabel.setIcon(ii);
        //_messagePane.setBackground(new Color(value,value,value));
        //_canvas.setBackground(new Color(value,value,value));
    }catch(Exception e){
      e.printStackTrace();
    }
	}
	
    public static void usage() {
    	System.err.println("usage: CCNChat <ccn URI>");
    }
    
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length != 1) {
			usage();
			System.exit(-1);
		}
		CCNReceiver client;
		try {
			client = new CCNReceiver(args[0]);
			client.start();
		} catch (MalformedContentNameStringException e) {
			System.err.println("Not a valid ccn URI: " + args[0] + ": " + e.getMessage());
			e.printStackTrace();
		} catch (ConfigurationException e) {
			System.err.println("Configuration exception running ccnChat: " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("IOException handling chat messages: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	// =========================================================
	// Internal methods
	
	/**
	 * Called by window thread when when window closes
	 */
	protected void stop() throws IOException {
		_chat.shutdown();
	}
	
	/**
	 * This blocks until _chat.shutdown() called
	 * @throws IOException 
	 * @throws MalformedContentNameStringException 
	 * @throws ConfigurationException 
	 */
	protected void start() throws ConfigurationException, MalformedContentNameStringException, IOException {
		_chat.listen();
	}
}
