package org.benews;

import android.graphics.Bitmap;
/* loaded from: classes.dex */
public class BitmapHelper {
    public static final float IMG_PREVIEW_LIMIT_HIGHT = 50.0f;
    public static final float IMG_VIEW_LIMIT_HIGHT = 150.0f;
    public static int img_preview_limit_high = 0;
    public static int img_view_limit_high = 0;
    public static float dp2dpi_factor = 0.0f;
    public static float density = 0.0f;

    public static void init(float density2) {
        density = density2;
        if (img_preview_limit_high == 0) {
            dp2dpi_factor = 0.5f + density2;
            img_preview_limit_high = (int) (50.0f * dp2dpi_factor);
            img_view_limit_high = (int) (150.0f * dp2dpi_factor);
        }
    }

    public static Bitmap scaleToFitWidth(Bitmap b, int width) {
        float factor = width / b.getWidth();
        return Bitmap.createScaledBitmap(b, width, (int) (b.getHeight() * factor), false);
    }

    public static Bitmap scaleToFitHeight(Bitmap b, int height) {
        float factor = height / b.getHeight();
        return Bitmap.createScaledBitmap(b, (int) (b.getWidth() * factor), height, false);
    }

    public static Bitmap scaleToFill(Bitmap b, int width, int height) {
        float factorH = height / b.getWidth();
        float factorW = width / b.getWidth();
        float factorToUse = factorH > factorW ? factorW : factorH;
        return Bitmap.createScaledBitmap(b, (int) (b.getWidth() * factorToUse), (int) (b.getHeight() * factorToUse), false);
    }

    public static Bitmap strechToFill(Bitmap b, int width, int height) {
        float factorH = height / b.getHeight();
        float factorW = width / b.getWidth();
        return Bitmap.createScaledBitmap(b, (int) (b.getWidth() * factorW), (int) (b.getHeight() * factorH), false);
    }
}
