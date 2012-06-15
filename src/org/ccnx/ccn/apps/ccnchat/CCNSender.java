package org.ccnx.ccn.apps.ccnchat;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;

import javax.imageio.ImageIO;

import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.OpenCVFrameGrabber;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

import org.ccnx.ccn.apps.ccnchat.CCNChatNet.CCNChatCallback;
import org.ccnx.ccn.config.ConfigurationException;
import org.ccnx.ccn.protocol.MalformedContentNameStringException;

public class CCNSender implements Runnable, CCNChatCallback {
	
	public static void main(String[] args) {
    String namespace = "ccnx:/chat_room";
    if (args.length > 0)
      namespace = args[0];

		CCNSender client;
		try {
			client = new CCNSender(namespace);
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
	
	public void run()  {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0);
		IplImage image = null;

		try {
      grabber.start();
		
      image = grabber.grab();
		
      while (true) {
        image = grabber.grab();

        ByteArrayOutputStream baos = new ByteArrayOutputStream( 1000 );
        ImageIO.write(image.getBufferedImage(), "jpeg", baos);
        baos.flush();
        byte[] message = baos.toByteArray();
        baos.close();
        _chat.sendMessage(message);

        Thread.sleep(100);
      }
		} catch (Exception e) {
    }
	}
	
	private final CCNChatNet _chat;
	private final Thread _thd;
	
	private enum Mode {
		MODE_INPUT,
		MODE_OUTPUT
	}
	
	protected CCNSender(String namespace) throws MalformedContentNameStringException {
		_chat = new CCNChatNet(this, namespace);
		_thd = new Thread(this, "CCNSender");
	}
	
	protected void stop() throws IOException {
		_chat.shutdown();
	}
	
	protected void start() throws ConfigurationException, MalformedContentNameStringException, IOException {
		_thd.start();
		_chat.setLogging(Level.WARNING);
		_chat.listen();
	}
	
	public void recvMessage(byte[] message) {
	}
	
}
