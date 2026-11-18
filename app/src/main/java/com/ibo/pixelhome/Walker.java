package com.ibo.pixelhome;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import java.util.Random;

/**
 * IBO's family walking along the bottom of the screen: dad in front, then mum, the little one
 * and the two cats. Everyone follows the exact path dad walked, so at each edge the family stops,
 * smiles at the camera and turns around one after the other.
 * Every 5 laps (there and back = 1 lap) they celebrate: the family raises a
 * "WELCOME TO OUR FAMILY" banner with confetti, and the cats jump in surprise and dance.
 */
final class Walker {
    static final int SPEED = 3;          // sprite pixels per step (8 steps a second)
    static final int PAUSE = 14;         // steps the family stands smiling at an edge
    static final int HISTORY = 64;
    static final int LAPS = 5;           // celebrate every 5 laps
    static final int CEL = 64;           // celebration length in steps (8 seconds)
    static final String BANNER = "WELCOME TO OUR FAMILY";
    static final int[] CONFETTI = {0xFFFFD84A, 0xFFE8343B, 0xFF5AD1E6, 0xFF26B257, 0xFFFF7AB0, 0xFFFFFFFF};

    /** Set from the menu or adb to celebrate right away. */
    static volatile boolean requestCelebrate;

    /** One family member (or cat). */
    static final class Member {
        final Bitmap[] walk, cheer;      // cheer: humans' raised-arms frames, cats' dance frames
        final Bitmap front, surprise;    // front: humans only; surprise: cats only
        final int delay;                 // steps behind dad
        float x = Float.NaN;             // left edge, screen pixels
        int face = 1, step;
        boolean moving;
        Member(Bitmap[] walk, Bitmap front, Bitmap[] cheer, Bitmap surprise, int delay) {
            this.walk = walk; this.front = front; this.cheer = cheer; this.surprise = surprise; this.delay = delay;
        }
        boolean isCat() { return front == null; }
    }

    final Member[] family;
    final Paint p = new Paint(), shadow = new Paint(), fill = new Paint(), text = new Paint();
    final float[] hist = new float[HISTORY];
    final Random rnd = new Random();
    int histCount, dir = 1, pause, edges, viewW;
    int celebrate = -1;                  // -1 = not celebrating, else step of the celebration
    boolean flipAfter;
    final float[][] confetti = new float[48][5];   // x, y, vx, vy, colour index (y < 0 = unused)

    Walker(Resources r) {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inScaled = false;
        family = new Member[]{
                new Member(load(r, o, R.drawable.dad_walk0, R.drawable.dad_walk1, R.drawable.dad_walk2, R.drawable.dad_walk3),
                        bmp(r, o, R.drawable.dad_front), load(r, o, R.drawable.dad_cheer0, R.drawable.dad_cheer1), null, 0),
                new Member(load(r, o, R.drawable.mom_walk0, R.drawable.mom_walk1, R.drawable.mom_walk2, R.drawable.mom_walk3),
                        bmp(r, o, R.drawable.mom_front), load(r, o, R.drawable.mom_cheer0, R.drawable.mom_cheer1), null, 11),
                new Member(load(r, o, R.drawable.kid_walk0, R.drawable.kid_walk1, R.drawable.kid_walk2, R.drawable.kid_walk3),
                        bmp(r, o, R.drawable.kid_front), load(r, o, R.drawable.kid_cheer0, R.drawable.kid_cheer1), null, 20),
                new Member(load(r, o, R.drawable.cat_a0, R.drawable.cat_a1, R.drawable.cat_a2, R.drawable.cat_a3), null,
                        load(r, o, R.drawable.cat_a_dance0, R.drawable.cat_a_dance1, R.drawable.cat_a_dance2),
                        bmp(r, o, R.drawable.cat_a_surprise), 28),
                new Member(load(r, o, R.drawable.cat_b0, R.drawable.cat_b1, R.drawable.cat_b2, R.drawable.cat_b3), null,
                        load(r, o, R.drawable.cat_b_dance0, R.drawable.cat_b_dance1, R.drawable.cat_b_dance2),
                        bmp(r, o, R.drawable.cat_b_surprise), 34)};
        p.setFilterBitmap(false);
        p.setAntiAlias(false);
        shadow.setColor(0x55000000);
        fill.setAntiAlias(false);
        text.setAntiAlias(false);
        text.setTypeface(r.getFont(R.font.press_start_2p));
        for (float[] c : confetti) c[1] = -1;
    }

    static Bitmap bmp(Resources r, BitmapFactory.Options o, int id) { return BitmapFactory.decodeResource(r, id, o); }

    static Bitmap[] load(Resources r, BitmapFactory.Options o, int... ids) {
        Bitmap[] b = new Bitmap[ids.length];
        for (int i = 0; i < ids.length; i++) b[i] = bmp(r, o, ids[i]);
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

    float past(int back) {
        int i = Math.max(0, histCount - 1 - back);
        return hist[i % HISTORY];
    }

    void startCelebration(boolean flip) {
        celebrate = 0;
        flipAfter = flip;
        pause = 0;
        for (float[] c : confetti) c[1] = -1;
    }

    /** Called 8 times a second. */
    void update(int w, float px) {
        viewW = w;
        int k = zoom(px);
        Member dad = family[0];
        float speed = SPEED * k, dadW = dad.walk[0].getWidth() * k, margin = 4 * k;
        if (Float.isNaN(dad.x)) {                        // start mid-screen, family already lined up behind
            dad.x = w * 0.35f;
            for (int i = HISTORY - 1; i >= 0; i--) push(dad.x - i * speed);
        }
        if (requestCelebrate && celebrate < 0) {
            requestCelebrate = false;
            startCelebration(false);
        }
        if (celebrate >= 0) {                            // everyone stands still and parties
            celebrate++;
            for (Member m : family) m.moving = false;
            moveConfetti(k);
            if (celebrate >= CEL) {
                celebrate = -1;
                if (flipAfter) dir = -dir;
            }
            return;
        }
        if (pause > 0) {
            if (--pause == 0) dir = -dir;                // done smiling: turn around
            dad.moving = false;
        } else {
            dad.step = (dad.step + 1) % 4;
            dad.x += dir * speed;
            dad.face = dir;
            dad.moving = true;
            boolean edge = false;
            if (dad.x > w - dadW - margin) { dad.x = w - dadW - margin; edge = true; }
            if (dad.x < margin) { dad.x = margin; edge = true; }
            push(dad.x);
            if (edge) {
                edges++;
                if (edges % (2 * LAPS) == 0) startCelebration(true); else pause = PAUSE;
            }
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

    void moveConfetti(int k) {
        if (celebrate == 12 || celebrate == 24) {        // two bursts
            for (float[] c : confetti) {
                if (c[1] >= 0 && celebrate == 24) continue;
                c[0] = bannerX + rnd.nextFloat() * bannerW;
                c[1] = bannerY + rnd.nextFloat() * bannerH;
                c[2] = (rnd.nextFloat() * 2 - 1) * 2 * k;
                c[3] = -(1 + rnd.nextFloat() * 3) * k;
                c[4] = rnd.nextInt(CONFETTI.length);
            }
        }
        for (float[] c : confetti) {
            if (c[1] < 0) continue;
            c[0] += c[2];
            c[1] += c[3];
            c[3] += 0.7f * k;                            // gravity
            c[2] *= 0.95f;
            if (c[1] > 4000) c[1] = -1;
        }
    }

    // banner geometry, worked out while drawing (confetti uses it)
    float bannerX, bannerY, bannerW, bannerH;

    void draw(Canvas c, float px, float feetY) {
        int k = zoom(px);
        Member dad = family[0];
        float dadW = dad.walk[0].getWidth() * k;
        if (celebrate >= 0) { drawCelebration(c, k, feetY, dadW); return; }
        for (int i = family.length - 1; i >= 0; i--) {   // back of the line first, dad on top
            Member m = family[i];
            if (Float.isNaN(m.x)) continue;
            Bitmap b = m.moving ? m.walk[m.step] : (m.front != null ? m.front : m.walk[1]);
            boolean mirror = (m.moving || m.isCat()) && m.face < 0;
            drawSprite(c, b, m.x + (dadW - b.getWidth() * k) / 2f, feetY, k, mirror, 0);
        }
    }

    void drawCelebration(Canvas c, int k, float feetY, float dadW) {
        int t = celebrate;
        // banner above the humans' heads: unrolls, stays, rolls up again
        float minC = Float.MAX_VALUE, maxC = -Float.MAX_VALUE;
        for (int i = 0; i < 3; i++) {
            float cx = family[i].x + dadW / 2f;
            minC = Math.min(minC, cx);
            maxC = Math.max(maxC, cx);
        }
        text.setTextSize(8 * k);
        float tw = text.measureText(BANNER);
        bannerW = tw + 12 * k;
        bannerH = 16 * k;
        bannerY = feetY - 54 * k - bannerH;
        bannerX = Math.max(4 * k, Math.min(viewW - bannerW - 4 * k, (minC + maxC) / 2f - bannerW / 2f));
        float open = t < 8 ? 0 : t < 16 ? (t - 8) / 8f : t < CEL - 8 ? 1 : (CEL - t) / 8f;

        // poles from dad's and mum's raised hands up to the banner
        if (open > 0) {
            fill.setColor(0xFF7A4A2A);
            for (int i = 0; i < 2; i++) {
                float cx = family[i].x + dadW / 2f;
                float hand = cx + (i == 0 ? 11 : -11) * k;
                c.drawRect(hand - k / 2f, bannerY + bannerH, hand + k / 2f + k, feetY - 38 * k, fill);
            }
        }
        // the family and the cats
        for (int i = family.length - 1; i >= 0; i--) {
            Member m = family[i];
            if (Float.isNaN(m.x)) continue;
            Bitmap b;
            float lift = 0;
            boolean mirror = false;
            if (!m.isCat()) {
                b = (t >= 8 && t < CEL - 8) ? m.cheer[(t / 3) % 2] : m.front;
            } else if (t < 10) {                          // surprised: jump up
                b = m.surprise;
                lift = t < 5 ? (5 - Math.abs(t - 2.5f) * 2) * k * 2 : 0;
            } else if (t < CEL - 6) {                     // dance!
                int[] seq = {0, 1, 2, 1};
                b = m.cheer[seq[(t / 2) % 4]];
                lift = ((t / 2) % 2) * 2 * k;
                mirror = ((t / 8) % 2) == 1;
            } else {
                b = m.walk[1];
                mirror = m.face < 0;
            }
            float left = m.x + (dadW - b.getWidth() * k) / 2f;
            drawSprite(c, b, left, feetY, k, mirror, lift);
            if (m.isCat()) {
                float top = feetY - b.getHeight() * k - lift;
                if (t < 10) drawLabel(c, "!", left + b.getWidth() * k / 2f, top - 2 * k, k, 0xFFFFD84A);
                else if (t < CEL - 6) drawNote(c, left + ((t / 4) % 2 == 0 ? -4 : b.getWidth() + 1) * k,
                        top - ((t % 8) + 2) * k, k);
            }
        }
        if (open > 0) {
            float w = bannerW * open, x0 = bannerX + (bannerW - w) / 2f;
            fill.setColor(PixelActivity.INK);
            c.drawRect(x0 - k, bannerY - k, x0 + w + k, bannerY + bannerH + k, fill);
            fill.setColor(0xFFFFF1A8);
            c.drawRect(x0, bannerY, x0 + w, bannerY + bannerH, fill);
            fill.setColor(0xFFE8343B);                    // red stripes top and bottom
            c.drawRect(x0, bannerY, x0 + w, bannerY + k, fill);
            c.drawRect(x0, bannerY + bannerH - k, x0 + w, bannerY + bannerH, fill);
            c.save();
            c.clipRect(x0, bannerY, x0 + w, bannerY + bannerH);
            float ty = bannerY + bannerH / 2f + 4 * k;
            text.setColor(0xFFFFB0C0);
            c.drawText(BANNER, bannerX + 6 * k + k, ty + k, text);
            text.setColor(0xFFE8343B);
            c.drawText(BANNER, bannerX + 6 * k, ty, text);
            c.restore();
        }
        for (float[] cf : confetti) {
            if (cf[1] < 0) continue;
            fill.setColor(CONFETTI[(int) cf[4]]);
            c.drawRect(cf[0], cf[1], cf[0] + k * 1.5f, cf[1] + k * 1.5f, fill);
        }
    }

    void drawLabel(Canvas c, String s, float cx, float bottom, int k, int color) {
        text.setTextSize(8 * k);
        float w = text.measureText(s);
        text.setColor(PixelActivity.INK);
        c.drawText(s, cx - w / 2f + k, bottom + k, text);
        text.setColor(color);
        c.drawText(s, cx - w / 2f, bottom, text);
    }

    /** A little pixel music note. */
    void drawNote(Canvas c, float x, float y, int k) {
        fill.setColor(0xFFFFFFFF);
        c.drawRect(x + 2 * k, y, x + 3 * k, y + 5 * k, fill);       // stem
        c.drawRect(x, y + 4 * k, x + 3 * k, y + 6 * k, fill);       // head
        c.drawRect(x + 3 * k, y, x + 5 * k, y + k, fill);           // flag
    }

    void drawSprite(Canvas c, Bitmap b, float left, float feetY, int k, boolean mirror, float lift) {
        float w = b.getWidth() * k, h = b.getHeight() * k;
        float sw = lift > 0 ? 0.4f : 0.6f;
        c.drawRect(left + w * (0.5f - sw / 2), feetY - k, left + w * (0.5f + sw / 2), feetY + k, shadow);
        RectF dst = new RectF(left, feetY - h - lift, left + w, feetY - lift);
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
