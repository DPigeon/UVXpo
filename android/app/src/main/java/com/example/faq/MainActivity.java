package com.example.faq;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {


    TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView=findViewById(R.id.textView);
        /* Set button to display time */
        Calendar calendar= Calendar.getInstance();
        SimpleDateFormat simpleDateFormat= new SimpleDateFormat("hh:mm:ss a");
        String dateTime= simpleDateFormat.format(calendar.getTime());
        textView.setText(dateTime);

    }

/* Linking website to FAQ button */

    public void faq(View view){

        Intent browserIntent=new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.ccohs.ca/oshanswers/phys_agents/ultravioletradiation.html?fbclid=IwAR05zwUhYrQqcc0bNr-nSeWcbN7J1LUsjgW3K7Bs5oT49s_O9XrgfFpZybY"));
        startActivity(browserIntent);



    }


}
