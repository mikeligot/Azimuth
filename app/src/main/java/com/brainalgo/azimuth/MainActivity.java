package com.brainalgo.azimuth;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.Date;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager mSensorManager;
    private Sensor mRotationMatrixSensor;
    private float[] mRotationVectorData;
    private int mDirection;
    private Date mStartTime;
    private ImageView mImageView;

    private float mCurrentDegree = 0f;
    private String mBearingText;
    private boolean mIsRotationVectorHasData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImageView = (ImageView)findViewById(R.id.imageView);
        mImageView.setVisibility(View.GONE);
        this.mRotationVectorData = new float[3];
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if( event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR ) {

            System.arraycopy(event.values, 0, mRotationVectorData, 0, mRotationVectorData.length);

            float[] var13 = new float[9];
            float[] var20 = new float[9];
            float TWENTY_FIVE_DEGREE_IN_RADIAN =(float) Math.toRadians(25);
            float ONE_FIFTY_FIVE_DEGREE_IN_RADIAN =(float) Math.toRadians(155);

            SensorManager.getRotationMatrixFromVector(var13, mRotationVectorData);
            float[] orientationVals = new float[3];
            float inclination = (float) Math.acos(var13[8]);
            if (inclination < TWENTY_FIVE_DEGREE_IN_RADIAN
                    || inclination > ONE_FIFTY_FIVE_DEGREE_IN_RADIAN)
            {
                // device is flat just call getOrientation

                SensorManager.getOrientation(var13, orientationVals);
                //System.out.println("FLAT");
            }
            else
            {
                SensorManager
                        .remapCoordinateSystem(var13,
                                SensorManager.AXIS_X, SensorManager.AXIS_Z,
                                var20);
                //System.out.println("NO FLAT");
                SensorManager.getOrientation(var20, orientationVals);
            }

            // Optionally convert the result from radians to degrees

            mDirection = (int) Math.toDegrees(orientationVals[0]);
            if (mDirection < 0) {
                mDirection = mDirection + 360;
            }
           // mDirection = mDirection % 360;

            // create a rotation animation (reverse turn degree degrees)
            if(Math.abs(Math.abs(mCurrentDegree) - mDirection) < 300 ){
                RotateAnimation ra = new RotateAnimation(mCurrentDegree, -mDirection,
                        Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF,
                        0.5f);

                // how long the animation will take place
                ra.setDuration(200);
                ra.setInterpolator(new LinearInterpolator());

                // set the animation after the end of the reservation status
                ra.setFillAfter(true);
                mImageView.startAnimation(ra);
                System.out.println("DIR : " + mDirection);
               // System.out.println("DIRCUR : " + mCurrentDegree);
            }else{
               // System.out.println("DIRNOTROTATE : " + mCurrentDegree);
            }

            // Start the animation

            mCurrentDegree = -mDirection;
  /*          if ( (360 >= mDirection && mDirection >= 337.5) || (0 <= mDirection && mDirection <= 22.5) ) mBearingText = "N";
            else if (mDirection > 22.5 && mDirection < 67.5) mBearingText = "NE";
            else if (mDirection >= 67.5 && mDirection <= 112.5) mBearingText = "E";
            else if (mDirection > 112.5 && mDirection < 157.5) mBearingText = "SE";
            else if (mDirection >= 157.5 && mDirection <= 202.5) mBearingText = "S";
            else if (mDirection > 202.5 && mDirection < 247.5) mBearingText = "SW";
            else if (mDirection >= 247.5 && mDirection <= 292.5) mBearingText = "W";
            else if (mDirection > 292.5 && mDirection < 337.5) mBearingText = "NW";

            mBearingText+=" " + String.valueOf(mDirection);*/


        }



        if(System.currentTimeMillis() - mStartTime.getTime()<1000){
            mDirection = 1000;
            mIsRotationVectorHasData = false;
            mImageView.setVisibility(View.VISIBLE);

        }else{
            mIsRotationVectorHasData = true;
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        mSensorManager.unregisterListener(this,mRotationMatrixSensor);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mRotationMatrixSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        if(mRotationMatrixSensor == null){
            Toast.makeText(MainActivity.this, "sensors are not available", Toast.LENGTH_LONG).show();
        }
        mStartTime = new Date();
        mIsRotationVectorHasData=false;
        mSensorManager.registerListener(MainActivity.this,mRotationMatrixSensor,SensorManager
                .SENSOR_DELAY_NORMAL);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if (sensor.getType() == Sensor.TYPE_ROTATION_VECTOR ){

            if(accuracy != SensorManager.SENSOR_STATUS_ACCURACY_HIGH ){
                Toast.makeText(MainActivity.this, "sensors are not calibrated", Toast.LENGTH_LONG).show();
            }else{
                // textView.setVisibility(View.GONE);
                //Toast.makeText(MainActivity.this, "sensors are calibrated", Toast.LENGTH_SHORT).show();
            }

        }
    }
}
