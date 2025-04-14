package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ForgetPatient extends AppCompatActivity {
    EditText e1,e2,e3;
    Button b1;
    DatabaseHelper helper;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forget_password_of_patient);
        e1 = findViewById(R.id.email);
        e2 = findViewById(R.id.pass);
        e3 = findViewById(R.id.confirm_pass);
        b1 = findViewById(R.id.login_btn);
        helper = new DatabaseHelper(ForgetPatient.this);

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = e1.getText().toString();
                String password = e2.getText().toString();
                String confirm = e3.getText().toString();
                if (username.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
                    Toast.makeText(ForgetPatient.this, "Enter Data", Toast.LENGTH_SHORT).show();
                } else {
                    helper.updateUserPassword(username, password, confirm, new DatabaseHelper.OnResultListener<String>() {
                        @Override
                        public void onSuccess(String result) {
                            Toast.makeText(ForgetPatient.this, result, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(ForgetPatient.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            }
        });
    }

}
