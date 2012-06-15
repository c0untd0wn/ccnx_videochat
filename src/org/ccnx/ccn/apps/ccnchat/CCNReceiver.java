package org.ccnx.ccn.apps.ccnchat;

import java.awt.Canvas;
import java.awt.Container;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.ccnx.ccn.apps.ccnchat.CCNChatNet.CCNChatCallback;
import org.ccnx.ccn.config.ConfigurationException;
import org.ccnx.ccn.protocol.MalformedContentNameStringException;

import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.*;


public class CCNReceiver extends JFrame implements CCNChatCallback {
	private static final long serialVersionUID = -8779269133035264361L;

  protected Canvas _canvas = new Canvas();
  protected JLabel imageLabel = new JLabel();
  private final CCNChatNet _chat;
	protected CanvasFrame canvasFrame = new CanvasFrame("Video Chat");

  public CCNReceiver(String namespace) throws MalformedContentNameStringException {

    _chat = new CCNChatNet(this, namespace);
    
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

		/*
    Container content = getContentPane();
    imageLabel.setSize(300,300);
    content.add(imageLabel);
    setSize(300,300);

    setTitle("CCNChat 1.2: [" + namespace + "]");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    pack();
    setVisible(true);
		*/
		canvasFrame.setCanvasSize(300, 300);
    canvasFrame.setDefaultCloseOperation(CanvasFrame.EXIT_ON_CLOSE);

  }
	
	public void recvMessage(byte[] message) {
    try {
      BufferedImage bimage = ImageIO.read(new ByteArrayInputStream(message));
			IplImage image = IplImage.createFrom(bimage);
			/*
      ImageIcon ii = new ImageIcon(image);
      imageLabel.setIcon(ii);
			*/
    } catch(Exception e) {
      e.printStackTrace();
    }
	}
	
	public static void main(String[] args) {
    String namespace = "ccnx:/chat_room";
    if (args.length > 0)
      namespace = args[0];

		CCNReceiver client;
		try {
			client = new CCNReceiver(namespace);
			client.start();
		} catch (MalformedContentNameStringException e) {
			System.err.println("Not a valid ccn URI: " + namespace + ": " + e.getMessage());
			e.printStackTrace();
		} catch (ConfigurationException e) {
			System.err.println("Configuration exception running ccnChat: " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("IOException handling chat messages: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	protected void stop() throws IOException {
		_chat.shutdown();
	}
	
	protected void start() throws ConfigurationException, MalformedContentNameStringException, IOException {
		_chat.listen();
	}
}
