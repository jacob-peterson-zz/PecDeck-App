/* Author: Jacob Peterson
 * Title: PecDeck
 * Emulates the deck of cards push-up challenge
 */
package com.jpeterson.pecdeck;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.StringTokenizer;

/* Workout - Main Activity */
public class workout extends AppCompatActivity {
    ArrayList<Integer> shuffle = new ArrayList<>();
    ImageView cards;
    TextView stopWatch;
    TextView botText;
    TextView total;
    int touches = 0;
    int cardsFlipped = 0;
    int totalPushups = 0;
    ArrayList<Integer> picList = loadImages();
    ArrayList<Integer> cardValues = loadCardVals();
    boolean pushupsDone = false;
    Toolbar myToolbar;
    int screenHeight;
    int screenWidth;
    Dialog popup;
    String spadesWorkout = "Push-ups";
    String heartsWorkout = "Push-ups";
    String diamondsWorkout = "Push-ups";
    String clubsWorkout = "Push-ups";
    boolean customWorkout = false;
    EditText spadeCustom;
    EditText heartCustom;
    EditText diamondsCustom;
    EditText clubsCustom;

    /* onCreate
     * savedInstanceState - Saved data from when the app was last destroyed
     * Responsibilities:
     *  Get saved variables
     *  Create initial UI
     *  Shuffle deck
     *  Initialize Image view for the deck
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout);
        total = (TextView) findViewById(R.id.totalPushups);
        botText = (TextView) findViewById(R.id.pushups);
        stopWatch = (TextView) findViewById(R.id.timer);

        getScreenSize();
        if(screenHeight > 1500){
            botText.setTextSize(TypedValue.COMPLEX_UNIT_SP,40);
            stopWatch.setTextSize(TypedValue.COMPLEX_UNIT_SP,50);
        } else if(screenHeight < 1001){
            stopWatch.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
        }

        myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        shuffleDeck();

        //Create image view that changes picture on touch
        cards = (ImageView) findViewById(R.id.cards);
        cards.setOnClickListener(new View.OnClickListener(){
            boolean restFinished = true;
            @Override
            public void onClick(View v) {
                if(restFinished) {
                    if (cardsFlipped < 55) {
                        if ((touches % 2) == 0) {
                            pushupsDone = false;
                            cards.setImageResource(picList.get(shuffle.get(cardsFlipped)));
                            int value = cardValues.get(shuffle.get(cardsFlipped));
                            String newText = value + " Push-ups!";
                            if(value != 30) {
                                if(customWorkout){
                                    if((shuffle.get(cardsFlipped) % 4) == 0){
                                        newText = value + " " + clubsWorkout + "!";
                                    }else if((shuffle.get(cardsFlipped) % 4) == 1){
                                        newText = value + " " + diamondsWorkout + "!";
                                    }else if((shuffle.get(cardsFlipped) % 4) == 2){
                                        newText = value + " " + heartsWorkout + "!";
                                    }else if((shuffle.get(cardsFlipped) % 4) == 3){
                                        newText = value + " " + spadesWorkout + "!";
                                    }
                                }
                                totalPushups = totalPushups + value;
                                botText.setText(newText);
                            }else{
                                botText.setText(R.string.joker);
                            }
                            cardsFlipped++;
                        } else {
                            pushupsDone = true;
                            restFinished = false;
                            botText.setText(R.string.rest);
                            if(customWorkout){
                                total.setText("Total Exercises: " + totalPushups);
                            }else {
                                total.setText("Total Push-ups: " + totalPushups);
                            }

                            //Create a rest timer
                            CountDownTimer timer = new CountDownTimer(cardValues.get(shuffle.get(cardsFlipped - 1)) * 2010, 1000) {
                                public void onTick(long millisUntilFinished) {
                                    stopWatch.setText(millisUntilFinished / 1000 + "s");
                                }
                                public void onFinish() {
                                    stopWatch.setText(R.string.go);
                                    restFinished = true;
                                }
                            }.start();
                        }
                        touches++;
                    } else{
                        cards.setImageResource(R.drawable.back);
                        cardsFlipped = 0;
                        touches = 0;
                        botText.setText(R.string.congrats);
                        Collections.shuffle(shuffle);
                        pushupsDone = false;
                    }
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /* onPause - called when the app is paused or closed
     * Save variables so user can start where they left off
     */
    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences totalPush = getPreferences(0);
        SharedPreferences.Editor editor = totalPush.edit();

        editor.putInt("totalPush", totalPushups);
        editor.putInt("touches", touches);
        editor.putInt("flipped", cardsFlipped);
        editor.putBoolean("pDone", pushupsDone);
        editor.putBoolean("custom", customWorkout);
        editor.putString("spades", spadesWorkout);
        editor.putString("hearts", heartsWorkout);
        editor.putString("diamonds", diamondsWorkout);
        editor.putString("clubs", clubsWorkout);

        if(!shuffle.isEmpty()) {
            StringBuilder str = new StringBuilder();
            for (int i = 0; i < 54; i++) {
                str.append(shuffle.get(i)).append(",");
            }
            editor.putString("shuffle", str.toString());
        }
        editor.apply();
    }


    //Reshuffle, set image to card back, and go back to beginning of deck
    public void resetClicked(MenuItem item) {
        cards.setImageResource(R.drawable.back);
        touches = 0;
        cardsFlipped = 0;
        Collections.shuffle(shuffle);
        botText.setText(R.string.newDeck);
    }


    //Reset the total push-ups counter back to 0
    public void resetPushups(MenuItem item) {
        totalPushups = 0;
        if(customWorkout){
            total.setText("Total Exercises: " + totalPushups);
        }else {
            total.setText("Total Push-ups: " + totalPushups);
        }
    }


    //Change to a custom workout
    public void customizeClicked(MenuItem item) {
        popup = new Dialog(workout.this);
        popup.setContentView(R.layout.popup_menu);
        spadeCustom = popup.findViewById(R.id.customSpades);
        heartCustom = popup.findViewById(R.id.customHearts);
        diamondsCustom = popup.findViewById(R.id.customDiamonds);
        clubsCustom = popup.findViewById(R.id.customClubs);
        popup.show();
    }

    //Customize workout form is submitted
    public void submitClick(View view) {
        try {
            spadesWorkout = spadeCustom.getText().toString();
            heartsWorkout = heartCustom.getText().toString();
            diamondsWorkout = diamondsCustom.getText().toString();
            clubsWorkout = clubsCustom.getText().toString();
        }catch (NullPointerException e){
        }
        total.setText("Total Exercises: " + totalPushups);
        customWorkout = true;
        popup.hide();
    }


    //Set back to a push-up workout
    public void backToDefault(MenuItem item) {
        customWorkout = false;
        spadesWorkout = "Push-ups";
        heartsWorkout = "Push-ups";
        diamondsWorkout = "Push-ups";
        clubsWorkout = "Push-ups";
        total.setText("Total Push-ups: " + totalPushups);
    }


    //Add all cards images into an array list
    private ArrayList<Integer> loadImages() {

        ArrayList<Integer> picList = new ArrayList<>();

        picList.add(R.drawable.a2_of_clubs);
        picList.add(R.drawable.a2_of_diamonds);
        picList.add(R.drawable.a2_of_hearts);
        picList.add(R.drawable.a2_of_spades);
        picList.add(R.drawable.a3_of_clubs);
        picList.add(R.drawable.a3_of_diamonds);
        picList.add(R.drawable.a3_of_hearts);
        picList.add(R.drawable.a3_of_spades);
        picList.add(R.drawable.a4_of_clubs);
        picList.add(R.drawable.a4_of_diamonds);
        picList.add(R.drawable.a4_of_hearts);
        picList.add(R.drawable.a4_of_spades);
        picList.add(R.drawable.a5_of_clubs);
        picList.add(R.drawable.a5_of_diamonds);
        picList.add(R.drawable.a5_of_hearts);
        picList.add(R.drawable.a5_of_spades);
        picList.add(R.drawable.a6_of_clubs);
        picList.add(R.drawable.a6_of_diamonds);
        picList.add(R.drawable.a6_of_hearts);
        picList.add(R.drawable.a6_of_spades);
        picList.add(R.drawable.a7_of_clubs);
        picList.add(R.drawable.a7_of_diamonds);
        picList.add(R.drawable.a7_of_hearts);
        picList.add(R.drawable.a7_of_spades);
        picList.add(R.drawable.a8_of_clubs);
        picList.add(R.drawable.a8_of_diamonds);
        picList.add(R.drawable.a8_of_hearts);
        picList.add(R.drawable.a8_of_spades);
        picList.add(R.drawable.a9_of_clubs);
        picList.add(R.drawable.a9_of_diamonds);
        picList.add(R.drawable.a9_of_hearts);
        picList.add(R.drawable.a9_of_spades);
        picList.add(R.drawable.a10_of_clubs);
        picList.add(R.drawable.a10_of_diamonds);
        picList.add(R.drawable.a10_of_hearts);
        picList.add(R.drawable.a10_of_spades);
        picList.add(R.drawable.a11_of_clubs);
        picList.add(R.drawable.a11_of_diamonds);
        picList.add(R.drawable.a11_of_hearts);
        picList.add(R.drawable.a11_of_spades);
        picList.add(R.drawable.a12_of_clubs);
        picList.add(R.drawable.a12_of_diamonds);
        picList.add(R.drawable.a12_of_hearts);
        picList.add(R.drawable.a12_of_spades);
        picList.add(R.drawable.a13_of_clubs);
        picList.add(R.drawable.a13_of_diamonds);
        picList.add(R.drawable.a13_of_hearts);
        picList.add(R.drawable.a13_of_spades);
        picList.add(R.drawable.a14_of_clubs);
        picList.add(R.drawable.a14_of_diamonds);
        picList.add(R.drawable.a14_of_hearts);
        picList.add(R.drawable.a14_of_spades);
        picList.add(R.drawable.red_joker);
        picList.add(R.drawable.black_joker);
        return picList;
    }


    /* OnResume - called every time the program is opened or unpaused
     * Saves all variables that need to be saved
     */
    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
        if(settings.contains("totalPush")) {
            totalPushups = settings.getInt("totalPush", totalPushups);
            total.setText("Total Push-ups: " + totalPushups);
        }
        if(settings.contains("pDone")){
            pushupsDone = settings.getBoolean("pDone", pushupsDone);
        }
        if(settings.contains("shuffle")) {
            String savedString = settings.getString("shuffle", "");
            StringTokenizer st = new StringTokenizer(savedString, ",");
            shuffle = new ArrayList<>();
            for (int j = 0; j < 54; j++){
                shuffle.add(Integer.parseInt(st.nextToken()));
            }
        }
        if (settings.contains("flipped")){
            cardsFlipped = settings.getInt("flipped", cardsFlipped);
            //If the pushups havent been finished then start them at that card
            if(!pushupsDone) {
                cardsFlipped--;
            }
        }
        if (settings.contains("touches")){
            touches = settings.getInt("touches", touches);
            if((touches % 2) != 0){
                touches--;
            }
        }
        if (settings.contains("custom")){
            customWorkout = settings.getBoolean("custom", customWorkout);
            spadesWorkout = settings.getString("spades", spadesWorkout);
            heartsWorkout = settings.getString("hearts", heartsWorkout);
            diamondsWorkout = settings.getString("diamonds", diamondsWorkout);
            clubsWorkout = settings.getString("clubs", clubsWorkout);
        }
    }


    private ArrayList<Integer> loadCardVals() {
        ArrayList<Integer> cardVals = new ArrayList<>();
        for (int j = 2; j < 15; j++) {
            cardVals.add(j);
            cardVals.add(j);
            cardVals.add(j);
            cardVals.add(j);
        }
        cardVals.add(30);
        cardVals.add(30);

        return cardVals;
    }


    private void getScreenSize(){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenHeight = displayMetrics.heightPixels;
        screenWidth = displayMetrics.widthPixels;
    }


    private void shuffleDeck(){
        if(shuffle.isEmpty()) {
            for (int k = 0; k < 54; k++) {
                shuffle.add(k);
            }
            //Shuffle the deck
            Collections.shuffle(shuffle);
        }
    }
}