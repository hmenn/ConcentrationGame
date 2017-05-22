package com.hasanmen.concentrationgame;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.hasanmen.concentrationgame.POJO.PixabayImage;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_KEY="MainActivity";

    private ImageButton btn_init=null;
    private ArrayList<PixabayImage> pixabayImages = new ArrayList<>();
    private ArrayList<Bitmap> bitmaps = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_init = (ImageButton)findViewById(R.id.imgBtn_init);

        btn_init.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    ImageListDownThread imageListDownThread = new ImageListDownThread(pixabayImages); // run thread to download list
                    imageListDownThread.start();

                    // TODO: ADD LOADING IMAGE

                    imageListDownThread.join(); // wait until image list downloaded

                    ImageDownThread imageDownThread = new ImageDownThread(pixabayImages.get(0),bitmaps);
                    imageDownThread.start();

                    imageDownThread.join();
                    Log.d(LOG_KEY,"Downloaded "+bitmaps.size());

                    btn_init.setImageBitmap(bitmaps.get(0));

                }catch (Exception e){
                    Log.d(LOG_KEY,"ImageListDownThread "+e.getMessage());
                }

            }
        });

    }
}
