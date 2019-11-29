package com.example.cs125finalproject.ui.similar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.cs125finalproject.MySingleton;
import com.example.cs125finalproject.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

public class SimilarFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_similar, container, false);

        //Nothing special, create database reference.
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        //Create a new ArrayAdapter with your context and the simple layout for the dropdown menu provided by Android
        final ArrayAdapter<String> autoComplete = new ArrayAdapter<>(getActivity(),android.R.layout.simple_list_item_1);
        //Child the root before all the push() keys are found and add a ValueEventListener()
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Basically, this says "For each DataSnapshot *Data* in dataSnapshot, do what's inside the method.
                for (DataSnapshot suggestionSnapshot : dataSnapshot.getChildren()){
                    //Get the suggestion by childing the key of the string you want to get.
                    String suggestion = suggestionSnapshot.child("Number").getValue(String.class) + " -- " + suggestionSnapshot.child("Name").getValue(String.class);
                    //Add the retrieved string to the list
                    autoComplete.add(suggestion);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
        final AutoCompleteTextView ACTV = root.findViewById(R.id.auto);
        ACTV.setAdapter(autoComplete);

        Button similar = root.findViewById(R.id.button);
        similar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String str = ACTV.getText().toString();
                boolean contains = false;
                ListAdapter listAdapter = ACTV.getAdapter();
                for(int i = 0; i < listAdapter.getCount(); i++) {
                    String temp = listAdapter.getItem(i).toString();
                    if(str.compareTo(temp) == 0) {
                        contains = true;
                    }
                }
                if (contains == false) {
                    ACTV.setText("");
                }
                if (ACTV.getText().toString().equals("")) {
                    Toast.makeText(getActivity(), "Error: Choose Class From List", Toast.LENGTH_LONG).show();
                    TextView api = root.findViewById(R.id.apiResponse);
                    api.setText("");
                    return;
                }
                String[] course = ACTV.getText().toString().split("--");
                connect(course[1].trim(), root);
            }
        });

        return root;
    }

    private void connect(String course, View root) {
        JSONObject param = new JSONObject();
        try {
            param.put("course", course);
        } catch (Exception e) {
            return;
        }
        final TextView api = root.findViewById(R.id.apiResponse);
        String url = "http://uiuc.us-east-1.elasticbeanstalk.com/similar";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, url, param, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        String classes = "";
                        try {
                            JSONArray result = response.getJSONArray("result");
                            for (int i = 0; i < result.length(); i++) {
                                classes = classes + result.getJSONObject(i).get("Number").toString()+ " -- " + result.getJSONObject(i).get("Name").toString() + "\n";
                            }
                        } catch (Exception e) {
                            return;
                        }
                        api.setText("Similar Courses to " + classes);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getActivity(), error.toString(), Toast.LENGTH_LONG).show();
                    }
                });
        // Access the RequestQueue through your singleton class.
        MySingleton.getInstance(getActivity()).addToRequestQueue(jsonObjectRequest);
    }
}
