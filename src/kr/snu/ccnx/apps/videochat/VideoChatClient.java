package kr.ac.snu.ccnx.apps.videochat;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;

import javax.imageio.ImageIO;

import org.ccnx.ccn.apps.ccnchat.CCNChatNet;
import org.ccnx.ccn.apps.ccnchat.CCNChatNet.CCNChatCallback;
import org.ccnx.ccn.config.ConfigurationException;
import org.ccnx.ccn.protocol.MalformedContentNameStringException;

import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.CanvasFrame;
import com.googlecode.javacv.OpenCVFrameGrabber;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

public class VideoChatClient implements Runnable, CCNChatCallback {

	public static void main(String[] args) {
    String namespace = "ccnx:/chat_room";
    if (args.length > 0)
      namespace = args[0];

		try {
      VideoChatClient client = new VideoChatClient(namespace);
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
        chat.sendMessage(message);

        Thread.sleep(100);
      }
		} catch (Exception e) {
    }
	}

  public void recvMessage(byte[] message) {
    try {
      BufferedImage bimage = ImageIO.read(new ByteArrayInputStream(message));
			IplImage image = IplImage.createFrom(bimage);
      canvasFrame.showImage(image);
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

  private final CCNChatNet chat;
  private final Thread thread;
  private CanvasFrame canvasFrame = new CanvasFrame("VideoChat");

  private VideoChatClient(String namespace) throws MalformedContentNameStringException {
    chat = new CCNChatNet(this, namespace);
    thread = new Thread(this, "VideoChatClient");

    canvasFrame.setCanvasSize(300, 300);
    canvasFrame.setDefaultCloseOperation(CanvasFrame.EXIT_ON_CLOSE);
  }

  private void stop() throws IOException {
    chat.shutdown();
  }

  private void start() throws ConfigurationException, MalformedContentNameStringException, IOException {
    thread.start();
    chat.setLogging(Level.WARNING);
    chat.listen();
  }
}
