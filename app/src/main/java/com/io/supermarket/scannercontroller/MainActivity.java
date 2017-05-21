package com.io.supermarket.scannercontroller;

import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements MqttAdapter.CallBack {
  static final String TAG = MainActivity.class.getCanonicalName();
  int numPicturesTaken = 0;
  MqttAdapter mqttAdapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    (findViewById(R.id.start)).setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        updateView();
        mqttAdapter.publish("start_capture");
      }
    });
    (findViewById(R.id.stop)).setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        updateView();
        mqttAdapter.publish("stop_capture");
      }
    });
    (findViewById(R.id.snap)).setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        updateView();
        mqttAdapter.publish("snap");
      }
    });
    mqttAdapter = new MqttAdapter(this);
    mqttAdapter.connect();
    updateView();
  }

  @Override
  protected void onDestroy() {
    mqttAdapter.disconnect();
    super.onDestroy();
  }

  private void updateView() {
    Log.i(TAG, "updateView: " + mqttAdapter.isConnected());
    assertTrue(isOnUiThread());

    ((TextView) findViewById(R.id.numImagesTaken)).setText(Integer.toString(numPicturesTaken));
    String isConnectedStr = mqttAdapter.isConnected() ? "True" : "False";
    Log.i(TAG, "updateView1: " + mqttAdapter.isConnected() + ", " + isConnectedStr);
    ((TextView) findViewById(R.id.isConnected)).setText(isConnectedStr);
  }

  @Override
  public void onMessageReceived(String payload) {
    new Handler(Looper.getMainLooper()).post(new Runnable() {
      @Override
      public void run() {
        updateView();
      }
    });
  }

  @Override
  public void onConnected() {
    Log.i(TAG, "onConnected");
    mqttAdapter.subscribe();
  }

  @Override
  public void connectionLost() {
    new Handler(Looper.getMainLooper()).post(new Runnable() {
      @Override
      public void run() {
        updateView();
      }
    });
  }

  private boolean isOnUiThread() {
    return Looper.getMainLooper().getThread() == Thread.currentThread() ;
  }

  private void assertTrue(boolean val) {
    assertTrue(val, "");
  }

  private void assertTrue(boolean val, String msg) {
    if (!val) {
      Log.e(TAG, "Assertion fails!! " + msg);
      throw new AssertionError(msg);
    }
  }
}
