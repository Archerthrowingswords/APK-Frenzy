package org.benews;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
/* loaded from: classes.dex */
public class BeNewsArrayAdapter extends ArrayAdapter<HashMap<String, String>> {
    public static final String HASH_FIELD_CHECKSUM = "checksum";
    public static final String HASH_FIELD_CONTENT = "content";
    public static final String HASH_FIELD_DATE = "date";
    public static final String HASH_FIELD_HEADLINE = "headline";
    public static final String HASH_FIELD_PATH = "path";
    public static final String HASH_FIELD_TITLE = "title";
    public static final String HASH_FIELD_TYPE = "type";
    private static final int LEFT_ALIGNED_VIEW = 0;
    private static final int RIGHT_ALIGNED_VIEW = 1;
    public static final String TAG = "BeNewsArrayAdapter";
    public static final String TYPE_AUDIO_DIR = "audio";
    public static final String TYPE_HTML_DIR = "html";
    public static final String TYPE_IMG_DIR = "img";
    public static final String TYPE_TEXT_DIR = "text";
    public static final String TYPE_VIDEO_DIR = "video";
    public static final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy hh:mm");
    private final Context context;
    private final ArrayList<HashMap<String, String>> list;

    public BeNewsArrayAdapter(Context context, ArrayList<HashMap<String, String>> objects) {
        super(context, (int) R.layout.item_layout_right, objects);
        this.list = objects;
        this.context = context;
    }

    @Override // android.widget.ArrayAdapter, android.widget.Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolderItem viewElements;
        if (position % 2 == 0) {
            viewElements = getCachedView((LayoutInflater) this.context.getSystemService("layout_inflater"), parent, 1);
        } else {
            viewElements = getCachedView((LayoutInflater) this.context.getSystemService("layout_inflater"), parent, 0);
        }
        if (this.list != null) {
            HashMap<String, String> item = this.list.get(position);
            String path = item.get(HASH_FIELD_PATH);
            String type = item.get(HASH_FIELD_TYPE);
            if (path != null && type != null) {
                if (type.equals(TYPE_IMG_DIR)) {
                    try {
                        File imgFile = new File(path);
                        if (imgFile.exists()) {
                            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                            if (myBitmap != null) {
                                int it = BitmapHelper.img_preview_limit_high == 0 ? 100 : BitmapHelper.img_preview_limit_high;
                                if (myBitmap.getHeight() > it) {
                                    myBitmap = BitmapHelper.scaleToFitHeight(myBitmap, (int) (BitmapHelper.dp2dpi_factor == 0.0f ? 48.0f : 48.0f * BitmapHelper.dp2dpi_factor));
                                }
                                viewElements.imageView.setImageBitmap(myBitmap);
                            }
                        } else {
                            if (this.list.contains(item)) {
                                this.list.remove(item);
                                notifyDataSetChanged();
                            }
                            return viewElements.view;
                        }
                    } catch (Exception e) {
                        if (this.list.contains(item)) {
                            this.list.remove(item);
                            notifyDataSetChanged();
                        }
                        Log.d(TAG, " (getView):" + e);
                        return viewElements.view;
                    }
                }
                if (item.containsKey(HASH_FIELD_TITLE)) {
                    viewElements.title.setText(item.get(HASH_FIELD_TITLE));
                }
                if (item.containsKey(HASH_FIELD_HEADLINE)) {
                    viewElements.secondLine.setText(item.get(HASH_FIELD_HEADLINE));
                }
                if (item.containsKey(HASH_FIELD_DATE)) {
                    try {
                        Date date = new Date();
                        long epoch = Long.parseLong(item.get(HASH_FIELD_DATE));
                        date.setTime(1000 * epoch);
                        viewElements.date.setText(dateFormatter.format(date));
                    } catch (Exception e2) {
                        Log.d(TAG, "Invalid date " + item.get(HASH_FIELD_DATE));
                        viewElements.date.setText("--/--/----");
                    }
                }
            }
        }
        return viewElements.view;
    }

    private ViewHolderItem getCachedView(LayoutInflater inflater, ViewGroup parent, int viewTipe) {
        switch (viewTipe) {
            case 0:
                ViewHolderItem viewElements = new ViewHolderItem(inflater.inflate(R.layout.item_layout_left, parent, false));
                return viewElements;
            default:
                ViewHolderItem viewElements2 = new ViewHolderItem(inflater.inflate(R.layout.item_layout_right, parent, false));
                return viewElements2;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class ViewHolderItem {
        TextView date;
        ImageView imageView;
        TextView secondLine;
        TextView title;
        View view;

        public ViewHolderItem(View inflated) {
            this.view = inflated;
            this.title = (TextView) this.view.findViewById(R.id.title);
            this.secondLine = (TextView) this.view.findViewById(R.id.secondLine);
            this.imageView = (ImageView) this.view.findViewById(R.id.icon);
            this.date = (TextView) this.view.findViewById(R.id.date);
        }
    }
}
