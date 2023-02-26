package com.file.filecloud;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.file.filecloud.Firebase.NetworkConnection;
import com.file.filecloud.Tabs.Sign_Up;
import com.file.cloud.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

   private Button mLoginBtn;
   private TextView memailEt, mpasswordEt, resetTv;

   private ProgressDialog progressDialog;
   private Button signIn;

    private FirebaseAuth auth;
    FirebaseUser user;
    ProgressDialog mprogressDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        auth = FirebaseAuth.getInstance();
        user =auth.getCurrentUser();

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        memailEt = findViewById(R.id.EmailEt);
        mpasswordEt = findViewById(R.id.PasswordEt);
        mLoginBtn = findViewById(R.id.loginBtn);
        resetTv =findViewById(R.id.resetTv);


        signIn = findViewById(R.id.signIn);
        mprogressDialog = new ProgressDialog(this);
        mprogressDialog.setMessage("Logging In Please Wait...");
        mprogressDialog.setCanceledOnTouchOutside(false);

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String Email =memailEt.getText().toString();
                String Password = mpasswordEt.getText().toString().trim();
                if (!Patterns.EMAIL_ADDRESS.matcher(Email).matches()){
                    memailEt.setError("Invalid Email");
                    memailEt.setFocusable(true);
                }
                else{
                    if (!NetworkConnection.isNetworkAvailable(LoginActivity.this)){
                        new androidx.appcompat.app.AlertDialog.Builder(LoginActivity.this)
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
                    LoginUser(Email, Password);}
                }
            }
        });

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,Sign_Up.class));
                finish();
            }
        });

        resetTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PasswordRecover();
            }
        });



    }


    private void LoginUser(String email, String password) {
        mprogressDialog.setMessage("Logging in Please Wait...");
        mprogressDialog.show();

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            mprogressDialog.dismiss();
                            if (auth.getCurrentUser().isEmailVerified()){
                                // Sign in success, update UI with the signed-in user's information
                                startActivity(new Intent(LoginActivity.this, PinVerification.class));
                                finish();

                            }else {
                                mprogressDialog.dismiss();
                                // If sign in fails, display a message to the user.
                                Toast toast = Toast.makeText(LoginActivity.this,"Please Verify your Email Address",Toast.LENGTH_LONG);
                                toast.setGravity(Gravity.CENTER,0,0);
                                toast.show();
                            }

                        } else {
                            mprogressDialog.dismiss();
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this, "Authentication Failed.", Toast.LENGTH_LONG).show();

                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                mprogressDialog.dismiss();
                Toast.makeText(LoginActivity.this, "" + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });



    }

    private void PasswordRecover() {
        
        ShowRecoverPasswordDialog();

    }


    private void ShowRecoverPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Recover Password");

        LinearLayout linearLayout = new LinearLayout(this);

        final EditText emailEt = new EditText(this);
        emailEt.setHint("E-mail");
        emailEt.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        emailEt.setMinEms(16);
        linearLayout.addView(emailEt);
        linearLayout.setPadding(10,10,10,10);
        builder.setView(linearLayout);
               builder.setPositiveButton("Recover", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String email = emailEt.getText().toString().trim();
                if (TextUtils.isEmpty(email)){
                    Toast.makeText(LoginActivity.this, "Please Provide Your Email.", Toast.LENGTH_LONG).show();
                    return;
                }
                else {
                    BeginRecovery(email);
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

            }
        });

        builder.create().show();

    }

    private void BeginRecovery(String email) {

        mprogressDialog.setMessage("Sending Recovery E-mail Please Wait...");
        mprogressDialog.show();
        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        mprogressDialog.dismiss();

                        if (task.isSuccessful()){
                            Toast.makeText(LoginActivity.this, "Email Sent For Recovery, Check your  mail", Toast.LENGTH_LONG).show();
                        }
                        else{
                            mprogressDialog.dismiss();
                            Toast.makeText(LoginActivity.this, "Failed Try again", Toast.LENGTH_SHORT).show();
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

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