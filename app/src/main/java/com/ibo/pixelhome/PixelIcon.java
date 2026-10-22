package com.ibo.pixelhome;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

/** Draws a small character grid as chunky pixels ('#' main colour, 'y' yellow, 'b' blue). */
class PixelIcon extends View {
    final Paint p = new Paint();
    String[] grid;
    int main = 0xFFFFFFFF;

    PixelIcon(Context c) {
        super(c);
        p.setAntiAlias(false);
    }

    void set(String[] g, int color) {
        grid = g;
        main = color;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas c) {
        if (grid == null) return;
        float cell = Math.min(getWidth(), getHeight()) / (float) grid.length;
        for (int r = 0; r < grid.length; r++)
            for (int k = 0; k < grid[r].length(); k++) {
                char ch = grid[r].charAt(k);
                if (ch == '.') continue;
                p.setColor(ch == 'y' ? 0xFFFFD84A : ch == 'b' ? 0xFF5AD1E6 : main);
                c.drawRect(k * cell, r * cell, (k + 1) * cell, (r + 1) * cell, p);
            }
    }
}
