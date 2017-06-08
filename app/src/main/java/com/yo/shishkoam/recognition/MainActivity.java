package com.yo.shishkoam.recognition;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class MainActivity extends AppCompatActivity {
    private TextView tvText;
    private Button startButton;
    private SensorManager sensorManager;
    private Sensor sensorAccel;
    private Sensor sensorLinAccel;
    private Sensor sensorGravity;
    private StringBuilder sb = new StringBuilder();
    private DBHelper dbHelper;
    private Timer timer;
    private final static int UPDATE_TIME = 400;
    private boolean writingData = false;

    private float[] valuesAccel = new float[3];
    private float[] valuesAccelMotion = new float[3];
    private float[] valuesAccelGravity = new float[3];
    private float[] valuesLinAccel = new float[3];
    private float[] valuesGravity = new float[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvText = (TextView) findViewById(R.id.tvText);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorAccel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorLinAccel = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        sensorGravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        dbHelper = new DBHelper(this);
        startButton = (Button) findViewById(R.id.start);
        Button saveButton = (Button) findViewById(R.id.save);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
            }
        });
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (writingData) {
                    saveData();
                } else {
                    writingData = !writingData;
                    startButton.setText(writingData ? R.string.stop_writing : R.string.start_writing_data);
                }
            }
        });
        Button shareButton = (Button) findViewById(R.id.share);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                share();
            }
        });

    }

    private void saveData() {
        boolean result = Utils.saveFile(
                new File(getExternalFilesDir(null), "accel" + System.currentTimeMillis() + ".txt"),
                dbHelper.readData().getBytes());
        if (!result) {
            Toast.makeText(getApplicationContext(), "Can't write to file!", Toast.LENGTH_LONG).show();
        }
        dbHelper.clearDataBase();
        writingData = false;
        startButton.setText(R.string.start_writing_data);
    }

    private void share() {
        File dir = getExternalFilesDir(null);


        File zipFile = new File(dir, "accel.zip");
        if (zipFile.exists()) {
            zipFile.delete();
        }
        File[] fileList = dir.listFiles();
        try {
            zipFile.createNewFile();
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile));
            out.setLevel(Deflater.DEFAULT_COMPRESSION);
            for (File file : fileList) {
                zipFile(out, file);
            }
            out.close();
            sendBundleInfo(zipFile);
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "Can't send file!", Toast.LENGTH_LONG).show();
        }
    }

    private static void zipFile(ZipOutputStream zos, File file) throws IOException {
        zos.putNextEntry(new ZipEntry(file.getName()));
        FileInputStream fis = new FileInputStream(file);
        byte[] buffer = new byte[4092];
        int byteCount = 0;
        try {
            while ((byteCount = fis.read(buffer)) != -1) {
                zos.write(buffer, 0, byteCount);
            }
        } finally {
            safeClose(fis);
        }
        zos.closeEntry();
    }

    private static void safeClose(FileInputStream fis) {
        try {
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendBundleInfo(File file) {
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setType("message/rfc822");
            emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + file));
        startActivity(Intent.createChooser(emailIntent, "Send data"));
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(listener, sensorAccel, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(listener, sensorLinAccel, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(listener, sensorGravity, SensorManager.SENSOR_DELAY_NORMAL);

        timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showInfo();
                        if (writingData) {
                            dbHelper.addData(valuesAccel, valuesAccelMotion, valuesLinAccel);
                        }
                    }
                });
            }
        };
        timer.schedule(task, 0, UPDATE_TIME);
    }

    @Override
    protected void onPause() {
        super.onPause();
//        saveData();
        sensorManager.unregisterListener(listener);
        timer.cancel();
    }


    String format(float values[]) {
        return String.format(Locale.US, "%1$.2f\t\t%2$.2f\t\t%3$.2f", values[0], values[1], values[2]);
    }

    void showInfo() {
        sb.setLength(0);
        sb.append("Accelerometer: ").append(format(valuesAccel))
                .append("\n\nAccel motion: ").append(format(valuesAccelMotion))
                .append("\nAccel gravity : ").append(format(valuesAccelGravity))
                .append("\n\nLin accel : ").append(format(valuesLinAccel))
                .append("\nGravity : ").append(format(valuesGravity));
        tvText.setText(sb);
    }


    SensorEventListener listener = new SensorEventListener() {
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            switch (event.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                    for (int i = 0; i < 3; i++) {
                        valuesAccel[i] = event.values[i];
                        valuesAccelGravity[i] = (float) (0.1 * event.values[i] + 0.9 * valuesAccelGravity[i]);
                        valuesAccelMotion[i] = event.values[i] - valuesAccelGravity[i];
                    }
                    break;
                case Sensor.TYPE_LINEAR_ACCELERATION:
                    System.arraycopy(event.values, 0, valuesLinAccel, 0, 3);
                    break;
                case Sensor.TYPE_GRAVITY:
                    System.arraycopy(event.values, 0, valuesGravity, 0, 3);
                    break;
            }
        }
    };
}
