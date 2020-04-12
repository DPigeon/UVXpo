package com.example.utilisateur.uvexposureapp;

import android.content.Context;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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

import static java.lang.Integer.parseInt;

public class ChangePasswordFragment extends DialogFragment {

    protected EditText currentpassEditText;
    protected EditText newpassEditText;
    protected EditText confirmpassEditText;
    protected Button savepassButton;
    protected Button cancelpassButton;
    String usernameIntent;
    Boolean hasInternet;
    List<User> userInfo;
    DatabaseHelper dbhelper;
    FirebaseFirestore fireStore;
    String userID;
    String userUsername;
    boolean userNotifications;
    boolean newUser;
    int ageInteger;
    int skin_type;

    @Override
    public void onResume() {

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


        View view = inflater.inflate(R.layout.fragment_change_password, container, false);
        currentpassEditText = view.findViewById(R.id.currentpassEditText);
        newpassEditText = view.findViewById(R.id.newpassEditText);
        confirmpassEditText = view.findViewById(R.id.newpassconfirmEditText);
        savepassButton = view.findViewById(R.id.savepassButton);
        cancelpassButton = view.findViewById(R.id.cancelpassButton);
        Bundle bundle = getArguments();
        usernameIntent = bundle.getString("username");
        hasInternet = bundle.getBoolean("hasInternet");

        fireStore = FirebaseFirestore.getInstance();

        savepassButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String oldpass = currentpassEditText.getText().toString();
                String newpass = newpassEditText.getText().toString();
                final String confirmpass = confirmpassEditText.getText().toString();
                String databasePassword = null;

                if (haveNetworkConnection()) { // Offline
                    dbhelper = new DatabaseHelper(getActivity());
                    userInfo = dbhelper.getAllUserData();
                    for (int i = 0; i < userInfo.size(); i++) {
                        if (userInfo.get(i).getUsername().equals(usernameIntent)) {
                            databasePassword = userInfo.get(i).getPassword();
                            userID = Integer.toString(userInfo.get(i).getUserId());
                            userUsername = userInfo.get(i).getUsername();
                            userNotifications = userInfo.get(i).getNotifications();
                            newUser = userInfo.get(i).getNewUser();
                            ageInteger = userInfo.get(i).getAge();
                            skin_type = userInfo.get(i).getSkin();
                        }
                    }

                    if (!oldpass.equals(databasePassword)) {

                        currentpassEditText.setText(null);
                        newpassEditText.setText(null);
                        confirmpassEditText.setText(null);
                        Toast.makeText(getActivity(), "That is not your old password. Try Again.", Toast.LENGTH_SHORT).show();

                    }
                    if (oldpass.equals("") || newpass.equals("") || currentpassEditText.equals("")) {
                        currentpassEditText.setText(null);
                        newpassEditText.setText(null);
                        confirmpassEditText.setText(null);
                        Toast.makeText(getActivity(), "Entries can't be blank.", Toast.LENGTH_SHORT).show();

                    } else if (newpass.length() < 8) {
                        currentpassEditText.setText(null);
                        newpassEditText.setText(null);
                        confirmpassEditText.setText(null);
                        Toast.makeText(getActivity(), "New password must be 8 or more characters.", Toast.LENGTH_SHORT).show();

                    } else if (!newpass.equals(confirmpass)) {
                        currentpassEditText.setText(null);
                        newpassEditText.setText(null);
                        confirmpassEditText.setText(null);
                        Toast.makeText(getActivity(), "Passwords don't match. Try Again.", Toast.LENGTH_SHORT).show();
                    } else {
                        // Online
                        final CollectionReference users = fireStore.collection(DatabaseConfig.USER_TABLE_NAME);
                        users.whereEqualTo(DatabaseConfig.COLUMN_USERNAME, usernameIntent).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document_user : task.getResult()) {
                                        String oldPassword = document_user.getData().get("password").toString(); // Getting old password
                                        if (currentpassEditText.getText().toString().equals(oldPassword) && newpassEditText.getText().toString().equals(confirmpassEditText.getText().toString())) { // old passes matches and new too
                                            users.document(document_user.getId()).update("password", confirmpassEditText.getText().toString());
                                            dbhelper.updateData(userID, userUsername, confirmpassEditText.getText().toString(), ageInteger, skin_type, userNotifications, newUser);
                                            getDialog().dismiss();
                                            Toast.makeText(getActivity(), "Password Changed", Toast.LENGTH_SHORT).show();
                                        } else
                                            Toast.makeText(getActivity(), "Passwords don't match. Try Again.", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(getActivity(), "Passwords don't match. Try Again.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }
                else{
                    Toast.makeText(getActivity(), "Password cannot be changed while not connected to internet.", Toast.LENGTH_SHORT).show();
                    getDialog().dismiss();
                }
            }
        });

        cancelpassButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v){
                getDialog().dismiss();
            }
        });

        return view;
    }

    private boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }
}
