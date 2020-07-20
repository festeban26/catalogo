package com.dulacs.festeban26;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.File;

/**
 * Naming conventions: https://jeroenmols.com/blog/2016/03/07/resourcenaming/
 */

/*
What should happen in this activity:
- It serves as a welcome screen
- Because it is very likely that on the first launch, just after downloading the app, the user
will have some sort of internet access, the app updates the information from a server.
If there is no internet connection, the app will still work but with outdated information
Things to do in the future:
- Add progress bar that is synchronized with the download status / any process that's is
happening.
 */

/**
 * Load products barcodes:
 * There is a file "Barcode.csv" in the raw resource folder.
 */


public class WelcomeActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set app to full screen and hides the title bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.getSupportActionBar().hide();
        // Needed to run once
        ResourcesManager.getInstance().setFilesDir(getFilesDir());

        setContentView(R.layout.activity_welcome);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!doesAppDataExist())
            setAppData();
        else
            initializeDataBaseToMemory();
    }

    private void initializeDataBaseToMemory() {
        Database.getInstance().initializeDatabase(new Database.OnInitializeDatabaseListener() {
            @Override
            public void databaseReady() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(WelcomeActivity.this, HomeActivity.class);

                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        WelcomeActivity.this.startActivity(intent);
                        WelcomeActivity.this.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        WelcomeActivity.this.finish();
                    }
                }, 500);
            }
        });
    }

    private void setAppData() {
        ResourcesManager.getInstance().updateApplicationData(
                new ResourcesManager.OnUpdateApplicationDataListener() {
                    @Override
                    public void onDataSuccessfullyUpdated(boolean successfullyCompleted) {
                        if (successfullyCompleted)
                            initializeDataBaseToMemory();
                        else
                            showFirstTimeRunError();
                    }
                });
    }

    private boolean doesAppDataExist() {
        File file = getFilesDir();
        File productsDataFile = new File(file, "products_data.json");
        return productsDataFile.exists();
    }

    private void showFirstTimeRunError() {
        Toast.makeText(this, "Error: verifique su conexión a internet y reinicie la aplicación.",
                Toast.LENGTH_LONG).show();
    }
}
