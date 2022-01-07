package layout;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;

import phdsound.ubi.com.phdsoundfingerprinting.Variables;

public class AccelerometerCapture extends Service implements SensorEventListener {

    private SensorManager senSensorManager;
    private Sensor senAccelerometer;

    public AccelerometerCapture() {

    }

    // BroadcastReceiver for handling ACTION_SCREEN_OFF.
    public BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // Check action just to be on the safe side.
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                // Unregisters the listener and registers it again.
                senSensorManager.unregisterListener(AccelerometerCapture.this);
                senSensorManager.registerListener(AccelerometerCapture.this, senAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);

        // Register our receiver for the ACTION_SCREEN_OFF action. This will make our receiver
        // code be called whenever the phone enters standby mode.
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mReceiver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        senSensorManager.unregisterListener(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        boolean error = false;
        Sensor mySensor = event.sensor;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            String filePath = Variables.folderPath + "accelerometer.txt";
            File file = new File(filePath);

            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (Exception e) {
                    error = true;
                }
            }

            if(error == false) {
                long currTime = System.currentTimeMillis();
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

                if(currTime >= Variables.timeCaptureStart && currTime <= Variables.timeCaptureEnd) {
                    try {
                        //BufferedWriter for performance, true to set append to file flag
                        BufferedWriter buf = new BufferedWriter(new FileWriter(file, true));
                        buf.append(currTime+"\t"+x+"\t"+y+"\t"+z+"\n");
                        buf.newLine();
                        buf.close();

                    } catch (Exception e) {
                        // nao faz nada
                    }
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // nao faz nada
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }
}
