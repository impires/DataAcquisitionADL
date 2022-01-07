package phdsound.ubi.com.phdsoundfingerprinting;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.sql.SQLOutput;

import layout.AccelerometerCapture;
import layout.GyroscopeCapture;
import layout.LocationCapture;
import layout.MagnetometerCapture;
import layout.SoundCapture;
import layout.WiFiChange;

public class MainActivity extends AppCompatActivity {

    private Button startBtn;
    private Button stopBtn;
    private File dir;
    private TextView text;
    private Spinner adl;
    private static final String PATH_NAME = Environment.getExternalStorageDirectory().getAbsolutePath() + "/PhDSoundFilesNew/";
    private final Handler handler = new Handler();

    private static final String[] INITIAL_PERMS={
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.RECORD_AUDIO,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_WIFI_STATE,
            android.Manifest.permission.ACCESS_NETWORK_STATE,
            android.Manifest.permission.INTERNET
    };
    private static final int INITIAL_REQUEST=1337;

    private PowerManager powerManager;
    private PowerManager.WakeLock wakeLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "PhDSoundFingerprintingWakelock");

        adl = (Spinner) findViewById(R.id.spinner);
        adl.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SharedPreferences prefs = getApplicationContext().getSharedPreferences("phdsound.ubi.com.phdsoundfingerprinting", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt("adlSelected", position);
                editor.apply();

                if(position == 0) {
                    Variables.ADL_SELECTED = "undifferentiated";
                } else if(position == 1) {
                    Variables.ADL_SELECTED = "walking";
                } else if(position == 2) {
                    Variables.ADL_SELECTED = "running";
                } else if(position == 3) {
                    Variables.ADL_SELECTED = "downstairs";
                } else if(position == 4) {
                    Variables.ADL_SELECTED = "sleeping";
                } else if(position == 5) {
                    Variables.ADL_SELECTED = "jumping";
                } else if(position == 6) {
                    Variables.ADL_SELECTED = "upstairs";
                } else if(position == 7) {
                    Variables.ADL_SELECTED = "gym";
                } else if(position == 8) {
                    Variables.ADL_SELECTED = "bar";
                } else if(position == 9) {
                    Variables.ADL_SELECTED = "classroom";
                } else if(position == 10) {
                    Variables.ADL_SELECTED = "library";
                } else if(position == 11) {
                    Variables.ADL_SELECTED = "driving";
                } else if(position == 12) {
                    Variables.ADL_SELECTED = "kitchen";
                } else if(position == 13) {
                    Variables.ADL_SELECTED = "standing";
                }

                Variables.POS_ADL_SELECTED = position;

                if(Variables.ADL_SELECTED.equals("downstairs") || Variables.ADL_SELECTED.equals("upstairs")) {
                    Variables.TIME_LOOP = 30 * 1000;
                } else {
                    Variables.TIME_LOOP = 1 * 1000;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        SharedPreferences prefs = getApplicationContext().getSharedPreferences("phdsound.ubi.com.phdsoundfingerprinting", Context.MODE_PRIVATE);
        int position = prefs.getInt("adlSelected", 0);

        if(position == 0) {
            Variables.ADL_SELECTED = "undifferentiated";
        } else if(position == 1) {
            Variables.ADL_SELECTED = "walking";
        } else if(position == 2) {
            Variables.ADL_SELECTED = "running";
        } else if(position == 3) {
            Variables.ADL_SELECTED = "downstairs";
        } else if(position == 4) {
            Variables.ADL_SELECTED = "sleeping";
        } else if(position == 5) {
            Variables.ADL_SELECTED = "jumping";
        } else if(position == 6) {
            Variables.ADL_SELECTED = "upstairs";
        }else if(position == 7) {
            Variables.ADL_SELECTED = "gym";
        } else if(position == 8) {
            Variables.ADL_SELECTED = "bar";
        } else if(position == 9) {
            Variables.ADL_SELECTED = "classroom";
        } else if(position == 10) {
            Variables.ADL_SELECTED = "library";
        } else if(position == 11) {
            Variables.ADL_SELECTED = "driving";
        } else if(position == 12) {
            Variables.ADL_SELECTED = "kitchen";
        } else if(position == 13) {
            Variables.ADL_SELECTED = "standing";
        }
        Variables.POS_ADL_SELECTED = position;

        adl.setSelection(Variables.POS_ADL_SELECTED);

        if(Variables.ADL_SELECTED.equals("downstairs") || Variables.ADL_SELECTED.equals("upstairs")) {
            Variables.TIME_LOOP = 30 * 1000;
        } else {
            Variables.TIME_LOOP = 1 * 1000;
        }

        startBtn = (Button)findViewById(R.id.startButton_mainActivity);
        startBtn.setEnabled(true);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                adl.setEnabled(false);
                wakeLock.acquire();

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            dir.mkdirs();

                            Intent i = new Intent(MainActivity.this, SoundCapture.class);
                            MainActivity.this.startService(i);
                            Intent j = new Intent(MainActivity.this, AccelerometerCapture.class);
                            MainActivity.this.startService(j);
                            Intent k = new Intent(MainActivity.this, LocationCapture.class);
                            MainActivity.this.startService(k);
                            Intent l = new Intent(MainActivity.this, WiFiChange.class);
                            MainActivity.this.startService(l);
                            Intent m = new Intent(MainActivity.this, GyroscopeCapture.class);
                            MainActivity.this.startService(m);
                            Intent n = new Intent(MainActivity.this, MagnetometerCapture.class);
                            MainActivity.this.startService(n);

                            text.setText(getString(R.string.recording));
                            startBtn.setEnabled(false);
                            stopBtn.setEnabled(true);

                            Toast.makeText(getApplicationContext(), getString(R.string.start_capture), Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }, Variables.TIME_BEFORE);
            }
        });

        stopBtn = (Button)findViewById(R.id.stopButton_mainActivity);
        stopBtn.setEnabled(false);
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                adl.setEnabled(true);
                wakeLock.release();

                Intent i = new Intent(MainActivity.this, SoundCapture.class);
                MainActivity.this.stopService(i);
                Intent j = new Intent(MainActivity.this, AccelerometerCapture.class);
                MainActivity.this.stopService(j);
                Intent k = new Intent(MainActivity.this, LocationCapture.class);
                MainActivity.this.stopService(k);
                Intent l = new Intent(MainActivity.this, WiFiChange.class);
                MainActivity.this.stopService(l);
                Intent m = new Intent(MainActivity.this, GyroscopeCapture.class);
                MainActivity.this.stopService(m);
                Intent n = new Intent(MainActivity.this, MagnetometerCapture.class);
                MainActivity.this.stopService(n);

                stopBtn.setEnabled(false);
                startBtn.setEnabled(true);
                text.setText(getString(R.string.waiting));

                Toast.makeText(getApplicationContext(), getString(R.string.stop_capture), Toast.LENGTH_SHORT).show();
            }
        });

        this.text = (TextView) findViewById(R.id.info_mainActivity);
        text.setText(getString(R.string.waiting));
        this.dir = new File(PATH_NAME);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(!canWriteExternalStorage() || !canRecordAudio() || !canAccessCoarseLocation() || !canAccessFineLocation() || !canAccessWiFiState() || !canAccessNetworkState() || !canInternet()){
                requestPermissions(INITIAL_PERMS, INITIAL_REQUEST);
            }
        }

    }

    private boolean canWriteExternalStorage() {
        return(hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE));
    }

    private boolean canRecordAudio() {
        return(hasPermission(Manifest.permission.RECORD_AUDIO));
    }

    private boolean canAccessFineLocation() {
        return(hasPermission(Manifest.permission.ACCESS_FINE_LOCATION));
    }

    private boolean canAccessCoarseLocation() {
        return(hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION));
    }

    private boolean canAccessWiFiState() {
        return(hasPermission(Manifest.permission.ACCESS_WIFI_STATE));
    }

    private boolean canAccessNetworkState() {
        return(hasPermission(Manifest.permission.ACCESS_NETWORK_STATE));
    }

    private boolean canInternet() {
        return(hasPermission(Manifest.permission.INTERNET));
    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean hasPermission(String perm) {
        return(PackageManager.PERMISSION_GRANTED == checkSelfPermission(perm));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode == INITIAL_REQUEST){
            if (canWriteExternalStorage() && canRecordAudio() && canAccessCoarseLocation() && canAccessFineLocation() && canAccessWiFiState() && canAccessNetworkState() && canInternet())  {
                //continue in the app
            }
            else {
                System.exit(0);
            }
        }

    }
}
