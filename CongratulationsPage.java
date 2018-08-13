package team5project.treasurehuntapp;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static team5project.treasurehuntapp.DataVault.animationSmoothnessMultiplier;

public class CongratulationsPage extends AppCompatActivity {

    //Constants for permissions
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int MY_REQUEST_CODE = 2;
    static final int INTERNET_REQUEST_CODE = 3;

    private static final int PROGRESS_BAR_ANIMATION_TIME = 2500;

    private static boolean selfieSaved = false;
    private static boolean selfieTaken = false;

    String photoPath;
    File photoFile = null;
    SharePhotoContent content;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MY_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                takePicture();
            }
        }
        if (requestCode == INTERNET_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                ShareDialog.show(CongratulationsPage.this, content);
            }
        }
    }

    private File storeImage() throws IOException {

        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        photoPath = image.getAbsolutePath();
        return image;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.congratulations_page);

        Button logoutButton = (Button) findViewById(R.id.logoutButton);

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DataVault.setUserInactive(DataVault.currentUser, DataVault.currentTeam);
                Intent intent = new Intent(CongratulationsPage.this, LoginScreen.class);
                startActivity(intent);
            }
        });

        //The following code creates an animation for the progress bar to go from 0 to max
        Resources res = getResources();
        Drawable progressBarDrawable = res.getDrawable(R.drawable.progress_circle);
        final ProgressBar congratulationsProgressBar = (ProgressBar) findViewById(R.id.congratulations_progress_bar);

        int completeProgress = Integer.parseInt(DataVault.locationCount) * animationSmoothnessMultiplier;

        congratulationsProgressBar.setProgressDrawable(progressBarDrawable);

        congratulationsProgressBar.setMax(completeProgress);

        congratulationsProgressBar.setSecondaryProgress(completeProgress);

        ObjectAnimator progressTransition = ObjectAnimator.ofInt(congratulationsProgressBar, "progress",
                0, completeProgress);
        progressTransition.setDuration(PROGRESS_BAR_ANIMATION_TIME);
        progressTransition.setInterpolator(new AccelerateDecelerateInterpolator());
        progressTransition.start();

        final TextView teamTextView = (TextView) findViewById(R.id.teamText);
        teamTextView.setText(DataVault.currentTeam);

        final ImageView selfieButton = (ImageView) findViewById(R.id.selfieButton);
        selfieButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                if (checkSelfPermission(Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {

                    requestPermissions(new String[]{Manifest.permission.CAMERA},
                            MY_REQUEST_CODE);
                } else {
                    takePicture();
                }
            }
        });

        ImageView facebookButton = (ImageView) findViewById(R.id.facebookButton);
        facebookButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {

                if(selfieTaken) {
                    File photoFile = null;

                    try {
                        photoFile = storeImage();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    SharePhoto photo = new SharePhoto.Builder()
                            .setBitmap(((BitmapDrawable) selfieButton.getDrawable()).getBitmap())
                            .build();
                    content = new SharePhotoContent.Builder()
                            .addPhoto(photo)
                            .build();

                    if (checkSelfPermission(Manifest.permission.INTERNET)
                            != PackageManager.PERMISSION_GRANTED) {

                        requestPermissions(new String[]{Manifest.permission.INTERNET},
                                INTERNET_REQUEST_CODE);
                    } else {
                        ShareDialog.show(CongratulationsPage.this, content);
                    }
                } else {

                    Toast.makeText(CongratulationsPage.this, "You need to take a selfie first", Toast.LENGTH_LONG);

                }

            }
        });

        ImageView saveButton = (ImageView) findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {

                if(!selfieSaved) {
                    try {
                        photoFile = storeImage();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    Uri photoURI = FileProvider.getUriForFile(selfieButton.getContext(),
                            "team5project.treasurehuntapp.fileprovider",
                            photoFile);

                    selfieSaved = true;

                    Toast.makeText(CongratulationsPage.this, "Selfie Saved!", Toast.LENGTH_LONG).show();

                }

            }
        });

    }

    private void takePicture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        ImageView selfieButton = (ImageView) findViewById(R.id.selfieButton);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            selfieTaken = true;
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            selfieButton.setImageBitmap(imageBitmap);
            selfieButton.setBackground(null);
            TextView takeSelfie = (TextView) findViewById(R.id.take_selfie_text_view);
            takeSelfie.setVisibility(View.GONE);
            Toast.makeText(CongratulationsPage.this, "Selfie Taken! Share with Friends below!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onBackPressed() {

    }

}
