
package id.oer.browser;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.Paint;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.GradientDrawable;
import android.util.Base64;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {

    private static final String SUPABASE_URL =
            "https://jhopfzmyxuafdbwwkjbf.supabase.co";

    private static final String SUPABASE_KEY =
            "sb_publishable_FE4-8ZuWD3FgKCcjxjaErg_w97N3fwk";

    private static final String PREFS = "oer_auth";
    private static final String ACCESS = "access_token";
    private static final String REFRESH = "refresh_token";
    private static final String EMAIL = "email";
    private static final String PKCE_VERIFIER = "pkce_verifier";

    private static final String GOOGLE_REDIRECT =
            "id.oer.browser://login-callback/";

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    private EditText addressInput;
    private EditText homeSearchInput;
    private EditText htmlInput;

    private OeRRenderView renderView;
    private WebView webView;

    private LinearLayout root;
    private LinearLayout addressRow;
    private LinearLayout navRow;
    private LinearLayout homePanel;
    private LinearLayout htmlPanel;
    private FrameLayout contentFrame;
    private Button accountButton;

    private final int black = Color.rgb(5, 5, 5);
    private final int dark = Color.rgb(23, 23, 23);
    private final int card = Color.rgb(32, 33, 36);
    private final int green = Color.rgb(54, 226, 121);
    private final int white = Color.WHITE;
    private final int gray = Color.rgb(165, 165, 165);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(black);
        getWindow().setNavigationBarColor(black);

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(black);

        createHeader();
        createAddressBar();
        createNavigation();
        createContent();

        setContentView(root);
        updateAccountButton();
        showHome();

        handleAuthCallback(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleAuthCallback(intent);
    }

    private int dp(float value) {
        return (int) (value * getResources()
                .getDisplayMetrics().density + 0.5f);
    }

    private GradientDrawable rounded(int color, float radius) {
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(color);
        shape.setCornerRadius(dp(radius));
        return shape;
    }

    private TextView makeText(String text, int size, int color) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextSize(size);
        view.setTextColor(color);
        return view;
    }

    private Button makeButton(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextColor(white);
        button.setTextSize(13);
        button.setAllCaps(false);
        button.setMinWidth(0);
        button.setMinimumWidth(0);
        button.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(dark));
        return button;
    }

    private EditText makeInput(String hint) {
        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setTextSize(16);
        input.setTextColor(white);
        input.setHintTextColor(gray);
        input.setHint(hint);
        input.setPadding(dp(4), 0, dp(4), 0);
        input.setBackgroundColor(Color.TRANSPARENT);
        input.setImeOptions(EditorInfo.IME_ACTION_GO);
        return input;
    }

    // ==================== LOGO ====================

    private class RainbowTextView extends TextView {
        public RainbowTextView(String text, int size) {
            super(MainActivity.this);
            setText(text);
            setTextSize(size);
            setTypeface(null, Typeface.BOLD);
            setGravity(Gravity.CENTER);
            setIncludeFontPadding(false);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            if (getWidth() > 0) {
                getPaint().setShader(new LinearGradient(
                        0, 0, getWidth(), 0,
                        new int[]{
                                Color.rgb(255, 65, 75),
                                Color.rgb(255, 180, 45),
                                Color.rgb(255, 230, 65),
                                Color.rgb(54, 226, 121),
                                Color.rgb(55, 140, 255),
                                Color.rgb(190, 90, 255)
                        },
                        null,
                        Shader.TileMode.CLAMP
                ));
            }
            super.onDraw(canvas);
            getPaint().setShader(null);
        }
    }

    private void addLogo(LinearLayout parent, int size) {
        LinearLayout logo = new LinearLayout(this);
        logo.setOrientation(LinearLayout.HORIZONTAL);
        logo.setGravity(Gravity.CENTER);

        logo.addView(new RainbowTextView("O", size));

        TextView oe = makeText("oe", size, white);
        oe.setTypeface(null, Typeface.BOLD);
        oe.setIncludeFontPadding(false);
        logo.addView(oe);

        TextView r = makeText("R", size, green);
        r.setTypeface(null, Typeface.BOLD);
        r.setIncludeFontPadding(false);
        logo.addView(r);

        parent.addView(logo);
    }

    // ==================== IKON ====================

    private class SearchIcon extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        public SearchIcon() {
            super(MainActivity.this);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            float cx = getWidth() * 0.42f;
            float cy = getHeight() * 0.42f;
            float radius = Math.min(getWidth(), getHeight()) * 0.28f;

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(2));
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setColor(Color.rgb(180, 180, 180));

            canvas.drawCircle(cx, cy, radius, paint);
            canvas.drawLine(
                    cx + radius * 0.72f,
                    cy + radius * 0.72f,
                    cx + radius * 1.45f,
                    cy + radius * 1.45f,
                    paint
            );
        }
    }

    private View createMicIcon() {
        return new View(this) {
            private final Paint paint =
                    new Paint(Paint.ANTI_ALIAS_FLAG);

            @Override
            protected void onDraw(Canvas canvas) {
                float cx = getWidth() / 2f;
                float cy = getHeight() / 2f - dp(1);

                paint.setColor(Color.rgb(180, 180, 180));
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(dp(2));
                paint.setStrokeCap(Paint.Cap.ROUND);

                canvas.drawRoundRect(
                        cx - dp(5.5f), cy - dp(11),
                        cx + dp(5.5f), cy + dp(4),
                        dp(6), dp(6), paint
                );
                canvas.drawArc(
                        cx - dp(11), cy - dp(5),
                        cx + dp(11), cy + dp(13),
                        0, 180, false, paint
                );
                canvas.drawLine(
                        cx, cy + dp(13), cx, cy + dp(18), paint
                );
                canvas.drawLine(
                        cx - dp(6), cy + dp(18),
                        cx + dp(6), cy + dp(18), paint
                );
            }
        };
    }

    // ==================== AKUN SUPABASE ====================

    private void createHeader() {
        accountButton = makeButton("Akun");
        accountButton.setTextColor(green);
        accountButton.setTextSize(15);
        accountButton.setTypeface(null, Typeface.BOLD);
        accountButton.setGravity(Gravity.CENTER);
        accountButton.setPadding(dp(20), 0, dp(20), 0);
        accountButton.setMinHeight(0);
        accountButton.setMinimumHeight(0);
        accountButton.setBackground(rounded(dark, 28));
        accountButton.setOnClickListener(v -> showAccountDialog());
    }

    private String saved(String key) {
        return getSharedPreferences(PREFS, MODE_PRIVATE)
                .getString(key, "");
    }

    private void updateAccountButton() {
        String email = saved(EMAIL);
        accountButton.setText(
                email.isEmpty() ? "Akun" : "Akun ✓"
        );
    }

    private void showAccountDialog() {
        String email = saved(EMAIL);

        if (!email.isEmpty() && !saved(ACCESS).isEmpty()) {
            new AlertDialog.Builder(this)
                    .setTitle("Akun OeR")
                    .setMessage("Login sebagai:\n" + email)
                    .setPositiveButton("Keluar", (d, w) -> logout())
                    .setNegativeButton("Tutup", null)
                    .show();
            return;
        }

        String[] options = {
                "Login",
                "Daftar akun",
                "Login dengan Google"
        };

        new AlertDialog.Builder(this)
                .setTitle("Akun OeR")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        showEmailForm(false);
                    } else if (which == 1) {
                        showEmailForm(true);
                    } else {
                        startGoogleLogin();
                    }
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void showEmailForm(boolean signup) {
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(22), dp(8), dp(22), dp(4));

        EditText emailInput = new EditText(this);
        emailInput.setSingleLine(true);
        emailInput.setHint("Email");
        emailInput.setInputType(
                android.text.InputType.TYPE_CLASS_TEXT
                        | android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        );
        form.addView(emailInput);

        EditText passwordInput = new EditText(this);
        passwordInput.setSingleLine(true);
        passwordInput.setHint("Kata sandi");
        passwordInput.setInputType(
                android.text.InputType.TYPE_CLASS_TEXT
                        | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        );
        form.addView(passwordInput);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(signup ? "Daftar akun OeR" : "Login OeR")
                .setView(form)
                .setPositiveButton(signup ? "Daftar" : "Login", null)
                .setNegativeButton("Batal", null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(
                AlertDialog.BUTTON_POSITIVE
        ).setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this,
                        "Email dan kata sandi wajib diisi",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(this,
                        "Kata sandi minimal 6 karakter",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            dialog.dismiss();
            emailAuth(email, password, signup);
        }));

        dialog.show();
    }

    private void emailAuth(
            String email, String password, boolean signup
    ) {
        Toast.makeText(this, "Menghubungkan ke Supabase...",
                Toast.LENGTH_SHORT).show();

        executor.execute(() -> {
            HttpURLConnection connection = null;

            try {
                String endpoint = signup
                        ? "/auth/v1/signup"
                        : "/auth/v1/token?grant_type=password";

                URL url = new URL(SUPABASE_URL + endpoint);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(15000);
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type",
                        "application/json");
                connection.setRequestProperty("apikey", SUPABASE_KEY);

                JSONObject body = new JSONObject();
                body.put("email", email);
                body.put("password", password);

                byte[] bytes = body.toString().getBytes(
                        StandardCharsets.UTF_8);

                try (OutputStream out = connection.getOutputStream()) {
                    out.write(bytes);
                }

                int status = connection.getResponseCode();
                InputStream stream = status >= 200 && status < 300
                        ? connection.getInputStream()
                        : connection.getErrorStream();

                String response = readStream(stream);
                JSONObject json = new JSONObject(response);

                if (status >= 200 && status < 300) {
                    String access = json.optString("access_token", "");
                    String refresh = json.optString("refresh_token", "");

                    if (!access.isEmpty()) {
                        getSharedPreferences(PREFS, MODE_PRIVATE)
                                .edit()
                                .putString(ACCESS, access)
                                .putString(REFRESH, refresh)
                                .putString(EMAIL, email)
                                .apply();

                        runOnUiThread(() -> {
                            updateAccountButton();
                            Toast.makeText(this,
                                    "Login berhasil",
                                    Toast.LENGTH_LONG).show();
                        });
                    } else if (signup) {
                        runOnUiThread(() -> new AlertDialog.Builder(this)
                                .setTitle("Periksa email")
                                .setMessage(
                                        "Pendaftaran diterima. Jika konfirmasi " +
                                        "email diaktifkan di Supabase, buka email " +
                                        "untuk mengonfirmasi akun lalu login."
                                )
                                .setPositiveButton("Mengerti", null)
                                .show());
                    } else {
                        showAuthError(
                                "Login belum menghasilkan sesi. Periksa email " +
                                "dan pengaturan autentikasi Supabase."
                        );
                    }
                } else {
                    String message = json.optString("msg",
                            json.optString("message",
                                    json.optString("error_description",
                                            "Permintaan gagal (" + status + ")")));
                    showAuthError(message);
                }

            } catch (Exception e) {
                showAuthError(
                        "Tidak dapat terhubung. Periksa internet dan konfigurasi Supabase."
                );
            } finally {
                if (connection != null) connection.disconnect();
            }
        });
    }

    private String readStream(InputStream stream) throws Exception {
        if (stream == null) return "{}";

        StringBuilder result = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
        }

        return result.toString();
    }

    private void showAuthError(String message) {
        runOnUiThread(() -> {
            if (isFinishing() || isDestroyed()) return;

            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Autentikasi gagal")
                    .setMessage(message)
                    .setPositiveButton("Tutup", null)
                    .show();
        });
    }

    private void logout() {
        getSharedPreferences(PREFS, MODE_PRIVATE)
                .edit().clear().apply();

        updateAccountButton();

        Toast.makeText(this, "Kamu telah keluar dari akun",
                Toast.LENGTH_SHORT).show();
    }

    // ==================== GOOGLE OAUTH PKCE ====================

    private void startGoogleLogin() {
        executor.execute(() -> {
            try {
                byte[] randomBytes = new byte[32];
                new SecureRandom().nextBytes(randomBytes);

                String verifier = Base64.encodeToString(
                        randomBytes,
                        Base64.URL_SAFE
                                | Base64.NO_WRAP
                                | Base64.NO_PADDING
                );

                MessageDigest digest =
                        MessageDigest.getInstance("SHA-256");

                byte[] challengeBytes = digest.digest(
                        verifier.getBytes(StandardCharsets.US_ASCII)
                );

                String challenge = Base64.encodeToString(
                        challengeBytes,
                        Base64.URL_SAFE
                                | Base64.NO_WRAP
                                | Base64.NO_PADDING
                );

                getSharedPreferences(PREFS, MODE_PRIVATE)
                        .edit()
                        .putString(PKCE_VERIFIER, verifier)
                        .apply();

                String authUrl = SUPABASE_URL
                        + "/auth/v1/authorize?provider=google"
                        + "&redirect_to="
                        + URLEncoder.encode(GOOGLE_REDIRECT, "UTF-8")
                        + "&code_challenge="
                        + URLEncoder.encode(challenge, "UTF-8")
                        + "&code_challenge_method=s256"
                        + "&apikey="
                        + URLEncoder.encode(SUPABASE_KEY, "UTF-8");

                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) return;

                    try {
                        Intent browserIntent = new Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(authUrl)
                        );
                        startActivity(browserIntent);
                    } catch (Exception e) {
                        showAuthError(
                                "Tidak dapat membuka browser untuk login Google."
                        );
                    }
                });

            } catch (Exception e) {
                showAuthError(
                        "Gagal menyiapkan autentikasi Google."
                );
            }
        });
    }

    private void handleAuthCallback(Intent intent) {
        if (intent == null || intent.getData() == null) return;

        Uri uri = intent.getData();

        if (!"id.oer.browser".equals(uri.getScheme())
                || !"login-callback".equals(uri.getHost())) {
            return;
        }

        String error = uri.getQueryParameter("error_description");

        if (error == null || error.isEmpty()) {
            error = uri.getQueryParameter("error");
        }

        if (error != null && !error.isEmpty()) {
            showAuthError("Login Google gagal: " + error);
            return;
        }

        String code = uri.getQueryParameter("code");

        if (code == null || code.isEmpty()) {
            showAuthError(
                    "Kode login tidak ditemukan. Silakan coba login Google lagi."
            );
            return;
        }

        exchangeGoogleCode(code);
    }

    private void exchangeGoogleCode(String code) {
        String verifier = saved(PKCE_VERIFIER);

        if (verifier.isEmpty()) {
            showAuthError(
                    "Data verifikasi login tidak ditemukan. " +
                    "Silakan ulangi login Google dari aplikasi."
            );
            return;
        }

        executor.execute(() -> {
            HttpURLConnection connection = null;

            try {
                URL url = new URL(
                        SUPABASE_URL
                                + "/auth/v1/token?grant_type=pkce"
                );

                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(15000);
                connection.setDoOutput(true);
                connection.setRequestProperty(
                        "Content-Type", "application/json"
                );
                connection.setRequestProperty("apikey", SUPABASE_KEY);

                JSONObject body = new JSONObject();
                body.put("auth_code", code);
                body.put("code_verifier", verifier);

                byte[] bytes = body.toString().getBytes(
                        StandardCharsets.UTF_8
                );

                try (OutputStream out = connection.getOutputStream()) {
                    out.write(bytes);
                }

                int status = connection.getResponseCode();

                InputStream stream = status >= 200 && status < 300
                        ? connection.getInputStream()
                        : connection.getErrorStream();

                String response = readStream(stream);
                JSONObject json = new JSONObject(response);

                if (status >= 200 && status < 300) {
                    String access = json.optString("access_token", "");
                    String refresh = json.optString("refresh_token", "");

                    JSONObject user = json.optJSONObject("user");
                    String email = user == null
                            ? ""
                            : user.optString("email", "");

                    if (access.isEmpty() || refresh.isEmpty()) {
                        showAuthError(
                                "Supabase belum mengembalikan sesi lengkap."
                        );
                        return;
                    }

                    getSharedPreferences(PREFS, MODE_PRIVATE)
                            .edit()
                            .putString(ACCESS, access)
                            .putString(REFRESH, refresh)
                            .putString(EMAIL, email)
                            .remove(PKCE_VERIFIER)
                            .apply();

                    runOnUiThread(() -> {
                        if (isFinishing() || isDestroyed()) return;

                        updateAccountButton();
                        showHome();

                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("Login berhasil")
                                .setMessage(
                                        email.isEmpty()
                                                ? "Kamu berhasil login dengan Google."
                                                : "Login sebagai:\n" + email
                                )
                                .setPositiveButton("Oke", null)
                                .show();
                    });

                } else {
                    String message = json.optString(
                            "msg",
                            json.optString(
                                    "message",
                                    json.optString(
                                            "error_description",
                                            "Pertukaran kode login gagal ("
                                                    + status + ")."
                                    )
                            )
                    );

                    showAuthError(message);
                }

            } catch (Exception e) {
                showAuthError(
                        "Tidak dapat menyelesaikan login Google. " +
                        "Periksa koneksi internet dan konfigurasi Supabase."
                );
            } finally {
                if (connection != null) connection.disconnect();
            }
        });
    }

    // ==================== ADDRESS BAR ====================

    private void createAddressBar() {
        addressRow = new LinearLayout(this);
        addressRow.setGravity(Gravity.CENTER_VERTICAL);
        addressRow.setPadding(dp(12), dp(6), dp(12), dp(6));
        addressRow.setBackgroundColor(black);

        addressInput = makeInput("Cari atau masukkan alamat...");
        addressRow.addView(addressInput,
                new LinearLayout.LayoutParams(0, dp(48), 1));

        Button open = makeButton("Buka");
        open.setTextColor(green);
        addressRow.addView(open);
        open.setOnClickListener(v -> openAddress(addressInput));

        addressInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO
                    || actionId == EditorInfo.IME_ACTION_DONE) {
                openAddress(addressInput);
                return true;
            }
            return false;
        });

        root.addView(addressRow);
        addressRow.setVisibility(View.GONE);
    }

    // ==================== NAVIGASI ====================

    private void createNavigation() {
        navRow = new LinearLayout(this);
        navRow.setGravity(Gravity.CENTER);
        navRow.setBackgroundColor(black);

        Button back = makeButton("‹ Kembali");
        Button refresh = makeButton("↻ Muat ulang");
        Button home = makeButton("⌂ Beranda");
        Button editor = makeButton("</> HTML");

        navRow.addView(back,
                new LinearLayout.LayoutParams(0, dp(48), 1));
        navRow.addView(refresh,
                new LinearLayout.LayoutParams(0, dp(48), 1));
        navRow.addView(home,
                new LinearLayout.LayoutParams(0, dp(48), 1));
        navRow.addView(editor,
                new LinearLayout.LayoutParams(0, dp(48), 1));

        back.setOnClickListener(v -> {
            if (webView.canGoBack()) webView.goBack();
            else showHome();
        });

        refresh.setOnClickListener(v -> webView.reload());
        home.setOnClickListener(v -> showHome());
        editor.setOnClickListener(v -> showEditor());

        root.addView(navRow);
        navRow.setVisibility(View.GONE);
    }

    // ==================== KONTEN ====================

    private void createContent() {
        contentFrame = new FrameLayout(this);

        homePanel = new LinearLayout(this);
        homePanel.setOrientation(LinearLayout.VERTICAL);
        homePanel.setGravity(Gravity.CENTER_HORIZONTAL);
        homePanel.setBackgroundColor(black);

        View topSpace = new View(this);
        homePanel.addView(topSpace,
                new LinearLayout.LayoutParams(1, 0, 1f));

        addLogo(homePanel, 76);

        TextView subtitle = makeText(
                "Jelajahi dunia dengan OeR", 14, gray);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, dp(8), 0, dp(16));
        homePanel.addView(subtitle);

        LinearLayout searchBar = new LinearLayout(this);
        searchBar.setOrientation(LinearLayout.HORIZONTAL);
        searchBar.setGravity(Gravity.CENTER_VERTICAL);
        searchBar.setPadding(dp(12), 0, dp(10), 0);
        searchBar.setBackground(rounded(card, 32));

        SearchIcon searchIcon = new SearchIcon();
        searchBar.addView(searchIcon,
                new LinearLayout.LayoutParams(dp(32), dp(42)));

        homeSearchInput = makeInput("Cari apa");
        homeSearchInput.setPadding(dp(6), 0, dp(4), 0);
        searchBar.addView(homeSearchInput,
                new LinearLayout.LayoutParams(0, dp(54), 1));

        View mic = createMicIcon();
        searchBar.addView(mic,
                new LinearLayout.LayoutParams(dp(34), dp(42)));
        mic.setOnClickListener(v -> Toast.makeText(this,
                "Fitur mikrofon akan dikembangkan",
                Toast.LENGTH_SHORT).show());

        Button sendBtn = new Button(this);
        sendBtn.setText("↑");
        sendBtn.setTextSize(20);
        sendBtn.setTypeface(null, Typeface.BOLD);
        sendBtn.setTextColor(Color.BLACK);
        sendBtn.setAllCaps(false);
        sendBtn.setGravity(Gravity.CENTER);
        sendBtn.setPadding(0, 0, 0, dp(2));
        sendBtn.setMinWidth(0);
        sendBtn.setMinimumWidth(0);
        sendBtn.setMinimumHeight(0);
        sendBtn.setBackground(rounded(green, 22));
        sendBtn.setStateListAnimator(null);

        searchBar.addView(sendBtn,
                new LinearLayout.LayoutParams(dp(42), dp(42)));

        LinearLayout.LayoutParams searchParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, dp(64));
        searchParams.setMargins(dp(16), 0, dp(16), 0);
        homePanel.addView(searchBar, searchParams);

        sendBtn.setOnClickListener(v -> openAddress(homeSearchInput));
        homeSearchInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO
                    || actionId == EditorInfo.IME_ACTION_DONE) {
                openAddress(homeSearchInput);
                return true;
            }
            return false;
        });

        View bottomSpace = new View(this);
        homePanel.addView(bottomSpace,
                new LinearLayout.LayoutParams(1, 0, 1f));

        webView = new WebView(this);
        webView.setBackgroundColor(Color.WHITE);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.setWebViewClient(new WebViewClient());

        htmlPanel = new LinearLayout(this);
        htmlPanel.setOrientation(LinearLayout.VERTICAL);
        htmlPanel.setPadding(dp(16), dp(12), dp(16), dp(12));
        htmlPanel.setBackgroundColor(black);

        TextView htmlTitle = makeText("Editor HTML OeR", 18, green);
        htmlPanel.addView(htmlTitle);

        htmlInput = new EditText(this);
        htmlInput.setHint("Masukkan kode HTML di sini");
        htmlInput.setHintTextColor(gray);
        htmlInput.setTextColor(white);
        htmlInput.setGravity(Gravity.TOP | Gravity.START);
        htmlInput.setMinLines(5);
        htmlInput.setPadding(dp(14), dp(14), dp(14), dp(14));
        htmlInput.setBackground(rounded(dark, 12));
        htmlInput.setText(
                "<h1>Selamat datang di OeR</h1>\n" +
                "<p>Ini adalah mesin render HTML OeR.</p>\n" +
                "<h2>Fitur awal</h2>\n" +
                "<ul>\n<li>Judul</li>\n<li>Paragraf</li>\n" +
                "<li>Daftar</li>\n</ul>\n<hr>\n" +
                "<p>Mesin akan terus dikembangkan.</p>"
        );

        htmlPanel.addView(htmlInput,
                new LinearLayout.LayoutParams(-1, 0, 1));

        Button renderButton = makeButton("Tampilkan HTML");
        renderButton.setTextColor(green);
        htmlPanel.addView(renderButton);

        TextView resultTitle = makeText("Hasil render:", 15, white);
        resultTitle.setPadding(0, dp(12), 0, dp(8));
        htmlPanel.addView(resultTitle);

        ScrollView resultScroll = new ScrollView(this);
        resultScroll.setBackgroundColor(Color.WHITE);
        resultScroll.setPadding(dp(12), dp(12), dp(12), dp(12));

        renderView = new OeRRenderView(this);
        resultScroll.addView(renderView);
        htmlPanel.addView(resultScroll,
                new LinearLayout.LayoutParams(-1, 0, 1));

        renderButton.setOnClickListener(v ->
                renderView.renderHtml(htmlInput.getText().toString()));

        contentFrame.addView(homePanel,
                new FrameLayout.LayoutParams(-1, -1));
        contentFrame.addView(webView,
                new FrameLayout.LayoutParams(-1, -1));
        contentFrame.addView(htmlPanel,
                new FrameLayout.LayoutParams(-1, -1));

        FrameLayout.LayoutParams accountParams =
                new FrameLayout.LayoutParams(
                        dp(100), dp(44), Gravity.TOP | Gravity.LEFT);
        accountParams.leftMargin = dp(16);
        accountParams.topMargin = dp(12);
        contentFrame.addView(accountButton, accountParams);

        root.addView(contentFrame,
                new LinearLayout.LayoutParams(-1, 0, 1));
    }

    // ==================== TAMPILAN ====================

    private void showHome() {
        homePanel.setVisibility(View.VISIBLE);
        webView.setVisibility(View.GONE);
        htmlPanel.setVisibility(View.GONE);
        addressRow.setVisibility(View.GONE);
        navRow.setVisibility(View.GONE);
        accountButton.setVisibility(View.VISIBLE);
    }

    private void showBrowser() {
        homePanel.setVisibility(View.GONE);
        webView.setVisibility(View.VISIBLE);
        htmlPanel.setVisibility(View.GONE);
        addressRow.setVisibility(View.VISIBLE);
        navRow.setVisibility(View.VISIBLE);
        accountButton.setVisibility(View.GONE);
    }

    private void showEditor() {
        homePanel.setVisibility(View.GONE);
        webView.setVisibility(View.GONE);
        htmlPanel.setVisibility(View.VISIBLE);
        addressRow.setVisibility(View.GONE);
        navRow.setVisibility(View.GONE);
        accountButton.setVisibility(View.GONE);
        renderView.renderHtml(htmlInput.getText().toString());
    }

    // ==================== PENCARIAN ====================


private void openAddress(EditText input) {
    String query = input.getText().toString().trim();

    if (query.isEmpty()) {
        Toast.makeText(this, "Masukkan alamat atau pencarian",
                Toast.LENGTH_SHORT).show();
        return;
    }

    // Tombol pencarian beranda membuka OeR Search.
    if (input == homeSearchInput) {
        showBrowser();

        // Sembunyikan kolom alamat dan navigasi atas.
        addressInput.setVisibility(View.GONE);
        navRow.setVisibility(View.GONE);

        webView.loadUrl(
                "file:///android_asset/search.html?q="
                        + Uri.encode(query)
        );
        return;
    }

    // Alamat website tetap bisa dibuka seperti biasa.
    String url;

    if (query.matches("(?i)^[a-z][a-z0-9+.-]*://.*")) {
        url = query;
    } else if (query.matches(
            "(?i)^[a-z0-9.-]+\\.[a-z]{2,}(:\\d+)?(/.*)?$")) {
        url = "https://" + query;
    } else {
        url = "https://www.google.com/search?q="
                + Uri.encode(query);
    }

    addressInput.setText(url);
    showBrowser();

    addressInput.setVisibility(View.VISIBLE);
    navRow.setVisibility(View.VISIBLE);

    webView.loadUrl(url);
}

   
    // ==================== KEMBALI ====================

    @Override
    public void onBackPressed() {
        if (htmlPanel.getVisibility() == View.VISIBLE) {
            showHome();
        } else if (webView.getVisibility() == View.VISIBLE) {
            if (webView.canGoBack()) webView.goBack();
            else showHome();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        executor.shutdownNow();
        if (webView != null) webView.destroy();
        super.onDestroy();
    }

    // ==================== RENDERER HTML ====================

    private static class OeRRenderView extends LinearLayout {
        public OeRRenderView(android.content.Context context) {
            super(context);
            setOrientation(VERTICAL);
            setPadding(8, 8, 8, 8);
        }

        public void renderHtml(String html) {
            removeAllViews();

            if (html == null || html.trim().isEmpty()) {
                TextView empty = new TextView(getContext());
                empty.setText("Tidak ada konten");
                empty.setTextColor(Color.GRAY);
                addView(empty);
                return;
            }

            String[] lines = html.split("\n");

            for (String raw : lines) {
                String line = raw.trim();
                if (line.isEmpty()) continue;

                TextView tv = new TextView(getContext());
                tv.setTextColor(Color.BLACK);
                tv.setPadding(0, 4, 0, 4);

                if (line.startsWith("<h1>") && line.endsWith("</h1>")) {
                    tv.setText(stripTags(line));
                    tv.setTextSize(24);
                    tv.setTypeface(null, Typeface.BOLD);
                    tv.setPadding(0, 12, 0, 8);
                } else if (line.startsWith("<h2>")
                        && line.endsWith("</h2>")) {
                    tv.setText(stripTags(line));
                    tv.setTextSize(20);
                    tv.setTypeface(null, Typeface.BOLD);
                    tv.setPadding(0, 10, 0, 6);
                } else if (line.startsWith("<p>")
                        && line.endsWith("</p>")) {
                    tv.setText(stripTags(line));
                    tv.setTextSize(15);
                    tv.setPadding(0, 6, 0, 6);
                } else if (line.startsWith("<li>")
                        && line.endsWith("</li>")) {
                    tv.setText("•  " + stripTags(line));
                    tv.setTextSize(15);
                    tv.setPadding(16, 3, 0, 3);
                } else if (line.equals("<hr>")
                        || line.equals("<hr/>")
                        || line.equals("<hr />")) {
                    View lineView = new View(getContext());
                    lineView.setBackgroundColor(Color.rgb(200, 200, 200));
                    addView(lineView, new LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, 2));
                    continue;
                } else if (line.startsWith("<ul>")
                        || line.startsWith("</ul>")
                        || line.startsWith("<ol>")
                        || line.startsWith("</ol>")) {
                    continue;
                } else {
                    String clean = stripTags(line);
                    if (clean.isEmpty()) continue;
                    tv.setText(clean);
                    tv.setTextSize(15);
                }

                addView(tv);
            }
        }

        private String stripTags(String input) {
            return input.replaceAll("<[^>]+>", "")
                    .replace("&nbsp;", " ")
                    .replace("&lt;", "<")
                    .replace("&gt;", ">")
                    .replace("&amp;", "&")
                    .trim();
        }
    }
}
