package com.letscooee.trigger.inapp.renderer;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Environment;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.letscooee.CooeeFactory;
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
        this.checkFontIsPresent();
    }

    private void checkFontIsPresent() {
        String cachePath = context.getCacheDir().getAbsolutePath();
        Typeface typeface = null;

        // Check if user has granted internal storage permission
        if (this.checkWriteStoragePermission()) {
            typeface = checkInternalStorage();
        }

        if (typeface != null) {
            ((TextView) newElement).setTypeface(typeface);
            return;
        }

        File fontFile = new File(cachePath + "/" + font.getFamily() + ".ttf");
        if (fontFile.exists()) {
            typeface = Typeface.createFromFile(fontFile);
            ((TextView) newElement).setTypeface(typeface);
            return;
        }

        this.checkDeviceFonts();
    }


    private void checkDeviceFonts() {
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
        String internalStoragePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Cooee";
        File file = new File(internalStoragePath);

        if (!file.isDirectory()) {
            return null;
        }
        File fontPath = new File(internalStoragePath, font.getFamily() + ".ttf");
        if (!file.exists()) {
            return null;
        }
        return Typeface.createFromFile(fontPath);
    }

    protected boolean checkWriteStoragePermission() {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }
}
