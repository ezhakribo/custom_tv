package com.ibo.pixelhome;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** When the TV finishes starting, open Pixel Home (Xiaomi may have started its own home screen first). */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context c, Intent i) {
        if (!c.getSharedPreferences(KeyService.PREFS, Context.MODE_PRIVATE).getBoolean(KeyService.BLOCK_STOCK, true)) return;
        Intent home = new Intent(c, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        try {
            c.startActivity(home);
        } catch (RuntimeException ignored) { }
    }
}
