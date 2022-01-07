package layout;

import android.app.Service;
import android.net.wifi.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.List;

import phdsound.ubi.com.phdsoundfingerprinting.Variables;

/**
 * Created by Ivan on 20/12/15.
 */
public class WiFiChange extends Service {

    private android.os.Handler Handler = null;
    private Runnable runnableCode = null;

    public WiFiChange() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        HandlerThread thread = new HandlerThread("PhD Sound Capture - WIFI", android.os.Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        Looper serviceLooper = thread.getLooper();
        this.Handler = new Handler(serviceLooper);

        // Define the code block to be executed
        this.runnableCode = new Runnable() {
            @Override
            public void run() {
                // Do something here on the main thread

                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();

                if (activeNetInfo != null && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                    WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                    WifiInfo info = wifiManager.getConnectionInfo();
                    String bssid = info.getBSSID();
                    int signalStrength= 0;

                    int state = wifiManager.getWifiState();
                    if(state == WifiManager.WIFI_STATE_ENABLED) {
                        List<ScanResult> results = wifiManager.getScanResults();

                        for (ScanResult result : results) {
                            if(result.BSSID.equals(bssid)) {
                                int level = WifiManager.calculateSignalLevel(wifiManager.getConnectionInfo().getRssi(), result.level);
                                int difference = level * 100 / result.level;

                                if(difference >= 100)
                                    signalStrength = 4;
                                else if(difference >= 75)
                                    signalStrength = 3;
                                else if(difference >= 50)
                                    signalStrength = 2;
                                else if(difference >= 25)
                                    signalStrength = 1;
                            }

                        }

                        boolean error = false;

                        String filePath = Variables.folderPath + "wifi.txt";
                        File file = new File(filePath);

                        long currTime = System.currentTimeMillis();

                        if(currTime >= Variables.timeCaptureStart && currTime <= Variables.timeCaptureEnd) {

                            if (!file.exists()) {
                                try {
                                    file.createNewFile();
                                } catch (Exception e) {
                                    error = true;
                                }
                            }

                            if (error == false) {
                                try {
                                    //BufferedWriter for performance, true to set append to file flag
                                    BufferedWriter buf = new BufferedWriter(new FileWriter(file, true));
                                    buf.append(currTime + "\t" + bssid + "\t" + signalStrength + "\n");
                                    buf.newLine();
                                    buf.close();

                                } catch (Exception e) {
                                    // nao faz nada
                                }
                            }
                        }
                    }
                }

                // Repeat this the same runnable code block again another 500 milliseconds
                Handler.postDelayed(runnableCode, 500);
            }
        };

    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        if(Handler != null) {
            Handler.removeCallbacks(runnableCode);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null) { // May not have an Intent is the service was killed and restarted (See STICKY_SERVICE).
            Handler.post(runnableCode);
        }
        return Service.START_STICKY;
    }
}
