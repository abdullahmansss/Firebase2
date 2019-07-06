package softagi.firebase2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import de.hdodenhof.circleimageview.CircleImageView;
import softagi.firebase2.Models.UserModel;

public class MainActivity extends AppCompatActivity
{
    CircleImageView profile_picture;
    EditText email_field,password_field,confirmpassword_field,username_field,mobile_field,address_Field;
    String email,password,confirmpassword,username,mobile,address;

    FirebaseAuth auth;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    FirebaseStorage firebaseStorage;
    StorageReference storageReference;

    ProgressDialog progressDialog;

    Uri photoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        email_field = findViewById(R.id.email_field);
        password_field = findViewById(R.id.password_field);
        confirmpassword_field = findViewById(R.id.confirmpassword_field);
        username_field  =findViewById(R.id.username_field);
        mobile_field  =findViewById(R.id.mobile_field);
        address_Field  =findViewById(R.id.address_field);
        profile_picture  =findViewById(R.id.profile_picture);

        auth = FirebaseAuth.getInstance();

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();

        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        profile_picture.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON_TOUCH)
                        .setAspectRatio(1,1)
                        .start(MainActivity.this);
            }
        });
    }

    public void signIn(View view)
    {
        startActivity(new Intent(getApplicationContext(), SigninActivity.class));
    }

    public void rergister(View view)
    {
        email = email_field.getText().toString();
        password = password_field.getText().toString();
        confirmpassword = confirmpassword_field.getText().toString();
        username = username_field.getText().toString();
        mobile = mobile_field.getText().toString();
        address = address_Field.getText().toString();

        if (TextUtils.isEmpty(email))
        {
            Toast.makeText(getApplicationContext(), "enter your email", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6)
        {
            Toast.makeText(getApplicationContext(), "password is too short", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!confirmpassword.equals(password))
        {
            Toast.makeText(getApplicationContext(), "password isn't match", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(username))
        {
            Toast.makeText(getApplicationContext(), "enter your name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(mobile))
        {
            Toast.makeText(getApplicationContext(), "enter your mobile", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(address))
        {
            Toast.makeText(getApplicationContext(), "enter your address", Toast.LENGTH_SHORT).show();
            return;
        }

        if (photoPath == null)
        {
            Toast.makeText(getApplicationContext(), "select a photo", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("Please Wait Until Register ...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.show();
        progressDialog.setCancelable(false);

        createUser(email,password,username,mobile,address);
    }

    private void createUser(final String email, String password, final String username, final String mobile, final String address)
    {
        auth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>()
                {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if (task.isSuccessful())
                        {
                            String id = task.getResult().getUser().getUid();
                            uploadPic(email,username,mobile,address,id);
                        } else
                            {
                                Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }
                    }
                });
    }

    private void uploadPic(final String email, final String username, final String mobile, final String address, final String id)
    {
        UploadTask uploadTask;

        final StorageReference ref = storageReference.child("images/" + photoPath.getLastPathSegment());

        uploadTask = ref.putFile(photoPath);

        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>()
        {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task)
            {
                if (!task.isSuccessful())
                {
                    Toast.makeText(MainActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }

                // Continue with the task to get the download URL
                return ref.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>()
        {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onComplete(@NonNull Task<Uri> task)
            {
                Uri downloadUri = task.getResult();

                String selectedimageurl = downloadUri.toString();

                saveUser(email,username,mobile,address,id,selectedimageurl);
            }
        }).addOnFailureListener(new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception exception)
            {
                // Handle unsuccessful uploads
                Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        });
    }

    private void saveUser(String email, String username, String mobile, String address, String id, String image)
    {
        UserModel userModel = new UserModel(email,username,mobile,address,image);

        databaseReference.child("Users").child(id).setValue(userModel);

        startActivity(new Intent(getApplicationContext(), StartActivity.class));
        progressDialog.dismiss();
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null)
        {
            startActivity(new Intent(getApplicationContext(), StartActivity.class));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == Activity.RESULT_OK)
            {
                if (result != null)
                {
                    photoPath = result.getUri();

                    Picasso.get()
                            .load(photoPath)
                            .placeholder(R.drawable.me)
                            .error(R.drawable.me)
                            .into(profile_picture);
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE)
            {
                Exception error = result.getError();
            }
        }
    }

    @Override
    public void onBackPressed()
    {
        finishAffinity();
    }
}
