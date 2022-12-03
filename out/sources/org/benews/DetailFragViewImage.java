package org.benews;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import java.io.File;
import java.util.Date;
/* loaded from: classes.dex */
public class DetailFragViewImage extends DetailFragView {
    private final String TAG = "DetailFragViewImage";

    @Override // org.benews.DetailFragView, android.support.v4.app.Fragment
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (this.item_path != null && this.item_type != null) {
            if (this.item_type.equals(BeNewsArrayAdapter.TYPE_IMG_DIR)) {
                File imgFile = new File(this.item_path);
                if (imgFile.exists()) {
                    try {
                        Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                        if (myBitmap != null) {
                            ImageView imageView = (ImageView) this.media;
                            float it_M = BitmapHelper.img_view_limit_high == 0 ? 600.0f : BitmapHelper.img_view_limit_high;
                            float it_m = 60.0f * BitmapHelper.dp2dpi_factor;
                            int h = myBitmap.getHeight();
                            if (h > it_M || h < it_m) {
                                myBitmap = BitmapHelper.scaleToFitHeight(myBitmap, (int) (BitmapHelper.dp2dpi_factor != 0.0f ? 200.0f * BitmapHelper.dp2dpi_factor : 200.0f));
                            }
                            imageView.setImageBitmap(myBitmap);
                        }
                    } catch (Exception e) {
                        Log.d("DetailFragViewImage", " (onActivityCreated):" + e);
                    }
                }
            }
            if (this.item_title != null && this.title != null) {
                ((TextView) this.title).setText(this.item_title);
            }
            if (this.item_headline != null && this.headline != null) {
                ((TextView) this.headline).setText(this.item_headline);
            }
            if (this.item_content != null && this.content != null) {
                ((TextView) this.content).setText(this.item_content);
            }
            if (this.item_date != null && this.date != null) {
                try {
                    Date date_f = new Date();
                    long epoch = Long.parseLong(this.item_date);
                    date_f.setTime(1000 * epoch);
                    ((TextView) this.date).setText(BeNewsArrayAdapter.dateFormatter.format(date_f));
                } catch (Exception e2) {
                    Log.d("DetailFragViewImage", "Invalid date " + this.item_date);
                    ((TextView) this.date).setText("--/--/----");
                }
            }
        }
    }
}
