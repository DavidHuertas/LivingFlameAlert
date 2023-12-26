package com.dosoft.livingflamealert;

import android.os.Bundle;
import android.widget.TextView;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements UiUpdateListener {

    TextView realmInfoTextView;
    View lockStatusView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set UiUpdateListener in the application class
        LFAlertApp myApplication = (LFAlertApp) getApplication();
        myApplication.setUiUpdateListener(this);

        realmInfoTextView = findViewById(R.id.realmInfoTextView);
        lockStatusView = findViewById(R.id.lockStatusView);
    }

    @Override
    public void updateUi(String realmInfo, int color) {
        runOnUiThread(() -> {
            realmInfoTextView.setText(realmInfo);
            lockStatusView.setBackgroundColor(getResources().getColor(color));

            // Set a message inside the lockStatusView based on color
            String statusMessage = getStatusMessage(color);
            ((TextView) lockStatusView).setText(statusMessage);
        });
    }

    private String getStatusMessage(int color) {
        if (color == android.R.color.holo_red_light) {
            return "Locked";
        } else {
            return "Unlocked: GO CREATE A PJ";
        }
    }
}
