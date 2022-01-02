package com.letscooee.trigger.inapp.renderer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import com.letscooee.CooeeFactory;
import com.letscooee.R;
import com.letscooee.font.FontProcessor;
import com.letscooee.models.trigger.blocks.Font;
import com.letscooee.models.trigger.elements.BaseElement;
import com.letscooee.models.trigger.elements.PartElement;
import com.letscooee.models.trigger.elements.TextElement;
import com.letscooee.trigger.inapp.TriggerContext;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Chek for thee font availability on device
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
        Typeface typeface = getTypeFaceFromBrandFont();

        if (typeface == null) {
            typeface = this.getSystemTypeface();
        }

        if (typeface == null) {
            typeface = this.getFontWRToStyle();
        }

        if (typeface == null) {
            typeface = ResourcesCompat.getFont(context, R.font.arial);
        }

        if (typeface != null) {
            ((TextView) newElement).setTypeface(typeface);
        }
    }

    protected void setPartElement(PartElement partElement) {
        this.partElement = partElement;
    }

    /**
     * Process font name and <code>Bold</code>, <code>Italic</code> property to the name of font to
     * verify specific font file in local storage
     *
     * @return {@link Typeface} if file is present at local storage. Otherwise <code>null</code>
     */
    private Typeface getFontWRToStyle() {
        if (partElement == null || TextUtils.isEmpty(font.getName())) {
            return null;
        }

        List<String> fontNameArray = new ArrayList<>();
        String fontFileName = font.getName().toLowerCase().trim();

        fontNameArray.add(fontFileName);

        if (partElement.isBold()) {
            fontNameArray.add("bold");
        }

        if (partElement.isItalic()) {
            fontNameArray.add("italic");
        }

        fontFileName = TextUtils.join("-", fontNameArray);

        File fontFile = FontProcessor.getFontFile(this.fontsDirectory, fontFileName);

        if (!fontFile.exists()) {
            return null;
        }

        return Typeface.createFromFile(fontFile);
    }

    private Typeface getSystemTypeface() {
        Typeface typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL);
        try {
            Field field = Typeface.class.getDeclaredField("sSystemFontMap");
            field.setAccessible(true);

            Map<String, Typeface> map = (Map<String, Typeface>) field.get(typeface);
            if (map == null) {
                return null;
            }

            return map.get(font.getName());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            CooeeFactory.getSentryHelper().captureException(e);
            return null;
        }
    }

    private Typeface getTypeFaceFromBrandFont() {

        File fontFile = FontProcessor.getFontFile(this.fontsDirectory, this.font.getName());

        if (!fontFile.exists()) {
            return null;
        }

        return Typeface.createFromFile(fontFile);
    }
}
