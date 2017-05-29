package com.dda15.www.progmobgo_ronaldo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.plus.PlusShare;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private Button btnLogout;
    private Button btnRandom;
    private TextView txtNama;
    private TextView txtAngka;
    private TextView txtHasil;
    private ImageView imgFoto;
    private GoogleSignInOptions gso;
    private GoogleApiClient mGoogleApiClient;

    private ProgressBar progressBar1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }

        btnLogout = (Button) findViewById(R.id.btnLogout);
        btnRandom = (Button) findViewById(R.id.btnRandom);
        txtNama = (TextView) findViewById(R.id.txtNama);
        txtAngka = (TextView) findViewById(R.id.txtAngka);
        txtHasil = (TextView) findViewById(R.id.txtHasil);
        imgFoto = (ImageView) findViewById(R.id.imgFoto);
        progressBar1 = (ProgressBar) findViewById(R.id.progressBar1);

        txtAngka.setVisibility(View.GONE);
        txtHasil.setVisibility(View.GONE);
        progressBar1.setVisibility(View.GONE);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        mGoogleApiClient.connect();
        super.onStart();

        Intent i = getIntent();
        txtNama.setText(i.getStringExtra("p_name"));
        final String fotoUrl = i.getStringExtra("p_profilepicture");

        new Thread((new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(fotoUrl);
                    InputStream is = url.openConnection().getInputStream();
                    final Bitmap bmp = BitmapFactory.decodeStream(is);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            imgFoto.setImageBitmap(bmp);
                        }
                    });
                }catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        })).start();

        btnRandom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtAngka.setVisibility(View.GONE);
                txtHasil.setVisibility(View.GONE);
                progressBar1.setVisibility(View.VISIBLE);
                randomAndShare();
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });
    }

    private void randomAndShare() {
        Random rand;
        rand = new Random();
        int res = -1;

        res = rand.nextInt(101);
        txtAngka.setText(Integer.toString(res));

        String message;
        String photoType;
        if(res>=0 && res<=33){
            message = "Sepertinya hari ini saya sedang sial";
            photoType = "A";
        }
        else if(res>=34 && res<=66){
            message = "Hari ini nampaknya biasa saja";
            photoType = "B";
        }
        else if(res>=67 && res<=100){
            message = "Saya merasa beruntung hari ini";
            photoType = "C";
        }
        else{
            message = "Terjadi error";
            photoType = "X";
        }
        txtHasil.setText(message);

        int imageId = 0;

        switch(photoType){
            case "A":   imageId = R.drawable.a;
                        break;
            case "B":   imageId = R.drawable.b;
                        break;
            case "C":
            default :   imageId = R.drawable.c;
                        break;
        }

        try {
            Uri imageUri = null;
            try {
                imageUri = Uri.parse(MediaStore.Images.Media.insertImage(this.getContentResolver(),
                        BitmapFactory.decodeResource(getResources(), imageId), null, null));
            } catch (NullPointerException e) {
            } catch (Exception e){
                Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
            }


            PlusShare.Builder shareIntent = new PlusShare.Builder(this)
                    .setType("image/*")
                    .setText(message + "\n" + "http://ukdw.ac.id")
                    .addStream(imageUri)
                    .addCallToAction("VISIT",Uri.parse("http://ukdw.ac.id"),"")
                    .setContentUrl(Uri.parse("http://ukdw.ac.id"));
            startActivity(shareIntent.getIntent());

            progressBar1.setVisibility(View.GONE);
            txtAngka.setVisibility(View.VISIBLE);
            txtHasil.setVisibility(View.VISIBLE);
        }catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(getApplicationContext(),"Gplus is not installed, sharing cancelled", Toast.LENGTH_LONG).show();
            progressBar1.setVisibility(View.GONE);
        }catch (Exception e){
            Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
            progressBar1.setVisibility(View.GONE);
        }

    }

    private void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        Toast.makeText(getApplicationContext(),"Anda telah logout", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });

    }
}
