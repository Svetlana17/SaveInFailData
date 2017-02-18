package com.yo.shishkoam.recognition;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

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
                writingData = !writingData;
                startButton.setText(writingData ? R.string.stop_writing : R.string.start_writing_data);
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
