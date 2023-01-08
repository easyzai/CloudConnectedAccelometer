package com.awaissaikhu.cloudconnectedaccelometer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager sensorMan;
    private Sensor accelerometer;

    private float[] mGravity;
    private double mAccel;
    private double mAccelCurrent;
    private double mAccelLast;

    private double mAccel1;
    private double mAccelCurrent1;
    private double mAccelLast1;

    ArrayList<leaderboardModal> users;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    boolean activityonforground=true;
    Timer timer;
    TextView timeago,timetogo;
    RecyclerView recyclerView;
    LeaderboardAdapter leaderboardAdapter;
    int countdown=0;
    ImageView edit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeUI();
        sharedPreferences=getSharedPreferences("cloudapp",MODE_PRIVATE);
        editor=sharedPreferences.edit();
        users=new ArrayList<>();
        recyclerView.hasFixedSize();
        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        leaderboardAdapter=new LeaderboardAdapter(this,users);
        recyclerView.setAdapter(leaderboardAdapter);
        sensorMan = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorMan.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mAccel = 0.00f;
        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        mAccelLast = SensorManager.GRAVITY_EARTH;
        sensorMan.registerListener(this, accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL);
        update();


    }

    private void updatetime() {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                timeago.setText(Calendar.getInstance().get(Calendar.HOUR)+":"+Calendar.getInstance().get(Calendar.MINUTE));
                timetogo.setText(""+(60-countdown));

            }
        });

    }

    void update(){
        db.collection("Users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            users.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                User user=document.toObject(User.class);

                                calculatescore(user.name,user.accelerometer_data);
                            }


                        } else {
                            Log.d("test", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }
    private int hitCount = 0;
    private double hitSum = 0;
    private double hitResult = 0;

    private final int SAMPLE_SIZE = 50; // change this sample size as you want, higher is more precise but slow measure.
    private final double THRESHOLD = 0.2; // change this threshold as you want, higher is more spike movement

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            mGravity = event.values.clone();
            // Shake detection
            float x = mGravity[0];
            float y = mGravity[1];
            float z = mGravity[2];
            mAccelLast = mAccelCurrent;
            mAccelCurrent = Math.sqrt(x * x + y * y + z * z);
            double delta = mAccelCurrent - mAccelLast;
            mAccel = mAccel * 0.9f + delta;

            if (hitCount <= SAMPLE_SIZE) {
                hitCount++;
                hitSum += Math.abs(mAccel);
            } else {
                hitResult = hitSum / SAMPLE_SIZE;

                if (hitResult > THRESHOLD) {
                   Points points=new Points(x,y,z);
                   if(activityonforground)
                  updateshareprefresence(points);

                } else {
                }

                hitCount = 0;
                hitSum = 0;
                hitResult = 0;
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        activityonforground=false;

        timer.cancel();
    }

    @Override
    protected void onResume() {
        super.onResume();
        activityonforground=true;
        timer=new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                countdown++;
                if(countdown>=60){
                    countdown=0;
                    update();
                }
                updatetime();
            }
        },0,1000);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void updateshareprefresence(Points points) {

        String oldarray=sharedPreferences.getString("data","null");
        if(oldarray.equals("null")){
            JSONArray jsonArray = new JSONArray();
            jsonArray.put(points.getJSONObject());
         oldarray=   jsonArray.toString();
            editor.putString("data",oldarray).apply();
        }
        else{
            try {
                JSONArray jsonArray=new JSONArray(oldarray);
                jsonArray.put(points.getJSONObject());
                oldarray=jsonArray.toString();
                if(jsonArray.length()>=1000){
                    uploadtofirestore(oldarray);
                }
                else{

                    editor.putString("data",oldarray).apply();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadtofirestore(String oldarray) {
        DocumentReference Ref = db.collection("Users").document(sharedPreferences.getString("email","").split("@")[0]);
        Ref.update("accelerometer_data",oldarray).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(MainActivity.this, "Data uploaded Successfully", Toast.LENGTH_SHORT).show();

                editor.remove("data").apply();
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Fail to upload data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private int hitCount1 = 0;
    private double hitSum1 = 0;
    private double hitResult1 = 0;
    void calculatescore(String name,String jsondata){
        Log.e("user",name);
        Double score=0.0;
        if(jsondata.length()>0) {
            try {
                JSONArray jsonArray = new JSONArray(jsondata);

                mAccelLast1 = Math.sqrt(jsonArray.getJSONObject(0).getDouble("x") * jsonArray.getJSONObject(0).getDouble("x") + jsonArray.getJSONObject(0).getDouble("y") * jsonArray.getJSONObject(0).getDouble("y") + jsonArray.getJSONObject(0).getDouble("z") * jsonArray.getJSONObject(0).getDouble("z"));
                for (int i = 1; i < jsonArray.length(); i++) {
                    double x = jsonArray.getJSONObject(i).getDouble("x");
                    double y = jsonArray.getJSONObject(i).getDouble("y");
                    double z = jsonArray.getJSONObject(i).getDouble("z");
                    mAccelCurrent1 = Math.sqrt(x * x + y * y + z * z);
                    double delta = mAccelCurrent1 - mAccelLast1;
                    mAccel1 = mAccel1 * 0.9f + delta;

                    if (hitCount1 <= SAMPLE_SIZE) {
                        hitCount1++;
                        hitSum1 += Math.abs(mAccel1);
                    } else {
                        hitResult1 = hitSum1 / SAMPLE_SIZE;

                        if (hitResult1 > THRESHOLD) {
                            score += hitResult1;
                        } else {
                        }

                        hitCount1 = 0;
                        hitSum1 = 0;
                        hitResult1 = 0;
                    }
                    mAccelLast1 = mAccelCurrent1;
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        users.add(new leaderboardModal(1,name,score));
        Log.e("user",users.size()+name);
        //sorting for ranking
        Collections.sort(users, new Comparator<leaderboardModal>(){
            public int compare(leaderboardModal obj1, leaderboardModal obj2) {

                return Integer.valueOf((int) obj2.score).compareTo(Integer.valueOf((int) obj1.score)); // To compare string values
            }
        });
        Log.e("user",users.size()+name);
        leaderboardAdapter.notifyDataSetChanged();
    }
    private void initializeUI() {

        edit = findViewById(R.id.edit);
        timeago = findViewById(R.id.timeago);
        timetogo = findViewById(R.id.timetogo);
        recyclerView = findViewById(R.id.recylerview);
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditInfoActivity.class);

                startActivity(intent);
            }
        });
    }
}