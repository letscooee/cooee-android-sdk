package com.letscooee.trigger.inapp.renderer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import com.letscooee.R;
import com.letscooee.enums.trigger.FontStyle;
import com.letscooee.font.FontProcessor;
import com.letscooee.models.trigger.blocks.Font;
import com.letscooee.models.trigger.blocks.FontFamily;
import com.letscooee.models.trigger.blocks.SDKFont;
import com.letscooee.models.trigger.elements.BaseElement;
import com.letscooee.models.trigger.elements.PartElement;
import com.letscooee.models.trigger.elements.TextElement;
import com.letscooee.trigger.inapp.TriggerContext;
import java.io.File;
import java9.util.stream.StreamSupport;

/**
 * Check for thee font availability on device
 *
 * @author Ashish Gaikwad 30/07/21
 * @since 1.0.0
 */
public abstract class FontRenderer extends AbstractInAppRenderer {

    protected final Font font;
    protected final TextElement textElement;
    private PartElement partElement;
    private final File fontsDirectory;

    protected FontRenderer(Context context, ViewGroup parentElement, BaseElement element, TriggerContext globalData) {
        super(context, parentElement, element, globalData);
        textElement = (TextElement) element;
        font = ((TextElement) element).getFont();
        fontsDirectory = FontProcessor.getFontsStorageDirectory(context);
    }

    protected void processFont() {
        if (font != null) {
            this.applyFont();
            this.applyLineHeight();
        }
    }

    private void applyLineHeight() {
        TextView textView = (TextView) newElement;
        Float lineHeight = font.getLineHeight();

        if (lineHeight == null) {
            return;
        }

        if (font.hasUnit()) {
            textView.setLineSpacing(lineHeight, 1f);
        } else {
            textView.setLineSpacing(0, lineHeight);
        }
    }

    @SuppressLint("WrongConstant")
    private void applyFont() {
        Typeface typeface = getFontWRToStyle();

        if (typeface != null) {
            ((TextView) newElement).setTypeface(typeface);
        }
    }

    protected void setPartElement(PartElement partElement) {
        this.partElement = partElement;
    }

    /**
     * Checks if pre cached font or returns default ARIAL font as a {@link Typeface}
     *
     * @return {@link Typeface} if file is present at local storage. Otherwise default Arial is returned
     */
    private Typeface getFontWRToStyle() {
        FontFamily fontFamily = this.font.getFontFamily();
        if (fontFamily == null || fontFamily.getFonts() == null) {
            return ResourcesCompat.getFont(this.context, R.font.arial);
        }

        File fontDirectory = FontProcessor.getFontsStorageDirectory(this.context);

        SDKFont regular = getSDKFont(FontStyle.REGULAR, fontFamily);
        SDKFont bold = getSDKFont(FontStyle.BOLD, fontFamily);
        SDKFont italics = getSDKFont(FontStyle.ITALICS, fontFamily);
        SDKFont boldItalics = getSDKFont(FontStyle.BOLD_ITALICS, fontFamily);

        File defaultFont;
        if (regular != null && !TextUtils.isEmpty(regular.getUrl())) {
            assert regular.getUrl() != null;
            defaultFont = new File(fontDirectory, getFileName(regular.getUrl()));
        } else if (bold != null && !TextUtils.isEmpty(bold.getUrl())) {
            assert bold.getUrl() != null;
            defaultFont = new File(fontDirectory, getFileName(bold.getUrl()));
        } else if (italics != null && !TextUtils.isEmpty(italics.getUrl())) {
            assert italics.getUrl() != null;
            defaultFont = new File(fontDirectory, getFileName(italics.getUrl()));
        } else if (boldItalics != null && !TextUtils.isEmpty(boldItalics.getUrl())) {
            assert boldItalics.getUrl() != null;
            defaultFont = new File(fontDirectory, getFileName(boldItalics.getUrl()));
        } else {
            return ResourcesCompat.getFont(this.context, R.font.arial);
        }

        /*
         * Check if file exist or not
         */
        if (!defaultFont.exists()) {
            return ResourcesCompat.getFont(this.context, R.font.arial);
        }


        return Typeface.createFromFile(defaultFont);
    }

    /**
     * Gets the file name from the URL.
     *
     * @param fileURL URL from which file name need to to extracted.
     * @return Name of the file with extension
     */
    private String getFileName(@NonNull String fileURL) {
        return fileURL.substring(fileURL.lastIndexOf('/') + 1, fileURL.length());
    }

    /**
     * Get the {@link SDKFont} of the given style
     *
     * @param style      {@link FontStyle} which want to be get
     * @param fontFamily {@link FontFamily} from which given style to be taken out.
     * @return {@link SDKFont} if exist.
     */
    @Nullable
    private SDKFont getSDKFont(@NonNull FontStyle style, @NonNull FontFamily fontFamily) {
        return StreamSupport.stream(fontFamily.getFonts())
                .filter(sdkFont -> sdkFont.getStyle() != style)
                .findAny()
                .orElse(null);
    }
}
