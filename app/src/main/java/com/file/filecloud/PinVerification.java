package com.file.filecloud;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.file.cloud.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import static com.file.cloud.R.*;

public class PinVerification extends AppCompatActivity implements View.OnClickListener {

    private  static boolean RUN_ONCE=true;
    View view_01, view_02, view_03, view_04;
    Button btn_01, btn_02, btn_03, btn_04, btn_05, btn_06, btn_07, btn_08, btn_09, btn_00, forgetPinTv, resetPinTv;
    ImageButton btn_clear;
    TextInputLayout textL1;

    ArrayList<String> numbers_list = new ArrayList<>();
    String passCode = "";
    String num_01, num_02, num_03, num_04,uid;
    TextView resultTv;
    ImageView lockIcon;
    FirebaseAuth auth;
    FirebaseUser user;

    Activity fa;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.pin_verification);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        uid=auth.getUid();


        initializeComponents();


    }



    private void initializeComponents() {
        view_01 = findViewById(id.view_01);
        view_02 = findViewById(id.view_02);
        view_03 = findViewById(id.view_03);
        view_04 = findViewById(id.view_04);
        btn_01 = findViewById(id.btn_01);
        btn_02 = findViewById(id.btn_02);
        btn_03 = findViewById(id.btn_03);
        btn_04 = findViewById(id.btn_04);
        btn_05 = findViewById(id.btn_05);
        btn_06 = findViewById(id.btn_06);
        btn_07 = findViewById(id.btn_07);
        btn_08 = findViewById(id.btn_08);
        btn_09 = findViewById(id.btn_09);
        btn_00 = findViewById(id.btn_00);
        btn_clear = findViewById(id.btn_clear);
        resultTv = findViewById(id.resultTv);
        forgetPinTv = findViewById(id.forgetPinTv);
        lockIcon = findViewById(id.lockIcon);
        resetPinTv = findViewById(id.resetPinTv);
        textL1 = findViewById(R.id.textL1);

        btn_01.setOnClickListener(this);
        btn_02.setOnClickListener(this);
        btn_03.setOnClickListener(this);
        btn_04.setOnClickListener(this);
        btn_05.setOnClickListener(this);
        btn_06.setOnClickListener(this);
        btn_07.setOnClickListener(this);
        btn_08.setOnClickListener(this);
        btn_09.setOnClickListener(this);
        btn_00.setOnClickListener(this);
        btn_clear.setOnClickListener(this);


        forgetPinTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShowRecoverPassCodeDialog();
            }
        });
        resetPinTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowResetPassCodeDialog();
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_01:
                numbers_list.add("1");
                passNumber(numbers_list);
                break;
            case R.id.btn_02:
                numbers_list.add("2");
                passNumber(numbers_list);
                break;
            case R.id.btn_03:
                numbers_list.add("3");
                passNumber(numbers_list);
                break;
            case R.id.btn_04:
                numbers_list.add("4");
                passNumber(numbers_list);
                break;
            case R.id.btn_05:
                numbers_list.add("5");
                passNumber(numbers_list);
                break;
            case R.id.btn_06:
                numbers_list.add("6");
                passNumber(numbers_list);
                break;
            case R.id.btn_07:
                numbers_list.add("7");
                passNumber(numbers_list);
                break;
            case R.id.btn_08:
                numbers_list.add("8");
                passNumber(numbers_list);
                break;
            case R.id.btn_09:
                numbers_list.add("9");
                passNumber(numbers_list);
                break;
            case R.id.btn_00:
                numbers_list.add("0");
                passNumber(numbers_list);
                break;
            case R.id.btn_clear:
                numbers_list.clear();
                passNumber(numbers_list);
                break;
        }

    }

    private void passNumber(ArrayList<String> numbers_list) {
        if (numbers_list.size() == 0) {
            view_01.setBackgroundResource(drawable.bg_view_gray_ova);
            view_02.setBackgroundResource(drawable.bg_view_gray_ova);
            view_03.setBackgroundResource(drawable.bg_view_gray_ova);
            view_04.setBackgroundResource(drawable.bg_view_gray_ova);
        } else {
            switch (numbers_list.size()) {
                case 1:
                    num_01 = numbers_list.get(0);
                    view_01.setBackgroundResource(drawable.bg_view_blue_oval);
                    break;
                case 2:
                    num_02 = numbers_list.get(1);
                    view_02.setBackgroundResource(drawable.bg_view_blue_oval);
                    break;
                case 3:
                    num_03 = numbers_list.get(2);
                    view_03.setBackgroundResource(drawable.bg_view_blue_oval);
                    break;
                case 4:
                    num_04 = numbers_list.get(3);
                    view_04.setBackgroundResource(drawable.bg_view_blue_oval);
                    passCode = num_01 + num_02 + num_03 + num_04;
                    if (getPassCode().length() == 0) {
                        savePassCode(passCode);
                    } else {
                        matchPassCode();
                    }
                    break;
            }
        }

    }

    private void matchPassCode() {
        if (getPassCode().equals(passCode)) {
            startActivity(new Intent(this, Dashboard.class));
            lockIcon.setImageResource(drawable.ic_verified02);
            resultTv.setText("NICE!!!");
            resultTv.setTextColor(Color.parseColor("#00FF00"));
            finish();
        } else {
            numbers_list.clear();
            passNumber(numbers_list);
            resultTv.setText("incorrect try again");
            resultTv.setTextColor(Color.parseColor("#E71A0C"));
            resultTv.setText("incorrect try again");
        }
    }

    private SharedPreferences.Editor savePassCode(String passCode) {
        SharedPreferences preferences = getSharedPreferences("passcode.pref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("passcode", passCode);
        editor.apply();

        return editor;
    }

    private String getPassCode() {
        SharedPreferences preferences = getSharedPreferences("passcode.pref", Context.MODE_PRIVATE);
        return preferences.getString("passcode", "");
    }

    private void ShowRecoverPassCodeDialog() {
        View view = LayoutInflater.from(this).inflate(layout.register_pin_dialog, null);
        final Button setpinBtn = view.findViewById(R.id.setpinBtn);
        final EditText pinEt = view.findViewById(R.id.pinEt);
        final EditText setpassEt = view.findViewById(R.id.setpassEt);
        final TextView dialogTitle = view.findViewById(R.id.dialogTitle);
        final TextInputLayout textL1 = view.findViewById(R.id.textL1);
        final TextInputLayout textL2 = view.findViewById(R.id.textL2);

        textL1.setVisibility(View.GONE);


        ///pinEt.setHint("Enter your new 4 digit pincode");
        setpassEt.setHint("Provide your secret recovery password");
        dialogTitle.setText("Recover your Pincode");
        setpinBtn.setText("Recover");

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setView(view);

        final android.app.AlertDialog alertDialog = builder.create();
        alertDialog.show();

        setpinBtn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint({"ResourceAsColor", "ResourceType"})
            @Override
            public void onClick(View v) {
                final String passCode = setpassEt.getText().toString().trim();

                /*final String pinRest = pinEt.getText().toString().trim();
                if (TextUtils.isEmpty(pinRest)){
                    pinEt.setHint("Pincode cannot be empty");
                    Context context = null;
                    pinEt.setHintTextColor(color.red);
                    //Toast.makeText(PinVerification.this, "Please Enter a 4 digit pincode", Toast.LENGTH_LONG).show();

                }else if (pinRest.length()<4){
                    Toast.makeText(PinVerification.this, "Pincode must be more than 4 digits", Toast.LENGTH_LONG).show();

                }else if (pinRest.length()>4){
                    Toast.makeText(PinVerification.this, "Pincode must be 4 digits", Toast.LENGTH_LONG).show();
                }*/
                if (TextUtils.isEmpty(passCode)) {
                    setpassEt.setHint("Provide your secret recovery password");
                    setpassEt.setHintTextColor(color.red);
                } else {
                    final ProgressDialog progressDialog = new ProgressDialog(PinVerification.this);
                    progressDialog.setMessage("Recovering Pincode Please Wait....");
                    progressDialog.show();

                    DatabaseReference database = FirebaseDatabase.getInstance().getReference("Users");
                    database.orderByChild("uid").equalTo(uid).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                String passcode = "" + ds.child("passcode").getValue();
                                String pincode = "" + ds.child("pincode").getValue();
                                if (passcode.equals(passCode)) {

                                    /*SharedPreferences preferences = getSharedPreferences("passcode.pref", Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = preferences.edit();
                                    editor.remove("passcode");
                                    editor.apply();
                                    editor.putString("passcode", pinRest);
                                    editor.apply();*/
                                    /*SharedPreferences shf = getSharedPreferences("passcode.pref",  Context.MODE_PRIVATE);
                                    String strPref = shf.getString("passcode", null);*/
                                    if (pincode != null) {
                                        setpinBtn.setVisibility(View.GONE);
                                        setpassEt.setTextColor(color.red);
                                        setpassEt.setText(pincode);
                                        dialogTitle.setText("Your Pincode");
                                    } else {
                                        progressDialog.dismiss();
                                        alertDialog.dismiss();
                                        View view = LayoutInflater.from(PinVerification.this).inflate(R.layout.warning_dialog, null);
                                        final Button msgeBtn = view.findViewById(R.id.msgeBtn);
                                        TextView msgeTv = view.findViewById(R.id.msgeTv);
                                        msgeTv.setText("There is no pincode to recover, Set a pincode");
                                        msgeBtn.setText("Yes");

                                        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(PinVerification.this);
                                        builder.setView(view);

                                        final AlertDialog dialog = builder.create();
                                        dialog.setCancelable(false);
                                        dialog.setCanceledOnTouchOutside(false);
                                        dialog.show();

                                        msgeBtn.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                dialog.dismiss();
                                                ShowResetPassCodeDialog();
                                            }
                                        });
                                    }
                                    progressDialog.dismiss();
                                    //alertDialog.dismiss();




                                    /*View view = LayoutInflater.from(PinVerification.this).inflate(R.layout.warning_dialog, null);
                                    final Button msgeBtn = view.findViewById(R.id.msgeBtn);
                                    TextView msgeTv = view.findViewById(R.id.msgeTv);
                                    msgeTv.setText("You can now enter your new Pin code");
                                    msgeTv.setTextColor(color.blue);
                                    msgeBtn.setText("OK");

                                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(PinVerification.this);
                                    builder.setView(view);

                                    final AlertDialog dialog = builder.create();
                                    dialog.setCancelable(false);
                                    dialog.setCanceledOnTouchOutside(false);
                                    dialog.show();

                                    msgeBtn.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            dialog.dismiss();
                                        }
                                    });*/

                                } else {
                                    progressDialog.dismiss();
                                    alertDialog.dismiss();
                                    View view = LayoutInflater.from(PinVerification.this).inflate(R.layout.warning_dialog, null);
                                    final Button msgeBtn = view.findViewById(R.id.msgeBtn);
                                    TextView msgeTv = view.findViewById(R.id.msgeTv);
                                    msgeTv.setText("Your recovery password does not match, try again");
                                    msgeBtn.setText("OK");

                                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(PinVerification.this);
                                    builder.setView(view);

                                    final AlertDialog dialog = builder.create();
                                    dialog.setCancelable(false);
                                    dialog.setCanceledOnTouchOutside(false);
                                    dialog.show();

                                    msgeBtn.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            dialog.dismiss();
                                        }
                                    });

                                }
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }
        });
    }

    private void ShowResetPassCodeDialog() {
        View view = LayoutInflater.from(this).inflate(layout.register_pin_dialog, null);
        final Button setpinBtn = view.findViewById(R.id.setpinBtn);
        final EditText pinEt = view.findViewById(R.id.pinEt);
        final EditText setpassEt = view.findViewById(R.id.setpassEt);
        final TextView dialogTitle = view.findViewById(R.id.dialogTitle);


        pinEt.setHint("Enter your new 4 digit pincode");
        setpassEt.setHint("Provide your secret recovery password");
        dialogTitle.setText("Reset your pincode");


        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setView(view);

        final android.app.AlertDialog alertDialog = builder.create();
        alertDialog.show();

        setpinBtn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint({"ResourceAsColor", "ResourceType"})
            @Override
            public void onClick(View v) {

                final String pinRest = pinEt.getText().toString().trim();
                final String passCode = setpassEt.getText().toString().trim();
                if (TextUtils.isEmpty(pinRest)) {
                    pinEt.setHint("Pincode cannot be empty");
                    pinEt.setHintTextColor(color.red);
                    //Toast.makeText(PinVerification.this, "Please Enter a 4 digit pincode", Toast.LENGTH_LONG).show();

                } else if (pinRest.length() < 4) {
                    Toast.makeText(PinVerification.this, "Pincode must be more than 4 digits", Toast.LENGTH_LONG).show();

                } else if (pinRest.length() > 4) {
                    Toast.makeText(PinVerification.this, "Pincode must be 4 digits", Toast.LENGTH_LONG).show();
                } else if (TextUtils.isEmpty(passCode)) {
                    setpassEt.setHint("Provide your secret recovery password");
                    setpassEt.setHintTextColor(color.red);
                    Toast.makeText(PinVerification.this, "Provide your secret recovery password", Toast.LENGTH_LONG).show();

                } else {
                    final ProgressDialog progressDialog = new ProgressDialog(PinVerification.this);
                    progressDialog.setMessage("Resetting Pincode Please Wait....");
                    progressDialog.show();

                    DatabaseReference database = FirebaseDatabase.getInstance().getReference("Users");
                    database.orderByChild("uid").equalTo(uid).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                String passcode = "" + ds.child("passcode").getValue();
                                if (passcode.equals(passCode)) {

                                    HashMap<String, Object> hashMap = new HashMap<>();
                                    hashMap.put("passcode", passCode);
                                    hashMap.put("pincode", pinRest);

                                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                                    DatabaseReference reference = database.getReference("Users");
                                    reference.child(user.getUid()).updateChildren(hashMap)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        SharedPreferences preferences = getSharedPreferences("passcode.pref", Context.MODE_PRIVATE);
                                                        SharedPreferences.Editor editor = preferences.edit();
                                                        editor.remove("passcode");
                                                        editor.apply();
                                                        editor.putString("passcode", pinRest);
                                                        editor.apply();

                                                        progressDialog.dismiss();
                                                        alertDialog.dismiss();
                                                        View view = LayoutInflater.from(PinVerification.this).inflate(R.layout.warning_dialog, null);
                                                        final Button msgeBtn = view.findViewById(R.id.msgeBtn);
                                                        TextView msgeTv = view.findViewById(R.id.msgeTv);
                                                        msgeTv.setText("You can now enter your new Pin code");
                                                        msgeTv.setTextColor(color.blue);
                                                        msgeBtn.setText("OK");

                                                        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(PinVerification.this);
                                                        builder.setView(view);

                                                        final AlertDialog dialog = builder.create();
                                                        dialog.setCancelable(false);
                                                        dialog.setCanceledOnTouchOutside(false);
                                                        dialog.show();

                                                        msgeBtn.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {
                                                                dialog.dismiss();
                                                            }
                                                        });

                                                    } else {
                                                        Toast.makeText(PinVerification.this, "Error try again", Toast.LENGTH_LONG).show();

                                                    }

                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(PinVerification.this, "" + e.getMessage(), Toast.LENGTH_LONG).show();
                                                }
                                            });


                                } else {
                                    progressDialog.dismiss();
                                    alertDialog.dismiss();
                                    View view = LayoutInflater.from(PinVerification.this).inflate(R.layout.warning_dialog, null);
                                    final Button msgeBtn = view.findViewById(R.id.msgeBtn);
                                    TextView msgeTv = view.findViewById(R.id.msgeTv);
                                    msgeTv.setText("Your recovery password does not match, try again");
                                    msgeBtn.setText("OK");

                                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(PinVerification.this);
                                    builder.setView(view);

                                    final AlertDialog dialog = builder.create();
                                    dialog.setCancelable(false);
                                    dialog.setCanceledOnTouchOutside(false);
                                    dialog.show();

                                    msgeBtn.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            dialog.dismiss();
                                        }
                                    });

                                }
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }
        });
    }

    private void verifyInfoDialog() {
        View view = LayoutInflater.from(PinVerification.this).inflate(R.layout.warning_dialog, null);
        final Button msgeBtn = view.findViewById(R.id.msgeBtn);
        TextView msgeTv = view.findViewById(R.id.msgeTv);
        msgeTv.setText("Please register a 4 digit pincode; that will be used to login anytime you open the app");
        msgeBtn.setText("OK");

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(PinVerification.this);
        builder.setView(view);

        final AlertDialog alertDialog = builder.create();
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();

        msgeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                openEnterPinDialog(alertDialog);
            }
        });
    }

    private void openEnterPinDialog(final AlertDialog Dialog) {
        View view = LayoutInflater.from(this).inflate(layout.register_pin_dialog, null);
        final Button setpinBtn = view.findViewById(R.id.setpinBtn);
        final EditText pinEt = view.findViewById(R.id.pinEt);
        final EditText setpassEt = view.findViewById(R.id.setpassEt);

        pinEt.setHint("Register your new 4 digit pincode");
        setpassEt.setHint("Register a secret recovery password");

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setView(view);

        final android.app.AlertDialog alertDialog = builder.create();
        alertDialog.show();

        setpinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String pinRest = pinEt.getText().toString().trim();
                final String passCode = setpassEt.getText().toString().trim();

                if (TextUtils.isEmpty(pinRest)) {
                    Toast.makeText(PinVerification.this, "Please Enter a 4 digit pincode", Toast.LENGTH_LONG).show();

                } else if (pinRest.length() < 4) {
                    Toast.makeText(PinVerification.this, "Please Enter a 4 digit pincode", Toast.LENGTH_LONG).show();

                } else if (pinRest.length() > 4) {
                    Toast.makeText(PinVerification.this, "Please Enter a 4 digit pincode", Toast.LENGTH_LONG).show();
                } else if (passCode.isEmpty()) {
                    Toast.makeText(PinVerification.this, "Please provide a secret recovery password", Toast.LENGTH_LONG).show();
                    setpassEt.setHintTextColor(getResources().getColor(R.color.red));
                } else {

                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("passcode", passCode);
                    hashMap.put("pincode", pinRest);

                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference reference = database.getReference("Users");
                    reference.child(user.getUid()).updateChildren(hashMap)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        SharedPreferences preferences = getSharedPreferences("passcode.pref", Context.MODE_PRIVATE);
                                        SharedPreferences.Editor editor = preferences.edit();
                                        editor.remove("passcode");
                                        editor.apply();
                                        editor.putString("passcode", pinRest);
                                        editor.apply();
                                        alertDialog.dismiss();
                                        Dialog.dismiss();
                                        Toast toast = Toast.makeText(PinVerification.this, "You can now enter your new Pin code", Toast.LENGTH_LONG);
                                        toast.setGravity(Gravity.CENTER, 0, 0);
                                        toast.show();
                                    } else {
                                        Toast.makeText(PinVerification.this, "Error try again", Toast.LENGTH_LONG).show();

                                    }

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(PinVerification.this, "" + e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });

                }


            }
        });

    }

    @Override
    protected void onStart() {

        if (user != null && user.isEmailVerified()) {
            checkpref();
        } else {
            startActivity(new Intent(PinVerification.this, LoginActivity.class));
        }
        super.onStart();
    }

    private void checkpref() {///check if pincode exits
        SharedPreferences shf = getSharedPreferences("passcode.pref", Context.MODE_PRIVATE);
        final String strPref = shf.getString("passcode", null);

        DatabaseReference database = FirebaseDatabase.getInstance().getReference("Users");
        database.orderByChild("uid").equalTo(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String passcode = "" + ds.child("passcode").getValue();
                    String pincode = "" + ds.child("pincode").getValue();

                    if (pincode != null) {
                        if (pincode.isEmpty()) {
                            verifyInfoDialog();
                        }
                    } else {
                        verifyInfoDialog();
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}