package com.io.supermarket.scannercontroller;

import android.os.Handler;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MqttAdapter implements MqttCallback {

  @Override
  public void connectionLost(Throwable cause) {
    callBack.connectionLost();
    connect();
  }

  @Override
  public void messageArrived(String topic, MqttMessage msg) throws Exception {
    Log.i(TAG, "messageArrived");
    callBack.onMessageReceived(new String(msg.getPayload()));
    Log.i(TAG, "messageArrived callback finished");
  }

  @Override
  public void deliveryComplete(IMqttDeliveryToken token) {
    Log.i(TAG, "deliveryComplete");
  }

  public interface CallBack {
    public void onMessageReceived(String payload);
    public void onConnected();
    public void connectionLost();
  }

  static final String TAG = MqttAdapter.class.getCanonicalName();
  final private CallBack callBack;
  private MqttClient mqttClient = null;
  String topic = "ImageScanner";
  int qos = 2;

  MqttAdapter(final CallBack callBack) {
    this.callBack = callBack;
  }

  public boolean isConnected() {
    return (mqttClient != null) && mqttClient.isConnected();
  }

  public void connect() {
    String broker = "tcp://m13.cloudmqtt.com:11714";
    String clientId = "oujibpyy";
    String password = "-mKBDKwYQ1CC";

    MemoryPersistence persistence = new MemoryPersistence();
    try {
      mqttClient = new MqttClient(broker, clientId, persistence);
      mqttClient.setCallback(this);
      MqttConnectOptions connOpts = new MqttConnectOptions();
      connOpts.setCleanSession(true);
      connOpts.setUserName("oujibpyy");
      connOpts.setPassword(password.toCharArray());
      connOpts.setCleanSession(false);
      connOpts.setConnectionTimeout(60 * 1000);
      connOpts.setKeepAliveInterval(60 * 1000);
      mqttClient.connect(connOpts);
    } catch (MqttException me) {
      Log.i(TAG, "reason " + me.getReasonCode());
      Log.i(TAG, "msg " + me.getMessage());
      Log.i(TAG, "loc " + me.getLocalizedMessage());
      Log.i(TAG, "cause " + me.getCause());
      Log.i(TAG, "except " + me);
      me.printStackTrace();
      new Handler().postDelayed(new Runnable() {
        @Override
        public void run() {
          connect();
        }
      }, 2000);

    }
  }

  public void disconnect() {
    if (isConnected()) {
      try {
        mqttClient.disconnect();
      } catch (MqttException e) {
        e.printStackTrace();
      }
    }
  }

  public void subscribe() {
    if (isConnected()) {
      try {
        mqttClient.subscribe(topic, qos);
      } catch (MqttException e) {
        e.printStackTrace();
      }
    }
  }

  public void publish(String content) {
    if (isConnected()) {
      MqttMessage message = new MqttMessage(content.getBytes());
      message.setQos(qos);
      try {
        mqttClient.publish(topic, message);
      } catch (MqttException e) {
        e.printStackTrace();
      }
    }
  }
}
