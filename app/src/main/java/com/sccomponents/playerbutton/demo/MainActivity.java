package com.sccomponents.playerbutton.demo;

import android.Manifest;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.SeekBar;

import com.sccomponents.playerbutton.ScPlayerButton;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Permission
        this.checkRunTimePermission();

        // Get the components
        ScPlayerButton player = (ScPlayerButton) this.findViewById(R.id.player);
        assert player != null;

        SeekBar seekBar = (SeekBar) this.findViewById(R.id.seekBar);
        assert seekBar != null;

        Uri defaultRintoneUri = RingtoneManager
                .getActualDefaultRingtoneUri(this.getApplicationContext(), RingtoneManager.TYPE_RINGTONE);
        player.setSource(defaultRintoneUri.toString());
        player.setSeekBar(seekBar);
    }

    private void checkRunTimePermission() {
        String[] permissionArrays = new String[]{Manifest.permission.RECORD_AUDIO};

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissionArrays, 0);
        }
    }
}
