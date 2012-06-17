package kr.ac.snu.ccnx.apps.videochat;

import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.CanvasFrame;
import com.googlecode.javacv.FFmpegFrameRecorder;
import com.googlecode.javacv.FrameRecorder;
import com.googlecode.javacv.OpenCVFrameGrabber;
import com.googlecode.javacv.FrameGrabber.Exception;
import com.googlecode.javacv.cpp.avcodec;
import com.googlecode.javacv.cpp.avutil;
import com.googlecode.javacv.cpp.opencv_objdetect;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

public class MyTimerTask extends TimerTask {
	private static FrameRecorder recorder;
	private static IplImage grabbedImage;
	private static OpenCVFrameGrabber grabber;
	//private static CanvasFrame frame;
	private static int width, height;
	private static int sequence = 0;

	public static void init() throws Exception, com.googlecode.javacv.FrameRecorder.Exception{
		Loader.load(opencv_objdetect.class);
		grabber = new OpenCVFrameGrabber(0);
		grabber.start();

		grabbedImage = grabber.grab();
		width  = grabbedImage.width();
		height = grabbedImage.height();

		//frame = new CanvasFrame("Web Cam", CanvasFrame.getDefaultGamma()/grabber.getGamma());
		//frame.setDefaultCloseOperation(CanvasFrame.EXIT_ON_CLOSE);
		grabber.stop();
	}

	private void capture() throws Exception, com.googlecode.javacv.FrameRecorder.Exception {
		++sequence;
		recorder = FFmpegFrameRecorder.createDefault("output" + sequence + ".wmv", width, height);
		recorder.setCodecID(avcodec.CODEC_ID_WMV1);
		recorder.setFormat("wmv");
		recorder.setPixelFormat(avutil.PIX_FMT_YUV420P);
		grabber.start();
		recorder.start();

		for (long stop=System.nanoTime()+TimeUnit.SECONDS.toNanos(3);stop>System.nanoTime();) {
			grabbedImage = grabber.grab();
			//frame.showImage(grabbedImage);
			recorder.record(grabbedImage);
		}

		grabber.stop();
		recorder.stop();
	}
	public void run() {
		try {
			System.out.println("In run " + sequence);
			capture();

			File file = new File("output" + sequence + ".wmv");
			InputStream is = new FileInputStream(file);

			// Get the size of the file
			long length = file.length();

			if (length > Integer.MAX_VALUE) {
				// File is too large
			}

			// Create the byte array to hold the data
			byte[] bytes = new byte[(int)length];

			// Read in the bytes
			int offset = 0;
			int numRead = 0;
			while (offset < bytes.length
					&& (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
				offset += numRead;
			}

			// Ensure all the bytes have been read in
			if (offset < bytes.length) {
				throw new IOException("Could not completely read file "+file.getName());
			}

			// Close the input stream and return bytes
			is.close();


			VideoChatClient.chat.sendMessage(bytes);
		} catch(Exception e){
			e.printStackTrace();
		} catch (com.googlecode.javacv.FrameRecorder.Exception e) {
			e.printStackTrace();
		} catch (FileNotFoundException e){
			e.printStackTrace();
		} catch (IOException e){
			e.printStackTrace();
		}
	}
}
