package com.nyusamma.a16;
package com.nyusamma.a16;

import android.content.Context;
import android.graphics.*;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import java.util.Random;

public class NyuPetView extends View {
    public enum State { IDLE, BALL, BONE, REST, LISTENING, SPEAKING }

    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Random random = new Random();
    private final Bitmap portrait;
    private State state = State.IDLE;
    private float phase = 0f;

    public NyuPetView(Context context) {
        super(context);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        portrait = BitmapFactory.decodeResource(getResources(), R.drawable.nyu_portrait);
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
            if (state == State.IDLE || state == State.BALL
                    || state == State.BONE || state == State.REST) {
                int n = random.nextInt(4);
                state = n == 0 ? State.BALL
                        : n == 1 ? State.BONE
                        : n == 2 ? State.REST : State.IDLE;
            }
            scheduleIdleVariation();
        }, 4500 + random.nextInt(3500));
    }

    @Override
    protected void onDraw(Canvas c) {
        super.onDraw(c);

        float w = getWidth();
        float h = getHeight();
        float bob = (float) Math.sin(phase) * 2.5f;
        float photoTop = 8 + bob;
        float photoBottom = h - 43 + bob;
        RectF photoRect = new RectF(8, photoTop, w - 8, photoBottom);

        p.setStyle(Paint.Style.FILL);
        p.setColor(Color.WHITE);
        p.setShadowLayer(10, 0, 4, 0x55000000);
        c.drawRoundRect(photoRect, 18, 18, p);
        p.clearShadowLayer();

        if (portrait != null && !portrait.isRecycled()) {
            c.save();
            Path clip = new Path();
            clip.addRoundRect(photoRect, 18, 18, Path.Direction.CW);
            c.clipPath(clip);

            float targetAspect = photoRect.width() / photoRect.height();
            int sourceWidth = portrait.getWidth();
            int sourceHeight = portrait.getHeight();
            int cropWidth = Math.min(
                    sourceWidth, Math.round(sourceHeight * targetAspect));
            int left = Math.max(0, Math.min(
                    sourceWidth - cropWidth, Math.round(sourceWidth * 0.18f)));
            Rect source = new Rect(left, 0, left + cropWidth, sourceHeight);
            c.drawBitmap(portrait, source, photoRect, p);
            c.restore();
        }

        if (state == State.BALL) {
            p.setColor(Color.rgb(62, 181, 73));
            c.drawCircle(w - 24, photoBottom - 18, 12, p);
        } else if (state == State.BONE) {
            p.setColor(Color.WHITE);
            p.setShadowLayer(4, 0, 2, 0x66000000);
            c.drawRoundRect(w - 54, photoBottom - 23,
                    w - 22, photoBottom - 13, 6, 6, p);
            c.drawCircle(w - 53, photoBottom - 23, 6, p);
            c.drawCircle(w - 53, photoBottom - 13, 6, p);
            c.drawCircle(w - 23, photoBottom - 23, 6, p);
            c.drawCircle(w - 23, photoBottom - 13, 6, p);
            p.clearShadowLayer();
        } else if (state == State.LISTENING) {
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(4);
            p.setColor(Color.rgb(76, 175, 80));
            float pulse = 4 + (float) (Math.sin(phase * 2) + 1) * 4;
            c.drawRoundRect(photoRect.left - pulse, photoRect.top - pulse,
                    photoRect.right + pulse, photoRect.bottom + pulse,
                    20, 20, p);
            p.setStyle(Paint.Style.FILL);
        } else if (state == State.SPEAKING) {
            p.setColor(Color.rgb(33, 150, 243));
            for (int i = 0; i < 3; i++) {
                c.drawCircle(w - 22, photoTop + 24 + i * 14, 4, p);
            }
        }

        p.setColor(0xCCFFFFFF);
        c.drawRoundRect(12, h - 36, w - 12, h - 5, 12, 12, p);
        p.setColor(Color.DKGRAY);
        p.setTextAlign(Paint.Align.CENTER);
        p.setTextSize(13);
        c.drawText(label(), w / 2, h - 17, p);
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
