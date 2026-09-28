package com.nyusamma.a16;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())
                && Settings.canDrawOverlays(context)) {
            Intent service = new Intent(context, NyuOverlayService.class);
            try {
                if (Build.VERSION.SDK_INT >= 26) context.startForegroundService(service);
                else context.startService(service);
            } catch (Exception ignored) {
                // Android may block microphone foreground-service starts from the background.
                // Opening the app once allows the user to restart it explicitly.
            }
        }
    }
}
