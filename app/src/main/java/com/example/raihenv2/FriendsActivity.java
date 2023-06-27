package com.example.raihenv2;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FriendsActivity extends Fragment {

    public static String transfer;

    private DatabaseReference databaseReference;
    private FirebaseAuth myAuth;
    private DatabaseReference profileRef;

    private StorageReference storageReference;
    private StorageReference pictureRef;

    String userId;
    TextView uname, uid, email, endList;
    Button friendsTab, groupTab, addFriend;
    FloatingActionButton addGroup;
    ImageView friendImage;

    SearchView search;
    ListView myListView;

    ContextWrapper cw;
    File directory, gDirectory;

    String[] images;
    String[] names;
    int friendCount = 0;
    int groupCount = 0;
    int click = 0;

    ConstraintLayout friend_info;
    LinearLayout friend_list;

    boolean flag = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_friends, container,false);

        try {
            myAuth = FirebaseAuth.getInstance();
            databaseReference = FirebaseDatabase.getInstance().getReference().child("users");
            storageReference = FirebaseStorage.getInstance().getReference();
            //UserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            //profileRef = databaseReference.child("users/" + myAuth.getCurrentUser().getUid() + "/profile.jpg");
        } catch (Exception e) {
            e.printStackTrace();
        }

        cw = new ContextWrapper(getContext());
        directory = cw.getDir("Friends", Context.MODE_PRIVATE);
        gDirectory = cw.getDir("Groups", Context.MODE_PRIVATE);

        friend_info = view.findViewById(R.id.friend_profile);
        friend_list = view.findViewById(R.id.listLayout);
        myListView = (ListView) view.findViewById(R.id.friendsListView);

        myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (click == 0) {
                    TextView temp = view.findViewById(R.id.textView);
                    String temporary = temp.getText().toString().trim();
                    lookUpFriend(temporary);
                } else {
                    transfer = names[position];
                    startActivity(new Intent(getContext(), GroupActivity.class));
                }
            }
        });

        friendImage = view.findViewById(R.id.friend_image);
        uname = view.findViewById(R.id.friend_uname);
        uid = view.findViewById(R.id.friend_uid);
        email = view.findViewById(R.id.friend_email);
        friendsTab = view.findViewById(R.id.friendsBtn);
        groupTab = view.findViewById(R.id.groupBtn);
        addFriend = view.findViewById(R.id.addFriendBtn);
        addGroup = view.findViewById(R.id.addGroup);
        endList = view.findViewById(R.id.listEnd);

        addGroup.setOnClickListener(this::OnClick);
        addFriend.setOnClickListener((this::OnClick));
        friendsTab.setOnClickListener((this::OnClick));
        groupTab.setOnClickListener((this::OnClick));

        search = view.findViewById(R.id.friendSearch);

        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (flag) {
                    flag = false;
                    lookUpFriend(query);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        setFriendsList();

        return view;
    }

    public void setGroupList() {
        click = 1;
        friendCount = 0;
        addGroup.setVisibility(View.VISIBLE);
        if (groupCount == 0) {
            images = new String[gDirectory.listFiles().length];
            names = new String[gDirectory.listFiles().length];
            for (File file : gDirectory.listFiles()) {
                names[groupCount] = file.getName();
                File image = new File(file, "profile.jpg");
                images[groupCount] = image.getPath();
                groupCount++;
            }
        }

        CustomAdapter customAdapter = new CustomAdapter();
        myListView.setAdapter(customAdapter);
    }

    public void setFriendsList() {
        click = 0;
        groupCount = 0;
        addGroup.setVisibility(View.GONE);
        if (friendCount == 0) {
            images = new String[directory.listFiles().length];
            names = new String[directory.listFiles().length];
            for (File file : directory.listFiles()) {
                File destination = new File(file, "friend_auth.json");
                File image = new File(file, "profile.jpg");
                if (destination.exists()) {
                    try {
                        FileReader fileReader = new FileReader(destination);
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

                        names[friendCount] = jsonObject.get("uname").toString();
                        images[friendCount] = image.getPath();
                        friendCount++;
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        CustomAdapter customAdapter = new CustomAdapter();
        myListView.setAdapter(customAdapter);
    }

    public void OnClick(View v) {
        switch (v.getId()) {
            case R.id.addFriendBtn:
                friendCount = 0;

                String state = addFriend.getText().toString().trim();
                String username = uname.getText().toString().trim();
                userId = uid.getText().toString().trim();
                String friendEmail = email.getText().toString().trim();

                String JSON_STRING = "{\"uname\":\"" + username +"\",\"uid\":\"" + userId + "\",\"email\":\"" + friendEmail + "\"}";
                File friendFile = new File(directory, username);
                File file = new File(friendFile, "friend_auth.json");

                if (state.equals("Add to friendlist")) {
                    try {
                        FileWriter writer = new FileWriter(file);
                        writer.write(JSON_STRING);
                        writer.close();
                        addFriend.setText("Remove from friendlist");
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(),"Friend couldn't be added, try again later!", Toast.LENGTH_SHORT);
                    }
                } else {

                    try {
                        file.delete();
                        addFriend.setText("Add to friendlist");
                    } catch (Exception e) {
                        e.printStackTrace();
                        //Toast.makeText(getApplicationContext(),"Friend couldn't be removed, try again later!", Toast.LENGTH_SHORT);
                    }
                }
                break;
            case R.id.friendsBtn:
                friend_info.setVisibility(View.GONE);
                friend_list.setVisibility(View.VISIBLE);
                endList.setVisibility(View.VISIBLE);
                setFriendsList();
                break;
            case R.id.groupBtn:
                friend_info.setVisibility(View.GONE);
                friend_list.setVisibility(View.VISIBLE);
                endList.setVisibility(View.VISIBLE);
                setGroupList();
                break;
            case R.id.addGroup:
                transfer = "";
                startActivity(new Intent(getContext(), GroupActivity.class));
                break;
        }
    }

    private void lookUpFriend(String input) {
        if (input.isEmpty()) {
            Toast.makeText(getContext(), "Please input something to search!", Toast.LENGTH_SHORT);
            flag = true;
            return;
        }
        try {
            endList.setVisibility(View.GONE);
            profileRef = databaseReference.child(input);
            profileRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (task.getResult().getValue() != null) {
                        friend_info.setVisibility(View.VISIBLE);
                        friend_list.setVisibility(View.GONE);
                        uname.setText(task.getResult().child("uname").getValue().toString());
                        userId = task.getResult().child("uid").getValue().toString();
                        uid.setText(userId);
                        email.setText(task.getResult().child("email").getValue().toString());

                        File friendFile = new File(directory, input);
                        File file = new File(friendFile, "friend_auth.json");

                        if (file.exists()) {
                            addFriend.setText("Remove from friendlist");
                        }

                        //File friendFile = new File(directory, input);
                        //File file = new File(friendFile, "profile.jpg");
                        File newFile = new File(friendFile, "profile.jpg");

                        if (newFile.exists()) {
                            friendImage.setImageBitmap(BitmapFactory.decodeFile(newFile.getPath()));
                        } else {
                            friendFile.mkdir();
                        }
                        try {
                            pictureRef = storageReference.child("users/" + userId + "/profile.jpg");
                            pictureRef.getFile(newFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                    friendImage.setImageBitmap(BitmapFactory.decodeFile(newFile.getPath()));
                                    //Toast.makeText(FriendsActivity.this,"Profile picture saved/updated for future loads", Toast.LENGTH_SHORT);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    friendImage.setImageDrawable(getResources().getDrawable(R.drawable.jotaro));
                                    //Toast.makeText(FriendsActivity.this,"Can't find custom profile picture, loading default profile picture!", Toast.LENGTH_SHORT);
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    } else {
                        Toast.makeText(getContext(),"Username can't be found!", Toast.LENGTH_SHORT);
                        return;
                    }
                }
            });
        } finally {
            File file = new File(directory, input);
            File destination = new File(file, "friend_auth.json");
            File image = new File(file, "profile.jpg");
            if (destination.exists() && uname.getText().toString().trim().isEmpty()) {
                try {
                    FileReader fileReader = new FileReader(destination);
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

                    String temp;

                    friend_info.setVisibility(View.VISIBLE);
                    friend_list.setVisibility(View.GONE);

                    temp = jsonObject.get("uname").toString();
                    uname.setText(temp);
                    temp = jsonObject.get("uid").toString();
                    uid.setText(temp);
                    temp = jsonObject.get("email").toString();
                    email.setText(temp);

                    friendImage.setImageBitmap(BitmapFactory.decodeFile(image.getPath()));

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            flag = true;
        }

    }

    class CustomAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            if (click == 0) {
                return friendCount;//length of array
            } else {
                return groupCount;
            }
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = getLayoutInflater().inflate(R.layout.customlayout, null);
            ImageView myImageView = view.findViewById(R.id.imageView);
            TextView myTextView = view.findViewById(R.id.textView);

            //set image for [position]
            File checkFile = new File(images[position]);
            if (checkFile.exists()) {
                myImageView.setImageBitmap(BitmapFactory.decodeFile(images[position]));
            } else {
                myImageView.setImageDrawable(getResources().getDrawable(R.drawable.jotaro));
            }
            Log.d("TAG", "getView: " + images[position]);
            //set name for [position]
            myTextView.setText(names[position]);
            return view;
        }
    }
}