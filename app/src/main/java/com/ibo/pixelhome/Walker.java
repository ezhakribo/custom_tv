package com.ibo.pixelhome;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

/**
 * IBO's family walking along the bottom of the screen: dad in front, then mum, the little one
 * and the two cats. Everyone follows the exact path dad walked, a few steps behind, so at each
 * edge the family stops, smiles at the camera, and turns around one after the other.
 */
final class Walker {
    static final int SPEED = 3;          // sprite pixels per step (8 steps a second)
    static final int PAUSE = 14;         // steps dad stands smiling at an edge
    static final int HISTORY = 64;

    /** One family member (or cat). */
    static final class Member {
        final Bitmap[] walk;
        final Bitmap front;              // null for the cats
        final int delay;                 // steps behind dad
        float x = Float.NaN;             // left edge, screen pixels
        int face = 1, step;
        boolean moving;
        Member(Bitmap[] walk, Bitmap front, int delay) { this.walk = walk; this.front = front; this.delay = delay; }
    }

    final Member[] family;
    final Paint p = new Paint(), shadow = new Paint();
    final float[] hist = new float[HISTORY];
    int histCount, dir = 1, pause;

    Walker(Resources r) {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inScaled = false;
        family = new Member[]{
                new Member(load(r, o, R.drawable.dad_walk0, R.drawable.dad_walk1, R.drawable.dad_walk2, R.drawable.dad_walk3),
                        BitmapFactory.decodeResource(r, R.drawable.dad_front, o), 0),
                new Member(load(r, o, R.drawable.mom_walk0, R.drawable.mom_walk1, R.drawable.mom_walk2, R.drawable.mom_walk3),
                        BitmapFactory.decodeResource(r, R.drawable.mom_front, o), 11),
                new Member(load(r, o, R.drawable.kid_walk0, R.drawable.kid_walk1, R.drawable.kid_walk2, R.drawable.kid_walk3),
                        BitmapFactory.decodeResource(r, R.drawable.kid_front, o), 20),
                new Member(load(r, o, R.drawable.cat_a0, R.drawable.cat_a1, R.drawable.cat_a2, R.drawable.cat_a3), null, 28),
                new Member(load(r, o, R.drawable.cat_b0, R.drawable.cat_b1, R.drawable.cat_b2, R.drawable.cat_b3), null, 34)};
        p.setFilterBitmap(false);
        p.setAntiAlias(false);
        shadow.setColor(0x55000000);
    }

    static Bitmap[] load(Resources r, BitmapFactory.Options o, int... ids) {
        Bitmap[] b = new Bitmap[ids.length];
        for (int i = 0; i < ids.length; i++) b[i] = BitmapFactory.decodeResource(r, ids[i], o);
        return b;
    }

    /** Whole-number zoom so pixels stay crisp (3x on a 1080p TV). */
    static int zoom(float px) {
        return Math.max(1, Math.round(px / 2f));
    }

    void push(float x) {
        hist[histCount % HISTORY] = x;
        histCount++;
    }

    /** Dad's position {@code back} steps ago. */
    float past(int back) {
        int i = Math.max(0, histCount - 1 - back);
        return hist[i % HISTORY];
    }

    /** Called 8 times a second. */
    void update(int viewW, float px) {
        int k = zoom(px);
        Member dad = family[0];
        float speed = SPEED * k, dadW = dad.walk[0].getWidth() * k, margin = 4 * k;
        if (Float.isNaN(dad.x)) {                        // start mid-screen, family already lined up behind
            dad.x = viewW * 0.35f;
            for (int i = HISTORY - 1; i >= 0; i--) push(dad.x - i * speed);
        }
        if (pause > 0) {
            if (--pause == 0) dir = -dir;                // done smiling: turn around
            dad.moving = false;
        } else {
            dad.step = (dad.step + 1) % 4;
            dad.x += dir * speed;
            dad.face = dir;
            dad.moving = true;
            if (dad.x > viewW - dadW - margin) { dad.x = viewW - dadW - margin; pause = PAUSE; }
            if (dad.x < margin) { dad.x = margin; pause = PAUSE; }
            push(dad.x);
        }
        for (int i = 1; i < family.length; i++) {
            Member m = family[i];
            float target = past(m.delay);
            float d = Float.isNaN(m.x) ? 0 : target - m.x;
            m.moving = Math.abs(d) > 0.5f;
            if (m.moving) {
                m.face = d > 0 ? 1 : -1;
                m.step = (m.step + 1) % 4;
            }
            m.x = target;
        }
    }

    void draw(Canvas c, float px, float feetY) {
        int k = zoom(px);
        Member dad = family[0];
        float dadW = dad.walk[0].getWidth() * k;
        for (int i = family.length - 1; i >= 0; i--) {   // back of the line first, dad on top
            Member m = family[i];
            if (Float.isNaN(m.x)) continue;
            Bitmap b = m.moving ? m.walk[m.step] : (m.front != null ? m.front : m.walk[1]);
            float w = b.getWidth() * k;
            float left = m.x + (dadW - w) / 2f;          // everyone walks on dad's centre line
            boolean mirror = (m.moving || m.front == null) && m.face < 0;
            drawSprite(c, b, left, feetY, k, mirror);
        }
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
