package id.oer.app;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Toast;
import android.os.Handler;
import android.os.Looper;

import org.json.JSONObject;
import org.json.JSONArray;

import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

public class MainActivity extends Activity {

    private static final String API = "http://127.0.0.1:3000";
    private static final int GREEN = Color.rgb(101, 163, 13);

    private final Handler handler = new Handler(Looper.getMainLooper());

    private LinearLayout root;
    private LinearLayout messages;
    private EditText input;
    private TextView status;
    private ScrollView scroll;
    private Button sendButton;
    private Button newButton;

    private String sessionId = "1";
    private boolean busy = false;

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    private void setStatus(String text) {
        handler.post(() -> {
            if (status != null) {
                status.setText(text);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(Color.WHITE);
        getWindow().setNavigationBarColor(Color.WHITE);

        buildScreen();
        // Buat sesi pelatihan otomatis saat pertama kali dibuka
        createSession();
    }

    private void buildScreen() {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(248, 250, 247));
        root.setPadding(dp(16), dp(12), dp(16), dp(10));

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setPadding(0, dp(4), 0, dp(12));

        TextView logo = new TextView(this);
        logo.setText("OoeR AI");
        logo.setTextColor(GREEN);
        logo.setTextSize(26);
        logo.setTypeface(null, Typeface.BOLD);

        TextView subtitle = new TextView(this);
        subtitle.setText("Ruang Pelatihan AI");
        subtitle.setTextColor(Color.DKGRAY);
        subtitle.setTextSize(15);

        status = new TextView(this);
        status.setText("Menghubungkan ke backend Termux...");
        status.setTextSize(12);
        status.setTextColor(Color.GRAY);
        status.setPadding(0, dp(4), 0, 0);

        header.addView(logo);
        header.addView(subtitle);
        header.addView(status);
        root.addView(header);

        LinearLayout toolbar = new LinearLayout(this);
        toolbar.setOrientation(LinearLayout.HORIZONTAL);
        toolbar.setGravity(Gravity.CENTER_VERTICAL);

        newButton = new Button(this);
        newButton.setText("＋ Pelatihan Baru");
        newButton.setOnClickListener(v -> createSession());

        Button reloadButton = new Button(this);
        reloadButton.setText("Muat Ulang");
        reloadButton.setOnClickListener(v -> loadSession(sessionId));

        toolbar.addView(newButton, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        toolbar.addView(reloadButton);
        root.addView(toolbar);

        scroll = new ScrollView(this);
        scroll.setFillViewport(true);

        messages = new LinearLayout(this);
        messages.setOrientation(LinearLayout.VERTICAL);
        messages.setPadding(0, dp(8), 0, dp(12));

        scroll.addView(messages);
        root.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));

        TextView hint = new TextView(this);
        hint.setText("Tulis materi atau ajarkan sesuatu kepada OoeR AI:");
        hint.setTextColor(Color.GRAY);
        hint.setTextSize(12);
        hint.setPadding(0, dp(6), 0, dp(4));
        root.addView(hint);

        input = new EditText(this);
        input.setHint("Ketik materi di sini...");
        input.setTextSize(15);
        input.setGravity(Gravity.TOP | Gravity.START);
        input.setMinLines(2);
        input.setMaxLines(4);
        input.setPadding(dp(10), dp(10), dp(10), dp(10));
        input.setBackgroundColor(Color.WHITE);

        root.addView(input, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        sendButton = new Button(this);
        sendButton.setText("Kirim Materi ke OoeR AI");
        sendButton.setOnClickListener(v -> sendMessage());
        root.addView(sendButton);

        setContentView(root);
    }

    private void addMessage(String speaker, String content) {
        TextView bubble = new TextView(this);
        boolean human = "human".equalsIgnoreCase(speaker) || "user".equalsIgnoreCase(speaker);

        bubble.setText((human ? "Kamu (Guru)" : "OoeR AI") + "\n\n" + content);
        bubble.setTextColor(Color.rgb(35, 35, 35));
        bubble.setTextSize(14);
        bubble.setPadding(dp(12), dp(10), dp(12), dp(10));
        bubble.setBackgroundColor(human ? Color.rgb(220, 245, 200) : Color.WHITE);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, dp(4), 0, dp(4));
        messages.addView(bubble, params);

        scroll.post(() -> scroll.fullScroll(View.FOCUS_DOWN));
    }

    private JSONObject request(String path, String method, JSONObject body) throws Exception {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(API + path);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(60000);
            connection.setRequestProperty("Accept", "application/json");

            if (body != null) {
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                byte[] data = body.toString().getBytes(StandardCharsets.UTF_8);
                try (OutputStream out = connection.getOutputStream()) {
                    out.write(data);
                }
            }

            int code = connection.getResponseCode();
            java.io.InputStream stream = (code >= 200 && code < 400)
                    ? connection.getInputStream()
                    : connection.getErrorStream();

            if (stream == null) {
                throw new Exception("Server tidak mengirim respons.");
            }

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            try (java.io.InputStream in = stream) {
                byte[] bytes = new byte[4096];
                int count;
                while ((count = in.read(bytes)) != -1) {
                    buffer.write(bytes, 0, count);
                }
            }

            String response = new String(buffer.toByteArray(), StandardCharsets.UTF_8);

            if (code < 200 || code >= 300) {
                throw new Exception("HTTP " + code + ": " + response);
            }

            return new JSONObject(response);
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    private void loadSession(String id) {
        if (busy) return;
        busy = true;
        setStatus("Memuat riwayat pelatihan sesi " + id + "...");

        new Thread(() -> {
            try {
                JSONObject result = request("/ai/training/sessions/" + id, "GET", null);
                JSONArray history = result.optJSONArray("messages");

                handler.post(() -> {
                    sessionId = id;
                    messages.removeAllViews();
                    if (history != null) {
                        for (int i = 0; i < history.length(); i++) {
                            JSONObject item = history.optJSONObject(i);
                            if (item != null) {
                                addMessage(item.optString("speaker", "ai"), item.optString("content", ""));
                            }
                        }
                    }
                    setStatus("Terhubung • Sesi " + sessionId);
                });
            } catch (Exception e) {
                handler.post(() -> setStatus("Gagal memuat sesi: " + e.getMessage()));
            } finally {
                handler.post(() -> busy = false);
            }
        }).start();
    }

    private void createSession() {
        if (busy) return;
        busy = true;
        setStatus("Membuat ruang pelatihan baru...");

        new Thread(() -> {
            try {
                JSONObject result = request("/ai/training/sessions", "POST", new JSONObject());
                JSONObject session = result.optJSONObject("session");
                final String newId = session != null ? session.optString("id", "1") : "1";
                final String question = session != null ? session.optString("question", "Halo! Ada yang bisa diajarkan?") : "";

                handler.post(() -> {
                    sessionId = newId;
                    messages.removeAllViews();
                    addMessage("ai", question);
                    setStatus("Terhubung • Sesi Pelatihan " + sessionId);
                });
            } catch (Exception e) {
                handler.post(() -> setStatus("Gagal membuat sesi: " + e.getMessage()));
            } finally {
                handler.post(() -> busy = false);
            }
        }).start();
    }

    private void sendMessage() {
        final String text = input.getText().toString().trim();
        if (text.isEmpty() || busy) return;

        busy = true;
        input.setText("");
        addMessage("human", text);
        setStatus("OoeR AI sedang memproses materi...");

        new Thread(() -> {
            try {
                JSONObject payload = new JSONObject();
                payload.put("content", text);

                JSONObject result = request("/ai/training/sessions/" + sessionId + "/messages", "POST", payload);
                final String reply = result.optString("reply", "Materi diterima dan dipelajari!");

                handler.post(() -> {
                    addMessage("ai", reply);
                    setStatus("Terhubung • Sesi Pelatihan " + sessionId);
                });
            } catch (Exception e) {
                handler.post(() -> {
                    addMessage("ai", "⚠️ Gagal mengirim: " + e.getMessage());
                    setStatus("Error saat mengirim materi.");
                });
            } finally {
                handler.post(() -> busy = false);
            }
        }).start();
    }
}
