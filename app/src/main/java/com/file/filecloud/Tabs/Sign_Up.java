package com.file.filecloud.Tabs;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.file.filecloud.Dashboard;
import com.file.filecloud.Firebase.NetworkConnection;
import com.file.filecloud.LoginActivity;
import com.file.cloud.R;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class Sign_Up extends AppCompatActivity {

    private FirebaseAuth auth;
    FirebaseUser user;
    private static final int RC_SIGN_IN = 100;
    private GoogleSignInClient mGoogleSignInClient;
    private SignInButton GLogin;

    private Button RegisterBtn;
    private TextView mEmailEt, mPasswordEt;
    private ProgressDialog progressDialog;
    private TextView mHaveAccount;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        auth = FirebaseAuth.getInstance();
        user =auth.getCurrentUser();

        //checkConnection();

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mEmailEt = findViewById(R.id.EmailEt);
        mPasswordEt = findViewById(R.id.PasswordEt);
        RegisterBtn = findViewById(R.id.RegisterBtn);
        mHaveAccount = findViewById(R.id.have_accountTv);


        RegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String Email = mEmailEt.getText().toString().trim();
                String Password = mPasswordEt.getText().toString().trim();

                if(!Patterns.EMAIL_ADDRESS.matcher(Email).matches()){
                    mEmailEt.setError("Invalid Email");
                    mEmailEt.setFocusable(true);

                }
                else if (Password.length()<6){
                    mPasswordEt.setError("Password must be at least 6 characters ");
                    mPasswordEt.setFocusable(true);

                }
                else {
                    if (!NetworkConnection.isNetworkAvailable(Sign_Up.this)){
                        new androidx.appcompat.app.AlertDialog.Builder(Sign_Up.this)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setTitle("No Internet Connection")
                                .setMessage("Restore Internet connectivity and try again")
                                .setPositiveButton("Setup", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                            startActivity(new Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY));
                                        }
                                    }
                                })
                                .setNegativeButton("Cancel", null)
                                .show();
                    }else {
                    RegisterUser( Email, Password );}
                }

            }
        });

        mHaveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Sign_Up.this,LoginActivity .class));
                finish();
            }
        });



    }

    private void RegisterUser(String email, String password) {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Registering Please Wait....");
        progressDialog.show();
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            progressDialog.dismiss();
                            FirebaseUser user = auth.getCurrentUser();

                            final String email = user.getEmail();
                            final String uid = user.getUid();

                            HashMap<Object, String> hashMap = new HashMap<>();
                            hashMap.put("email", email);
                            hashMap.put("uid", uid);
                            hashMap.put("firstName", "");
                            hashMap.put("surName", "");
                            hashMap.put("fullName", "");
                            hashMap.put("image", "");
                            hashMap.put("phone", "");
                            hashMap.put("passcode", "");
                            hashMap.put("pincode", "");

                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference reference = database.getReference("Users");
                            reference.child(uid).setValue(hashMap);
                            auth.getCurrentUser().sendEmailVerification()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                verifyInfoDialog();
                                            }

                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(Sign_Up.this, ""+e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });


                        } else {
                            // If sign in fails, display a message to the user.
                            progressDialog.dismiss();
                            Toast.makeText(Sign_Up.this, "Authentication failed.", Toast.LENGTH_LONG).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(Sign_Up.this, "" + e.getMessage(), Toast.LENGTH_LONG).show();

            }
        });

    }

    /*public void checkConnection() {
        ConnectivityManager manager = (ConnectivityManager) this.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = manager.getActiveNetworkInfo();
        if (null != activeNetwork) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                //   Toast.makeText(this, "wifi enabled", Toast.LENGTH_SHORT).show();
            }

            if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                // Toast.makeText(this, "mobile network enabled", Toast.LENGTH_SHORT).show();
            }

        } else {
            Toast toast = Toast.makeText(this,"No Network Enabled",Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER,0,0);
            toast.show();


        }
    }*/

    private void verifyInfoDialog() {
        View view = LayoutInflater.from(Sign_Up.this).inflate(R.layout.warning_dialog, null);
        final Button msgeBtn = view.findViewById(R.id.msgeBtn);
        TextView msgeTv = view.findViewById(R.id.msgeTv);
        msgeTv.setText("A verification mail has been sent to your email, be sure to check your spam if you haven't received it.");
        msgeBtn.setText("OK");

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(Sign_Up.this);
        builder.setView(view);

        final AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        msgeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Sign_Up.this, LoginActivity.class));
                dialog.dismiss();
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        finish();
        return super.onSupportNavigateUp();
    }



}