package com.ryansteckler.nlpunbounce.tasker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.ryansteckler.nlpunbounce.XposedReceiver;
import com.ryansteckler.nlpunbounce.models.UnbounceStatsCollection;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class TaskerReceiver extends BroadcastReceiver {
    public TaskerReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        //Unpack the bundle.
        if (intent != null) {
            Bundle savedBundle = intent.getBundleExtra(TaskerActivity.EXTRA_BUNDLE);
            if (savedBundle != null) {
                String type = savedBundle.getString(TaskerActivity.BUNDLE_TYPE);
                if (type.equals("reset")) {
                    Intent resetIntent = new Intent(XposedReceiver.RESET_ACTION);
                    resetIntent.putExtra(XposedReceiver.STAT_TYPE, UnbounceStatsCollection.STAT_CURRENT);
                    try {
                        context.sendBroadcast(resetIntent);
                    } catch (IllegalStateException ise) {

                    }

                } else if (type.startsWith("regex")) {
                    long seconds = savedBundle.getLong(TaskerActivity.BUNDLE_SECONDS);
                    boolean enabled = savedBundle.getBoolean(TaskerActivity.BUNDLE_ENABLED);
                    String name = savedBundle.getString(TaskerActivity.BUNDLE_NAME);
                    type = type.substring(type.indexOf("_") + 1);

                    String blockName = name + "$$||$$" + Long.toString(seconds) + "$$||$$" + (enabled ? "enabled" : "disabled");

                    //set the prefs appropriately.
                    SharedPreferences prefs = context.getSharedPreferences("com.ryansteckler.nlpunbounce" + "_preferences", Context.MODE_WORLD_READABLE);
                    Set<String> sampleSet = new HashSet<String>();
                    Set<String> set = new HashSet<String>(prefs.getStringSet(type + "_regex_set", sampleSet));
                    for (Iterator<String> i = set.iterator(); i.hasNext(); ) {
                        String str = i.next();
                        if (str.startsWith(name + "$$||$$")) {
                            i.remove();
                        }
                    }
                    set.add(blockName);

                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putStringSet(type + "_regex_set", set);
                    editor.apply();

                } else {
                    long seconds = savedBundle.getLong(TaskerActivity.BUNDLE_SECONDS);
                    boolean enabled = savedBundle.getBoolean(TaskerActivity.BUNDLE_ENABLED);
                    String name = savedBundle.getString(TaskerActivity.BUNDLE_NAME);

                    //set the prefs appropriately.
                    SharedPreferences prefs = context.getSharedPreferences("com.ryansteckler.nlpunbounce" + "_preferences", Context.MODE_WORLD_READABLE);
                    String enabledName = type + "_" + name + "_enabled";
                    String secondsName = type + "_" + name + "_seconds";
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean(enabledName, enabled);
                    editor.putLong(secondsName, seconds);
                    editor.apply();
                }
            }

        }
    }
}
