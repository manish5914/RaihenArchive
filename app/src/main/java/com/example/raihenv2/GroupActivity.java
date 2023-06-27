package com.example.raihenv2;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class GroupActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 100;

    private Uri imageUri = null;

    ContextWrapper cw;
    File directory, fDirectory;
    //ProgressDialog progressDialog;


    ConstraintLayout groupInfo, addFriend;

    ImageView img;
    Button addMember, groupName, back, apply, cancel;
    TextView gName;
    EditText g_Name;
    ListView memberList, friendList;

    String[] images;
    String[] names;
    boolean[] chosen;
    //String[] mNames;

    boolean flag, newG;
    int memberCount = 0;
    int friendCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        groupInfo = findViewById(R.id.groupInfo);
        addFriend = findViewById(R.id.adding_member);

        cw = new ContextWrapper(getApplicationContext());
        directory = cw.getDir("Groups", Context.MODE_PRIVATE);
        fDirectory = cw.getDir("Friends", Context.MODE_PRIVATE);

        memberList = findViewById(R.id.member_list);
        friendList = findViewById(R.id.f_list);
        img = findViewById(R.id.group_image);

        addMember = findViewById(R.id.add_member);
        groupName = findViewById(R.id.change_GN);
        back = findViewById(R.id.back);
        gName = findViewById(R.id.group_name);
        g_Name = findViewById(R.id.group_name_edit);
        apply = findViewById(R.id.apply);
        cancel = findViewById(R.id.cancel);

        groupName.setOnClickListener(this::OnClick);
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });
        addMember.setOnClickListener(this::OnClick);
        apply.setOnClickListener(this::OnClick);
        cancel.setOnClickListener(this::OnClick);
        back.setOnClickListener(this::OnClick);

        setPage();

    }

    public void OnClick(View v) {
//        progressDialog = new ProgressDialog(GroupActivity.this);
//        progressDialog.show();
//        progressDialog.setContentView(R.layout.progress_dialog);
//        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        switch (v.getId()) {
            case R.id.change_GN:
                if (groupName.getText().equals("Change name")) {
                    groupName.setText("Save");
                    gName.setVisibility(View.GONE);
                    g_Name.setVisibility(View.VISIBLE);
                    g_Name.setText(gName.getText());
                    g_Name.requestFocus();
                } else {
                    groupName.setText("Change name");
                    g_Name.clearFocus();
                    gName.setVisibility(View.VISIBLE);
                    g_Name.setVisibility(View.GONE);
                    gName.setText(g_Name.getText());

                    String name = gName.getText().toString().trim();

                    if (name.isEmpty()) {
                        groupName.setText("Save");
                        gName.setVisibility(View.GONE);
                        g_Name.setVisibility(View.VISIBLE);
                        g_Name.setText(gName.getText());
                        g_Name.setError("Group name can't be empty!");
                        g_Name.requestFocus();
                        return;
                    }

                    File groupFile = new File(directory, name);

                    if (FriendsActivity.transfer.isEmpty() || (!FriendsActivity.transfer.isEmpty() && !FriendsActivity.transfer.equals(name))) {
                        if (groupFile.exists()) {
                            groupName.setText("Save");
                            gName.setVisibility(View.GONE);
                            g_Name.setVisibility(View.VISIBLE);
                            g_Name.setText(gName.getText());
                            g_Name.setError("Group name already exist!");
                            g_Name.requestFocus();
                            return;
                        }
                    }

                    if (!FriendsActivity.transfer.isEmpty() && !FriendsActivity.transfer.equals(name)) {
                        File oldFile = new File(directory, FriendsActivity.transfer);
                        if (oldFile.renameTo(groupFile)) {
                            FriendsActivity.transfer = name;
                            //newG = false;
                        }
                    } else {
                        //if file doesn't exist, create it
                        groupFile.mkdir();
                    }

                    try {
                        //saving image
                        if (imageUri != null) {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);

                            File file = new File(groupFile, "GroupPicture.jpg");
                            file.delete();

                            //Log.d("path", file.toString());
                            FileOutputStream fos = null;
                            try {
                                fos = new FileOutputStream(file);
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                                fos.flush();
                                fos.close();
                            } catch (java.io.IOException e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case R.id.back:
                finish();
                //Ashley add code to call FriendsActivity fragment
                break;

            case R.id.add_member:
                if (groupName.getText().equals("Save")) {
                    //g_Name.setText(gName.getText());
                    g_Name.setError("Save a group name first!");
                    g_Name.requestFocus();
                    return;
                }

                addFriend.setVisibility(View.VISIBLE);
                groupInfo.setVisibility(View.GONE);
                setFriendsList();
                break;
            case R.id.cancel:
                addFriend.setVisibility(View.GONE);
                groupInfo.setVisibility(View.VISIBLE);
                //setMemberList();
                break;
            case R.id.apply:
                saveData();
                addFriend.setVisibility(View.GONE);
                groupInfo.setVisibility(View.VISIBLE);
                setMemberList();
                break;
        }
    }

    public void setPage() {
        if (FriendsActivity.transfer.isEmpty()) {
            newG = true;
            groupName.setText("Save");
            gName.setVisibility(View.GONE);
            g_Name.setVisibility(View.VISIBLE);
        } else {
            newG = false;
            gName.setText(FriendsActivity.transfer);
            loadGroupPicture();
            setMemberList();
        }
    }

    public void setFriendsList() {
        flag = true;
        friendCount = 0;
        int length = fDirectory.listFiles().length;
        images = new String[length];
        names = new String[length];
        chosen = new boolean[length];
        for (File file : fDirectory.listFiles()) {
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
                    chosen[friendCount] = false;
                    if (!newG) {
                        try {
                            File groupFile = new File(directory, FriendsActivity.transfer);
                            File textFile = new File(groupFile, "namelist.txt");
                            FileReader testReader = new FileReader(textFile);
                            BufferedReader bReader = new BufferedReader(testReader);
                            String tLine = bReader.readLine();
                            while (tLine != null) {
                                //Log.d("TAG", "setFriendsList: ||" + tLine + "||");
                                if (tLine.contains(jsonObject.get("uname").toString())) {
                                    chosen[friendCount] = true;
                                    break;
                                }
                                tLine = bReader.readLine();
                            }
                            bReader.close();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
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


        CustomAdapter customAdapter = new CustomAdapter();
        friendList.setAdapter(customAdapter);
    }

    public void setMemberList() {
        memberCount = 0;
        //friendCount = 0;
        flag = false;
        //if (!newG) {
            images = new String[fDirectory.listFiles().length];
            names = new String[fDirectory.listFiles().length];

            try {
                Log.d("TAG", "setMemberList: " + FriendsActivity.transfer);
                File groupFile = new File(directory, FriendsActivity.transfer);
                if (FriendsActivity.transfer.isEmpty()) {
                    groupFile = new File(directory, gName.getText().toString().trim());
                }
                File destination = new File(groupFile, "namelist.txt");
                FileReader fileReader = new FileReader(destination);
                BufferedReader bufferedReader = new BufferedReader(fileReader);
                //StringBuilder stringBuilder = new StringBuilder();
                String line = bufferedReader.readLine();
                while (line != null){
                    Log.d("Member name", "setMemberList: " + line);
                    //stringBuilder.append(line).append("\n");
                    File friendFile = new File(fDirectory, line);
                    File text = new File(friendFile, "friend_auth.json");
                    File image = new File(friendFile, "profile.jpg");

                    try {
                        FileReader secondReader = new FileReader(text);
                        BufferedReader secondBufferedReader = new BufferedReader(secondReader);
                        StringBuilder stringBuilder = new StringBuilder();
                        String secondLine = secondBufferedReader.readLine();
                        while (secondLine != null) {
                            stringBuilder.append(secondLine).append("\n");
                            secondLine = secondBufferedReader.readLine();
                        }
                        secondBufferedReader.close();
                        // This responce will have Json Format String
                        String responce = stringBuilder.toString();

                        JSONObject jsonObject = new JSONObject(responce);

                        names[memberCount] = jsonObject.get("uname").toString();
                        images[memberCount] = image.getPath();
                        memberCount++;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    line = bufferedReader.readLine();
                }
                    bufferedReader.close();

                CustomAdapter customAdapter = new CustomAdapter();
                memberList.setAdapter(customAdapter);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


        //}
    }

    public void saveData() {
        String name = gName.getText().toString().trim();

//        if (name.isEmpty()) {
//            groupName.setText("Save");
//            gName.setVisibility(View.GONE);
//            g_Name.setVisibility(View.VISIBLE);
//            g_Name.setText(gName.getText());
//            g_Name.setError("Group name can't be empty!");
//            g_Name.requestFocus();
//            return;
//        }

        try {
            File groupFile = new File(directory, name);

//            if (FriendsActivity.transfer.isEmpty() || (!FriendsActivity.transfer.isEmpty() && !FriendsActivity.transfer.equals(name))) {
//                if (groupFile.exists()) {
//                    groupName.setText("Save");
//                    gName.setVisibility(View.GONE);
//                    g_Name.setVisibility(View.VISIBLE);
//                    g_Name.setText(gName.getText());
//                    g_Name.setError("Group name already exist!");
//                    g_Name.requestFocus();
//                    return;
//                }
//            }

            //change file name
            if (!FriendsActivity.transfer.isEmpty() && !FriendsActivity.transfer.equals(name)) {
                File oldFile = new File(directory, FriendsActivity.transfer);
                if (oldFile.renameTo(groupFile)) {
                    FriendsActivity.transfer = gName.getText().toString().trim();
                }
            } else {
                //if file doesn't exist, create it
                groupFile.mkdir();
            }

            //save member names
            try {
                File textFile = new File(groupFile, "namelist.txt");
                textFile.delete();
                String fNames = "";
                int len = fDirectory.listFiles().length;
                for (int i = 0; i < len; i++) {
                    if (chosen[i]) {
                        fNames += names[i] + "\n";
                    }
                }

                FileWriter fw = new FileWriter(textFile);
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write(fNames);
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Toast.makeText(getApplicationContext(), "Group created!", Toast.LENGTH_SHORT);

        } catch (Exception e) {
            e.printStackTrace();
        }
        newG = false;
    }

    private void openGallery() {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, PICK_IMAGE);
    }

    private void loadGroupPicture() {
        File groupFile = new File(directory, FriendsActivity.transfer);
        File file = new File(groupFile, "GroupPicture.jpg");
        if (file.exists()) {
            img.setImageBitmap(BitmapFactory.decodeFile(file.getPath()));
        } else {
            img.setImageDrawable(getResources().getDrawable(R.drawable.jotaro));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == PICK_IMAGE){
            imageUri = data.getData();
            img.setImageURI(imageUri);
        }
    }

    class CustomAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            if (flag) {
                return friendCount;
            } else {
                return memberCount;
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
            if (flag) {
                CheckBox myCheckBox = view.findViewById(R.id.checkBox);
                myCheckBox.setVisibility(View.VISIBLE);
                myCheckBox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (myCheckBox.isChecked()) {
                            chosen[position] = true;
                            //Log.d("Test", "onItemClick: " + position);
                            //Log.d("Test", "onItemClick: " + chosen[position]);
                        } else {
                            chosen[position] = false;
                        }
                    }
                });
                if (chosen[position]) {
                    myCheckBox.setChecked(true);
                }
            }
            //set image for [position]
            Log.d("TAG", "getView: " + images[position]);
            File checkFile = new File(images[position]);
            if (checkFile.exists()) {
                myImageView.setImageBitmap(BitmapFactory.decodeFile(images[position]));
            } else {
                myImageView.setImageDrawable(getResources().getDrawable(R.drawable.jotaro));
            }

            //set name for [position]
            myTextView.setText(names[position]);
            return view;
        }
    }
}