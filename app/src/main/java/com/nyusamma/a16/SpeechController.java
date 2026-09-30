package com.nyusamma.a16;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Locale;

public class SpeechController implements RecognitionListener {
    public interface Listener {
        void onWakeWordDetected(String heardText);
        void onSpeechActivity(boolean active);
        void onSpeechError();
    }

    private final Context context;
    private final Listener listener;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private SpeechRecognizer recognizer;
    private Intent recognizerIntent;
    private boolean destroyed = false;
    private boolean paused = false;
    private long lastWakeAt = 0L;

    public SpeechController(Context context, Listener listener) {
        this.context = context.getApplicationContext();
        this.listener = listener;
    }

    public void start() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            listener.onSpeechError();
            return;
        }
        handler.post(() -> {
            if (destroyed) return;
            recognizer = SpeechRecognizer.createSpeechRecognizer(context);
            recognizer.setRecognitionListener(this);
            recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pt-BR");
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
            restartListening(500);
        });
    }

    private void restartListening(long delayMs) {
        handler.postDelayed(() -> {
            if (destroyed || paused || recognizer == null) return;
            try {
                recognizer.startListening(recognizerIntent);
            } catch (Exception e) {
                restartListening(1500);
            }
        }, delayMs);
    }

    private void inspect(Bundle results) {
        if (destroyed || paused || results == null) return;
        ArrayList<String> matches =
                results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if (matches == null) return;

        for (String raw : matches) {
            String normalized = normalize(raw);
            if (containsWakeWord(normalized)) {
                long now = System.currentTimeMillis();
                if (now - lastWakeAt < 5000L) return;
                lastWakeAt = now;
                paused = true;
listener.onWakeWordDetected(raw);
return;
            }
        }
    }

    private boolean containsWakeWord(String text) {
        return text.matches(".*\\b(nyu|niu|new|nью)\\b.*");
    }

    private String normalize(String text) {
        String n = Normalizer.normalize(text.toLowerCase(Locale.ROOT), Normalizer.Form.NFD);
        return n.replaceAll("\\p{M}", "").trim();
    }

    public void resumeAfterReply(long delayMs) {
        handler.postDelayed(() -> {
            if (destroyed) return;
            paused = false;
            restartListening(250);
        }, delayMs);
    }

    @Override public void onReadyForSpeech(Bundle params) { listener.onSpeechActivity(true); }
    @Override public void onBeginningOfSpeech() { listener.onSpeechActivity(true); }
    @Override public void onRmsChanged(float rmsdB) {}
    @Override public void onBufferReceived(byte[] buffer) {}
    @Override public void onEndOfSpeech() { listener.onSpeechActivity(false); }

    @Override public void onError(int error) {
        listener.onSpeechError();
        restartListening(error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY ? 1800 : 800);
    }

    @Override public void onResults(Bundle results) {
        inspect(results);
        restartListening(400);
    }

    @Override public void onPartialResults(Bundle partialResults) {
    }

    @Override public void onEvent(int eventType, Bundle params) {}

    public void destroy() {
        destroyed = true;
        handler.removeCallbacksAndMessages(null);
        if (recognizer != null) {
            try { recognizer.cancel(); } catch (Exception ignored) {}
            recognizer.destroy();
            recognizer = null;
        }
    }
}