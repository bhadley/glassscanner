package com.example.helloms;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

import com.google.android.glass.media.Sounds;
import com.moodstocks.android.AutoScannerSession;
import com.moodstocks.android.Configuration;
import com.moodstocks.android.MoodstocksError;
import com.moodstocks.android.Result;
import com.moodstocks.android.Scanner;
import com.moodstocks.android.Scanner.SearchOption;

/**
 * This class is the main activity; it turns on the camera and tries to recognize images using 
 * the extremely helpful Moodstocks API. In fact, much of the code in this activity is from Moodstocks.
 * 
 * This code is really not in "production state". It is not well documented nor optimally coded. 
 * We were trying to explore many capabilities, so we spent little time refining code once things started working.
 * Bottom line is, don't judge me based on this code.
 *
 */
public class MainActivity extends Activity implements Scanner.SyncListener, AutoScannerSession.Listener {

  // Moodstocks API key/secret pair
  private static final String API_KEY    = "bjejd0eovrerthsq1dhb";
  private static final String API_SECRET = "EFRtUnmys7fIQtSi";

  private static final int TYPES = Result.Type.IMAGE | Result.Type.QRCODE ;

  private boolean compatible = false;
  private Scanner scanner;
  private AutoScannerSession session = null;
  private View resultView;
  private TextView resultID;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Configuration.platform = Configuration.Platform.GOOGLE_GLASS;

    compatible = Scanner.isCompatible();
    if (compatible) {
      try {
        scanner = Scanner.get();
        String path = Scanner.pathFromFilesDir(this, "scanner.db");
        scanner.open(path, API_KEY, API_SECRET);
        scanner.setSyncListener(this);
        scanner.sync();
      } catch (MoodstocksError e) {
        e.printStackTrace();
      }

      SurfaceView preview = (SurfaceView)findViewById(R.id.preview);

      try {
        session = new AutoScannerSession(this, Scanner.get(), this, preview);
        session.setSearchOptions(SearchOption.SMALLTARGET);
        session.setResultTypes(TYPES);
      } catch (MoodstocksError e) {
        e.printStackTrace();
      }
    }

    resultView = findViewById(R.id.result);
    resultID = (TextView) findViewById(R.id.result_id);
  }

  @Override
  protected void onResume() {
	  Log.i("BETH", "onResume");
	  onKeyDown(KeyEvent.KEYCODE_DPAD_CENTER,null);
    super.onResume();
    session.start();
  }

  @Override
  protected void onPause() {
    super.onPause();
    session.stop();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (compatible) {
      try {
        scanner.close();
        scanner.destroy();
        scanner = null;
      } catch (MoodstocksError e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  public void onSyncStart() {
    Log.d("Moodstocks SDK", "Sync will start.");
  }

  @Override
  public void onSyncComplete() {
    try {
      Log.d("Moodstocks SDK", "Sync succeeded ("+ scanner.count() + " images)");
    } catch (MoodstocksError e) {
      e.printStackTrace();
    }
  }

  @Override
  public void onSyncFailed(MoodstocksError e) {
    Log.d("Moodstocks SDK", "Sync error #" + e.getErrorCode() + ": " + e.getMessage());
  }

  @Override
  public void onSyncProgress(int total, int current) {
    int percent = (int) ((float) current / (float) total * 100);
    Log.d("Moodstocks SDK", "Sync progressing: " + percent + "%");
  }

  @Override
  public void onCameraOpenFailed(Exception e) {
    // Implemented in a few steps
  }

  @Override
  public void onWarning(String debugMessage) {
    // Implemented in a few steps
  }

  @Override
  public void onResult(Result result) {
    resultID.setText(result.getValue());
    //resultView.setVisibility(View.VISIBLE);
    ((AudioManager) getSystemService(Context.AUDIO_SERVICE)).playSoundEffect(Sounds.SUCCESS);
    
    Intent intent = new Intent(this, DisplayProtocol.class);
    intent.putExtra("imageCue", result.getValue());
    startActivity(intent);
    
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
      if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) { // && resultView.getVisibility() == View.VISIBLE) {
          //resultView.setVisibility(View.INVISIBLE);

          session.resume();
          return true;
      }
      return super.onKeyDown(keyCode, event);
  }
  

}
