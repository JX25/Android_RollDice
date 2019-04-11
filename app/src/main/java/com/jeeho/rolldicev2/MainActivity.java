package com.jeeho.rolldicev2;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SensorEventListener{
    private Button start;
    private ImageView cube;
    private TextView info;
    private Sensor accelerometer;
    private SensorManager sensorManager;
    private boolean isNextUpdate = false;
    private long checkSensorTime = 100;
    private long lastUpdate, lastUpdateStop;
    private double prevX, prevY, prevZ, x,y,z;
    private int minimumTHRESHOLD = 100;
    private int averageTHRESHOLD = 300;
    private int maximumTHRESHOLD = 500;
    private int timeTHRESHOLD = 2500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        start = findViewById((R.id.startButton));
        cube = findViewById(R.id.cubeView);
        info = findViewById(R.id.infoBox);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lastUpdate = 0;
                lastUpdateStop =0;
                performAction();
            }});
    }

    private void changeCube(int i, String text, int time){
        info.setText(text);
        String cubeNumber = "d" + String.valueOf(i);
        int res = getResources().getIdentifier(cubeNumber,"drawable",getPackageName());
        cube.setImageResource(res);
        lastUpdateStop = lastUpdate;
        isNextUpdate = true;
        checkSensorTime = time;
    }

    private void performAction(){
        isNextUpdate = false;
        sensorManager.registerListener(this, accelerometer , SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            x = event.values[0];
            y = event.values[1];
            z = event.values[2];
            long currentTime = System.currentTimeMillis();
            if ((currentTime - lastUpdate) > checkSensorTime) {
                long diffrence = (currentTime - lastUpdate);
                lastUpdate = currentTime;
                double shakeSpeed = Math.abs(x + y + z - prevX - prevY - prevZ)/ diffrence * 10000;
                if (shakeSpeed > maximumTHRESHOLD) changeCube(value(),"Reaching max value of shaking",45);
                else if(shakeSpeed >= averageTHRESHOLD) changeCube(value(),"Raching average value of shaking",100);
                else if(shakeSpeed >= minimumTHRESHOLD) changeCube(value(),"Reaching minimum value of shaking",250);
                else {
                    currentTime = System.currentTimeMillis();
                    if(lastUpdate!=0) info.setText(" Is Stopping in two seconds... " + (currentTime - lastUpdateStop)/1000);
                    if (((currentTime - lastUpdateStop) > timeTHRESHOLD) && isNextUpdate == true){
                        info.setText("Drawing finished");
                        sensorManager.unregisterListener(this,sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
                    }
                }
                prevX = x;
                prevY = y;
                prevZ = z;
            }
        }
    }

    private int value() {
        return ((int)(x*4+y*2+z*5))%6+1;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}