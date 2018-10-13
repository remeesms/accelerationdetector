package com.example.user.accelerationdetector;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private float lastX;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private float deltaX = 0;


    private float vibrateThreshold = 20;

    private float mAccel; // acceleration apart from gravity
    private float mAccelCurrent; // current acceleration including gravity
    private float mAccelLast; // last acceleration including gravity

    public Vibrator v;
    boolean indicateacc = true;
    boolean indicatebreak = true;
    TextView txt_acceleration,txt_breaking;
    int delayMillis = 5000;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeViews();

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) != null) {
            // success! we have an accelerometer
            Log.d("onCreate","success! we have an accelerometer");
            Toast.makeText(MainActivity.this,"success! we have an accelerometer",Toast.LENGTH_LONG).show();
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
//			vibrateThreshold = accelerometer.getMaximumRange() / 2;
            Log.d("accelerometer max range",String.valueOf(accelerometer.getMaximumRange()));
            mAccel = 0.00f;
            mAccelCurrent = SensorManager.GRAVITY_EARTH;
            mAccelLast = SensorManager.GRAVITY_EARTH;

        } else {
            // fai! we dont have an accelerometer!
            Log.d("onCreate","fail we don't have an accelerometer!");
            Toast.makeText(MainActivity.this,"fail, we don't have an accelerometer!",Toast.LENGTH_LONG).show();

        }

        //initialize vibration
        v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);

    }

    public void initializeViews() {
        txt_acceleration = (TextView) findViewById(R.id.txt_acceleration);
        txt_breaking = (TextView) findViewById(R.id.txt_break);
    }

    //onResume() register the accelerometer for listening the events
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    //onPause() unregister the accelerometer for stop listening the events
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {


        // get the change of the x,y,z values of the accelerometer
        deltaX = Math.abs(lastX - event.values[0]);


        // if the change is below 2 nothing to do
        if (deltaX < 2)
            deltaX = 0;

        // set the last know values of x,y,z
        lastX = event.values[0];

        if(deltaX < 20 && indicateacc){
//            Toast.makeText(MainActivity.this,"Smooth acceleration!",Toast.LENGTH_LONG).show();
//            Log.d("onSensorChanged","Smooth acceleration!");
            txt_acceleration.postDelayed(new Runnable() {
                public void run() {
                    txt_acceleration.setText("Smooth Acceleration!");
                    txt_acceleration.setCompoundDrawablesWithIntrinsicBounds(R.drawable.smile, 0, 0, 0);
                }
            }, delayMillis);
            indicateacc = false;
        }else if (deltaX > 20){
//            Toast.makeText(MainActivity.this,"Hard acceleration!",Toast.LENGTH_LONG).show();
//            Log.d("onSensorChanged","Hard acceleration!");
            txt_acceleration.postDelayed(new Runnable() {
                public void run() {
                    txt_acceleration.setText("Hard Acceleration!");
                    txt_acceleration.setCompoundDrawablesWithIntrinsicBounds(R.drawable.angry, 0, 0, 0);
                }
            }, 1000);
            indicateacc = true;
        }
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        mAccelLast = mAccelCurrent;
        mAccelCurrent = (float) Math.sqrt((double) (x*x + y*y + z*z));
        float delta = mAccelCurrent - mAccelLast;
        mAccel = mAccel * 0.9f + delta; // perform low-cut filter

        if(mAccel < 6 && indicatebreak){
//            Toast toast = Toast.makeText(getApplicationContext(), "Smooth Brakes.", Toast.LENGTH_LONG);
//            toast.show();
            txt_breaking.postDelayed(new Runnable() {
                public void run() {
                    txt_breaking.setText("Smooth Breaking!");
                    txt_breaking.setCompoundDrawablesWithIntrinsicBounds(R.drawable.smile, 0, 0, 0);
                }
            }, delayMillis);
            indicatebreak = false;
        }else if (mAccel > 12) {
            txt_breaking.postDelayed(new Runnable() {
                public void run() {
                    txt_breaking.setText("Hard Breaking!");
                    txt_breaking.setCompoundDrawablesWithIntrinsicBounds(R.drawable.angry, 0, 0, 0);
                }
            }, 1000);
            indicatebreak = true;
        }

        vibrate();

    }

    // if the change in the accelerometer value is big enough, then vibrate!
    public void vibrate() {
        if ((deltaX > vibrateThreshold) || (mAccel > 12)) {
            v.vibrate(50);
        }
    }

}
