package com.jwetherell.heart_rate_monitor;

//first change
//second change

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import com.kirit.android.mintercept.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * This class extends Activity to handle a picture preview, process the preview
 * for a red values and determine a heart beat.
 * 
 * @author Justin Wetherell <phishman3579@gmail.com>
 */
@SuppressLint("NewApi")
public class HeartRateMonitor extends Fragment {

    private static final String TAG = "HeartRateMonitor";
    private static final AtomicBoolean processing = new AtomicBoolean(false);

    private static SurfaceView preview = null;
    private static SurfaceHolder previewHolder = null;
    private static Camera camera = null;
    private static View image = null;
    private static TextView text = null;

//    private static WakeLock wakeLock = null;

    private static int averageIndex = 0;
    private static final int averageArraySize = 4;
    private static final int[] averageArray = new int[averageArraySize];
  
    public static enum TYPE {
        GREEN, RED
    };

    private static TYPE currentType = TYPE.GREEN;

    public static TYPE getCurrent() {
        return currentType;
    }

    private static int beatsIndex = 0;
    private static final int beatsArraySize = 10;
    private static int beatsTimeArrayIndex = 0;
    private static final int beatSamples = 4;
    private static final int beatsTimeArraySize = beatSamples + 1;
    private static final int[] beatsArray = new int[beatsArraySize];
    private static double beats = 0;
    private static long startTime = 0;
    private static  File logFile;
    private static File beatslogfile;
    private static final long[] beatsTimeArray = new long[beatsTimeArraySize];
    private static int frames = 0;
    
    public interface HeartBeatListener {
        public void heartBeat(Integer hb);
    }

    private static HeartBeatListener hListener;
   
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
        	hListener = (HeartBeatListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement HeartBeatListener");
        }
    }

    
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
      //  setContentView(R.layout.main);

   /*     preview = (SurfaceView) getView().findViewById(R.id.preview);
        previewHolder = preview.getHolder();
        previewHolder.addCallback(surfaceCallback);
        previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

    //    image = findViewById(R.id.image);
        text = (TextView) getView().findViewById(R.id.text);
     
    /*    PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "DoNotDimScreen");*/

  
    }

   
    
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, 
        Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.hr, container, false);
    }

    
    @Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		String format = "yyyy-MM-dd HH:mm:ss";
		SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
		logFile = new File("sdcard/gamehr/gme_hrlog2" + sdf.format(new Date())
				+ ".txt");
		
		beatslogfile = new File("sdcard/gamehr/beats_" + sdf.format(new Date()) + ".txt");
		if (!logFile.exists())
	       {
	          try
	          {
	             logFile.createNewFile();
	          } 
	          catch (IOException e)
	          {
	             // TODO Auto-generated catch block
	             e.printStackTrace();
	          }
	       }
		
		
		if (!beatslogfile.exists())
	       {
	          try
	          {
	        	  beatslogfile.createNewFile();
	          } 
	          catch (IOException e)
	          {
	             // TODO Auto-generated catch block
	             e.printStackTrace();
	          }
	       }
		

		preview = (SurfaceView) getView().findViewById(R.id.preview);
		previewHolder = preview.getHolder();
		previewHolder.addCallback(surfaceCallback);
		previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		// image = findViewById(R.id.image);
		text = (TextView) getView().findViewById(R.id.text);
	}




	/**
     * {@inheritDoc}
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onResume() {
        super.onResume();
  
    //    wakeLock.acquire();

        camera = Camera.open();

        startTime = System.currentTimeMillis();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPause() {
        super.onPause();

    //    wakeLock.release();

        camera.setPreviewCallback(null);
        camera.stopPreview();
       
        camera.release();
        camera = null;
    }

    
    public static void appendLog(File logFile, String text)
    {       
       
       try
       {
          //BufferedWriter for performance, true to set append to file flag
          BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true)); 
          buf.append(text);
          buf.newLine();
          buf.close();
       }
       catch (IOException e)
       {
          // TODO Auto-generated catch block
          e.printStackTrace();
       }
    }
    

    
    private static PreviewCallback previewCallback = new PreviewCallback() {

        /**
         * {@inheritDoc}
         */
        @Override
        public void onPreviewFrame(byte[] data, Camera cam) {
        	
        	
        	frames++;
            if (data == null) throw new NullPointerException();
            Camera.Size size = cam.getParameters().getPreviewSize();
            if (size == null) throw new NullPointerException();

            if (!processing.compareAndSet(false, true)) return;

            int width = size.width;
            int height = size.height;
            
          

            int imgAvg = ImageProcessing.decodeYUV420SPtoRedAvg(data.clone(), height, width);
            
           
            if (imgAvg == 0 || imgAvg == 255) {
                processing.set(false);
                return;
            }

            Log.d("imgAvg=", String.valueOf(imgAvg));
            int averageArrayAvg = 0;
            int averageArrayCnt = 0;
            for (int i = 0; i < averageArray.length; i++) {
                if (averageArray[i] > 0) {
                    averageArrayAvg += averageArray[i];
                    Log.d("average:", "average :" + i + ":" + averageArray[i]);  
                    averageArrayCnt++;
                }
            }

            int rollingAverage = (averageArrayCnt > 0) ? (averageArrayAvg / averageArrayCnt) : 0;
            
            Log.d("rollingAverage=", String.valueOf(rollingAverage));
            TYPE newType = currentType;
            if (imgAvg < rollingAverage) {
                newType = TYPE.RED;
                if (newType != currentType) {
                    beats++;
                 /*   if(beatsTimeArrayIndex < 11)
                    {
                    	beatsTimeArray[beatsTimeArrayIndex] = System.currentTimeMillis();
                    	beatsTimeArrayIndex++;
                    	if (beatsTimeArrayIndex == 10)
                    	{
                    		Log.i("hrv:", "here1");
                    		long[] rrs = new long[beatSamples];
                    		long sumofsquarerrdiffs = 0;
                    		int i = 0;
                    		while(i < beatSamples)
                    		{
                    			rrs[i] = beatsArray[i+1] - beatsArray[i];
                    			i++;
                    		}
                    		Log.i("hrv:", "here2");
                    		i=0;
                    		while(i < (beatSamples-1) )
                    		{
                    			sumofsquarerrdiffs = sumofsquarerrdiffs + (rrs[i + 1] -  rrs[i])*(rrs[i + 1] -  rrs[i]);
                    			i++;
                    		}
                    		
                    		Log.i("hrv:", "here3");
                    		double meanrrs = sumofsquarerrdiffs/(beatSamples-1);
                    		
                    		double rrsd = Math.sqrt(meanrrs);
                    		
                    		Log.i("hrv:", "hrv" +  rrsd);		
                    	}
                    	
                    	
                    }
                    else beatsTimeArrayIndex = 0;*/
                    
                    String format = "yyyy-MM-dd HH:mm:ss.SSS";
                    SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
                    appendLog(beatslogfile, sdf.format(new Date()) + " imgAvg=" + imgAvg  + " rollingAverage="  + rollingAverage);
                }
            } else if (imgAvg > rollingAverage) {
                newType = TYPE.GREEN;
            }

            if (averageIndex == averageArraySize) averageIndex = 0;
            averageArray[averageIndex] = imgAvg;
            averageIndex++;

            // Transitioned from one state to another to the same
            if (newType != currentType) {
                currentType = newType;
            //    image.postInvalidate();
            }

            long endTime = System.currentTimeMillis();
            double totalTimeInSecs = (endTime - startTime) / 1000d;
            if (totalTimeInSecs >= 1) {
                double bps = (beats / totalTimeInSecs);
                double fps = frames / totalTimeInSecs;
                Log.i("frame rate",  "fps :-" + fps);
                frames = 0;
                int dpm = (int) (bps * 60d);
                if (dpm < 30 || dpm > 180) {
                    startTime = System.currentTimeMillis();
                    beats = 0;
                    processing.set(false);
                    return;
                }

                // Log.d(TAG,
                // "totalTimeInSecs="+totalTimeInSecs+" beats="+beats);

                if (beatsIndex == beatsArraySize) beatsIndex = 0;
                beatsArray[beatsIndex] = dpm;
                beatsIndex++;

                int beatsArrayAvg = 0;
                int beatsArrayCnt = 0;
                for (int i = 0; i < beatsArray.length; i++) {
                    if (beatsArray[i] > 0) {
                        beatsArrayAvg += beatsArray[i];
                        beatsArrayCnt++;
                    }
                }
                int beatsAvg = (beatsArrayAvg / beatsArrayCnt);
                text.setText(String.valueOf(beatsAvg));
                startTime = System.currentTimeMillis();
                
                
                String format = "yyyy-MM-dd HH:mm:ss";
                SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
                
                
                
               // Log.d(TAG, "HB="+beatsAvg);
                appendLog(logFile, sdf.format(new Date()) +  "="  + beatsAvg + "\n");
                if(beatsAvg > 50)
                {
                	// Log.d(TAG, "Calling Listener="+beatsAvg);
                	hListener.heartBeat(beatsAvg);
                }
                beats = 0;
                
            }
            processing.set(false);
        }
    };

    private static SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {

        /**
         * {@inheritDoc}
         */
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                camera.setPreviewDisplay(previewHolder);
                camera.setPreviewCallback(previewCallback);
              //  tryDrawing(holder);
            } catch (Throwable t) {
                Log.e("PreviewDemo-surfaceCallback", "Exception in setPreviewDisplay()", t);
            }
        }

        /**
         * {@inheritDoc}
         */
        @SuppressWarnings("deprecation")
		@Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            Camera.Parameters parameters = camera.getParameters();
            Log.d(TAG, "got width=" + width + " height=" + height);
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            parameters.setRecordingHint(true);
            List<Integer> fps = parameters.getSupportedPreviewFrameRates();
            
            String rates = "";
            
            for(Integer fp: fps)
            {
            	rates += fp + ",";
            }
            
            Log.d(TAG, "rates =" + rates);
            parameters.setPreviewFrameRate(120);
            
            
            
            Camera.Size size = getSmallestPreviewSize(width, height, parameters);
            if (size != null) {
                parameters.setPreviewSize(size.width, size.height);
                Log.d(TAG, "Using width=" + size.width + " height=" + size.height);
            }
           // Log.d(TAG, "Using width=" + size.width + " height=" + size.height);
            camera.setParameters(parameters);
            camera.startPreview();
            tryDrawing(holder);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            // Ignore
        }
        
        
        private void tryDrawing(SurfaceHolder holder) {
            Log.i(TAG, "Trying to draw...");

            Canvas canvas = holder.lockCanvas();
            if (canvas == null) {
                Log.e(TAG, "Cannot draw onto the canvas as it's null");
            } else {
                drawMyStuff(canvas);
                holder.unlockCanvasAndPost(canvas);
            }
        }

        private void drawMyStuff(final Canvas canvas) {
            Random random = new Random();
            Log.i(TAG, "Drawing...");
            canvas.drawRGB(255, 128, 128);
        }
        
    };

    private static Camera.Size getSmallestPreviewSize(int width, int height, Camera.Parameters parameters) {
        Camera.Size result = null;

        for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
            if (size.width <= width && size.height <= height) {
                if (result == null) {
                    result = size;
                } else {
                    int resultArea = result.width * result.height;
                    int newArea = size.width * size.height;

                    if (newArea < resultArea) result = size;
                }
            }
        }

        return result;
    }
}
