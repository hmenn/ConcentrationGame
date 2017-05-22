package com.hasanmen.concentrationgame;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button btn_init=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_init = (Button)findViewById(R.id.btn_init);

        btn_init.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageListDownThread imageListDownThread = new ImageListDownThread(); // run thread to download list
                imageListDownThread.start();
            }
        });

    }
}
