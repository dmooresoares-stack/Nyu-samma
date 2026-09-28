package com.nyusamma.a16;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    private static final int REQ_MIC = 10;
    private TextView status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(24), dp(32), dp(24), dp(24));
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setBackgroundColor(Color.WHITE);

        TextView title = new TextView(this);
        title.setText("Nyu Samma • A16");
        title.setTextSize(28);
        title.setTextColor(Color.rgb(55,55,55));
        root.addView(title, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView info = new TextView(this);
        info.setText(
                "\nCheckpoint v0.4\n\n" +
                "1. Autorize microfone.\n" +
                "2. Autorize aparecer sobre outros apps.\n" +
                "3. Toque em iniciar Nyu Samma.\n\n" +
                "A Nyu fica em uma bolha flutuante e tenta ouvir o chamado “Nyu”."
        );
        info.setTextSize(17);
        info.setTextColor(Color.DKGRAY);
        root.addView(info);

        status = new TextView(this);
        status.setTextSize(15);
        status.setPadding(0, dp(18), 0, dp(18));
        root.addView(status);

        Button permissions = new Button(this);
        permissions.setText("Autorizar permissões");
        permissions.setOnClickListener(v -> requestNeededPermissions());
        root.addView(permissions);

        Button start = new Button(this);
        start.setText("Iniciar Nyu Samma");
        start.setOnClickListener(v -> startNyu());
        root.addView(start);

        Button stop = new Button(this);
        stop.setText("Parar Nyu Samma");
        stop.setOnClickListener(v -> {
            stopService(new Intent(this, NyuOverlayService.class));
            refreshStatus();
        });
        root.addView(stop);

        setContentView(root);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshStatus();
    }

    private void requestNeededPermissions() {
        if (Build.VERSION.SDK_INT >= 23 &&
                checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, REQ_MIC);
        }

        if (Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName())
            );
            startActivity(intent);
        }

        if (Build.VERSION.SDK_INT >= 33 &&
                checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 11);
        }
    }

    private void startNyu() {
        requestNeededPermissions();
        if (!Settings.canDrawOverlays(this) ||
                checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            refreshStatus();
            return;
        }
        Intent intent = new Intent(this, NyuOverlayService.class);
        if (Build.VERSION.SDK_INT >= 26) startForegroundService(intent);
        else startService(intent);
        refreshStatus();
    }

    private void refreshStatus() {
        boolean overlay = Build.VERSION.SDK_INT < 23 || Settings.canDrawOverlays(this);
        boolean mic = Build.VERSION.SDK_INT < 23 ||
                checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
        status.setText("Overlay: " + (overlay ? "OK" : "pendente") +
                "   •   Microfone: " + (mic ? "OK" : "pendente"));
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
