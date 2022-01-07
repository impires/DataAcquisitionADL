package layout;

import android.app.Service;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import phdsound.ubi.com.phdsoundfingerprinting.Variables;

public class SoundCapture extends Service {

    private Handler Handler = null;
    private Runnable runnableCode = null;
    private AudioRecord audioRecord;
    private static int SAMPLE_RATE_CD = 44100;
    private static int audioSource = MediaRecorder.AudioSource.MIC;
    private static int channelConfig = AudioFormat.CHANNEL_IN_MONO;
    private static int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private int bufferSizeInBytes;
    private long startTime = 0;
    private long currentTime = 0;
    private int frameByteSize; // short = 2 bytes
    private byte sData[];
    private static final String PATH_NAME = Environment.getExternalStorageDirectory().getAbsolutePath() + "/PhDSoundFilesNew/";

    public SoundCapture() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        this.bufferSizeInBytes = AudioRecord.getMinBufferSize(SAMPLE_RATE_CD, channelConfig, audioFormat);
        this.frameByteSize = bufferSizeInBytes / 2;
        this.audioRecord = new AudioRecord(audioSource, SAMPLE_RATE_CD, channelConfig, audioFormat, bufferSizeInBytes);
        this.sData = new byte[frameByteSize];

        HandlerThread thread = new HandlerThread("PhD Sound Capture", android.os.Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        Looper serviceLooper = thread.getLooper();
        this.Handler = new Handler(serviceLooper);
        this.audioRecord.startRecording();

        // Define the code block to be executed
        this.runnableCode = new Runnable() {
            @Override
            public void run() {

                // Do something here on the main thread
                startTime = System.currentTimeMillis();

                writeAudioDataToFile();

                // Repeat this the same runnable code block again another 5 minutes
                Handler.postDelayed(runnableCode, Variables.TIME_LOOP);
            }
        };
    }

    private void writeAudioDataToFile() {
        boolean error = false;

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
        Date now = new Date();
        String adl_path = Variables.ADL_SELECTED+"/";
        String fileName = "PhDSound_"+formatter.format(now);
        Variables.folderPath = PATH_NAME+adl_path+fileName+"_"+now.getTime()+"/";
        File adlFolder = new File(PATH_NAME+adl_path);
        adlFolder.mkdirs();
        File currFolder = new File(Variables.folderPath);
        currFolder.mkdirs();

        String filePath = Variables.folderPath + "sound.txt";
        File file = new File(filePath);
        Variables.timeCaptureStart = now.getTime();
        Variables.timeCaptureEnd = now.getTime() + Variables.TIME_DEFAULT;

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (Exception e) {
                error = true;
            }
        }

        if(error == false) {

            currentTime = System.currentTimeMillis();

            try {
                while ((currentTime - startTime) <= Variables.TIME_DEFAULT) {
                    // gets the voice output from microphone to byte format
                    try {
                        audioRecord.read(sData, 0, frameByteSize);

                        //BufferedWriter for performance, true to set append to file flag
                        BufferedWriter buf = new BufferedWriter(new FileWriter(file, true));
                        buf.append(Arrays.toString(sData).replace("[", "").replace("]", "").replace(", ", "\n"));
                        buf.newLine();
                        buf.close();

                    } catch (Exception e) {
                        // nao faz nada
                    }

                    currentTime = System.currentTimeMillis();
                }

            } catch (Exception e) {
                // nao faz nada
            }

        } else {
            // nao faz nada
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null) { // May not have an Intent is the service was killed and restarted (See STICKY_SERVICE).
            Handler.post(runnableCode);
        }
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(Handler != null) {
            Handler.removeCallbacks(runnableCode);
        }

        this.audioRecord.stop();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
