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
import android.graphics.drawable.GradientDrawable;
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

    // LINK API CLOUDFLARE AKTIF
    private static final String API = "https://spears-lol-instances-cod.trycloudflare.com";
    private static final int REQ_PICK_IMAGE = 101;

    // PALET WARNA MASTER DEVELOPER
    private static final int COLOR_BG = Color.parseColor("#090D16");           // Hitam Obsidian
    private static final int COLOR_SURFACE = Color.parseColor("#0F172A");      // Dark Slate Card
    private static final int COLOR_SURFACE_LIGHT = Color.parseColor("#1E293B");// Surface Elemen
    private static final int COLOR_ACCENT_CYAN = Color.parseColor("#06B6D4");  // Neon Cyan
    private static final int COLOR_ACCENT_EMERALD = Color.parseColor("#10B981");// Emerald Green
    private static final int COLOR_BORDER = Color.parseColor("#334155");       // Border Subtle
    private static final int COLOR_TEXT_PRIMARY = Color.parseColor("#F1F5F9"); // Text Terang
    private static final int COLOR_TEXT_SECONDARY = Color.parseColor("#94A3B8");// Text Muted

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

    // Helper Pembuat Box Modern (Rounded + Border)
    private GradientDrawable createBox(int bgColor, int cornerRadiusDp, int strokeColor, int strokeWidthDp) {
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(bgColor);
        gd.setCornerRadius(dp(cornerRadiusDp));
        if (strokeWidthDp > 0) {
            gd.setStroke(dp(strokeWidthDp), strokeColor);
        }
        return gd;
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
        root.setBackgroundColor(COLOR_BG);
        root.setFitsSystemWindows(true);
        root.setPadding(dp(16), dp(12), dp(16), dp(16));

        // HEADER CYBER IDE
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setPadding(dp(16), dp(14), dp(16), dp(14));
        header.setBackground(createBox(COLOR_SURFACE, 16, COLOR_BORDER, 1));

        LinearLayout titleRow = new LinearLayout(this);
        titleRow.setOrientation(LinearLayout.HORIZONTAL);
        titleRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView iconTv = new TextView(this);
        iconTv.setText("⚡ ");
        iconTv.setTextSize(20);

        TextView logo = new TextView(this);
        logo.setText("OoeR Pro");
        logo.setTextColor(COLOR_ACCENT_CYAN);
        logo.setTextSize(22);
        logo.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);

        TextView badge = new TextView(this);
        badge.setText(" MASTER IDE ");
        badge.setTextSize(10);
        badge.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        badge.setTextColor(COLOR_ACCENT_EMERALD);
        badge.setBackground(createBox(Color.parseColor("#064E3B"), 6, COLOR_ACCENT_EMERALD, 1));
        badge.setPadding(dp(6), dp(2), dp(6), dp(2));
        LinearLayout.LayoutParams badgeParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        badgeParams.setMargins(dp(8), 0, 0, 0);

        titleRow.addView(iconTv);
        titleRow.addView(logo);
        titleRow.addView(badge, badgeParams);
        header.addView(titleRow);

        TextView subtitle = new TextView(this);
        subtitle.setText("Expert AI Coding & Neural Training");
        subtitle.setTextColor(COLOR_TEXT_SECONDARY);
        subtitle.setTextSize(12);
        subtitle.setTypeface(Typeface.MONOSPACE, Typeface.NORMAL);
        subtitle.setPadding(0, dp(4), 0, dp(6));
        header.addView(subtitle);

        status = new TextView(this);
        status.setText("● READY • Terhubung ke Cloud Engine");
        status.setTextSize(11);
        status.setTextColor(COLOR_ACCENT_EMERALD);
        status.setTypeface(Typeface.MONOSPACE, Typeface.NORMAL);
        header.addView(status);

        root.addView(header);

        // TOOLBAR MODERN
        LinearLayout toolbar = new LinearLayout(this);
        toolbar.setOrientation(LinearLayout.HORIZONTAL);
        toolbar.setGravity(Gravity.CENTER_VERTICAL);
        toolbar.setPadding(0, dp(10), 0, dp(10));

        Button newBtn = new Button(this);
        newBtn.setText("+ Sesi Baru");
        newBtn.setTextSize(12);
        newBtn.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        newBtn.setTextColor(COLOR_TEXT_PRIMARY);
        newBtn.setBackground(createBox(COLOR_SURFACE_LIGHT, 10, COLOR_BORDER, 1));
        newBtn.setOnClickListener(v -> createSession());

        Button reloadBtn = new Button(this);
        reloadBtn.setText("↺ Sync");
        reloadBtn.setTextSize(12);
        reloadBtn.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        reloadBtn.setTextColor(COLOR_TEXT_SECONDARY);
        reloadBtn.setBackground(createBox(COLOR_SURFACE, 10, COLOR_BORDER, 1));
        reloadBtn.setOnClickListener(v -> loadSession(sessionId));

        LinearLayout.LayoutParams newBtnParams = new LinearLayout.LayoutParams(0, dp(40), 1);
        newBtnParams.setMargins(0, 0, dp(8), 0);
        toolbar.addView(newBtn, newBtnParams);

        LinearLayout.LayoutParams reloadParams = new LinearLayout.LayoutParams(dp(80), dp(40));
        toolbar.addView(reloadBtn, reloadParams);

        root.addView(toolbar);

        // CHAT / CONSOLE OUTPUT LIST
        scroll = new ScrollView(this);
        scroll.setFillViewport(true);

        messages = new LinearLayout(this);
        messages.setOrientation(LinearLayout.VERTICAL);
        messages.setPadding(0, dp(4), 0, dp(12));
        scroll.addView(messages);

        root.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));

        // PRATINJAU FOTO DENGAN BORDER NEON
        previewContainer = new LinearLayout(this);
        previewContainer.setOrientation(LinearLayout.HORIZONTAL);
        previewContainer.setGravity(Gravity.CENTER_VERTICAL);
        previewContainer.setPadding(dp(8), dp(6), dp(8), dp(6));
        previewContainer.setBackground(createBox(COLOR_SURFACE, 10, COLOR_ACCENT_CYAN, 1));
        previewContainer.setVisibility(View.GONE);

        previewImage = new ImageView(this);
        previewImage.setLayoutParams(new LinearLayout.LayoutParams(dp(60), dp(60)));
        previewImage.setScaleType(ImageView.ScaleType.CENTER_CROP);

        Button removeImgBtn = new Button(this);
        removeImgBtn.setText("✕ Hapus");
        removeImgBtn.setTextSize(11);
        removeImgBtn.setTextColor(Color.parseColor("#EF4444"));
        removeImgBtn.setBackground(createBox(Color.TRANSPARENT, 6, Color.parseColor("#EF4444"), 1));
        removeImgBtn.setOnClickListener(v -> clearSelectedPhoto());
        LinearLayout.LayoutParams rmParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(34));
        rmParams.setMargins(dp(12), 0, 0, 0);

        previewContainer.addView(previewImage);
        previewContainer.addView(removeImgBtn, rmParams);
        root.addView(previewContainer);

        // KOTAK INPUT TERMINAL
        input = new EditText(this);
        input.setHint("Ketik perintah atau masukkan kode program...");
        input.setHintTextColor(COLOR_TEXT_SECONDARY);
        input.setTextColor(COLOR_TEXT_PRIMARY);
        input.setTextSize(14);
        input.setTypeface(Typeface.MONOSPACE, Typeface.NORMAL);
        input.setMinLines(2);
        input.setMaxLines(4);
        input.setPadding(dp(14), dp(12), dp(14), dp(12));
        input.setBackground(createBox(COLOR_SURFACE, 14, COLOR_BORDER, 1));
        root.addView(input);

        // TOMBOL AKSI
        LinearLayout actionRow = new LinearLayout(this);
        actionRow.setOrientation(LinearLayout.HORIZONTAL);
        actionRow.setPadding(0, dp(8), 0, 0);

        Button cameraBtn = new Button(this);
        cameraBtn.setText("📷 Foto");
        cameraBtn.setTextSize(13);
        cameraBtn.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        cameraBtn.setTextColor(COLOR_TEXT_PRIMARY);
        cameraBtn.setBackground(createBox(COLOR_SURFACE_LIGHT, 12, COLOR_BORDER, 1));
        cameraBtn.setOnClickListener(v -> openImagePicker());

        Button sendBtn = new Button(this);
        sendBtn.setText("▶ RUN / KIRIM");
        sendBtn.setTextSize(13);
        sendBtn.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        sendBtn.setTextColor(Color.parseColor("#090D16"));
        sendBtn.setBackground(createBox(COLOR_ACCENT_CYAN, 12, COLOR_ACCENT_CYAN, 0));
        sendBtn.setOnClickListener(v -> sendMessage());

        LinearLayout.LayoutParams camParams = new LinearLayout.LayoutParams(0, dp(46), 1);
        camParams.setMargins(0, 0, dp(8), 0);
        actionRow.addView(cameraBtn, camParams);

        LinearLayout.LayoutParams sendParams = new LinearLayout.LayoutParams(0, dp(46), 2);
        actionRow.addView(sendBtn, sendParams);

        root.addView(actionRow);

        setContentView(root);
    }

    private void openImagePicker() {
        Intent pickIntent = new Intent(Intent.ACTION_GET_CONTENT);
        pickIntent.setType("image/*");

        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Intent chooserIntent = Intent.createChooser(pickIntent, "Pilih Sumber Materi Kode");
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

    private void typeWriter(TextView tv, String fullText, int index) {
        if (index <= fullText.length()) {
            tv.setText(fullText.substring(0, index));
            scroll.post(() -> scroll.fullScroll(View.FOCUS_DOWN));
            handler.postDelayed(() -> typeWriter(tv, fullText, index + 1), 16);
        }
    }

    private void addMessage(String speaker, String content, String base64Image) {
        addMessage(speaker, content, base64Image, false);
    }

    // GAYA BALON TERMINAL IDE MODERN
    private void addMessage(String speaker, String content, String base64Image, boolean animateTyping) {
        LinearLayout bubble = new LinearLayout(this);
        bubble.setOrientation(LinearLayout.VERTICAL);

        boolean human = "human".equalsIgnoreCase(speaker) || "user".equalsIgnoreCase(speaker);

        if (human) {
            bubble.setBackground(createBox(COLOR_SURFACE_LIGHT, 14, Color.parseColor("#475569"), 1));
        } else {
            bubble.setBackground(createBox(COLOR_SURFACE, 14, COLOR_BORDER, 1));
        }
        bubble.setPadding(dp(14), dp(12), dp(14), dp(12));

        // HEADER LABEL DI DALAM BALON
        TextView label = new TextView(this);
        label.setText(human ? "👤 DEVELOPER" : "⚡ OoeR MASTER AI");
        label.setTextSize(11);
        label.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        label.setTextColor(human ? COLOR_ACCENT_CYAN : COLOR_ACCENT_EMERALD);
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
                    iv.setPadding(0, dp(8), 0, dp(8));
                    bubble.addView(iv);
                }
            } catch (Exception ignored) {}
        }

        if (content != null && !content.isEmpty()) {
            TextView text = new TextView(this);
            text.setTextSize(14);
            text.setTextColor(COLOR_TEXT_PRIMARY);
            text.setTypeface(Typeface.MONOSPACE, Typeface.NORMAL);
            text.setLineSpacing(dp(3), 1.1f);
            text.setPadding(0, dp(4), 0, 0);
            bubble.addView(text);

            if (animateTyping) {
                text.setText("");
                typeWriter(text, content, 0);
            } else {
                text.setText(content);
            }
        }

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, dp(6), 0, dp(6));
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
        setStatus("● SYNCING • Memuat sesi " + id + "...");

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
                                        item.optString("image", null),
                                        false
                                );
                            }
                        }
                    }
                    setStatus("● READY • Terhubung ke Sesi #" + sessionId);
                });
            } catch (Exception e) {
                handler.post(() -> setStatus("✕ ERROR: " + e.getMessage()));
            } finally {
                handler.post(() -> busy = false);
            }
        }).start();
    }

    private void createSession() {
        if (busy) return;
        busy = true;
        setStatus("● INITIALIZING • Membuat sesi master baru...");

        new Thread(() -> {
            try {
                JSONObject res = request("/ai/training/sessions", "POST", new JSONObject());
                JSONObject session = res.optJSONObject("session");
                final String newId = session != null ? session.optString("id", "1") : "1";
                final String q = session != null ? session.optString("question", "OoeR Master IDE Siap. Ketik kode atau instruksi logika:") : "";

                handler.post(() -> {
                    sessionId = newId;
                    messages.removeAllViews();
                    clearSelectedPhoto();
                    addMessage("ai", q, null, false);
                    setStatus("● READY • Sesi Master #" + sessionId);
                });
            } catch (Exception e) {
                handler.post(() -> setStatus("✕ ERROR: " + e.getMessage()));
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

        addMessage("human", text, base64Img, false);
        addMessage("ai", "⚡ [EXECUTE] Menganalisis algoritma & logika kode...", null, false);
        setStatus("● EXECUTING • AI sedang memproses kode...");

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
                if (aiAnswer.isEmpty()) aiAnswer = res.optString("message", "Selesai dieksekusi!");

                final String reply = aiAnswer;

                handler.post(() -> {
                    int count = messages.getChildCount();
                    if (count > 0) {
                        messages.removeViewAt(count - 1);
                    }
                    addMessage("ai", reply, null, true);
                    setStatus("● READY • Sesi Master #" + sessionId);
                });
            } catch (Exception e) {
                handler.post(() -> {
                    int count = messages.getChildCount();
                    if (count > 0) {
                        messages.removeViewAt(count - 1);
                    }
                    addMessage("ai", "✕ [RUNTIME ERROR]: " + e.getMessage(), null, false);
                    setStatus("✕ Runtime Error!");
                });
            } finally {
                handler.post(() -> busy = false);
            }
        }).start();
    }
}
