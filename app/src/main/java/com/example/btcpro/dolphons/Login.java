package com.example.btcpro.dolphons;

import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.*;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;


public class Login extends AppCompatActivity {

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    private static final String TAG = "Login";

    String email;
    String password;
    String password2;
    String name;
    Boolean signIn = true;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser user;


    TextView nameView;
    TextView emailView;
    TextView passwordView;
    TextView passwordView2;
    Button loginSignupButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        nameView = findViewById(R.id.nameView);
        emailView = findViewById(R.id.emailView);
        passwordView = findViewById(R.id.passwordView);
        passwordView2 = findViewById(R.id.passwordView2);
        loginSignupButton = findViewById(R.id.loginSignupButton);

    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser != null);

    }

    /* one onclick function , manage with ID's  TO DO*/
    public void loginSignup(View v) {
        email = emailView.getText().toString();
        password = passwordView.getText().toString();
        password2 = passwordView2.getText().toString();
        name = nameView.getText().toString();

        if (loginSignupButton.getText() == "Sign Up") {
            createAccount(email, password, password2, name);
        } else {
            signIn(email, password);
        }
    }

    public void signUp(View v) {
        if (signIn) {
            nameView.setVisibility(View.VISIBLE);
            passwordView2.setVisibility(View.VISIBLE);
            loginSignupButton.setText("Sign Up");

            signIn = !signIn;
        }
    }

    public void login(View v) {
        //createAccount(name, password);
        if (!signIn) {
            nameView.setVisibility(View.GONE);
            passwordView2.setVisibility(View.GONE);
            loginSignupButton.setText("Login");

            signIn = !signIn;
        }
    }


    /* Thanks Google */

    private void createAccount(String email, String password, String password2, final String name) {
        final String userName = name;

        if (password.equals(password2)) {
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "createUserWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();

                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(userName)
                                        .build();

                                user.updateProfile(profileUpdates)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Log.d(TAG, "user profile updated.");
                                                    //writeUserData(name);
                                                    updateUI(true);
                                                }
                                            }
                                        });


                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                Toast.makeText(Login.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                                updateUI(false);
                            }

                            // ...
                        }
                    });
        } else {
            Toast.makeText(this, "Entered Passwords are not the same!", Toast.LENGTH_SHORT).show();
        }
    }


    private void writeUserData(final String name) {
        // DOES NOT WORK YET
        Map<String, String> data = new HashMap<>();
        data.put("Name", name);
        data.put("UID", user.getUid());

        db.collection("users").document(name)
                .set(data, SetOptions.merge());

    }

    private void signIn(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateUI(true);
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        Toast.makeText(Login.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                        updateUI(false);
                    }

                    // ...
                }
            });
    }

    public void updateUI(boolean successful) {
        if (successful) {
            System.out.println("Successful");
            startActivity(new Intent(Login.this, welcome.class));
        } else {
            System.out.println("Unsuccessful");
        }

    }


    /* Need to add this */

    private boolean validateForm() {
        return true;
    }

}
