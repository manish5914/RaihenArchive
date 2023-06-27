package com.example.raihenv2;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.Executor;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseAuth mAuth;
    TextView fgtPwd;
    private EditText editUname, editEmail, editPwd, editPwd2, editEnter;
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    //private Switch switchy;
    Button submitbtn, logbtn, signbtn;
    CheckBox rememberMe;
    ProgressDialog progressDialog;

    ConstraintLayout signUp, logIn;

    String email = "";
    String pwd = "";

    boolean page = false;
    static boolean log = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        try {
            mAuth = FirebaseAuth.getInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Set up for magic trick
        signUp = findViewById(R.id.signUpTextArea);
        logIn = findViewById(R.id.logInTextArea);
        //Was a short magic trick

        //switchy = findViewById(R.id.switch1);
        logbtn = findViewById(R.id.logBtn);
        signbtn = findViewById(R.id.signBtn);
        rememberMe = findViewById(R.id.remMe);

        logbtn.setOnClickListener(this::onClick);
        signbtn.setOnClickListener(this::onClick);

        //Press enter to click submit
        editEnter = findViewById(R.id.main_edit_pwd);
        editPwd2 = findViewById(R.id.signup_edit_pwd2);
        editEnter.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                    submitbtn.performClick();
                    return true;
                }
                return false;
            }
        });
        editPwd2.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                    submitbtn.performClick();
                    return true;
                }
                return false;
            }
        });
        //end of press enter to click submit

        try {
            loadCredentials();
        } catch (Exception e) {
            e.printStackTrace();
        }


        //They doing stuff
        submitbtn = (Button) findViewById(R.id.mainSubmit);
        submitbtn.setOnClickListener((View.OnClickListener) this);
        fgtPwd = (TextView) findViewById(R.id.forgotPwd);
        fgtPwd.setOnClickListener(this);
        //End of doing stuff

    }

    private void saveCredentials(String email, String password) throws IOException {
        String JSON_STRING = "{\"Email\":\"" + email + "\",\"password\":\"" + password + "\"}";
        ContextWrapper cw = new ContextWrapper(LoginActivity.this.getApplicationContext());
        File directory = cw.getDir("SomethingRepo", Context.MODE_PRIVATE);
        File file = new File(directory, "auth.json");
        FileWriter writer = new FileWriter(file);
        writer.write(JSON_STRING);
        writer.close();
    }

    private void deleteCredentials() {
        ContextWrapper cw = new ContextWrapper(LoginActivity.this.getApplicationContext());
        File directory = cw.getDir("SomethingRepo", Context.MODE_PRIVATE);
        File file = new File(directory, "auth.json");
        file.delete();
    }

    private void loadCredentials() throws IOException, JSONException {
        ContextWrapper cw = new ContextWrapper(LoginActivity.this.getApplicationContext());
        File directory = cw.getDir("SomethingRepo", Context.MODE_PRIVATE);
        File file = new File(directory, "auth.json");

        if (file.exists()) {
            try {
                FileReader fileReader = new FileReader(file);
                BufferedReader bufferedReader = new BufferedReader(fileReader);
                StringBuilder stringBuilder = new StringBuilder();
                String line = bufferedReader.readLine();
                while (line != null){
                    stringBuilder.append(line).append("\n");
                    line = bufferedReader.readLine();
                }
                bufferedReader.close();
                // This responce will have Json Format String
                String responce = stringBuilder.toString();

                JSONObject jsonObject  = new JSONObject(responce);
                //Java Object
                email = jsonObject.get("Email").toString();
                pwd = jsonObject.get("password").toString();
                editEmail = findViewById(R.id.main_edit_email);
                editPwd = findViewById(R.id.main_edit_pwd);
                editEmail.setText(email);
                editPwd.setText(pwd);
                editPwd.requestFocus();
                if (log) {
                    userLogin();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            rememberMe.setChecked(false);
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mainSubmit:
                if (page) {
                    editUname = findViewById(R.id.signup_edit_uname);
                    editEmail = findViewById(R.id.signup_edit_email);
                    editPwd = findViewById(R.id.signup_edit_pwd1);
                    registerUser();
                } else {
                    editEmail = findViewById(R.id.main_edit_email);
                    editPwd = findViewById(R.id.main_edit_pwd);
                    userLogin();
                    break;
                }
                break;
            case R.id.forgotPwd:
                editEmail = findViewById(R.id.main_edit_email);
                resetPwd();
                break;
            case R.id.signBtn:
                logIn.setVisibility(View.GONE);
                signUp.setVisibility(View.VISIBLE);
                page = true;
                break;
            case R.id.logBtn:
                page = false;
                logIn.setVisibility(View.VISIBLE);
                signUp.setVisibility(View.GONE);
                break;

        }
    }

    private void resetPwd() {
        String email = editEmail.getText().toString().trim();

        if (email.isEmpty()) {
            editEmail.setError("Requires email to send reset password mail!");
            editEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editEmail.setError("Please provide a valid email!");
            editEmail.requestFocus();
            return;
        }

        try {
            mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Check your email to reset password!", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(LoginActivity.this, "Try again, an error occurred!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(LoginActivity.this,"Request fail, please try again!", Toast.LENGTH_SHORT);
        }

    }

    private void userLogin() {
        progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.show();
        progressDialog.setContentView(R.layout.progress_dialog);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);


        if (email.isEmpty()) {
            email = editEmail.getText().toString().trim();
            pwd = editPwd.getText().toString().trim();
        }
        if (rememberMe.isChecked()) {
            try {
                saveCredentials(email, pwd);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                deleteCredentials();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (email.isEmpty()) {
            editEmail.setError("Email is required!");
            editEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editEmail.setError("Please provide a valid email!");
            editEmail.requestFocus();
            return;
        }

        if (pwd.isEmpty()) {
            editPwd.setError("Password is required!");
            editPwd.requestFocus();
            return;
        }

        if (pwd.length() < 6) {
            editPwd.setError("Passwords are at least 6 characters long!");
            editPwd.requestFocus();
            return;
        }

        try {
            mAuth.signInWithEmailAndPassword(email, pwd).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();

//                    if(user.isEmailVerified()) {
//                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
//                    } else {
//                        user.sendEmailVerification();
//                        Toast.makeText(LoginActivity.this, "Check your email to verify your account!", Toast.LENGTH_LONG).show();
//                    }
                    } else {
                        Toast.makeText(LoginActivity.this, "Unable to log in. Check credentials and try again!", Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                    }
                }
            });

            log = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void registerUser() {
        String uname = editUname.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String pwd = editPwd.getText().toString().trim();
        String pwd2 = editPwd2.getText().toString().trim();

        //uname
        if (uname.isEmpty()) {
            editUname.setError("Username is required!");
            editUname.requestFocus();
            return;
        }

        //email
        if (email.isEmpty()) {
            editEmail.setError("Email is required!");
            editEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editEmail.setError("Please provide a valid email!");
            editEmail.requestFocus();
            return;
        }

        //password
        if (pwd.isEmpty()) {
            editPwd.setError("Password is required!");
            editPwd.requestFocus();
            return;
        }

        if (pwd2.isEmpty()) {
            editPwd2.setError("Please confirm password.");
            editPwd2.requestFocus();
            return;
        }

        if (pwd.length() < 6) {
            editPwd.setError("Password should be more than 6 characters!");
            editPwd.requestFocus();
            return;
        }

        if (!pwd.equals(pwd2)) {
            editPwd2.setError("Password doesn't match!");
            editPwd2.requestFocus();
            return;
        }

        try {
            mAuth.createUserWithEmailAndPassword(email, pwd)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                String fuid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                User user = new User(uname, email, fuid);

                                FirebaseDatabase.getInstance().getReference("users")
                                        .child(uname)
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DataSnapshot> task) {
                                                if (task.getResult().getValue() != null) {
                                                    editUname.setError("Username already exists! Try another username!");
                                                    editUname.requestFocus();
                                                    return;
                                                } else {
                                                    FirebaseDatabase.getInstance().getReference("users")
                                                            .child(uname)
                                                            .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                Toast.makeText(LoginActivity.this, "User has been registered successfully!", Toast.LENGTH_LONG).show();
                                                            } else {
                                                                Toast.makeText(LoginActivity.this, "Failed to register! Try again later.", Toast.LENGTH_LONG).show();
                                                            }
                                                        }
                                                    });
                                                    FirebaseDatabase.getInstance().getReference("list").child(fuid).setValue(uname);
                                                }
                                            }
                                        });
                            } else {
                                Toast.makeText(LoginActivity.this, "Failed to register!", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}