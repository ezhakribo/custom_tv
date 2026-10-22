package com.ibo.pixelhome;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

/**
 * The walking character with two pet cats.
 * He walks back and forth along the bottom; at each edge he stops, faces the viewer with a smile,
 * then turns around. The cats trot after him and catch up when he stops.
 */
final class Walker {
    static final int HERO_W = 32, HERO_H = 56, CAT_W = 20, CAT_H = 11;

    final Bitmap[] hero = new Bitmap[4], catA = new Bitmap[4], catB = new Bitmap[4];
    final Bitmap heroFront;
    final Paint p = new Paint(), shadow = new Paint();

    float x = -1;        // hero's left edge, screen pixels
    int dir = 1, step, pause;
    final Cat[] cats = {new Cat(26), new Cat(48)};

    /** A cat that follows the hero at a fixed gap (in sprite pixels). */
    static final class Cat {
        final int gap;
        float x = -1;
        int face = 1, step;
        boolean moving;
        Cat(int gap) { this.gap = gap; }
    }

    Walker(Resources r) {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inScaled = false;
        int[] h = {R.drawable.hero_walk0, R.drawable.hero_walk1, R.drawable.hero_walk2, R.drawable.hero_walk3};
        int[] a = {R.drawable.cat_a0, R.drawable.cat_a1, R.drawable.cat_a2, R.drawable.cat_a3};
        int[] b = {R.drawable.cat_b0, R.drawable.cat_b1, R.drawable.cat_b2, R.drawable.cat_b3};
        for (int i = 0; i < 4; i++) {
            hero[i] = BitmapFactory.decodeResource(r, h[i], o);
            catA[i] = BitmapFactory.decodeResource(r, a[i], o);
            catB[i] = BitmapFactory.decodeResource(r, b[i], o);
        }
        heroFront = BitmapFactory.decodeResource(r, R.drawable.hero_front, o);
        p.setFilterBitmap(false);
        p.setAntiAlias(false);
        shadow.setColor(0x55000000);
    }

    /** Whole-number zoom for crisp pixels: about the same height as the old character. */
    static int zoom(float px) {
        return Math.max(1, Math.round(px / 2f));
    }

    /** Called 8 times a second. */
    void update(int viewW, float px) {
        int k = zoom(px);
        float heroW = HERO_W * k, margin = 4 * k, speed = 3 * k;
        if (x < 0) {
            x = viewW * 0.2f;
            for (Cat c : cats) c.x = x - c.gap * k;
        }
        if (pause > 0) {
            if (--pause == 0) dir = -dir;                  // done smiling: turn around
        } else {
            step = (step + 1) % 4;
            x += dir * speed;
            if (x > viewW - heroW - margin) { x = viewW - heroW - margin; pause = 12; }
            if (x < margin) { x = margin; pause = 12; }
        }
        for (Cat c : cats) {
            float target = dir > 0 ? x - c.gap * k : x + heroW + (c.gap - CAT_W) * k;
            float d = target - c.x, max = speed * 1.3f;
            c.moving = Math.abs(d) > k;
            if (c.moving) {
                c.x += Math.max(-max, Math.min(max, d));
                c.face = d > 0 ? 1 : -1;
                c.step = (c.step + 1) % 4;
            } else {
                c.face = dir;
            }
        }
    }

    void draw(Canvas c, float px, float feetY) {
        int k = zoom(px);
        Bitmap[][] catFrames = {catA, catB};
        for (int i = 0; i < cats.length; i++) {
            Cat cat = cats[i];
            Bitmap b = catFrames[i][cat.moving ? cat.step : 1];
            drawSprite(c, b, cat.x, feetY, k, cat.face < 0);
        }
        Bitmap h = pause > 0 ? heroFront : hero[step];
        drawSprite(c, h, x, feetY, k, pause == 0 && dir < 0);
    }

    void drawSprite(Canvas c, Bitmap b, float left, float feetY, int k, boolean mirror) {
        float w = b.getWidth() * k, h = b.getHeight() * k;
        c.drawRect(left + w * 0.2f, feetY - k, left + w * 0.8f, feetY + k, shadow);
        RectF dst = new RectF(left, feetY - h, left + w, feetY);
        if (mirror) {
            c.save();
            c.scale(-1, 1, left + w / 2f, 0);
            c.drawBitmap(b, null, dst, p);
            c.restore();
        } else {
            c.drawBitmap(b, null, dst, p);
        }
    }
}
