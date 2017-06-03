package com.hasanmen.concentrationgame;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.hasanmen.concentrationgame.POJO.PixabayImage;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_KEY = "MainActivity";
    private final static String REQUEST = "https://pixabay.com/api/?key=5429432-e78aeb946ac4de95966734b5c";
    private final int IMAGE_CROP_SIZE = 30;

    private ArrayList<Button> buttons = new ArrayList<>();
    private ArrayList<PixabayImage> pixabayImages = new ArrayList<>();
    private TableLayout tableLayout = null;
    private TextView tv_score;
    private TextView tv_change;
    private ArrayList<PixabayImage> randomImages = new ArrayList<>();

    private Button btnStart = null;
    private Button click2Btn;
    private Button click1Btn;

    private int trueNum; // number of true guess
    private int falseNum; // number of fail guess
    private int currentGameLevel;
    private int clickNum; // number of button clicks
    private int changeToNextGame;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnStart = (Button) findViewById(R.id.btn_start);
        tableLayout = (TableLayout) findViewById(R.id.btnArea);

        currentGameLevel = 4;
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnStart.setEnabled(false);
                startGame();
            }
        });


        tv_score = (TextView) findViewById(R.id.tv_score);
        tv_score.setText(R.string.score_board_init);

        tv_change = (TextView) findViewById(R.id.tv_change);

    }

    private void startGame() {
        trueNum = 0;
        falseNum = 0;
        clickNum = 0;
        try {
            pixabayImages.clear();
            buttons.clear();
            randomImages.clear();

            Toast.makeText(MainActivity.this, "Downloading Images", Toast.LENGTH_SHORT).show();

            Thread imageListDownThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    final String LOG_THREAD1_KEY = "GeneralImageDownThread";
                    StringBuilder timeSB = new StringBuilder();
                    long execTime = System.currentTimeMillis();
                    timeSB.append("Start:").append(new SimpleDateFormat("HH:mm:ss:SSS ").format(Calendar.getInstance().getTime()));
                    URL url = null;
                    HttpURLConnection urlConnection = null;
                    ArrayList<ImageDownThread> imageDownThreads = new ArrayList<>(); // these thread will parallel download images

                    try {
                        int imageNumber;
                        StringBuilder requestSB = new StringBuilder(REQUEST);
                        imageNumber = currentGameLevel * currentGameLevel / 2;
                        // prepare request
                        requestSB.append("&per_page=" + imageNumber);
                        requestSB.append("&q=flowers");
                        requestSB.append("&order=latest");

                        url = new URL(requestSB.toString());
                        urlConnection = (HttpURLConnection) url.openConnection();

                        Log.d(LOG_THREAD1_KEY, "Request url:" + requestSB.toString());

                        StringBuilder jsonMsg = new StringBuilder();

                        BufferedReader bfr = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

                        String line;
                        while ((line = bfr.readLine()) != null) {
                            jsonMsg.append(line);
                        }
                        //Log.d(LOG_KEY, "Response JSON:"+jsonMsg.toString());

                        // parse json string and get image list
                        pixabayImages = parsePixaJSON(jsonMsg.toString());

                        for (int i = 0; i < imageNumber * 2; ++i) {
                            ImageDownThread imageDownThread = new ImageDownThread(pixabayImages.get(i / 2), randomImages);
                            imageDownThreads.add(imageDownThread);
                            imageDownThread.start();
                            Log.d(LOG_THREAD1_KEY, pixabayImages.get(i / 2).toString());
                        }
                        Log.d(LOG_THREAD1_KEY, "Download :" + imageNumber + " image address");

                        for (int i = 0; i < imageNumber * 2; ++i) {
                            imageDownThreads.get(i).join();
                            //Log.d(LOG_THREAD1_KEY, "Thread " + i + " joined");
                        }
                    } catch (Exception e) {
                        Log.e(LOG_THREAD1_KEY, e.getMessage());
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "Downloading Images failed!", Toast.LENGTH_LONG).show();
                            }
                        });
                    } finally {
                        // close url connection
                        if (urlConnection != null)
                            urlConnection.disconnect();

                        execTime = System.currentTimeMillis() - execTime;
                        timeSB.append("-- End:").append(new SimpleDateFormat("HH:mm:ss:SSS ").format(Calendar.getInstance().getTime()));
                        timeSB.append("-- Execution Time(ms):").append(String.valueOf(execTime));
                        Log.d(LOG_THREAD1_KEY, timeSB.toString());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                btnStart.setText("RESTART");

                                btnStart.setEnabled(true);
                            }
                        });
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setButtonBackgrounds();
                        }
                    });
                }
            });

            initBtnArea(currentGameLevel);
            imageListDownThread.start();
            tv_score.setText(R.string.score_board_init);
            changeToNextGame = currentGameLevel * 10;
            tv_change.setText("Change:" + changeToNextGame);
        } catch (Exception e) {
            Log.d("ImageListDownThread", e.getMessage());
        } finally {
        }

    }

    private void setButtonBackgrounds() {
        Bitmap scaledQuestion = BitmapFactory.decodeResource(getResources(), R.drawable.question_mark);
        scaledQuestion = Bitmap.createScaledBitmap(scaledQuestion, buttons.get(0).getWidth() - IMAGE_CROP_SIZE, buttons.get(0).getHeight() - IMAGE_CROP_SIZE, true);
        BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), scaledQuestion);
        for (int i = 0; i < buttons.size(); ++i) {
            buttons.get(i).setBackground(bitmapDrawable);
            buttons.get(i).setEnabled(true);
        }
        Toast.makeText(MainActivity.this, "Images Downloaded.", Toast.LENGTH_SHORT).show();
    }

    private void initBtnArea(int n) {
        tableLayout.removeAllViews();
        for (int i = 0; i < n; ++i) {
            TableRow tableRow = new TableRow(MainActivity.this);
            tableRow.setLayoutParams(new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.MATCH_PARENT,
                    1.0f
            ));
            for (int j = 0; j < n; ++j) {
                Button btn = new Button(MainActivity.this);
                btn.setId(i * n + j);
                btn.setLayoutParams(new TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.MATCH_PARENT,
                        1.0f
                ));
                //btn.setBackground(null);
                btn.setEnabled(false);
                btn.setPadding(5, 5, 5, 5);
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Toast.makeText(MainActivity.this,String.valueOf(clickedBtn.getId()),Toast.LENGTH_SHORT).show();
                        Button clickedBtn = (Button) v;
                        gameLogic(clickedBtn);
                    }
                });

                buttons.add(btn);
                tableRow.addView(btn);
            }
            tableLayout.addView(tableRow);
        }
    }

    private void gameLogic(Button imgBtn) {

        if (changeToNextGame != 0) {
            if (clickNum == 0) {
                click1Btn = imgBtn;
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(randomImages.get(click1Btn.getId()).getBitmap(), imgBtn.getWidth() - IMAGE_CROP_SIZE, imgBtn.getHeight() - IMAGE_CROP_SIZE, true);

                ++clickNum;

                click1Btn.setBackground(new BitmapDrawable(getResources(), scaledBitmap));

                click1Btn.setEnabled(false);

            } else if (clickNum == 1) {
                click2Btn = imgBtn;
                //imgBtn.setImageBitmap(bitmaps.get(imgBtn.getId()));
                final PixabayImage image1 = randomImages.get(click1Btn.getId());
                final PixabayImage image2 = randomImages.get(click2Btn.getId());

                click2Btn.setBackground(new BitmapDrawable(getResources(), image2.getBitmap()));

                if (image1.equals(image2)) { // if images are equal, open two button
                    click1Btn.setEnabled(false);
                    click2Btn.setEnabled(false);
                    ++trueNum;
                } else {
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Bitmap scaledQuestion = BitmapFactory.decodeResource(getResources(), R.drawable.question_mark);
                            scaledQuestion = Bitmap.createScaledBitmap(scaledQuestion, click1Btn.getWidth() - IMAGE_CROP_SIZE, click1Btn.getHeight() - IMAGE_CROP_SIZE, true);
                            BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), scaledQuestion);

                            click1Btn.setBackground(bitmapDrawable);
                            click1Btn.setEnabled(true);
                            click2Btn.setBackground(bitmapDrawable);
                            click2Btn.setEnabled(true);
                        }
                    }, 300);
                    ++falseNum;
                    --changeToNextGame;
                    tv_change.setText("Change:" + changeToNextGame);
                }
                --clickNum;


            }
            tv_score.setText("SCORE -> True:" + trueNum + " False:" + falseNum);

            if (trueNum == (currentGameLevel * 2) && (falseNum < (currentGameLevel * 10))) {
                currentGameLevel += 2;
                startGame();
            }
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "You Failed! Try Again!", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private ArrayList<PixabayImage> parsePixaJSON(String json) {

        ArrayList<PixabayImage> images = new ArrayList<>();

        if (json.isEmpty()) {
            Log.e(LOG_KEY, "EMPTY JSON STRING");
            return null;
        }
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonImageArray = jsonObject.getJSONArray("hits");
            for (int i = 0; i < jsonImageArray.length(); ++i) {
                PixabayImage image = new PixabayImage();
                image.setPreviewURL(jsonImageArray.getJSONObject(i).getString("previewURL"));
                image.setImageHeight(jsonImageArray.getJSONObject(i).getInt("imageHeight"));
                image.setImageWidth(jsonImageArray.getJSONObject(i).getInt("imageWidth"));
                image.setWebformatURL(jsonImageArray.getJSONObject(i).getString("webformatURL"));
                images.add(image);
                //Log.d(LOG_KEY, "Image:" + image.toString());
            }
        } catch (Exception e) {
            Log.e(LOG_KEY, e.getMessage());
        }
        return images;
    }
}
