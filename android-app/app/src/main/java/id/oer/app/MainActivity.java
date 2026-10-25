package id.oer.app;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Base64;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.os.Handler;
import android.os.Looper;

import org.json.JSONObject;
import org.json.JSONArray;

import java.net.HttpURLConnection;
import java.net.URL;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

public class MainActivity extends Activity {

    private static final String API = "http://127.0.0.1:3000";
    private static final int REQ_PICK_IMAGE = 101;
    private static final int GREEN = Color.rgb(46, 125, 50);

    private final Handler handler = new Handler(Looper.getMainLooper());

    private LinearLayout messages;
    private EditText input;
    private TextView status;
    private ScrollView scroll;
    private ImageView previewImage;
    private LinearLayout previewContainer;

    private Bitmap selectedBitmap = null;
    private String sessionId = "1";
    private boolean busy = false;

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    private void setStatus(String text) {
        handler.post(() -> {
            if (status != null) status.setText(text);
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        buildScreen();
        createSession();
    }

    private void buildScreen() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(245, 247, 245));
        root.setFitsSystemWindows(true);
        root.setPadding(dp(16), dp(12), dp(16), dp(36));

        // Header
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setPadding(0, dp(4), 0, dp(10));

        TextView logo = new TextView(this);
        logo.setText("OoeR AI Studio");
        logo.setTextColor(GREEN);
        logo.setTextSize(24);
        logo.setTypeface(null, Typeface.BOLD);

        TextView subtitle = new TextView(this);
        subtitle.setText("Ruang Pelatihan AI (Teks & Visual)");
        subtitle.setTextColor(Color.DKGRAY);
        subtitle.setTextSize(14);

        status = new TextView(this);
        status.setText("Menghubungkan ke Termux...");
        status.setTextSize(12);
        status.setTextColor(Color.GRAY);

        header.addView(logo);
        header.addView(subtitle);
        header.addView(status);
        root.addView(header);

        // Toolbar
        LinearLayout toolbar = new LinearLayout(this);
        toolbar.setOrientation(LinearLayout.HORIZONTAL);
        toolbar.setGravity(Gravity.CENTER_VERTICAL);

        Button newBtn = new Button(this);
        newBtn.setText("＋ Sesi Baru");
        newBtn.setOnClickListener(v -> createSession());

        Button reloadBtn = new Button(this);
        reloadBtn.setText("Muat Ulang");
        reloadBtn.setOnClickListener(v -> loadSession(sessionId));

        toolbar.addView(newBtn, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        toolbar.addView(reloadBtn);
        root.addView(toolbar);

        // Chat List
        scroll = new ScrollView(this);
        scroll.setFillViewport(true);

        messages = new LinearLayout(this);
        messages.setOrientation(LinearLayout.VERTICAL);
        messages.setPadding(0, dp(8), 0, dp(12));
        scroll.addView(messages);

        root.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));

        // Pratinjau Foto
        previewContainer = new LinearLayout(this);
        previewContainer.setOrientation(LinearLayout.HORIZONTAL);
        previewContainer.setGravity(Gravity.CENTER_VERTICAL);
        previewContainer.setPadding(0, dp(4), 0, dp(4));
        previewContainer.setVisibility(View.GONE);

        previewImage = new ImageView(this);
        previewImage.setLayoutParams(new LinearLayout.LayoutParams(dp(70), dp(70)));
        previewImage.setScaleType(ImageView.ScaleType.CENTER_CROP);

        Button removeImgBtn = new Button(this);
        removeImgBtn.setText("❌ Batal");
        removeImgBtn.setOnClickListener(v -> clearSelectedPhoto());

        previewContainer.addView(previewImage);
        previewContainer.addView(removeImgBtn);
        root.addView(previewContainer);

        // Input
        input = new EditText(this);
        input.setHint("Tulis materi atau pertanyaan...");
        input.setTextSize(15);
        input.setMinLines(2);
        input.setMaxLines(3);
        input.setPadding(dp(12), dp(10), dp(12), dp(10));
        input.setBackgroundColor(Color.WHITE);
        root.addView(input);

        // Tombol Aksi (Foto & Kirim)
        LinearLayout actionRow = new LinearLayout(this);
        actionRow.setOrientation(LinearLayout.HORIZONTAL);
        actionRow.setPadding(0, dp(8), 0, 0);

        Button cameraBtn = new Button(this);
        cameraBtn.setText("📷 Foto");
        cameraBtn.setTextSize(15);
        cameraBtn.setOnClickListener(v -> openImagePicker());

        Button sendBtn = new Button(this);
        sendBtn.setText("🚀 Kirim Materi");
        sendBtn.setTextSize(15);
        sendBtn.setTypeface(null, Typeface.BOLD);
        sendBtn.setTextColor(Color.WHITE);
        sendBtn.setBackgroundColor(GREEN);
        sendBtn.setOnClickListener(v -> sendMessage());

        LinearLayout.LayoutParams camParams = new LinearLayout.LayoutParams(0, dp(48), 1);
        camParams.setMargins(0, 0, dp(8), 0);
        actionRow.addView(cameraBtn, camParams);

        LinearLayout.LayoutParams sendParams = new LinearLayout.LayoutParams(0, dp(48), 2);
        actionRow.addView(sendBtn, sendParams);

        root.addView(actionRow);

        setContentView(root);
    }

    private void openImagePicker() {
        Intent pickIntent = new Intent(Intent.ACTION_GET_CONTENT);
        pickIntent.setType("image/*");

        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Intent chooserIntent = Intent.createChooser(pickIntent, "Pilih Sumber Foto Materi");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{takePhotoIntent});

        startActivityForResult(chooserIntent, REQ_PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_PICK_IMAGE && resultCode == RESULT_OK) {
            Bitmap bmp = null;
            if (data != null) {
                if (data.getData() != null) {
                    try (InputStream is = getContentResolver().openInputStream(data.getData())) {
                        bmp = BitmapFactory.decodeStream(is);
                    } catch (Exception ignored) {}
                } else if (data.getExtras() != null && data.getExtras().get("data") != null) {
                    bmp = (Bitmap) data.getExtras().get("data");
                }
            }

            if (bmp != null) {
                // Dioptimalkan ke 480 agar pemrosesan visual jauh lebih cepat di CPU HP
                selectedBitmap = scaleDown(bmp, 480);
                previewImage.setImageBitmap(selectedBitmap);
                previewContainer.setVisibility(View.VISIBLE);
            }
        }
    }

    private void clearSelectedPhoto() {
        selectedBitmap = null;
        previewContainer.setVisibility(View.GONE);
    }

    private Bitmap scaleDown(Bitmap realImage, float maxImageSize) {
        float ratio = Math.min(
                maxImageSize / realImage.getWidth(),
                maxImageSize / realImage.getHeight()
        );
        if (ratio >= 1.0f) return realImage;
        int width = Math.round(ratio * realImage.getWidth());
        int height = Math.round(ratio * realImage.getHeight());
        return Bitmap.createScaledBitmap(realImage, width, height, true);
    }

    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
        return "data:image/jpeg;base64," + Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP);
    }

    private void addMessage(String speaker, String content, String base64Image) {
        LinearLayout bubble = new LinearLayout(this);
        bubble.setOrientation(LinearLayout.VERTICAL);

        boolean human = "human".equalsIgnoreCase(speaker) || "user".equalsIgnoreCase(speaker);
        bubble.setBackgroundColor(human ? Color.rgb(220, 245, 200) : Color.WHITE);
        bubble.setPadding(dp(12), dp(10), dp(12), dp(10));

        TextView label = new TextView(this);
        label.setText(human ? "👤 Kamu (Guru)" : "🤖 OoeR AI");
        label.setTextSize(13);
        label.setTypeface(null, Typeface.BOLD);
        label.setTextColor(human ? GREEN : Color.rgb(40, 100, 200));
        bubble.addView(label);

        if (base64Image != null && !base64Image.isEmpty()) {
            try {
                String cleanBase64 = base64Image.contains(",") ? base64Image.split(",")[1] : base64Image;
                byte[] decoded = Base64.decode(cleanBase64, Base64.DEFAULT);
                Bitmap img = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                if (img != null) {
                    ImageView iv = new ImageView(this);
                    iv.setImageBitmap(img);
                    iv.setAdjustViewBounds(true);
                    iv.setMaxHeight(dp(220));
                    iv.setPadding(0, dp(6), 0, dp(6));
                    bubble.addView(iv);
                }
            } catch (Exception ignored) {}
        }

        if (content != null && !content.isEmpty()) {
            TextView text = new TextView(this);
            text.setText(content);
            text.setTextSize(15);
            text.setTextColor(Color.rgb(35, 35, 35));
            bubble.addView(text);
        }

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, dp(4), 0, dp(4));
        messages.addView(bubble, params);

        scroll.post(() -> scroll.fullScroll(View.FOCUS_DOWN));
    }

    private JSONObject request(String path, String method, JSONObject body) throws Exception {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(API + path);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(method);
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(120000);
            conn.setRequestProperty("Accept", "application/json");

            if (body != null) {
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                byte[] data = body.toString().getBytes(StandardCharsets.UTF_8);
                try (OutputStream out = conn.getOutputStream()) {
                    out.write(data);
                }
            }

            int code = conn.getResponseCode();
            InputStream stream = (code >= 200 && code < 400) ? conn.getInputStream() : conn.getErrorStream();
            if (stream == null) throw new Exception("Tidak ada respons dari server.");

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] bytes = new byte[4096];
            int count;
            while ((count = stream.read(bytes)) != -1) {
                buffer.write(bytes, 0, count);
            }

            String resp = new String(buffer.toByteArray(), StandardCharsets.UTF_8);
            if (code < 200 || code >= 300) throw new Exception("HTTP " + code + ": " + resp);

            return new JSONObject(resp);
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    private void loadSession(String id) {
        if (busy) return;
        busy = true;
        setStatus("Memuat sesi " + id + "...");

        new Thread(() -> {
            try {
                JSONObject res = request("/ai/training/sessions/" + id, "GET", null);
                JSONArray history = res.optJSONArray("messages");

                handler.post(() -> {
                    sessionId = id;
                    messages.removeAllViews();
                    if (history != null) {
                        for (int i = 0; i < history.length(); i++) {
                            JSONObject item = history.optJSONObject(i);
                            if (item != null) {
                                addMessage(
                                        item.optString("speaker", "ai"),
                                        item.optString("content", ""),
                                        item.optString("image", null)
                                );
                            }
                        }
                    }
                    setStatus("Terhubung • Sesi " + sessionId);
                });
            } catch (Exception e) {
                handler.post(() -> setStatus("Gagal memuat: " + e.getMessage()));
            } finally {
                handler.post(() -> busy = false);
            }
        }).start();
    }

    private void createSession() {
        if (busy) return;
        busy = true;
        setStatus("Membuat sesi pelatihan...");

        new Thread(() -> {
            try {
                JSONObject res = request("/ai/training/sessions", "POST", new JSONObject());
                JSONObject session = res.optJSONObject("session");
                final String newId = session != null ? session.optString("id", "1") : "1";
                final String q = session != null ? session.optString("question", "Halo! Mau melatih apa hari ini?") : "";

                handler.post(() -> {
                    sessionId = newId;
                    messages.removeAllViews();
                    clearSelectedPhoto();
                    addMessage("ai", q, null);
                    setStatus("Terhubung • Sesi " + sessionId);
                });
            } catch (Exception e) {
                handler.post(() -> setStatus("Gagal sesi: " + e.getMessage()));
            } finally {
                handler.post(() -> busy = false);
            }
        }).start();
    }

    private void sendMessage() {
        final String text = input.getText().toString().trim();
        final Bitmap photoToSend = selectedBitmap;

        if (text.isEmpty() && photoToSend == null) return;
        if (busy) return;

        busy = true;
        input.setText("");
        clearSelectedPhoto();

        final String base64Img = (photoToSend != null) ? bitmapToBase64(photoToSend) : null;
        
        // 1. Tampilkan pesan pengguna
        addMessage("human", text, base64Img);

        // 2. Langsung tampilkan balon AI "sedang mengetik..." agar aplikasi responsif seketika
        addMessage("ai", "⏳ OoeR AI sedang berpikir dan mengetik...", null);
        setStatus("OoeR AI sedang memproses materi...");

        new Thread(() -> {
            try {
                JSONObject payload = new JSONObject();
                payload.put("content", text);
                if (base64Img != null) {
                    payload.put("image", base64Img);
                }

                JSONObject res = request("/ai/training/sessions/" + sessionId + "/messages", "POST", payload);

                String aiAnswer = res.optString("answer", "");
                if (aiAnswer.isEmpty()) aiAnswer = res.optString("reply", "");
                if (aiAnswer.isEmpty()) aiAnswer = res.optString("message", "Materi dan foto berhasil dipelajari!");

                final String reply = aiAnswer;

                handler.post(() -> {
                    // Hapus balon sementara "sedang mengetik...", lalu gantikan dengan jawaban asli
                    int count = messages.getChildCount();
                    if (count > 0) {
                        messages.removeViewAt(count - 1);
                    }
                    addMessage("ai", reply, null);
                    setStatus("Terhubung • Sesi " + sessionId);
                });
            } catch (Exception e) {
                handler.post(() -> {
                    int count = messages.getChildCount();
                    if (count > 0) {
                        messages.removeViewAt(count - 1);
                    }
                    addMessage("ai", "⚠️ Gagal mengirim: " + e.getMessage(), null);
                    setStatus("Error saat mengirim materi.");
                });
            } finally {
                handler.post(() -> busy = false);
            }
        }).start();
    }
}
