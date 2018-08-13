package team5project.treasurehuntapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import java.io.IOException;
import java.net.URLEncoder;


/**
 * Created by joebr on 21/04/2017.
 */

public class QRCodeDisplay extends AppCompatActivity{

    static String selectedQRCode;
    static int index;
    static String treasureHuntTitle;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.qr_code_display);

        treasureHuntTitle = getIntent().getStringExtra("Title");

        //button to go back to the hunt management page
        Button backButton = (Button) findViewById(R.id.managementButton);
        backButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                startActivity(new Intent(QRCodeDisplay.this, HuntManagement.class));
            }
        });


        //after selecting an item, will take user to a web browser showing the qr code to them
        Button viewOnlineButton = (Button) findViewById(R.id.viewOnlineButton);
        viewOnlineButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                selectedQRCode = QRFragment.selectedQRCode;
                index = QRFragment.index;
                try{
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://chart.googleapis.com/chart?cht=qr&chl=" + URLEncoder.encode(treasureHuntTitle, "UTF-8") + "%7C" + (index + 1) + "&chs=180x180&choe=UTF-8&chld=L|2"));
                    startActivity(browserIntent);
                }
                catch(IOException e){

                }

            }
        });

        Button logoutButton = (Button) findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DataVault.setUserInactive(DataVault.currentUser, DataVault.currentTeam);
                startActivity(new Intent(QRCodeDisplay.this, LoginScreen.class));
            }
        });

    }

    @Override
    public void onBackPressed() {

    }

}
