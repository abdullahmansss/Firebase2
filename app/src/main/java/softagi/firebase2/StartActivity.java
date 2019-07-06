package softagi.firebase2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import softagi.firebase2.Models.UserModel;

public class StartActivity extends AppCompatActivity
{
    TextView email_txt,username_txt,mobile_txt,address_txt;
    CircleImageView circleImageView;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        email_txt = findViewById(R.id.email_txt);
        username_txt = findViewById(R.id.username_txt);
        mobile_txt = findViewById(R.id.mobile_txt);
        address_txt = findViewById(R.id.address_txt);
        circleImageView = findViewById(R.id.image);

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();

        String id = FirebaseAuth.getInstance().getCurrentUser().getUid();

        databaseReference.child("Users").child(id).addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                UserModel userModel = dataSnapshot.getValue(UserModel.class);

                if (userModel != null)
                {
                    email_txt.setText(userModel.getEmail());
                    username_txt.setText(userModel.getUsername());
                    mobile_txt.setText(userModel.getMobile());
                    address_txt.setText(userModel.getAddress());

                    Picasso.get()
                            .load(userModel.getImageurl())
                            .into(circleImageView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {
                Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void signout(View view)
    {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
    }

    @Override
    public void onBackPressed()
    {
        finishAffinity();
    }
}
