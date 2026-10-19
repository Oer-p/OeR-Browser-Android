
package id.oer.browser;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.view.View;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OeRRenderView extends View {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final ArrayList<String> lines = new ArrayList<>();
    private final ArrayList<Float> sizes = new ArrayList<>();
    private final ArrayList<Boolean> bold = new ArrayList<>();
    private final ArrayList<Boolean> rules = new ArrayList<>();

    private float density;

    public OeRRenderView(Context context) {
        super(context);
        density = getResources().getDisplayMetrics().density;
        setBackgroundColor(0xFFFFFFFF);
        paint.setColor(0xFF202124);
        paint.setTextSize(16 * density);
    }

    private void addText(String text, float size, boolean isBold) {
        if (text == null || text.trim().isEmpty()) return;
        lines.add(text.trim());
        sizes.add(size);
        bold.add(isBold);
        rules.add(false);
    }

    private void addRule() {
        lines.add("");
        sizes.add(1f);
        bold.add(false);
        rules.add(true);
    }

    public void renderHtml(String html) {
        lines.clear();
        sizes.clear();
        bold.clear();
        rules.clear();

        if (html == null) html = "";

        html = html.replaceAll("(?is)<!--.*?-->", "");
        html = html.replaceAll("(?i)<br\\s*/?>", "\n");
        html = html.replaceAll("(?i)</p\\s*>", "\n");
        html = html.replaceAll("(?i)</h[1-6]\\s*>", "\n");
        html = html.replaceAll("(?i)</li\\s*>", "\n");
        html = html.replaceAll("(?i)<hr\\s*/?>", "\n__OER_RULE__\n");

        Pattern pattern = Pattern.compile(
            "(?is)<(h1|h2|h3|p|li|div|ul|ol|hr|br)\\b[^>]*>|</(h1|h2|h3|p|li|div|ul|ol)>|__OER_RULE__|[^<]+"
        );
        Matcher matcher = pattern.matcher(html);

        String currentTag = "";
        StringBuilder textBuffer = new StringBuilder();

        while (matcher.find()) {
            String token = matcher.group();

            if (token.equals("__OER_RULE__")) {
                flushText(textBuffer, currentTag);
                addRule();
                currentTag = "";
                continue;
            }

            if (token.startsWith("<")) {
                flushText(textBuffer, currentTag);

                Matcher tagMatcher = Pattern.compile(
                    "(?is)^<\\s*(/?)\\s*([a-z0-9]+)"
                ).matcher(token);

                if (tagMatcher.find()) {
                    boolean closing = !tagMatcher.group(1).isEmpty();
                    String tag = tagMatcher.group(2).toLowerCase();

                    if (!closing) {
                        currentTag = tag;
                        if (tag.equals("li")) {
                            textBuffer.append("• ");
                        }
                    } else {
                        if (tag.equals("p") || tag.startsWith("h")
                                || tag.equals("li") || tag.equals("div")) {
                            flushText(textBuffer, currentTag);
                            currentTag = "";
                        }
                    }
                }
            } else {
                textBuffer.append(decodeEntities(token));
            }
        }

        flushText(textBuffer, currentTag);

        if (lines.isEmpty()) {
            addText("Tidak ada konten HTML.", 16, false);
        }

        requestLayout();
        invalidate();
    }

    private void flushText(StringBuilder buffer, String tag) {
        String text = buffer.toString()
            .replaceAll("[\\t\\r]+", " ")
            .trim();
        buffer.setLength(0);

        if (text.isEmpty()) return;

        float size = 16;
        boolean isBold = false;

        if ("h1".equals(tag)) {
            size = 28;
            isBold = true;
        } else if ("h2".equals(tag)) {
            size = 23;
            isBold = true;
        } else if ("h3".equals(tag)) {
            size = 19;
            isBold = true;
        } else if ("li".equals(tag)) {
            size = 16;
        }

        String[] paragraphs = text.split("\\n+");
        for (String paragraph : paragraphs) {
            String cleaned = paragraph.trim();
            if (!cleaned.isEmpty()) {
                addText(cleaned, size, isBold);
            }
        }
    }

    private String decodeEntities(String text) {
        return text.replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#39;", "'");
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float left = 18 * density;
        float right = getWidth() - 18 * density;
        float y = 24 * density;
        float maxWidth = Math.max(1, right - left);

        for (int i = 0; i < lines.size(); i++) {
            if (rules.get(i)) {
                y += 8 * density;
                paint.setStrokeWidth(1 * density);
                paint.setColor(0xFFDADCE0);
                canvas.drawLine(left, y, right, y, paint);
                y += 18 * density;
                continue;
            }

            float size = sizes.get(i) * density;
            paint.setTextSize(size);
            paint.setColor(0xFF202124);
            paint.setTypeface(bold.get(i)
                ? Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                : Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));

            String text = lines.get(i);
            String[] words = text.split("\\s+");
            StringBuilder line = new StringBuilder();

            for (String word : words) {
                String candidate = line.length() == 0
                    ? word : line + " " + word;

                if (paint.measureText(candidate) > maxWidth
                        && line.length() > 0) {
                    canvas.drawText(line.toString(), left, y, paint);
                    y += size * 1.35f;
                    line.setLength(0);
                    line.append(word);
                } else {
                    if (line.length() > 0) line.append(" ");
                    line.append(word);
                }
            }

            if (line.length() > 0) {
                canvas.drawText(line.toString(), left, y, paint);
            }

            y += size * 1.7f;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        float estimatedHeight = 40 * density;

        for (int i = 0; i < lines.size(); i++) {
            estimatedHeight += (sizes.get(i) * 1.8f + 8) * density;
            if (rules.get(i)) estimatedHeight += 10 * density;
        }

        int desiredHeight = (int) estimatedHeight;
        int height = resolveSize(
            Math.max(desiredHeight, 200), heightMeasureSpec
        );
        setMeasuredDimension(width, height);
    }
}
