package com.example.utilisateur.uvexposureapp;

import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.List;

public class RegisterUserFragment extends DialogFragment {
    protected EditText usernameinputEditText;
    protected EditText passwordinputEditText;
    protected EditText confirmpassinputEditText;
    protected Button registeruserButton;
    protected Button cancelregisterButton;
    DatabaseHelper dbhelper;

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register_user, container, false);

        usernameinputEditText = view.findViewById(R.id.registerusernameEditText); /**INITIALIZE OBJECTS*/
        passwordinputEditText = view.findViewById(R.id.registerpasswordregEditText);
        confirmpassinputEditText = view.findViewById(R.id.confirmpasswordregEditText);
        registeruserButton = view.findViewById(R.id.registernewuserButton);
        cancelregisterButton = view.findViewById(R.id.cancelnewuserButton);
        dbhelper = new DatabaseHelper(getActivity());

        cancelregisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });

        registeruserButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            { /**WHEN REGISTER BUTTON IS CLICKED*/
                String registerusername = usernameinputEditText.getText().toString();
                String registerpassword = passwordinputEditText.getText().toString();
                String registerconfirmpassword = confirmpassinputEditText.getText().toString();
                Boolean newusercheck;
                int getpasslength = passwordinputEditText.length();
                int ivalueForStatement = -1;

                List<User> usernameCheck = dbhelper.getAllUserData();

                if (registerusername.equals("")) /**IF USERNAME IS NULL*/
                {

                    usernameinputEditText.setText(null);
                    passwordinputEditText.setText(null);
                    confirmpassinputEditText.setText(null);
                    newusercheck = false;
                    Toast.makeText(getActivity(), "Username cannot be blank.", Toast.LENGTH_SHORT).show();
                }
                else if (registerpassword.equals("") || registerconfirmpassword.equals("")) /**IF ANY OF THE PASSWORDS ARE NULL*/
                {

                    passwordinputEditText.setText(null);
                    confirmpassinputEditText.setText(null);
                    newusercheck = false;
                    Toast.makeText(getActivity(), "Passwords cannot be blank.", Toast.LENGTH_SHORT).show();
                }
                else if (getpasslength < 8) /**IF PASSWORD LENGTH IS LESS THAN 8 CHARACTERS*/
                {
                    passwordinputEditText.setText(null);
                    confirmpassinputEditText.setText(null);
                    newusercheck = false;
                    Toast.makeText(getActivity(), "Password has to be 8 or more characters.", Toast.LENGTH_SHORT).show();
                }


                for (int i = 0; i < usernameCheck.size(); i++)
                {
                    if (usernameCheck.get(i).getUsername().equals(registerusername)) { /**CHECKS IF USERNAME IS TAKEN*/
                        usernameinputEditText.setText(null);
                        passwordinputEditText.setText(null);
                        confirmpassinputEditText.setText(null);
                        ivalueForStatement = i; /**IF IT IS, THEN VALUE OF THIS INT WILL CHANGE FROM -1 to i*/
                        newusercheck = false;
                        Toast.makeText(getActivity(), "Username is taken.", Toast.LENGTH_SHORT).show();
                    }
                }
                if (registerconfirmpassword.equals(registerpassword) && ivalueForStatement == -1)
                {   /**IF THE PASSWORDS MATCH, AND INT is -1 WHICH MEANS THAT USERNAME ISNT TAKEN*/
                    dbhelper.insertUser(new User(registerusername, registerpassword, 0, null, 0, true, true));
                    //(String username, String password, int age, ArrayList<UV> uv, int skin, boolean notifications)
                    getDialog().dismiss();
                    newusercheck = true;
                    Intent intent = new Intent(getActivity(), UserActivity.class); /**STARTS NEW ACTIVITY*/
                    intent.removeExtra("checknewuser"); /**PASSES ON USERNAME, NEWUSERCHECK */
                    intent.removeExtra("username");
                    intent.putExtra("checknewuser", newusercheck);
                    intent.putExtra("username", registerusername);
                    startActivity(intent);
                    Toast.makeText(getActivity(), "Confirmed!", Toast.LENGTH_SHORT).show();
                }
            }

        });

        return view;
    }
}
