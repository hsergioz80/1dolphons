package com.example.btcpro.dolphons;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class createGroup extends AppCompatActivity
{
    private Button createGroup;
    private CheckBox privateGroup;
    //private ImageButton groupPicture;
    private EditText groupName;
    private EditText groupDesc;
    private FirebaseUser user;
    private Button chooseImage;
    private ImageView imageView;

    private Uri imageUri;
    private String name;
    private String desc;
    private boolean privateCheck;
    private StorageReference storageRef;
    private DatabaseReference databaseRef;

    private static final int PICK_IMAGE_REQUEST = 1;

    private FirebaseFirestore FireStore;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        FireStore = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        groupName = findViewById(R.id.groupNameTextEnter);
        groupDesc = findViewById(R.id.enterGroupDesc);
        privateGroup = findViewById(R.id.checkboxPrivate);
        createGroup = findViewById(R.id.buttonCreateGroup);
        chooseImage = findViewById(R.id.chooseImage);
        imageView = findViewById(R.id.imageView);

        storageRef = FirebaseStorage.getInstance().getReference("uploads");
        databaseRef = FirebaseDatabase.getInstance().getReference("uploads");

        chooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFileChooser();
            }
        });

        createGroup.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                //What to do after create button is pressed
                name = groupName.getText().toString();
                desc = groupDesc.getText().toString();

                //Checkbox not done yet
                privateCheck = privateGroup.isChecked();

                uploadFile();

            }
        });
    }
    private void openNextActivity(final String groupRefID){
        Intent intent = new Intent().setClass(createGroup.this, viewGroup.class);
        intent.putExtra("groupID", groupRefID);

        startActivity(intent);
    }

    private void addGroupToFireStore(final String name, String desc, boolean privateCheck, Uri imageUri) {
        Map<String, String> userMap = new HashMap<>();
        System.out.println(user.getUid());
        System.out.println(FireStore);
        userMap.put("groupName", name);
        userMap.put("groupDesc", desc);
        userMap.put("owner_uid", user.getUid());
        userMap.put("photoURL", imageUri.toString());


        FireStore.collection("groupss").add(userMap).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                System.out.println("before ID");
                System.out.println(documentReference.getId());
                addUserReference(documentReference.getId(), name);
                //Toast.makeText(createGroup.this, "Successful Group Creation", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                String error = e.getMessage();

                Toast.makeText(createGroup.this, "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
        //openNextActivity();
    }

    private void addUserReference(final String refID, String groupName) {

        Map<String, String> userMap = new HashMap<>();
        userMap.put("groupID", refID);
        userMap.put("groupName", groupName);


        FireStore
                .collection("users")
                .document(user.getUid())
                .collection("groupsApartOf")
                .add(userMap)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        addGroupUserReference(documentReference.getId(), refID);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //failll
            }
        });
    }

    private void addGroupUserReference(final String userRefID, final String groupRefID) {
        Map<String, String> userMap = new HashMap<>();
        userMap.put("userID", user.getUid());
        userMap.put("deleteID", userRefID);
        userMap.put("owner", /*boolean :( */ "true"); //I think we need this
        userMap.put("admin", /*boolean :( */ "true");
        userMap.put("user_name", user.getDisplayName());

        //we need to add name of user

        FireStore
                .collection("groupss")
                .document(groupRefID)
                .collection("users")
                .add(userMap)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        //it works!
                        System.out.println("OWNER INFO SET");
                        openNextActivity(groupRefID);
                        //addGroupEvent(userRefID, groupRefID);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                /* shake? */
            }
        });
    }

    private void openFileChooser()
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void uploadFile()
    {
        if (imageUri != null)
        {
            //StorageReference fileReference = storageRef.child(System.currentTimeMillis() + "." + getFileExtension(imageUri));
            final StorageReference fileReference = storageRef.child(imageUri.toString());

            fileReference.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()
                    {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                        {
                            //imageUri = Uri.parse(fileReference.getDownloadUrl().toString());
                            Toast.makeText(createGroup.this, "Group Successfully Created", Toast.LENGTH_LONG).show();
                            Upload upload = new Upload(taskSnapshot.getDownloadUrl().toString());
                            String uploadId = databaseRef.push().getKey();
                            imageUri = taskSnapshot.getDownloadUrl();
                            databaseRef.child(uploadId).setValue(upload);

                            addGroupToFireStore(name, desc, privateCheck, imageUri);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener()
                    {
                        @Override
                        public void onFailure(@NonNull Exception e)
                        {
                            //pop up notification needed
                            Toast.makeText(createGroup.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
        else
        {
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
        }
    }

    private String getFileExtension(Uri uri)
    {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null)
        {
            imageUri = data.getData();

            Picasso.with(this).load(imageUri).into(imageView);
            System.out.println("IMAGE URI: ");
            System.out.println(imageUri);
            //imageView.setImageURI(imageUri);
        }
    }

    /*private void addGroupEvent(String userRefID, final String groupRefID) {
        Map<String, String> userMap = new HashMap<>();
        userMap.put("groupID", groupRefID);
        userMap.put("title", " ");
        userMap.put("location", " ");
        userMap.put("summary", " "); //I think we need this
        userMap.put("date", " ");

        FireStore
                .collection("groupss")
                .document(groupRefID)
                .collection("events")
                .add(userMap)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        //it works!
                        openNextActivity(groupRefID);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                *//* shake? *//*
            }
        });
    }*/
}

/*public class createGroup extends AppCompatActivity {
    private Button createGroup;
    private CheckBox privateGroup;
    private ImageButton groupPicture;
    private TextView groupName;
    private TextView groupDescription;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        privateGroup = findViewById(R.id.checkboxPrivate);
        groupName = findViewById(R.id.textviewGroupName);
        groupDescription = findViewById(R.id.textviewGroupDescription);
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        /*mAuthListener = (FirebaseAuth){
                FirebaseUser user = FirebaseAuth.getCurrentUser();
                if(user != null){
                    Log.d(TAG, "onAuthStateChanged:signed_in" + user.getUid());
                    toastMessage
                }
        }
        createGroup = (Button)findViewById(R.id.buttonCreateGroup);
        createGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Write a message to the database
                /*FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference("groupName");
                myRef.setValue(groupName);
                DatabaseReference myRef2 = database.getReference("groupDescription");
                myRef2.setValue(groupDescription);
                //Go to next window.
                openNextActivity();
            }
        });
    }
    private void openNextActivity(){
        Intent intent = new Intent(this, welcome.class);
        startActivity(intent);
    }
         //Read from the database
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);
                Log.d(TAG, "Value is: " + value);
            }
            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    //configureNextButton();
    //nameText = (TextView)findViewById(R.id.name_text);
    //nameButton = (Button)findViewById(R.id.name_button);
        /*nameButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                MainActivity.this.nameText.setText(R.string.name_text);
            }
        });*/


    /*private void configureNextButton(){
        createGroup = (Button)findViewById(R.id.buttonCreateGroup);
        createGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(createGroup.this, welcome.class));
            }
        });
    }*/
//}


