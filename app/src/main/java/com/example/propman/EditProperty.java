package com.example.propman;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class EditProperty extends AppCompatActivity implements View.OnClickListener{

    private DatabaseReference propertydatabase;
    private DatabaseReference uniqueproperty;
    private DatabaseReference propref;
    FirebaseStorage storage;
    StorageReference storageReference;
    Property viewproperty;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser user = mAuth.getCurrentUser();
    boolean flag = false;
    private Uri filePath;
    private String uid;
    private EditText inputtitle2;
    private EditText inputprice2;
    private EditText inputrooms2;
    private EditText inputarea2;
    private EditText inputaddress2;
    private EditText inputdescription2;
    private Button addimage2;
    private Button addproperty2;
    String puid;
    private final int PICK_IMAGE_REQUEST = 71;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_property);
        mAuth = FirebaseAuth.getInstance();
        propertydatabase = FirebaseDatabase.getInstance().getReference();
        uid=user.getUid();
        Intent intent = getIntent();

         puid = intent.getExtras().getString("puid");
        final FirebaseUser user = mAuth.getCurrentUser();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        inputtitle2 = (EditText) findViewById(R.id.propertytitle2);
        inputprice2 = (EditText) findViewById(R.id.price2);
        inputrooms2 = (EditText) findViewById(R.id.rooms2);
        inputaddress2 = (EditText) findViewById(R.id.address2);
        inputdescription2 = (EditText) findViewById(R.id.description2);
        inputarea2 = (EditText) findViewById(R.id.area2);

        findViewById(R.id.addproperty2).setOnClickListener(this);
        findViewById(R.id.addimage2).setOnClickListener(this);
        propref = propertydatabase.child("propertylist");
        propref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    inputtitle2.setText( dataSnapshot.child(uid).child(puid).child("title").getValue(String.class));
                    inputaddress2.setText(dataSnapshot.child(uid).child(puid).child("address").getValue(String.class));
                    inputarea2.setText(dataSnapshot.child(uid).child(puid).child("area").getValue(String.class));
                    inputdescription2.setText(dataSnapshot.child(uid).child(puid).child("description").getValue(String.class));
                    inputprice2.setText(dataSnapshot.child(uid).child(puid).child("price").getValue(String.class));
                    inputrooms2.setText(dataSnapshot.child(uid).child(puid).child("rooms").getValue(String.class));

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null )
        {
            filePath = data.getData();

        }
    }
    private void uploadImage(String uniq) {

        if(filePath != null)
        {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            StorageReference ref = storageReference.child("images/"+ uniq);
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(EditProperty.this, "Uploaded", Toast.LENGTH_SHORT).show();
                            ParcelableProperty parcelableProperty = new ParcelableProperty(viewproperty);
                            Intent mIntent = new Intent(EditProperty.this, ViewProperty.class);

                            mIntent.putExtra("property", parcelableProperty);
                            startActivity(mIntent);

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(EditProperty.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Uploaded "+(int)progress+"%");
                        }

                    });

        }
    }
    private Property returnproperty(){
        StorageReference storageRef = storage.getReference();
        String title=inputtitle2.getText().toString();
        String price=inputprice2.getText().toString();
        String rooms=inputrooms2.getText().toString();
        String address=inputaddress2.getText().toString();
        String description=inputdescription2.getText().toString();
        String area=inputarea2.getText().toString();
        uniqueproperty = propertydatabase.child("propertylist").child(user.getUid()).child(puid);
        uniqueproperty.child("title").setValue(title);
        uniqueproperty.child("price").setValue(price);
        uniqueproperty.child("rooms").setValue(rooms);
        uniqueproperty.child("address").setValue(address);
        uniqueproperty.child("description").setValue(description);
        uniqueproperty.child("area").setValue(area);
        uniqueproperty.child("uid").setValue(user.getUid());

        viewproperty=new Property(title,price,rooms,address,description,area,uid,filePath.toString(),puid);


        uploadImage(puid);

        return viewproperty;
    }
    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.addproperty2) {
            if(flag){
                try {
                    returnproperty();
                }catch (Exception e){
                    Toast.makeText(EditProperty.this, "Choose an image to add first.",
                            Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(EditProperty.this, "Select an image to add first.",
                        Toast.LENGTH_SHORT).show();
            }


        }


        else if (view.getId() == R.id.addimage2) {
            chooseImage();
            flag = true;

        }
    }
}
