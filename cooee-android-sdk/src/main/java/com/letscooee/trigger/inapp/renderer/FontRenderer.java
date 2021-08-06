package com.letscooee.trigger.inapp.renderer;

import android.content.Context;
import android.graphics.Typeface;
import android.view.ViewGroup;
import android.widget.TextView;
import com.letscooee.CooeeFactory;
import com.letscooee.font.FontProcessor;
import com.letscooee.models.trigger.blocks.Font;
import com.letscooee.models.trigger.elements.BaseElement;
import com.letscooee.models.trigger.elements.TextElement;
import com.letscooee.trigger.inapp.InAppGlobalData;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Map;

/**
 * Chek for thee font availability on device
 *
 * @author Ashish Gaikwad 30/07/21
 * @since 1.0.0
 */
public abstract class FontRenderer extends AbstractInAppRenderer {

    protected final Font font;

    protected FontRenderer(Context context, ViewGroup parentElement, BaseElement element, InAppGlobalData globalData) {
        super(context, parentElement, element, globalData);
        font = ((TextElement) element).getFont();
    }

    protected void processFont() {
        if (font == null) return;
        this.applyFont();
    }

    private void applyFont() {
        Typeface typeface = checkInternalStorage();

        if (typeface != null) {
            ((TextView) newElement).setTypeface(typeface);
            return;
        }

        this.applySystemFont();
    }

    private void applySystemFont() {
        Typeface typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL);
        try {
            Field field = Typeface.class.getDeclaredField("sSystemFontMap");
            field.setAccessible(true);

            Map<String, Typeface> map = (Map<String, Typeface>) field.get(typeface);
            assert map != null;
            Typeface fontTypeface = map.get(font.getFamily());

            if (fontTypeface != null) {
                ((TextView) newElement).setTypeface(fontTypeface);
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            CooeeFactory.getSentryHelper().captureException(e);
        }
    }

    private Typeface checkInternalStorage() {
        File fontsDirectory = FontProcessor.getFontsStorageDirectory(context);

        File font = FontProcessor.getFontFile(fontsDirectory, this.font.getFamily());
        if (!font.exists()) {
            return null;
        }

        return Typeface.createFromFile(font);
    }
}
