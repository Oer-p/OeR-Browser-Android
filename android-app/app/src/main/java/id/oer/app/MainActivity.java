package id.oer.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.ClipboardManager;
import android.content.ClipData;
import android.provider.MediaStore;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
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
import android.widget.HorizontalScrollView;
import android.widget.Toast;
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
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class MainActivity extends Activity {

    private static final int REQ_PICK_IMAGE = 101;

    // TEMA WARNA MODERN (DARK STUDIO ALA CHATGPT/CLAUDE)
    private static final int COLOR_BG = Color.parseColor("#070A12");           // Deep Canvas
    private static final int COLOR_SURFACE = Color.parseColor("#0E1424");      // Card Base
    private static final int COLOR_SURFACE_LIGHT = Color.parseColor("#18233C");// Interactive Surface
    private static final int COLOR_CODE_BG = Color.parseColor("#040711");      // Terminal Code Black
    private static final int COLOR_ACCENT_CYAN = Color.parseColor("#38BDF8");  // Neon Cyan
    private static final int COLOR_ACCENT_EMERALD = Color.parseColor("#34D399");// Emerald
    private static final int COLOR_BORDER = Color.parseColor("#1E293B");       // Subtle Border
    private static final int COLOR_TEXT_PRIMARY = Color.parseColor("#F8FAFC"); // Crisp Text
    private static final int COLOR_TEXT_SECONDARY = Color.parseColor("#94A3B8");// Muted Text

    private final Handler handler = new Handler(Looper.getMainLooper());
    private SharedPreferences prefs;

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

    private GradientDrawable createBox(int bgColor, int cornerRadiusDp, int strokeColor, int strokeWidthDp) {
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(bgColor);
        gd.setCornerRadius(dp(cornerRadiusDp));
        if (strokeWidthDp > 0) gd.setStroke(dp(strokeWidthDp), strokeColor);
        return gd;
    }

    // PENGATURAN URL SERVER (ANTI BONGKAR PASANG APK)
    private String getApiUrl() {
        return prefs.getString("api_url", "https://sheer-framework-black-romantic.trycloudflare.com");
    }

    private void setApiUrl(String url) {
        prefs.edit().putString("api_url", url).apply();
    }

    private void setStatus(String text) {
        handler.post(() -> {
            if (status != null) status.setText(text);
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("ooer_pro_settings", MODE_PRIVATE);

        buildScreen();
        createSession();
    }

    private void buildScreen() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(COLOR_BG);
        root.setFitsSystemWindows(true);
        root.setPadding(dp(14), dp(10), dp(14), dp(14));

        // HEADER
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setPadding(dp(16), dp(12), dp(16), dp(12));
        header.setBackground(createBox(COLOR_SURFACE, 16, COLOR_BORDER, 1));

        LinearLayout topRow = new LinearLayout(this);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView iconTv = new TextView(this);
        iconTv.setText("⚡ ");
        iconTv.setTextSize(18);

        TextView logo = new TextView(this);
        logo.setText("OoeR Pro");
        logo.setTextColor(COLOR_ACCENT_CYAN);
        logo.setTextSize(20);
        logo.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);

        View spacer = new View(this);
        LinearLayout.LayoutParams spParams = new LinearLayout.LayoutParams(0, 1, 1);

        // TOMBOL SETTING SERVER (FITUR UTAMA ANTI BONGKAR PASANG)
        Button configBtn = new Button(this);
        configBtn.setText("⚙️ Server");
        configBtn.setTextSize(11);
        configBtn.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        configBtn.setTextColor(COLOR_ACCENT_CYAN);
        configBtn.setBackground(createBox(COLOR_SURFACE_LIGHT, 8, COLOR_ACCENT_CYAN, 1));
        configBtn.setOnClickListener(v -> showServerConfigDialog());
        LinearLayout.LayoutParams cfgParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(34));

        topRow.addView(iconTv);
        topRow.addView(logo);
        topRow.addView(spacer, spParams);
        topRow.addView(configBtn, cfgParams);
        header.addView(topRow);

        status = new TextView(this);
        status.setText("● READY • Terhubung ke Engine");
        status.setTextSize(11);
        status.setTextColor(COLOR_ACCENT_EMERALD);
        status.setTypeface(Typeface.MONOSPACE, Typeface.NORMAL);
        status.setPadding(0, dp(4), 0, 0);
        header.addView(status);

        root.addView(header);

        // TOOLBAR
        LinearLayout toolbar = new LinearLayout(this);
        toolbar.setOrientation(LinearLayout.HORIZONTAL);
        toolbar.setGravity(Gravity.CENTER_VERTICAL);
        toolbar.setPadding(0, dp(8), 0, dp(8));

        Button newBtn = new Button(this);
        newBtn.setText("＋ Sesi Baru");
        newBtn.setTextSize(12);
        newBtn.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        newBtn.setTextColor(COLOR_TEXT_PRIMARY);
        newBtn.setBackground(createBox(COLOR_SURFACE_LIGHT, 10, COLOR_BORDER, 1));
        newBtn.setOnClickListener(v -> createSession());

        Button syncBtn = new Button(this);
        syncBtn.setText("↺ Sync");
        syncBtn.setTextSize(12);
        syncBtn.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        syncBtn.setTextColor(COLOR_TEXT_SECONDARY);
        syncBtn.setBackground(createBox(COLOR_SURFACE, 10, COLOR_BORDER, 1));
        syncBtn.setOnClickListener(v -> loadSession(sessionId));

        LinearLayout.LayoutParams newParams = new LinearLayout.LayoutParams(0, dp(38), 1);
        newParams.setMargins(0, 0, dp(8), 0);
        toolbar.addView(newBtn, newParams);
        toolbar.addView(syncBtn, new LinearLayout.LayoutParams(dp(75), dp(38)));

        root.addView(toolbar);

        // CHAT LIST
        scroll = new ScrollView(this);
        scroll.setFillViewport(true);

        messages = new LinearLayout(this);
        messages.setOrientation(LinearLayout.VERTICAL);
        messages.setPadding(0, dp(4), 0, dp(10));
        scroll.addView(messages);

        root.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));

        // PRATINJAU FOTO
        previewContainer = new LinearLayout(this);
        previewContainer.setOrientation(LinearLayout.HORIZONTAL);
        previewContainer.setGravity(Gravity.CENTER_VERTICAL);
        previewContainer.setPadding(dp(8), dp(6), dp(8), dp(6));
        previewContainer.setBackground(createBox(COLOR_SURFACE, 10, COLOR_ACCENT_CYAN, 1));
        previewContainer.setVisibility(View.GONE);

        previewImage = new ImageView(this);
        previewImage.setLayoutParams(new LinearLayout.LayoutParams(dp(55), dp(55)));
        previewImage.setScaleType(ImageView.ScaleType.CENTER_CROP);

        Button removeImgBtn = new Button(this);
        removeImgBtn.setText("✕ Batal");
        removeImgBtn.setTextSize(11);
        removeImgBtn.setTextColor(Color.parseColor("#EF4444"));
        removeImgBtn.setBackground(createBox(Color.TRANSPARENT, 6, Color.parseColor("#EF4444"), 1));
        removeImgBtn.setOnClickListener(v -> clearSelectedPhoto());
        LinearLayout.LayoutParams rmParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(32));
        rmParams.setMargins(dp(12), 0, 0, 0);

        previewContainer.addView(previewImage);
        previewContainer.addView(removeImgBtn, rmParams);
        root.addView(previewContainer);

        // INPUT BOX
        input = new EditText(this);
        input.setHint("Ketik pertanyaan atau kode program...");
        input.setHintTextColor(COLOR_TEXT_SECONDARY);
        input.setTextColor(COLOR_TEXT_PRIMARY);
        input.setTextSize(14);
        input.setTypeface(Typeface.SANS_SERIF, Typeface.NORMAL);
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
        sendBtn.setTextColor(Color.parseColor("#070A12"));
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

    // DIALOG SETTING SERVER (PENGATURAN TANPA BONGKAR PASANG)
    private void showServerConfigDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("⚙️ Sambungkan Server AI");
        builder.setMessage("Masukkan link Cloudflare / URL backend baru di sini:");

        final EditText inputUrl = new EditText(this);
        inputUrl.setText(getApiUrl());
        inputUrl.setTextColor(COLOR_TEXT_PRIMARY);
        inputUrl.setTextSize(13);
        inputUrl.setBackground(createBox(COLOR_SURFACE_LIGHT, 8, COLOR_BORDER, 1));
        inputUrl.setPadding(dp(12), dp(10), dp(12), dp(10));

        LinearLayout container = new LinearLayout(this);
        container.setPadding(dp(20), dp(10), dp(20), dp(10));
        container.addView(inputUrl, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        builder.setView(container);

        builder.setPositiveButton("Simpan & Hubungkan", (dialog, which) -> {
            String val = inputUrl.getText().toString().trim();
            if (val.endsWith("/")) val = val.substring(0, val.length() - 1);
            setApiUrl(val);
            Toast.makeText(this, "✅ Server berhasil diperbarui!", Toast.LENGTH_SHORT).show();
            createSession();
        });
        builder.setNegativeButton("Batal", null);
        builder.show();
    }

    // PEWARNA KODE OTOMATIS (SYNTAX HIGHLIGHTER)
    private SpannableString highlightCode(String code) {
        SpannableString span = new SpannableString(code);
        // Strings (Hijau Emerald)
        applyColor(span, "\"[^\"]*\"|'[^']*'", Color.parseColor("#34D399"));
        // Angka (Oranye)
        applyColor(span, "\\b\\d+(\\.\\d+)?\\b", Color.parseColor("#FB923C"));
        // Kata Kunci Pemrograman (Neon Cyan)
        String kw = "\\b(def|return|if|else|elif|for|while|import|from|class|try|except|function|let|const|var|switch|case|break|continue|async|await|pass|True|False|null|undefined|print|public|private|static|void|int|String|new|this)\\b";
        applyColor(span, kw, Color.parseColor("#38BDF8"));
        // Komentar (Muted Gray)
        applyColor(span, "(#.*|//.*)", Color.parseColor("#64748B"));
        return span;
    }

    private void applyColor(SpannableString span, String regex, int color) {
        Matcher m = Pattern.compile(regex).matcher(span);
        while (m.find()) {
            span.setSpan(new ForegroundColorSpan(color), m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    // PEMBUAT KOTAK KODE DENGAN TOMBOL COPY
    private View createCodeBlockView(String lang, String codeContent) {
        LinearLayout codeCard = new LinearLayout(this);
        codeCard.setOrientation(LinearLayout.VERTICAL);
        codeCard.setBackground(createBox(COLOR_CODE_BG, 12, COLOR_BORDER, 1));
        codeCard.setPadding(0, 0, 0, dp(10));

        // Header Blok Kode
        LinearLayout codeHeader = new LinearLayout(this);
        codeHeader.setOrientation(LinearLayout.HORIZONTAL);
        codeHeader.setGravity(Gravity.CENTER_VERTICAL);
        codeHeader.setBackground(createBox(COLOR_SURFACE, 12, Color.TRANSPARENT, 0));
        codeHeader.setPadding(dp(12), dp(8), dp(12), dp(8));

        TextView langLabel = new TextView(this);
        langLabel.setText("</> " + lang.toUpperCase());
        langLabel.setTextSize(11);
        langLabel.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        langLabel.setTextColor(COLOR_TEXT_SECONDARY);

        View spacer = new View(this);
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(0, 1, 1);

        // TOMBOL SALIN KODE KHUSUS
        Button copyCodeBtn = new Button(this);
        copyCodeBtn.setText("📋 Salin Kode");
        copyCodeBtn.setTextSize(11);
        copyCodeBtn.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        copyCodeBtn.setTextColor(COLOR_ACCENT_CYAN);
        copyCodeBtn.setBackground(createBox(COLOR_SURFACE_LIGHT, 6, COLOR_BORDER, 1));
        copyCodeBtn.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Code", codeContent);
            clipboard.setPrimaryClip(clip);
            copyCodeBtn.setText("✅ Tersalin!");
            copyCodeBtn.setTextColor(COLOR_ACCENT_EMERALD);
            handler.postDelayed(() -> {
                copyCodeBtn.setText("📋 Salin Kode");
                copyCodeBtn.setTextColor(COLOR_ACCENT_CYAN);
            }, 2500);
        });

        codeHeader.addView(langLabel);
        codeHeader.addView(spacer, sp);
        codeHeader.addView(copyCodeBtn, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(32)));
        codeCard.addView(codeHeader);

        // Area Kode (Horizontal Scroll jika baris panjang)
        HorizontalScrollView hScroll = new HorizontalScrollView(this);
        hScroll.setPadding(dp(14), dp(10), dp(14), 0);

        TextView codeTv = new TextView(this);
        codeTv.setText(highlightCode(codeContent));
        codeTv.setTextSize(13);
        codeTv.setTextColor(COLOR_TEXT_PRIMARY);
        codeTv.setTypeface(Typeface.MONOSPACE, Typeface.NORMAL);
        codeTv.setLineSpacing(dp(4), 1.2f);
        codeTv.setTextIsSelectable(true);

        hScroll.addView(codeTv);
        codeCard.addView(hScroll);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, dp(8), 0, dp(8));
        codeCard.setLayoutParams(params);
        return codeCard;
    }

    // ADD MESSAGE
    private void addMessage(String speaker, String content, String base64Image) {
        LinearLayout bubble = new LinearLayout(this);
        bubble.setOrientation(LinearLayout.VERTICAL);

        boolean human = "human".equalsIgnoreCase(speaker) || "user".equalsIgnoreCase(speaker);
        bubble.setBackground(createBox(human ? COLOR_SURFACE_LIGHT : COLOR_SURFACE, 14, COLOR_BORDER, 1));
        bubble.setPadding(dp(14), dp(12), dp(14), dp(12));

        // HEADER
        TextView label = new TextView(this);
        label.setText(human ? "👤 DEVELOPER" : "⚡ OoeR PRO AI");
        label.setTextSize(11);
        label.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        label.setTextColor(human ? COLOR_ACCENT_CYAN : COLOR_ACCENT_EMERALD);
        bubble.addView(label);

        // GAMBAR
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

        // PARSER TEKS & BLOK KODE
        if (content != null && !content.isEmpty()) {
            if (content.contains("```")) {
                String[] parts = content.split("```");
                for (int i = 0; i < parts.length; i++) {
                    String part = parts[i];
                    if (i % 2 == 1) { // BLOK KODE
                        String lang = "CODE";
                        String codeBody = part;
                        int firstNl = part.indexOf("\n");
                        if (firstNl != -1) {
                            String fLine = part.substring(0, firstNl).trim();
                            if (!fLine.isEmpty() && fLine.length() < 15 && !fLine.contains(" ")) {
                                lang = fLine;
                                codeBody = part.substring(firstNl + 1);
                            }
                        }
                        bubble.addView(createCodeBlockView(lang, codeBody.trim()));
                    } else { // TEKS BIASA
                        if (!part.trim().isEmpty()) {
                            TextView regularText = new TextView(this);
                            regularText.setText(part.trim());
                            regularText.setTextSize(14);
                            regularText.setTextColor(COLOR_TEXT_PRIMARY);
                            regularText.setLineSpacing(dp(3), 1.15f);
                            regularText.setPadding(0, dp(4), 0, dp(4));
                            regularText.setTextIsSelectable(true);
                            bubble.addView(regularText);
                        }
                    }
                }
            } else { // TEKS MURNI
                TextView text = new TextView(this);
                text.setText(content);
                text.setTextSize(14);
                text.setTextColor(COLOR_TEXT_PRIMARY);
                text.setLineSpacing(dp(3), 1.15f);
                text.setPadding(0, dp(4), 0, 0);
                text.setTextIsSelectable(true);
                bubble.addView(text);
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
        float ratio = Math.min(maxImageSize / realImage.getWidth(), maxImageSize / realImage.getHeight());
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

    private JSONObject request(String path, String method, JSONObject body) throws Exception {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(getApiUrl() + path);
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
                                        item.optString("image", null)
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
        setStatus("● INITIALIZING • Membuat sesi...");

        new Thread(() -> {
            try {
                JSONObject res = request("/ai/training/sessions", "POST", new JSONObject());
                JSONObject session = res.optJSONObject("session");
                final String newId = session != null ? session.optString("id", "1") : "1";
                final String q = session != null ? session.optString("question", "OoeR Pro Master Siap. Masukkan instruksi kode:") : "";

                handler.post(() -> {
                    sessionId = newId;
                    messages.removeAllViews();
                    clearSelectedPhoto();
                    addMessage("ai", q, null);
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

        addMessage("human", text, base64Img);
        addMessage("ai", "⚡ [EXECUTE] Menganalisis algoritma & logika...", null);
        setStatus("● EXECUTING • AI sedang memproses...");

        new Thread(() -> {
            try {
                JSONObject payload = new JSONObject();
                payload.put("content", text);
                if (base64Img != null) payload.put("image", base64Img);

                JSONObject res = request("/ai/training/sessions/" + sessionId + "/messages", "POST", payload);

                String aiAnswer = res.optString("answer", "");
                if (aiAnswer.isEmpty()) aiAnswer = res.optString("reply", "");
                if (aiAnswer.isEmpty()) aiAnswer = res.optString("message", "Selesai dieksekusi!");

                final String reply = aiAnswer;

                handler.post(() -> {
                    int count = messages.getChildCount();
                    if (count > 0) messages.removeViewAt(count - 1);
                    addMessage("ai", reply, null);
                    setStatus("● READY • Sesi Master #" + sessionId);
                });
            } catch (Exception e) {
                handler.post(() -> {
                    int count = messages.getChildCount();
                    if (count > 0) messages.removeViewAt(count - 1);
                    addMessage("ai", "✕ [RUNTIME ERROR]: " + e.getMessage(), null);
                    setStatus("✕ Runtime Error!");
                });
            } finally {
                handler.post(() -> busy = false);
            }
        }).start();
    }
}
