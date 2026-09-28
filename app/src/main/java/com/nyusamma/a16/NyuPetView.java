package com.nyusamma.a16;

import android.content.Context;
import android.graphics.*;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import java.util.Random;

public class NyuPetView extends View {
    public enum State { IDLE, BALL, BONE, REST, LISTENING, SPEAKING }

    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Random random = new Random();
    private State state = State.IDLE;
    private float phase = 0f;

    public NyuPetView(Context context) {
        super(context);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        tick();
        scheduleIdleVariation();
    }

    public void setState(State state) {
        this.state = state;
        invalidate();
    }

    private void tick() {
        phase += 0.08f;
        invalidate();
        handler.postDelayed(this::tick, 33);
    }

    private void scheduleIdleVariation() {
        handler.postDelayed(() -> {
            if (state == State.IDLE || state == State.BALL ||
                    state == State.BONE || state == State.REST) {
                int n = random.nextInt(4);
                state = n == 0 ? State.BALL :
                        n == 1 ? State.BONE :
                        n == 2 ? State.REST : State.IDLE;
            }
            scheduleIdleVariation();
        }, 4500 + random.nextInt(3500));
    }

    @Override
    protected void onDraw(Canvas c) {
        super.onDraw(c);
        float w = getWidth(), h = getHeight();
        float bob = (float)Math.sin(phase) * 5f;
        float cx = w * 0.50f;
        float cy = h * 0.54f + bob;

        if (state == State.REST) cy += 18f;

        p.setStyle(Paint.Style.FILL);
        p.setShadowLayer(14, 0, 8, 0x55000000);

        // fluffy caramel body
        p.setColor(Color.rgb(196, 134, 75));
        c.drawOval(cx-66, cy-28, cx+63, cy+54, p);

        // head
        c.drawCircle(cx-30, cy-58, 52, p);

        // ears
        p.setColor(Color.rgb(160, 104, 55));
        c.drawOval(cx-77, cy-92, cx-42, cy-30, p);
        c.drawOval(cx-20, cy-91, cx+12, cy-33, p);

        // muzzle
        p.setColor(Color.rgb(231, 194, 148));
        c.drawOval(cx-58, cy-58, cx-7, cy-18, p);

        // eyes
        p.clearShadowLayer();
        p.setColor(Color.rgb(45, 33, 25));
        c.drawCircle(cx-50, cy-69, 5.5f, p);
        c.drawCircle(cx-19, cy-67, 5.5f, p);
        c.drawCircle(cx-31, cy-41, 5.5f, p);

        // paws
        p.setColor(Color.rgb(213, 154, 92));
        c.drawCircle(cx-39, cy+54, 18, p);
        c.drawCircle(cx+34, cy+54, 18, p);

        if (state == State.BALL) {
            p.setColor(Color.rgb(62, 181, 73));
            c.drawCircle(cx+78, cy+55, 22, p);
        } else if (state == State.BONE) {
            p.setColor(Color.WHITE);
            p.setShadowLayer(5,0,2,0x33000000);
            c.drawRoundRect(cx+50, cy+43, cx+100, cy+57, 8, 8, p);
            c.drawCircle(cx+51, cy+42, 10, p);
            c.drawCircle(cx+51, cy+58, 10, p);
            c.drawCircle(cx+99, cy+42, 10, p);
            c.drawCircle(cx+99, cy+58, 10, p);
            p.clearShadowLayer();
        } else if (state == State.LISTENING) {
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(5);
            p.setColor(Color.rgb(76, 175, 80));
            float pulse = 10 + (float)(Math.sin(phase*2) + 1) * 8;
            c.drawCircle(cx-31, cy-60, 74 + pulse, p);
            p.setStyle(Paint.Style.FILL);
        } else if (state == State.SPEAKING) {
            p.setColor(Color.rgb(33, 150, 243));
            for (int i=0;i<3;i++) {
                float r = 6 + i*6;
                c.drawCircle(cx+74+i*20, cy-72, r, p);
            }
        }

        p.setColor(0xCCFFFFFF);
        c.drawRoundRect(12, 8, w-12, 38, 12, 12, p);
        p.setColor(Color.DKGRAY);
        p.setTextAlign(Paint.Align.CENTER);
        p.setTextSize(17);
        c.drawText(label(), w/2, 29, p);
    }

    private String label() {
        switch (state) {
            case BALL: return "brincando com a bolinha";
            case BONE: return "roendo o ossinho";
            case REST: return "descansando";
            case LISTENING: return "ouvindo…";
            case SPEAKING: return "respondendo…";
            default: return "Nyu Samma";
        }
    }
}
