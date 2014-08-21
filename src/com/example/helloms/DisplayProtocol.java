package com.example.helloms;

import com.example.helloms.InstructionsManager;
import com.example.helloms.OR_Step;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.GenericTypeIndicator;
import com.firebase.client.ValueEventListener;
import com.google.android.glass.app.Card;
import com.google.android.glass.app.Card.ImageLayout;

import com.google.android.glass.touchpad.GestureDetector;

import android.app.Activity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ImageView.ScaleType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.android.glass.touchpad.Gesture;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.Parse;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseException;
import com.parse.ParseQuery;


/**
 * This class is used after the QR code is scanned. It downloads the information from
 * the database and displays it on Glass. Handled here is the display of images and timers.
 * 
 * This code is really not in "production state". It is not documented nor optimally coded. 
 * We were trying to explore many capabilities, so we spent little time refining code once things started working.
 * Bottom line is, please don't judge me by this code
 *
 */
public final class DisplayProtocol extends Activity implements SensorEventListener  {

	String[] currentProtocol = null;
	
	int onStepNumber = -1;
	int protocolNumber = 0;
	
    private static final String TAG = "Demo";
   
    private GestureDetector mGestureDetector;

    private InstructionsManager IM;
    
    private SensorManager mSensorManager;
    
    double previousYes = 0;
    double previousNo = 0;
    double previousTrigger = 0;
    Bitmap bmp;
    Map<Integer, String> imageMap;
    
    String imageToDisplayID;
    
	Card card;
    Boolean gyroLeft = false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
    	
		super.onCreate(savedInstanceState);
		
    	getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		
		mGestureDetector = createGestureDetector(this);
		sendToDisplay("Loading...");
		
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		
		String imageCue = getIntent().getExtras().getString("imageCue");
		if (imageCue.startsWith("QR")) {
			 imageCue = String.valueOf(Integer.parseInt(imageCue));
		}
		Log.i("BETH", "image cue: " + imageCue);
    	Firebase firebaseReference = new Firebase("https://labapp.firebaseio.com/input/").child(imageCue);
    	
    	/*
		cardView = new Card(this).setText(getCurrentStepText()).getView();

        // To receive touch events from the touchpad, the view should be focusable.
        cardView.setFocusable(true);
        cardView.setFocusableInTouchMode(true);
      
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
      */

        //setContentView(cardView);
      
        firebaseReference.addValueEventListener(new ValueEventListener() {
    	    @Override
    	    public void onDataChange(DataSnapshot snap) {
    	    	//GenericTypeIndicator<List<String>> t = new GenericTypeIndicator<List<String>>() {};
    	    	//List<String> ids = snap.child("imageMap").getValue(t);
    	    	//Log.i("BETH", ids.get(0));
    	    
    	        //snap.child("imageMap").getValue();
    	    	
    	    	if (snap.hasChild("imageMap")) {
	    	    	imageMap = new HashMap<Integer, String>();
	    	        for (DataSnapshot election : snap.child("imageMap").getChildren()) {
	    	        	imageMap.put(Integer.parseInt(election.getName()), election.getValue().toString());
	    	       
	    	        }
    	    	}
    	        
    	    	String commandString = snap.child("text").getValue().toString();
    	    	
    	    	Log.i("BETH", "command here: " + commandString);
    	    	if (commandString.startsWith("SIMPLE") ) {
    	    		
    	    		commandString = commandString.substring(commandString.indexOf("1."),commandString.length()).trim();
    	    		Log.i("BETH", commandString);
    	    		currentProtocol = commandString.split("\n");
    	    		
    	    		nextStep();
    	    	}
    	    	else if (commandString.startsWith("ADVANCED")) {
    	    	
        			IM = new InstructionsManager(parseIntoCommands(commandString));
        		    
        		    sendToDisplay(IM.firstStep());
    	    	}
    	    }

			@Override
			public void onCancelled(FirebaseError arg0) { }

    	});
    	
    	// Create a reference to a Firebase location
    	//Firebase protocols = new Firebase("https://labapp.firebaseio.com/").child(String.valueOf(protocolNumber));
    	

	}

	
	private OR_Step[] parseIntoCommands(String commandString) {
			
			String[] steps = commandString.split("\n");
			
			List<OR_Step> orsteps = new ArrayList<OR_Step>();
			for (String step: steps) {
				orsteps.add( parseToORStep(step ) );
			}
			OR_Step[] array = new OR_Step[orsteps.size()];
			orsteps.toArray(array); // fill the array
			return array;
			
		}
		
	private OR_Step parseToORStep(String step) {
		 //String step1 = "OR_CODE {{ Glasgow Coma Scale < 14 || Systolic blood pressure < 90 mmHg || Respiratory rate < 10 or > 29 breaths/minute (<20 in infant < 1 year) }} ?? Take to a trauma center :: goto(2)";
			
		  /*
	      Pattern pattern = Pattern.compile("\\{\\{([^\\|\\|]+\\|\\|)+[^\\|\\|][^??]+");
	      Matcher matcher = pattern.matcher(step1);
	      if (matcher.find()) {
	    	  Log.i("BETH", matcher.group());
	      }
	      */
	      int i = step.indexOf("{{");
	      int b = step.indexOf("}}");
	      
	      String m = step.substring(i+2,b);
	      String[] orsteps =  m.split("\\|\\|");
	      
	      String todoTrue = step.substring(step.indexOf("??")+2, step.indexOf("::"));
	      String todoFalse = step.substring(step.indexOf("::")+2);
	      
	      OR_Step stepObject = new OR_Step(orsteps, todoTrue, todoFalse);
		
	      return stepObject;
	}
	
	
	public void sendToDisplay(String stepText) {
		card = new Card(this);
		View cardView = card.setText(stepText).getView();
        // To receive touch events from the touchpad, the view should be focusable.
        cardView.setFocusable(true);
        cardView.setFocusableInTouchMode(true);
        setContentView(cardView);
	}
	
	
	
	/*
	@Override
    public boolean onKeyDown(int keycode, KeyEvent event) {
        if (keycode == KeyEvent.KEYCODE_DPAD_CENTER) {
        	nextStep();
        	return true;
        }
        else if (keycode == KeyEvent.KEYCODE_BACK){
        	Log.i("BETH", "onKeyDown");
        	finish();
        	
        	return true;
        }
        
        return false;
    }
	*/
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// Do something here if sensor accuracy changes.
		// You must implement this callback in your code.
	}
	
	@Override
    protected void onStart() {
    	Log.i(TAG, "onStart");
        super.onStart();
	}
	
	@Override
    protected void onStop() {
    	Log.i(TAG, "onStop");
        Firebase currentStepFB = new Firebase("https://labapp.firebaseio.com/currentStep");
        currentStepFB.setValue("");
        mSensorManager.unregisterListener(this);
      
    	super.onStop();
	}
	
	public String getCurrentStepText() {
		if (currentProtocol == null) { 
			onStepNumber-=1;
			return "Loading. . .";
		}
		return currentProtocol[onStepNumber];
	
	}

	
	public void previousStep() {
		onStepNumber-=2;
		if (onStepNumber< 0){
			onStepNumber = -1;
		}
		imageToDisplayID=null;
		nextStep();
	}
	
	public void nextStep() {
		 onStepNumber+=1;
		 if (onStepNumber == currentProtocol.length) {
			 onStepNumber=0;
		 }
		 
		 String stepText = getCurrentStepText();
		 Firebase currentStepFB = new Firebase("https://labapp.firebaseio.com/currentStep");
	   	
		 if (stepText.contains("{timer:")) {

			 
			  int start = stepText.indexOf('{');
			  int end = stepText.indexOf('}');
			  String timerString = stepText.substring(start+7,end);
			  String[] b = timerString.split(",");
			  final String timerName = b[0];
			  String timerDuration = b[1];
			  stepText = stepText.substring(0,start);
			 
			 final Firebase timerFB = new Firebase("https://labapp.firebaseio.com/timers");
			 timerFB.child(timerName).setValue(timerDuration);
			 new CountDownTimer(Integer.parseInt(timerDuration.trim())*1000, 1000) {
				
			     public void onTick(long millisUntilFinished) {
			       //  mTextField.setText("seconds remaining: " + millisUntilFinished / 1000);
			    	 
			     }

			     public void onFinish() {
			        // mTextField.setText("done!");
			    	 Context context = getApplicationContext();

		 			 CharSequence text = timerName + "Timer finished!";
		 			 int duration = Toast.LENGTH_SHORT;

		 			 Toast toast = Toast.makeText(context, text, duration);
		 			 toast.show();
		 			 
		 			 Firebase thisTimer = new Firebase("https://labapp.firebaseio.com/timers").child(timerName);
		 			 thisTimer.removeValue();
		 
			     }
			  }.start();   
			 
		 }
		 
		 if (imageToDisplayID != null) {
			// card = new Card(this);
		
		     ImageView iv = new ImageView(this);
             iv.setImageBitmap( bmp );
             iv.setPadding(0, 0, 0, 0);
             iv.setAdjustViewBounds(true);
             iv.setScaleType(ScaleType.CENTER_INSIDE);
             setContentView(iv);
			 
			 onStepNumber-=1;
			 if ( onStepNumber == -1) {
				 onStepNumber = currentProtocol.length - 1;
			 }
			 imageToDisplayID = null;
				 
		 }
		 else if (imageMap != null) {
			 int num = onStepNumber + 1;
			 imageToDisplayID = imageMap.get(num);
			 getQueuedBitmap();
			 sendToDisplay(stepText);
		 }
		 else {
			 sendToDisplay(stepText);
		 }
		 
	
		 
		 	 /*
		     currentStepFB.setValue(stepText);
			 cardView = new Card(this).setText(stepText).getView();
			 // To receive touch events from the touchpad, the view should be focusable.
			 cardView.setFocusable(true);
			 cardView.setFocusableInTouchMode(true);
			 setContentView(cardView);
			 */
		 
		
	}	

	
	
	
	
	private void getQueuedBitmap() {
		Parse.initialize(this, "nOAglaRvFGCCKyXx1HunXjwGS6YFmcqgiO9XqFIp", "2xBhaEOoCFxkLjjxsezqqo2wUVJGU6VGr9ibKYMO");
        /*
        byte[] data = "Working at Parse is great!".getBytes();
        ParseFile file = new ParseFile("resume.txt", data);
        file.saveInBackground();
        ParseObject jobApplication = new ParseObject("JobApplication");
        jobApplication.put("applicantName", "Joe Smith");
        jobApplication.put("applicantResumeFile", file);
        jobApplication.saveInBackground();
        */
    	card = new Card(this);

        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Photos");
        query.getInBackground(imageToDisplayID, new GetCallback<ParseObject>() {
            public void done(ParseObject object,ParseException e) {
                // TODO Auto-generated method stub
                // Locate the column named "ImageName" and set
                // the string
                ParseFile fileObject = (ParseFile) object.get("photo");
                fileObject.getDataInBackground(new GetDataCallback() {
                            public void done(byte[] data, ParseException e) {
                                if (e == null) {
                                    Log.d("test",
                                            "We've got data in data.");
                                    
                                    // Decode the Byte[] into
                                    // Bitmap
                                    bmp = BitmapFactory.decodeByteArray(data, 0,data.length);
               
                                  
                                    
                                    
                                } else {
                                    Log.d("test",
                                            "There was a problem downloading the data.");
                                }
                            }
                        });
            }
        });
	}
	
	private void displayQueuedParseImage() {
		Parse.initialize(this, "nOAglaRvFGCCKyXx1HunXjwGS6YFmcqgiO9XqFIp", "2xBhaEOoCFxkLjjxsezqqo2wUVJGU6VGr9ibKYMO");
        /*
        byte[] data = "Working at Parse is great!".getBytes();
        ParseFile file = new ParseFile("resume.txt", data);
        file.saveInBackground();
        ParseObject jobApplication = new ParseObject("JobApplication");
        jobApplication.put("applicantName", "Joe Smith");
        jobApplication.put("applicantResumeFile", file);
        jobApplication.saveInBackground();
        */
    	card = new Card(this);

        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Photos");
        query.getInBackground(imageToDisplayID, new GetCallback<ParseObject>() {
            public void done(ParseObject object,ParseException e) {
                // TODO Auto-generated method stub
                // Locate the column named "ImageName" and set
                // the string
                ParseFile fileObject = (ParseFile) object.get("photo");
                fileObject.getDataInBackground(new GetDataCallback() {
                            public void done(byte[] data, ParseException e) {
                                if (e == null) {
                                    Log.d("test",
                                            "We've got data in data.");
                                    
                                    // Decode the Byte[] into
                                    // Bitmap
                                    Bitmap bmp = BitmapFactory.decodeByteArray(data, 0,data.length);
                                    // Get the ImageView from main.xml
                                    //ImageView image = (ImageView) findViewById(R.id.ad1);
                                    //ImageView ad1=(ImageView) findViewById(R.id.ad1);
                                    // Set the Bitmap into the
                                    // ImageView
                                    //ad1.setImageBitmap(bmp);
                                    // Close progress dialog
                                    //progressDialog.dismiss();
                                    
                                    
                            	
                            		//card.setText("hi there!");
                                    card.setImageLayout(ImageLayout.FULL);
                                    card.addImage(bmp);
                                    View cardView = card.getView();
                                    cardView.setFocusable(true);
                                    cardView.setFocusableInTouchMode(true);
                                    setContentView(cardView);
                                    
                                    
                                } else {
                                    Log.d("test",
                                            "There was a problem downloading the data.");
                                }
                            }
                        });
            }
        });
	}
	
	
	
	
	
    @Override
    protected void onResume() {
        super.onStart();
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), mSensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), mSensorManager.SENSOR_DELAY_NORMAL);

    }
    
    /*
     * Send generic motion events to the gesture detector
     */
    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        if (mGestureDetector != null) {
            return mGestureDetector.onMotionEvent(event);
        }
        return false;
    }
    
    @Override
	public void onSensorChanged(SensorEvent event) {
		 synchronized (this) {
		        switch (event.sensor.getType()){
		            case Sensor.TYPE_LINEAR_ACCELERATION:
		            	float x = event.values[0];
		            	float y = event.values[1];
		            	float z = event.values[2];
		            	
		            	if (Math.abs(x) > 7 ) {//no {
		            		double timeNo = (System.nanoTime() - previousNo)*(Math.pow(10,-9));
		            		
		            		if (timeNo > 1) {
		            			
		            			previousNo = System.nanoTime() ;
		            		
		            			if (currentProtocol != null) {
		            				
		            					//nextStep();
		            				
		                    	}
		                    	else {
		                    		sendToDisplay(IM.nextStep(false));
		                    	}
		            		}
		        		}
		        		else if (Math.abs(y) > 5 ) {
		        			double timeYes = (System.nanoTime() - previousYes)*(Math.pow(10,-9));
		        			if (timeYes > 1) {
		        			
		        				previousYes = System.nanoTime() ;
		        				

		            			if (currentProtocol != null) {
		            				
		            					//nextStep();
		            				
		                    	}
		                    	else {
		                    		sendToDisplay(IM.nextStep(true));
		                    	}
		                    	
		        			
		        			}
		        		}
		              
		               
		                
		                //double elapsedSeconds = (System.nanoTime() - START_TIME)*(Math.pow(10,-9));
		 
		               // accData.setValue(elapsedSeconds);
		               // Log.i(LOG_TAG, "ACC: " + elapsedSeconds  + "," + x + ","+ y + "," + z);
		            break;

            case Sensor.TYPE_GYROSCOPE:
                //gyro_x.setText("x: "+Float.toString(event.values[0]));
               // gyro_y.setText("y: "+Float.toString(event.values[1]));
                double numY = event.values[1];
                
            	if (numY > 0.8 ) {//left 
            		double timeSinceLastTrigger = (System.nanoTime() - previousTrigger)*(Math.pow(10,-9));
            		
            		if (timeSinceLastTrigger > 1) {
            			Log.i("BETH", "BACK");
            			previousTrigger = System.nanoTime() ;
            			if (currentProtocol != null) {
            				previousStep();
            			}
            		}
        		}
        		else if (numY < -0.8 ) {
        			double timeSinceLastTrigger = (System.nanoTime() - previousTrigger)*(Math.pow(10,-9));
            		
        			if (timeSinceLastTrigger > 1) {
        				Log.i("BETH", "NEXT");
        				previousTrigger = System.nanoTime() ;
        				
        				if (currentProtocol != null) {
            				nextStep();
            			}
                    	
        			
        			}
        		}
            	break;
		        }
		 }
		    
	}
    
    
    private GestureDetector createGestureDetector(Context context) {
        GestureDetector gestureDetector = new GestureDetector(context);
            //Create a base listener for generic gestures
            gestureDetector.setBaseListener( new GestureDetector.BaseListener() {
                @Override
                public boolean onGesture(Gesture gesture) {
                    if (gesture == Gesture.TAP) {
                    	Log.i("BETH", "tap");
                    	if (currentProtocol != null) {
                    		nextStep();
                    	}
                    	else {
                    		sendToDisplay(IM.nextStep(true));
                    	}
                        return true;
                        
                    } else if (gesture == Gesture.TWO_TAP) {
                    	Log.i("BETH", "swipe down");
	                	IM.reset();
	                	sendToDisplay(IM.getCurrentStep().getFirstStep());
                        return true;
                        
                    } else if (gesture == Gesture.SWIPE_RIGHT) {
                    	Log.i("BETH", "swipe right");
                        return true;
                        
                    } else if (gesture == Gesture.SWIPE_LEFT) {
                        // do something on left (backwards) swipe
                    	Log.i("BETH", "swipe left");
                    	if (currentProtocol != null) {
                    		nextStep();
                    	}
                    	else {
                    		sendToDisplay(IM.nextStep(false));
                    	}
                    	
                        return true;
                	}
                    return false;
                }
            });
            gestureDetector.setFingerListener(new GestureDetector.FingerListener() {
                @Override
                public void onFingerCountChanged(int previousCount, int currentCount) {
                  // do something on finger count changes
                }
            });
            gestureDetector.setScrollListener(new GestureDetector.ScrollListener() {
                @Override
                public boolean onScroll(float displacement, float delta, float velocity) {
                   return true;
                }
            });
            return gestureDetector;
        }

	
}