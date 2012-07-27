package kr.ac.snu.ccnx.apps.videochat;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.*;
import java.util.logging.Level;
import java.util.Date;
import java.util.Timer;

import javax.imageio.ImageIO;

import org.ccnx.ccn.apps.ccnchat.CCNChatNet;
import org.ccnx.ccn.apps.ccnchat.CCNChatNet.CCNChatCallback;
import org.ccnx.ccn.config.ConfigurationException;
import org.ccnx.ccn.protocol.MalformedContentNameStringException;

import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.CanvasFrame;
import com.googlecode.javacv.OpenCVFrameGrabber;
import com.googlecode.javacv.FFmpegFrameGrabber;
import com.googlecode.javacv.FrameRecorder;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

public class VideoChatClient implements Runnable, CCNChatCallback {
	private int sequence = 0;

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
		try {

			Timer timer = new Timer();
    	MyTimerTask timerTask = new MyTimerTask();
    	MyTimerTask.init();
    	timer.scheduleAtFixedRate(timerTask, new Date(), 3000 );

		} catch (Exception e) {
    }
	}

  public void recvMessage(String sender, byte[] message) {
    try {
			++sequence;
			String filenamePrefix;
			filenamePrefix = "input";
			File inputFile = new File(filenamePrefix + sequence + ".wmv");
			FileOutputStream fos = new FileOutputStream(inputFile);
			fos.write(message);
			fos.flush();
			fos.close();

				FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputFile);
				grabber.start();

				for(int i=0; i<grabber.getLengthInFrames(); i++){
					canvasFrame.showImage(grabber.grab());
					Thread.sleep((long) (1000/grabber.getFrameRate()));
			}
			//canvasFrame.showImage(image);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static CCNChatNet chat;
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
