package com.ofertiber.sarabutton;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

final class NtfySender {
    private static final String LOG_TAG = "BluNtfyAlarm";
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    interface Callback { void done(boolean ok, String detail); }
    private NtfySender() {}

    static void send(Context context, Callback callback) {
        SharedPreferences p = AppPreferences.get(context);
        String server = p.getString(AppPreferences.KEY_NTFY_SERVER, "https://ntfy.sh");
        String topic = p.getString(AppPreferences.KEY_NTFY_TOPIC, "");
        String message = p.getString(AppPreferences.KEY_NTFY_MESSAGE, "");
        if (server == null) server = "https://ntfy.sh";
        if (topic == null || topic.trim().isEmpty()) { if (callback != null) callback.done(false, "Topic fehlt"); return; }
        final String finalServer = normalizeServer(server);
        final String finalTopic = topic.trim();
        final String finalMessage = message == null || message.trim().isEmpty() ? "Notfallalarm" : message.trim();
        EXECUTOR.execute(() -> {
            Exception last = null;
            for (int attempt=1; attempt<=3; attempt++) {
                HttpURLConnection c = null;
                try {
                    URL u = new URL(finalServer + "/" + encodePath(finalTopic));
                    c = (HttpURLConnection) u.openConnection();
                    c.setConnectTimeout(8000); c.setReadTimeout(8000); c.setRequestMethod("POST"); c.setDoOutput(true);
                    c.setRequestProperty("Content-Type", "text/plain; charset=utf-8");
                    c.setRequestProperty("Priority", "5");
                    c.setRequestProperty("Title", finalMessage);
                    try (OutputStream out = c.getOutputStream()) { out.write("BLU Button Alert wurde ausgelöst.".getBytes(StandardCharsets.UTF_8)); }
                    int code = c.getResponseCode();
                    if (code >= 200 && code < 300) { if (callback != null) callback.done(true, "HTTP " + code); return; }
                    last = new Exception("HTTP " + code);
                } catch (Exception e) { last = e; Log.w(LOG_TAG, "ntfy attempt " + attempt + " failed", e); }
                finally { if (c != null) c.disconnect(); }
                try { Thread.sleep(700L * attempt); } catch (InterruptedException e) { Thread.currentThread().interrupt(); break; }
            }
            if (callback != null) callback.done(false, last == null ? "Unbekannter Fehler" : last.getMessage());
        });
    }
    private static String normalizeServer(String s) { s=s.trim(); if (!s.startsWith("http://") && !s.startsWith("https://")) s="https://"+s; while(s.endsWith("/")) s=s.substring(0,s.length()-1); return s; }
    private static String encodePath(String s) { return s.replace("%","%25").replace("/","%2F").replace(" ","%20").replace("?","%3F").replace("#","%23"); }
}
