package com.example.raihenv2;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class ShareEvent extends AppCompatActivity {
    String currentDate;
    int currentEventNumber;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    public String currentUserUID = mAuth.getCurrentUser().getUid();
    ImageView image;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_event);
        Intent intent = getIntent();
        currentDate = intent.getStringExtra("date");
        currentEventNumber = intent.getIntExtra("eventNumber", 0);

        image = (ImageView) findViewById(R.id.qrEvent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseDatabase.getInstance().getReference("Events/" + currentUserUID +"/" + currentDate + "/Event-" +currentEventNumber).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                MultiFormatWriter mfw = new MultiFormatWriter();
                try{

                    Log.d("output task" , task.getResult().getValue().toString());

                    BitMatrix bm = mfw.encode("|"+currentDate+"|"+task.getResult().getValue().toString(), BarcodeFormat.QR_CODE, 500, 500);
                    BarcodeEncoder be = new BarcodeEncoder();
                    Bitmap bitmap = be.createBitmap(bm);
                    image.setImageBitmap(bitmap);
                }catch (Exception e){
                    Log.d("barcode", "qr code failed"+ e.toString());
                }

            }
        });
        Button shareqr = (Button)findViewById(R.id.shareqr);
        shareqr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShareEvent.super.onBackPressed();
                finish();
            }
        });
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}