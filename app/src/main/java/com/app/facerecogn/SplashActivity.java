package com.app.facerecogn;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Thread timer= new Thread()
        {
            public void run()
            {
                try
                {
                    //Display for 1 seconds
                    sleep(1000);
                }
                catch (InterruptedException e)
                {
                    // TODO: handle exception
                    e.printStackTrace();
                }
                finally
                {
                    //Goes to Activity  StartingPoint.java(STARTINGPOINT)
                    /*Intent openstartingpoint=new Intent("x.y.z.START");
                    startActivity(openstartingpoint);*/

                    Intent intent = new Intent(SplashActivity.this, HomeActivity.class);
                    startActivity(intent);

                }
            }
        };
        timer.start();
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        finish();
    }
}
