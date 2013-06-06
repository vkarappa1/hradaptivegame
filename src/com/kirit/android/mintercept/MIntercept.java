/*
    Copyright 1995-2010, Kirit Saelensminde.
    http://www.kirit.com/Missile%20intercept

    This file is part of Missile intercept.

    Missile intercept is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Missile intercept is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Missile intercept.  If not, see <http://www.gnu.org/licenses/>.
*/


package com.kirit.android.mintercept;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import com.kirit.android.Sounds;
import com.kirit.android.Vibrator;
import com.kirit.android.mintercept.views.Level;
import com.kirit.android.mintercept.views.Scene;
import com.kirit.android.mintercept.views.Title;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.view.Surface;

import com.jwetherell.heart_rate_monitor.ImageProcessing;



public class MIntercept extends Fragment   {
	
	
	private static final String TAG = "HeartRateMonitor";
    private static final AtomicBoolean processing = new AtomicBoolean(false);

    private static SurfaceView preview = null;
    private static SurfaceHolder previewHolder = null;
    private static Camera camera = null;
    private static View image = null;
    private static TextView text = null;

    private static WakeLock wakeLock = null;

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
    private static final int beatsArraySize = 3;
    private static final int[] beatsArray = new int[beatsArraySize];
    private static double beats = 0;
    private static long startTime = 0;
	
	
	
    private Handler handler = new Handler();
    private Runnable runner = new Runnable() {
        public void run() {
            view.invalidate();
            handler.postDelayed(runner, 50);
        }
    };

    Title title;
    Level level;
    View view;

    public Vibrator vibrator;
    public Sounds sounds;

    public MIntercept() {
       
    }

    public View startGame() {
     //   sounds.play(R.raw.blip);
      //  cameraSetup();
        return setView(level);
        
    }
    public void endGame() {
        setView(title);
    }

    private View setView(Scene scene) {
        scene.reset();
      /*  Activity activity =  getActivity();
        
        if (scene == level) {
            if (view.getHeight() > view.getWidth())
            	activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            else
            	activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else
        	activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR); */
        
        view = scene;
     //   setContentView(scene);
        view.requestFocus();
        return scene;
    }

    class takepicture extends AsyncTask<Object, Object, Object> {

    	@Override
    	protected Object doInBackground(Object... params) {
    		// TODO Auto-generated method stub
    		
    	    while(true)
    	    {
    	    	camera.takePicture(null, null, null, previewCallback );
        		try {
    				Thread.sleep(100);
    			} catch (InterruptedException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
    			camera.takePicture(null, null, null, previewCallback );
    			try {
    				Thread.sleep(100);
    			} catch (InterruptedException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
    	    }
    		
    	}
       
    }
    
    public void cameraSetup()
    {
    	//setContentView(R.layout.main);
		preview = new SurfaceView(getActivity());
    	preview = (SurfaceView) getView().findViewById(R.id.preview);
		previewHolder = preview.getHolder();
		previewHolder.addCallback(surfaceCallback);
		previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		
		
		
		
		Camera.Parameters parameters = camera.getParameters();
        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        
        
        Camera.Size size = getSmallestPreviewSize(10000, 10000, parameters);
        if (size != null) {
            parameters.setPreviewSize(size.width, size.height);
            Log.d(TAG, "Using width=" + size.width + " height=" + size.height);
        }
        /*
        List<Size> supportedPreviewSizes = camera.getParameters().getSupportedPreviewSizes();
        parameters.setPreviewSize(supportedPreviewSizes.get(0).width, supportedPreviewSizes.get(0).height);
		*/
      
		
		SurfaceTexture surText = new SurfaceTexture(100);
		surText.setDefaultBufferSize(size.width, size.height);
		
		/*ImageFormat imgF = new ImageFormat();
		
		int numbytes = imgF.getBitsPerPixel(camera.getParameters().getPreviewFormat())/8;
	
		
		
		Log.w("numbytes", String.valueOf(numbytes));
		
		List fps = camera.getParameters().getSupportedPreviewFpsRange();
		int [] arr = (int [])fps.get(0);
		
		
		Log.w("maxframerate",String.valueOf(arr[camera.getParameters().PREVIEW_FPS_MAX_INDEX]));
		Log.w("minframerate",String.valueOf(arr[camera.getParameters().PREVIEW_FPS_MIN_INDEX]));*/
		
		try {
			camera.setPreviewTexture(surText);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	
       /* Camera.Size size = getSmallestPreviewSize(100, 100, parameters);
        if (size != null) {
            parameters.setPreviewSize(size.width, size.height);
            Log.d(TAG, "Using width=" + size.width + " height=" + size.height);
        }*/
        camera.setParameters(parameters);
		
	//	 camera.setPreviewCallback(previewCallback);
		// camera.setPreviewCallbackWithBuffer(previewCallback);
		 
		 camera.startPreview();
		
		// camera.takePicture(null, null, null, previewCallback ); 
		 
		 
	
		 new takepicture().execute("hello");
		// new takepicture().execute("hello");
		// new takepicture().execute("hello");
		// new takepicture().execute("hello");
	/*	 int delay = 0; // delay for 5 sec.
		 int period = 10000; // repeat every 10 secs.

		 Timer timer = new Timer();

		 timer.scheduleAtFixedRate(new TimerTask() {

		 public void run() {
		
		 camera.takePicture(null, null, null, previewCallback ); 
		 System.out.println("repeating");

		 }

		 }, delay, period);*/
		 
		 
	/*	 int i = 100;
		 while(i<100)
		 {
			 
			 camera.takePicture(null, null, null, previewCallback ); 
			 try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			camera.takePicture(null, null, null, previewCallback );
			i++;
		 }*/
		 
		 
		

       


		//image = findViewById(R.id.image);
	   // text = (TextView) findViewById(R.id.text);
	    
		//PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		//wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "DoNotDimScreen");
    }
    

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
     
        
        title = new Title(this);
		level = new Level(this);
        
     /*   title = new Title(this);
        level = new Level(this);
        // Load the sounds
        sounds.load(R.raw.blip); // This first sound is used for the toggle noise
        sounds.load(R.raw.city_destroyed);
        sounds.load(R.raw.missile_destroyed);
        sounds.load(R.raw.missile_launch);
        sounds.load(R.raw.player_error);
        sounds.load(R.raw.player_launch);*/
        // Make full screen without title
       
    //    getActivity().requestWindowFeature(Window.FEATURE_NO_TITLE);
       
   //     getActivity().getWindow().setFlags(WindowManager.LayoutParams.MATCH_PARENT,
           //     WindowManager.LayoutParams.MATCH_PARENT);
        
        // Set the view to title, but don't use the setView API for this as it does too much
       // cameraSetup();
      //  camera = Camera.open();
    //    setView(title);
     
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, 
        Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return startGame();
    }

    @Override
   	public void onActivityCreated(Bundle savedInstanceState) {
   		// TODO Auto-generated method stub
   		super.onActivityCreated(savedInstanceState);
		vibrator = new Vibrator(getActivity());
		sounds = new Sounds(getActivity());
		
		// Load the sounds
		sounds.load(R.raw.blip); // This first sound is used for the toggle
									// noise
		sounds.load(R.raw.city_destroyed);
		sounds.load(R.raw.missile_destroyed);
		sounds.load(R.raw.missile_launch);
		sounds.load(R.raw.player_error);
		sounds.load(R.raw.player_launch);
   	}

    
    
    @Override
	public void onResume() {
        super.onResume();
        
     //   wakeLock.acquire();
   //     camera = Camera.open();

        startTime = System.currentTimeMillis();
        
        handler.post(runner);
    }
    @Override
	public void onPause() {
        super.onPause();
  //      wakeLock.release();

       // camera.setPreviewCallback(null);
    //    camera.stopPreview();
    //    camera.release();
     //   camera = null;
        

        handler.removeCallbacks(runner);
    }
    
    @Override
	public void onStop() {
        super.onPause();
  //      wakeLock.release();

        camera.setPreviewCallback(null);
        camera.stopPreview();
        camera.release();
        camera = null;
 
        handler.removeCallbacks(runner);
    }
    
   
    private static PictureCallback   previewCallback = new PictureCallback() {

        /**
         * {@inheritDoc}
         */
        @Override
        public void onPictureTaken(byte[] data, Camera cam) {
        	
        	Log.w("myApp", "inpreview0");
            if (data == null) throw new NullPointerException();
            
            
            Camera.Size size = cam.getParameters().getPreviewSize();
            if (size == null) throw new NullPointerException();
            Log.w("myApp", "inpreview1");
            
           
         //   if (!processing.compareAndSet(false, true)) return;

            int width = size.width;
            int height = size.height;
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inDither = true;
            opt.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length,opt);
            Log.i("bitmap height", String.valueOf(bitmap.getHeight()) );
            int[] pixels = new int[bitmap.getHeight()*bitmap.getWidth()];
            bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
            
      /*      int redCount = 0;
            for(int y=0; y<bitmap.getHeight(); y++)
            {
            	for(int x=0; x<bitmap.getWidth(); x++)
            	{
            		int pixel =  bitmap.getPixel(x,y);
            		int redValue = Color.red(pixel);
            		int blueValue = Color.blue(pixel);
            		int greenValue = Color.green(pixel);
            		
            		if((redValue >= 179)&&(blueValue <= 92) && (greenValue<=92))
            		{
            			redCount++;
            		}
            		else if (pixel == Color.RED)
            		{
            			redCount++;
            		}
            	}
            }
            
            Log.i("RedPixelCount", String.valueOf(redCount));
            
            FileOutputStream outStream = null;
            try{
                outStream = new FileOutputStream("/sdcard/hrImage"+System.currentTimeMillis()+".jpg");
                outStream.write(data);
                outStream.close();
            } catch (FileNotFoundException e){
                Log.d("CAMERA", e.getMessage());
            } catch (IOException e){
                Log.d("CAMERA", e.getMessage());
            }
            */
      
            
      //      int imgAvg = ImageProcessing.decodeYUV420SPtoRedAvg(pixels.clone());
            int imgAvg = 0;
            Log.i(TAG, "imgAvg="+imgAvg);
            if (imgAvg == 0 || imgAvg == 255) {
                processing.set(false);
                return;
            }

            int averageArrayAvg = 0;
            int averageArrayCnt = 0;
            for (int i = 0; i < averageArray.length; i++) {
                if (averageArray[i] > 0) {
                    averageArrayAvg += averageArray[i];
                    averageArrayCnt++;
                }
            }

            int rollingAverage = (averageArrayCnt > 0) ? (averageArrayAvg / averageArrayCnt) : 0;
            
            TYPE newType = currentType;
            if (imgAvg < rollingAverage) {
                newType = TYPE.RED;
                if (newType != currentType) {
                    beats++;
                     Log.d(TAG, "BEAT!! beats="+beats);
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
            if (totalTimeInSecs >= 10) {
                double bps = (beats / totalTimeInSecs);
                Log.d(TAG, "BPS!!="+bps);
                int dpm = (int) (bps * 60d);
                Log.d(TAG, "DPM!!="+dpm);
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
               // text.setText(String.valueOf(beatsAvg));
                startTime = System.currentTimeMillis();
                Log.w("time", String.valueOf(startTime));
                Log.w("beatsAvg", String.valueOf(beatsAvg));
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
               // camera.setPreviewCallback(previewCallback);
            } catch (Throwable t) {
                Log.e("PreviewDemo-surfaceCallback", "Exception in setPreviewDisplay()", t);
            }
        }

        /**
         * {@inheritDoc}
         */
        @SuppressLint("NewApi")
		@Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            Camera.Parameters parameters = camera.getParameters();
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            Camera.Size size = getSmallestPreviewSize(width, height, parameters);
            if (size != null) {
                parameters.setPreviewSize(size.width, size.height);
                Log.d(TAG, "Using width=" + size.width + " height=" + size.height);
            }
            camera.setParameters(parameters);
            camera.startPreview();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            // Ignore
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





