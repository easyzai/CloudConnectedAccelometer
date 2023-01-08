package com.awaissaikhu.cloudconnectedaccelometer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditInfoActivity extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String email="";
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    EditText name,course,year;
    Button save;
    ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_info);
        initializeUI();
        sharedPreferences=getSharedPreferences("cloudapp",MODE_PRIVATE);
        editor=sharedPreferences.edit();
        if(getIntent().hasExtra("email")){
            email=getIntent().getStringExtra("email");
            editor.putString("email",email);
            editor.apply();
        }
        else{
        email=sharedPreferences.getString("email","");
        name.setText(sharedPreferences.getString("name",""));
        course.setText(sharedPreferences.getString("course",""));
        year.setText(sharedPreferences.getString("year",""));
        save.setText("Update");
        }
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(TextUtils.isEmpty(name.getText().toString())||TextUtils.isEmpty(course.getText().toString())||TextUtils.isEmpty(year.getText().toString())){
                    Toast.makeText(EditInfoActivity.this, "Please Fill All the Fields", Toast.LENGTH_SHORT).show();
                }
                else{
                    editor.putString("name",name.getText().toString());
                    editor.putString("course",course.getText().toString());
                    editor.putString("year",year.getText().toString());
                    editor.apply();
                    Map<String, Object> data = new HashMap<>();
                    data.put("name", name.getText().toString());
                    data.put("course", course.getText().toString());
                    data.put("year", year.getText().toString());
                    data.put("accelerometer_data", "");
                    db.collection("Users").document(email.split("@")[0]).set(data);
                    startActivity(new Intent(EditInfoActivity.this,MainActivity.class));
                    finish();
                }
            }
        });

    }
    private void initializeUI() {
        name = findViewById(R.id.name);
        course = findViewById(R.id.course);
        year = findViewById(R.id.year);
        save = findViewById(R.id.save);
        progressBar = findViewById(R.id.progressBar);
    }
}