package com.app.facerecogn;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

public class HomeActivity extends Activity {
    Button start, setting;
    TextView txt_version;
    boolean versiondisplay =true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_main);
        start = (Button) findViewById(R.id.start);
        setting = (Button) findViewById(R.id.setting);
        txt_version = (TextView) findViewById(R.id.version);

        Typeface font = Typeface.createFromAsset(getAssets(), "EUROSTIB.ttf");
        start.setTypeface(font);
        setting.setTypeface(font);
        txt_version.setTypeface(font);

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                finish();
                Intent in=new Intent(HomeActivity.this,MainActivity.class);
                startActivity(in);

            }
        });

        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (versiondisplay) {
                    txt_version.setVisibility(View.VISIBLE);
                    versiondisplay = false;
                }else {
                    txt_version.setVisibility(View.GONE);
                    versiondisplay = true;
                }
            }
        });
    }
}
