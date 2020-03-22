package com.example.utilisateur.uvexposureapp;

import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class TutorialFragment extends DialogFragment {

    protected TextView instructionsTextView;
    protected Button continuestepsButton;
    protected int i;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tutorial, container, false);

        instructionsTextView = view.findViewById(R.id.informationTextView);
        continuestepsButton = view.findViewById(R.id.gotitButton);
        instructionsTextView.setText("Hello! Welcome to the app that lets you see your UV exposure!");
        i = 0;


        continuestepsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    if (i == 0)
                    {
                        instructionsTextView.setText(("The Settings Button lets you see your user preferences, and edit your " +
                                "age, skin tone, and whether or not you want notifications.").toString());
                        i++;
                    }
                    else if (i == 1)
                    {
                        instructionsTextView.setText(("The FAQ Button allows you to access different questions that" +
                                " may come to your mind when it comes to UV exposure and how it affects you.").toString());
                        i++;
                    }
                    else if (i == 2)
                    {
                        instructionsTextView.setText(("The Graph Exposure Button allows for you to see your UV " +
                                "exposure over time. You can see any UV exposure over any specific date ").toString());
                        i++;
                    }
                    else if (i == 3)
                    {
                        instructionsTextView.setText(("The Get Current Weather Data Button allows for you to see the " +
                                "weather conditions in your area, and allows for you to change your location.").toString());

                        i++;
                    }
                    else if (i == 4)
                    {
                        instructionsTextView.setText(("Hope you enjoy the app, and it's our pleasure to help you stay " +
                                "healthy and happy!").toString());

                        i++;
                    }
                    else if (i == 5)
                    {
                        getDialog().dismiss();
                    }

            }
        });



        return view;
    }

    @Override
    public void onResume() { /**MAKES THE FRAGMENT BIG*/

        // Store access variables for window and blank point
        Window window = getDialog().getWindow();
        Point size = new Point();

        // Store dimensions of the screen in `size`
        Display display = window.getWindowManager().getDefaultDisplay();
        display.getSize(size);

        //resize
        window.setLayout((int) (size.x *0.98), (int) (size.y *0.9));
        window.setGravity(Gravity.CENTER);
        super.onResume();

    }
}
