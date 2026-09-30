package com.nyusamma.a16;

import android.app.*;
import android.content.*;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.view.*;
import java.util.Locale;

public class NyuOverlayService extends Service implements SpeechController.Listener {
    private static final String CHANNEL_ID = "nyu_presence";
    private WindowManager windowManager;
    private NyuPetView petView;
    private WindowManager.LayoutParams params;
    private SpeechController speechController;
    private TextToSpeech tts;

    @Override
    public void onCreate() {
        super.onCreate();
        createChannel();
        startForeground(1001, buildNotification());

        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(new Locale("pt", "BR"));
                tts.setSpeechRate(0.95f);
            }
        });

        if (Settings.canDrawOverlays(this)) {
            showOverlay();
        }

        speechController = new SpeechController(this, this);
        speechController.start();
    }

    private void showOverlay() {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        petView = new NyuPetView(this);

        int type = Build.VERSION.SDK_INT >= 26
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;

        params = new WindowManager.LayoutParams(
                dp(220), dp(220), type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 24;
        params.y = 240;

        petView.setOnTouchListener(new View.OnTouchListener() {
            int startX, startY;
            float downX, downY;

            @Override
            public boolean onTouch(View v, android.view.MotionEvent event) {
                switch (event.getAction()) {
                    case android.view.MotionEvent.ACTION_DOWN:
                        startX = params.x;
                        startY = params.y;
                        downX = event.getRawX();
                        downY = event.getRawY();
                        petView.setState(NyuPetView.State.LISTENING);
                        return true;
                    case android.view.MotionEvent.ACTION_MOVE:
                        params.x = startX + (int)(event.getRawX() - downX);
                        params.y = startY + (int)(event.getRawY() - downY);
                        if (windowManager != null) windowManager.updateViewLayout(petView, params);

                        return true;
                    case android.view.MotionEvent.ACTION_UP:
                        petView.setState(NyuPetView.State.IDLE);
                        return true;
                }
                return false;
            }
        });

        windowManager.addView(petView, params);
    }

    @Override
    public void onWakeWordDetected(String heardText) {
        if (petView != null) petView.setState(NyuPetView.State.LISTENING);
        speak("Oi, Fofolete. Tô aqui.");
    }

    @Override
    public void onSpeechActivity(boolean active) {
        if (petView != null && active) petView.setState(NyuPetView.State.LISTENING);
    }

    @Override
    public void onSpeechError() {
        if (petView != null) petView.setState(NyuPetView.State.IDLE);
    }

    private void speak(String text) {
        if (tts == null) return;
        if (petView != null) petView.setState(NyuPetView.State.SPEAKING);
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "nyu_reply");
        new android.os.Handler(getMainLooper()).postDelayed(() -> {
            if (petView != null) petView.setState(NyuPetView.State.IDLE);
        }, 2200);
    }

    private Notification buildNotification() {
        Intent open = new Intent(this, MainActivity.class);
        PendingIntent pending = PendingIntent.getActivity(
                this, 1, open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        return new Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("Nyu Samma está ativa")
                .setContentText("Presença flutuante e escuta do chamado “Nyu”.")
                .setSmallIcon(android.R.drawable.ic_btn_speak_now)
                .setContentIntent(pending)
                .setOngoing(true)
                .build();
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Nyu Samma", NotificationManager.IMPORTANCE_LOW);
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
    }

    @Override
    public void onDestroy() {
        if (speechController != null) speechController.destroy();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        if (petView != null && windowManager != null) {
            try { windowManager.removeView(petView); } catch (Exception ignored) {}
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}