package com.example.vchat;


import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Summary extends AppCompatActivity {

    private DatePickerDialog datePickerDialog;
    private Button datebutton;
    private Button starttimebtn;
    private EditText summarybtn;

    FirebaseDatabase database;
    FirebaseAuth auth;

    int start_hour,start_minute;
    int end_hour, end_minute;
    private Button endtimebtn;
    private Button summaryview;
    String message;
    String username;
    String userid;
    String messageinput = "";
    String m;
    String inputdate;
    String input_start_time;
    String input_end_time;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        datebutton = findViewById(R.id.datePicker);
        ImageView btnclose = findViewById(R.id.close);

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        starttimebtn = findViewById(R.id.startingTimebtn);
        endtimebtn = findViewById(R.id.endingTimebtn);
        summaryview = findViewById(R.id.viewsummary);
        summarybtn = findViewById(R.id.summary);


        btnclose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(Summary.this,GroupChatActivity.class);
                startActivity(intent);
            }
        });

        datebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                month = month +1;
                int day = cal.get(Calendar.DATE);
                datePickerDialog = new DatePickerDialog(Summary.this, android.R.style.Theme_DeviceDefault_Dialog, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        datebutton.setText(day+"/"+month+"/"+year);
                        inputdate = day+"-"+month+"-"+year;
                    }
                },year,month,day );
                datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis()-1000);

                final Calendar calendar2 = Calendar.getInstance();
                //Set Minimum date of calendar
                calendar2.set(2022, 1, 1);
                datePickerDialog.getDatePicker().setMinDate(calendar2.getTimeInMillis());
                datePickerDialog.show();

            }
        });

        starttimebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        start_hour = selectedHour;
                        start_minute = selectedMinute;
                        input_start_time = String.format(Locale.getDefault(),"%02d:%02d:00",selectedHour,selectedMinute);
                        starttimebtn.setText(input_start_time);
                    }
                };
                TimePickerDialog timePickerDialog = new TimePickerDialog(Summary.this, android.R.style.Theme_Holo, onTimeSetListener,start_hour,start_minute,true);
                timePickerDialog.setTitle("select starting time");
                timePickerDialog.show();
            }
        });

        endtimebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        end_hour = selectedHour;
                        end_minute = selectedMinute;
                        input_end_time = String.format(Locale.getDefault(),"%02d:%02d:00",selectedHour,selectedMinute);
                        endtimebtn.setText(input_end_time);
                    }
                };
                TimePickerDialog timePickerDialog = new TimePickerDialog(Summary.this, android.R.style.Theme_Holo, onTimeSetListener,end_hour,end_minute,true);
                timePickerDialog.setTitle("select ending time");
                timePickerDialog.show();
            }
        });






        String url = "http://vchat24.herokuapp.com/summarize";

        summaryview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference reference = database.getReference();
                Query query = reference.child("Group chat").orderByChild("time").startAt(input_start_time).endAt(
                        input_end_time);
                query.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Query query1 = reference.child("Group chat").orderByChild("date").equalTo(inputdate);
                        query.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for(DataSnapshot ds : snapshot.getChildren()) {
                                    message = ds.child("message").getValue(String.class);
                                    userid = ds.child("uId").getValue(String.class);
                                    username = ds.child("userName").getValue(String.class);
                                    messageinput += username+": " +message+" ";
                                }
                                //summarybtn.setText(messageinput);
                                StringRequest request = new StringRequest(Request.Method.POST, url,  new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        try {
                                            JSONObject jsonObject = new JSONObject(response);
                                            SimpleDateFormat sfd = new SimpleDateFormat("dd-MM-yyyy");
                                            SimpleDateFormat sfd2 = new SimpleDateFormat("HH:mm:ss");
                                            //summarybtn.setText(sfd.format(new Date(1653801117746L)).toString());
                                            summarybtn.setText(jsonObject.getString("summaryText"));

                                        } catch (JSONException e) {
                                            Toast.makeText(Summary.this, "error", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                },
                                        new Response.ErrorListener() {
                                            @Override
                                            public void onErrorResponse(VolleyError error) {
                                                Toast.makeText(Summary.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }){
                                    @Override
                                    protected Map<String, String> getParams() throws AuthFailureError {
                                        Map<String, String> params = new HashMap<String, String>();
                                        params.put("summarytext",messageinput);
                                        return params;
                                    }
                                };
                                RequestQueue queue = Volley.newRequestQueue(Summary.this);
                                queue.add(request);

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });



                    }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

            }
        });


        // Post params to be sent to the server
//        summaryview.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
////
//
//            }
//        });









//        summaryview.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                ArrayList<String> cityList = new ArrayList<>();
//
//                database.getReference().child("Group chat").addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        cityList.clear();
//
//                        for(DataSnapshot dataSnapshot:snapshot.getChildren()){
//                            cityList.add(dataSnapshot.getValue().toString());
//                        }
//                        Summarizer s = new Summarizer();
//                        String result = s.Summarize("Human 1: Hi!\n" +
//                                "Human 2: What is your favorite holiday?\n" +
//                                "Human 1: one where I get to meet lots of different people.\n" +
//                                "Human 2: What was the most number of people you have ever met during a holiday?\n" +
//                                "Human 1: Hard to keep a count. Maybe 25.\n" +
//                                "Human 2: Which holiday was that?\n" +
//                                "Human 1: I think it was Australia\n" +
//                                "Human 2: Do you still talk to the people you met?\n" +
//                                "Human 1: Not really. The interactions are usually short-lived but it's fascinating to learn where people are coming from and what matters to them\n" +
//                                "Human 2: Yea, me too. I feel like God often puts strangers in front of you, and gives you an opportunity to connect with them in that moment in deeply meaningful ways. Do you ever feel like you know things about strangers without them telling you?\n" +
//                                "Human 1: what do you mean?\n" +
//                                "Human 2: I think it's like a 6th sense, often seen as \"cold readings\" to people, but can be remarkably accurate. I once sat next to a man in a coffee and I felt a pain in my back. I asked the stranger if he had a pain. It turns out that he did in the exact spot, and said he pulled a muscle while dancing at a party. I had never met the man before and never saw him again.\n" +
//                                "Human 1: Wow! That's interesting, borderline spooky\n" +
//                                "Human 2: There's this practice called \"Treasure Hunting\" that's kind of a fun game you play in a public place. There's a book called \"The Ultimate Treasure Hunt\" that talks about it. You use your creativity to imagine people you will meet, and you write down a description, then you associate them with a positive message or encouraging word. Maybe you saw a teenage boy in a red hat at the shopping mall in your imagination, then while at the mall, you may find someone who matches that description. You show that you have a message for him and that you have a message for a boy in a red hat. You then give him a message of kindness or whatever was on your heart. You have no idea, sometimes you meet someone who is having a really hard day, and it brings them to tears to have a stranger show them love.\n" +
//                                "Human 1: So, do you do treasure hunting often?\n" +
//                                "Human 2: I did more when I was in grad school (and had more time). I would usually go with friends. For a while I would go to the farmers market in Santa Cruz every week and try to feel if there is something I am supposed to tell a stranger. Usually, they are vague hope-filled messages, but it's weird when I blurt out something oddly specific..",20);
//
//                        summarybtn.setText(result);
//
//
////                      //   "context" must be an Activity, Service or Application object from your app.
////                        if (! Python.isStarted()) {
////                            Python.start(new AndroidPlatform(Summary.this));
////                        }
////
////                        Python py = Python.getInstance();
////
////                        PyObject pyObject = py.getModule("summary");
////
////                        PyObject obj = pyObject.callAttr("main");
////
////                        summarybtn.setText(obj.toString());
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//
//                    }
//                });
//            }
//        });


    }
}