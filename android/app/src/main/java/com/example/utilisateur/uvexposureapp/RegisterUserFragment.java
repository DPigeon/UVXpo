package com.example.utilisateur.uvexposureapp;

import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

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
    Boolean hasInternet;
    DatabaseHelper dbhelper;
    FirebaseFirestore fireStore;


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

        Bundle bundle = getArguments();
        hasInternet = bundle.getBoolean("hasInternet");

        usernameinputEditText = view.findViewById(R.id.registerusernameEditText); /**INITIALIZE OBJECTS*/
        passwordinputEditText = view.findViewById(R.id.registerpasswordregEditText);
        confirmpassinputEditText = view.findViewById(R.id.confirmpasswordregEditText);
        registeruserButton = view.findViewById(R.id.registernewuserButton);
        cancelregisterButton = view.findViewById(R.id.cancelnewuserButton);
        dbhelper = new DatabaseHelper(getActivity());
        fireStore = FirebaseFirestore.getInstance();

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
                final String registerusername = usernameinputEditText.getText().toString();
                final String registerpassword = passwordinputEditText.getText().toString();
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
                else if (!registerpassword.equals(registerconfirmpassword))
                {

                    confirmpassinputEditText.setText(null);
                    newusercheck = false;
                    Toast.makeText(getActivity(), "Passwords don't match.", Toast.LENGTH_SHORT).show();
                }
                else {
                    if (!hasInternet) {
                        for (int i = 0; i < usernameCheck.size(); i++) {
                            if (usernameCheck.get(i).getUsername().equals(registerusername)) { /**CHECKS IF USERNAME IS TAKEN*/
                                usernameinputEditText.setText(null);
                                passwordinputEditText.setText(null);
                                confirmpassinputEditText.setText(null);
                                ivalueForStatement = i; /**IF IT IS, THEN VALUE OF THIS INT WILL CHANGE FROM -1 to i*/
                                newusercheck = false;
                                Toast.makeText(getActivity(), "Username is taken.", Toast.LENGTH_SHORT).show();
                            }
                        }
                        if (registerconfirmpassword.equals(registerpassword) && ivalueForStatement == -1) {   /**IF THE PASSWORDS MATCH, AND INT is -1 WHICH MEANS THAT USERNAME ISNT TAKEN*/
                            dbhelper.insertUser(new User(registerusername, registerpassword, 0, 0, true, true));
                            //(String username, String password, int age, ArrayList<UV> uv, int skin, boolean notifications)
                            getDialog().dismiss();
                            newusercheck = true;
                            Intent intent = new Intent(getActivity(), UserActivity.class); /**STARTS NEW ACTIVITY*/
                            intent.removeExtra("checknewuser"); /**PASSES ON USERNAME, NEWUSERCHECK */
                            intent.removeExtra("username");
                            intent.putExtra("checknewuser", newusercheck);
                            intent.putExtra("username", registerusername);
                            startActivity(intent);
                            Toast.makeText(getActivity(), "Confirmed! Setup your preferences!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        final CollectionReference users = fireStore.collection(DatabaseConfig.USER_TABLE_NAME);

                        // Checks if user is already registered
                        // If not registered, add it to the database
                        users.whereEqualTo("username", registerusername).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    if (task.getResult().getDocuments().isEmpty()) { // Username does not exist
                                        User user = new User(registerusername, registerpassword, 0, 0, true, true);
                                        users.add(user); // Add a new user value

                                        getDialog().dismiss();
                                        Intent intent = new Intent(getActivity(), UserActivity.class); /**STARTS NEW ACTIVITY*/
                                        intent.removeExtra("checknewuser"); /**PASSES ON USERNAME, NEWUSERCHECK */
                                        intent.removeExtra("username");
                                        intent.putExtra("checknewuser", true);
                                        intent.putExtra("username", registerusername);
                                        startActivity(intent);
                                        Toast.makeText(getActivity(), "Confirmed! Setup your preferences!", Toast.LENGTH_SHORT).show();
                                        return;
                                    } else {
                                        Toast.makeText(getActivity(), "Username already exists!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                                Toast.makeText(getActivity(), "Username already exists!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }

        });

        return view;
    }
}
