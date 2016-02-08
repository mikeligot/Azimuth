package com.brainalgo.azimuth;

import android.content.Context;
import android.content.res.Configuration;
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
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager mSensorManager;
    private Sensor mRotationMatrixSensor;
    private float[] mRotationVectorData;
    private float[] mGravityVectorData;
    private float[]mMagneticFieldData;
    private int mDirection;
    private Date mStartTime;
    private ImageView mImageView;
    private TextView mTextViewBearing;
    private TextView mTextViewGravity;
    private TextView mTextViewMagnetic;
    private float mCurrentDegree = 0f;
    private String mBearingText;
    private Sensor mSensorMagnitude;
    private Sensor mSensorGravity;
    private boolean mIsRotationVectorHasData;
    private int mScreenRotation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImageView = (ImageView)findViewById(R.id.imageView);
        mImageView.setVisibility(View.INVISIBLE);
        mTextViewBearing = (TextView)findViewById(R.id.textViewBearing);
        mTextViewBearing.setText("");
        mTextViewGravity = (TextView)findViewById(R.id.TestViewGravity);
        mTextViewGravity.setText("");
        mTextViewMagnetic = (TextView)findViewById(R.id.textViewMagnetic);
        mTextViewMagnetic.setText("");
        this.mRotationVectorData = new float[3];
        mGravityVectorData = new float[3];
        mMagneticFieldData = new float[3];
    }
    public static final void fixRotation0(float[] orientation) {//azimuth, pitch, roll
        orientation[1] = -orientation[1];// pitch = -pitch
    }

    public static final  void fixRotation90(float[] orientation) {//azimuth, pitch, roll
        orientation[0] += Math.PI/2f; // offset
        float tmpOldPitch = orientation[1];
        orientation[1] = -orientation[2]; //pitch = -roll
        orientation[2] = -tmpOldPitch;	 // roll  = -pitch
    }

    public static final  void fixRotation180(float[] orientation) {//azimuth, pitch, roll
        orientation[0] = (float) (orientation[0] > 0f ? (orientation[0] - Math.PI): (orientation[0] + Math.PI));// offset
        orientation[2] = -orientation[2];// roll = -roll
    }

    public static final  void fixRotation270(float[] orientation) {//azimuth, pitch, roll
        orientation[0] -= Math.PI/2;// offset
        float tmpOldPitch = orientation[1];
        orientation[1] = orientation[2]; //pitch = roll
        orientation[2] = tmpOldPitch;	 // roll  = pitch
    }
    static final float ALPHA = 0.25f;


    protected float[] lowPass( float[] input, float[] output ) {
        if ( output == null ) return input;

        for ( int i=0; i<input.length; i++ ) {
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }
        return output;
    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        float TWENTY_FIVE_DEGREE_IN_RADIAN =(float) Math.toRadians(25);
        float ONE_FIFTY_FIVE_DEGREE_IN_RADIAN =(float) Math.toRadians(155);
        if( event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR ) {

            System.arraycopy(event.values, 0, mRotationVectorData, 0, mRotationVectorData.length);

            float[] var13 = new float[9];
            float[] var20 = new float[9];
            mTextViewGravity.setText("R: x =" + String.format("%.2f",mRotationVectorData[0]) + " y="+ String
                    .format("%.2f", mRotationVectorData[1])+ " z="+ String.format("%.2f",mRotationVectorData[2]));

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
                //mTextViewPitch.setText(String.valueOf(Math.toDegrees(inclination)));
            }

            // Optionally convert the result from radians to degrees

            mDirection = (int) Math.toDegrees(orientationVals[0]);
            int roll = (int) Math.toDegrees(orientationVals[2]);
           // mTextViewPitch.setText(String.valueOf(roll) + "-" + String.valueOf(event.accuracy));
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
                //System.out.println("DIR : " + mDirection);
               // System.out.println("DIRCUR : " + mCurrentDegree);
            }

            // Start the animation

            mCurrentDegree = -mDirection;
            if ( (360 >= mDirection && mDirection >= 337.5) || (0 <= mDirection && mDirection <= 22.5) ) mBearingText = "N";
            else if (mDirection > 22.5 && mDirection < 67.5) mBearingText = "NE";
            else if (mDirection >= 67.5 && mDirection <= 112.5) mBearingText = "E";
            else if (mDirection > 112.5 && mDirection < 157.5) mBearingText = "SE";
            else if (mDirection >= 157.5 && mDirection <= 202.5) mBearingText = "S";
            else if (mDirection > 202.5 && mDirection < 247.5) mBearingText = "SW";
            else if (mDirection >= 247.5 && mDirection <= 292.5) mBearingText = "W";
            else if (mDirection > 292.5 && mDirection < 337.5) mBearingText = "NW";
            mBearingText = String.valueOf(mDirection)+"°" + mBearingText;
            mTextViewBearing.setText(mBearingText);


        }else if(event.sensor.getType() == Sensor.TYPE_GRAVITY){
           /* if(mGravityVectorData == null){
                mGravityVectorData = event.values.clone();
            }else{
                mGravityVectorData = lowPass(event.values, mGravityVectorData);
                //mGravityVectorData = lowPass(mGravityVectorData,event.values );
            }*/
            System.arraycopy(event.values, 0, mGravityVectorData, 0, mGravityVectorData.length);
        }else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
        {
            mMagneticFieldData = event.values.clone();
        }
        if (!(mGravityVectorData == null || mMagneticFieldData == null))
        {
            float[] rotMatrix = new float[9];
            if (SensorManager.getRotationMatrix(rotMatrix, null,
                    mGravityVectorData, mMagneticFieldData))
            {
                mTextViewGravity.setText("G: x =" + String.format("%.2f",mGravityVectorData[0]) + " y="+ String
                        .format("%.2f", mGravityVectorData[1])+ " z="+ String.format("%.2f",mGravityVectorData[2]));
                mTextViewMagnetic.setText("M: x =" + String.format("%.2f",mMagneticFieldData[0]) + " y="+ String
                        .format("%.2f", mMagneticFieldData[1])+ " z="+ String.format("%.2f",mMagneticFieldData[2]));
                float inclination = (float) Math.acos(rotMatrix[8]);
                float[] orientationVals = new float[3];
                // device is flat
                if (inclination < TWENTY_FIVE_DEGREE_IN_RADIAN
                        || inclination > ONE_FIFTY_FIVE_DEGREE_IN_RADIAN)
                {
                    float[] orientation = SensorManager.getOrientation(rotMatrix, orientationVals);
                    mDirection = (int) Math.toDegrees(orientation[0]);
                    if (mDirection < 0) {
                        mDirection = mDirection + 360;
                    }
                    // mOrientation[0] = averageAngle();
                }
                else
                {
                    float[] var20 = new float[9];
                    SensorManager
                            .remapCoordinateSystem(rotMatrix,
                                    SensorManager.AXIS_X, SensorManager.AXIS_Z,
                                    var20);
                    System.out.println("NO FLAT");
                    float[] orientation = SensorManager.getOrientation(var20, orientationVals);
                    mDirection = (int) Math.toDegrees(orientation[0]);
                    if (mDirection < 0) {
                        mDirection = mDirection + 360;
                    }
                    //clearCompassHist();
                }

                if(Math.abs(Math.abs(mCurrentDegree) - mDirection) < 300 ){
                    RotateAnimation ra = new RotateAnimation(mCurrentDegree, -mDirection,
                            Animation.RELATIVE_TO_SELF, 0.5f,
                            Animation.RELATIVE_TO_SELF,
                            0.5f);

                    // how long the animation will take place
                    ra.setDuration(100);
                    ra.setInterpolator(new LinearInterpolator());

                    // set the animation after the end of the reservation status
                    ra.setFillAfter(true);
                    mImageView.startAnimation(ra);
                    //System.out.println("DIR : " + mDirection);
                    // System.out.println("DIRCUR : " + mCurrentDegree);
                }
                mCurrentDegree = -mDirection;
                mBearingText= String.valueOf(mDirection);
                mTextViewBearing.setText(mBearingText);
            }
            //mDirection = getOrientation();
        }


        if(System.currentTimeMillis() - mStartTime.getTime()<1000){
            mDirection = 1000;
            mIsRotationVectorHasData = false;


        }else{
            mIsRotationVectorHasData = true;
            mImageView.setVisibility(View.VISIBLE);

        }

    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        mScreenRotation = getWindowManager().getDefaultDisplay().getRotation();
    //    Log.v(TAG, "onConfigurationChanged(): screenRotation:" + (screenRotation * 90) + " degree");


    }
    @Override
    protected void onStop() {
        super.onStop();
       // mSensorManager.flush(this);
        mSensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mRotationMatrixSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        mSensorMagnitude = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mSensorGravity = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        if(mRotationMatrixSensor == null){
            Toast.makeText(MainActivity.this, "sensors are not available", Toast.LENGTH_LONG).show();
        }
        mStartTime = new Date();
        mIsRotationVectorHasData=false;
        mSensorManager.registerListener(MainActivity.this,mRotationMatrixSensor,SensorManager
                .SENSOR_DELAY_NORMAL);
     /*   mSensorManager.registerListener(MainActivity.this,mSensorMagnitude,SensorManager
                .SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(MainActivity.this,mSensorGravity,SensorManager
                .SENSOR_DELAY_NORMAL);*/

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
