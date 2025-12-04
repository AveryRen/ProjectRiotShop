package com.example.riotshop.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.riotshop.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SnowView extends View {

    private List<Snowflake> snowflakes;
    private Paint paint;
    private static final int NUM_SNOWFLAKES = 150;

    public SnowView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        snowflakes = new ArrayList<>();
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(ContextCompat.getColor(getContext(), R.color.riot_snow));
        paint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w != 0 && h != 0) {
            Random random = new Random();
            snowflakes.clear();
            for (int i = 0; i < NUM_SNOWFLAKES; i++) {
                snowflakes.add(new Snowflake(w, h, random));
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isInEditMode() || snowflakes.isEmpty()) {
            return;
        }

        for (Snowflake snowflake : snowflakes) {
            snowflake.update();
            canvas.drawCircle(snowflake.x, snowflake.y, snowflake.size, paint);
        }

        postInvalidateOnAnimation();
    }

    private static class Snowflake {
        float x;
        float y;
        float size;
        int speed;

        private int width;
        private int height;
        private Random random;

        Snowflake(int width, int height, Random random) {
            this.width = width;
            this.height = height;
            this.random = random;
            this.x = random.nextInt(width);
            this.y = random.nextInt(height);
            this.size = random.nextInt(4) + 2; // Range: 2 to 5
            this.speed = random.nextInt(4) + 2; // Range: 2 to 5
        }

        void update() {
            y += speed;
            if (y > height) {
                y = 0f;
                x = random.nextInt(width);
            }
        }
    }
}
