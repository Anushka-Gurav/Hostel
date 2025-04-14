package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

public class DoctorSignupFragmentTab extends Fragment {
    EditText username,email,mobile,specialisation,pass;
    Button sign;
    float v=0;
    DatabaseHelper2 databaseHelper;
    TextView login;
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        ViewGroup root=(ViewGroup)inflater.inflate(R.layout.sign_upfragment2,container,false);
        pass=root.findViewById(R.id.pass);
        username=root.findViewById(R.id.name);
        mobile=root.findViewById(R.id.Mobile);
        sign=root.findViewById(R.id.sign_up);
        login=root.findViewById(R.id.login);


        pass.setTranslationX(800);
        username.setTranslationX(800);
        mobile.setTranslationX(800);
        sign.setTranslationX(800);
        login.setTranslationX(800);


        pass.setAlpha(v);
        username.setAlpha(v);
        mobile.setAlpha(v);

        sign.setAlpha(v);
        login.setAlpha(v);

        databaseHelper = new DatabaseHelper2(getContext());
        username.animate().translationX(0).alpha(1).setDuration(800).setStartDelay(300).start();
        mobile.animate().translationX(0).alpha(1).setDuration(800).setStartDelay(700).start();
        pass.animate().translationX(0).alpha(1).setDuration(800).setStartDelay(900).start();
        sign.animate().translationX(0).alpha(1).setDuration(800).setStartDelay(900).start();
        login.animate().translationX(0).alpha(1).setDuration(800).setStartDelay(900).start();

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent it=new Intent(getActivity(), LoginActivity.class);
                startActivity(it);
            }
        });

        sign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = username.getText().toString();

                String phone = mobile.getText().toString();
                String password = pass.getText().toString();

                if (name.isEmpty() || phone.isEmpty() || password.isEmpty()) {
                    Toast.makeText(getActivity(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                } else {
                    databaseHelper.insertUser(name, password, phone,
                            () -> {
                                Toast.makeText(getContext(), "User Registered Successfully!", Toast.LENGTH_SHORT).show();
                                Intent it = new Intent(getContext(), AttendanceActivity.class);
                                startActivity(it);
                            },
                            () -> Toast.makeText(getContext(), "Username already exists or Registration Failed!", Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });


        return root;
        }

}
