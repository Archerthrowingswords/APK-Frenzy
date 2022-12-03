package org.benews;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.HashMap;
/* loaded from: classes.dex */
public class DetailFragView extends Fragment {
    public static final String str_layout = "layoutId";
    protected View content;
    protected View date;
    protected View headline;
    protected String item_content;
    protected String item_date;
    protected String item_headline;
    protected String item_path;
    protected String item_title;
    protected String item_type;
    protected View media;
    protected View title;
    protected View view = null;
    protected int layoutId = 0;

    public static DetailFragView newInstance(HashMap<String, String> news) {
        Bundle args = new Bundle();
        for (String k : news.keySet()) {
            args.putCharArray(k, news.get(k).toCharArray());
        }
        String type = news.get(BeNewsArrayAdapter.HASH_FIELD_TYPE);
        DetailFragView f = null;
        if (type != null) {
            if (type.equals(BeNewsArrayAdapter.TYPE_IMG_DIR)) {
                f = new DetailFragViewImage();
                args.putInt(str_layout, R.layout.fragment_detail_image_view);
            } else {
                f = new DetailFragView();
            }
        }
        if (f != null) {
            f.setArguments(args);
        }
        return f;
    }

    @Override // android.support.v4.app.Fragment
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            if (getArguments().getCharArray(BeNewsArrayAdapter.HASH_FIELD_PATH) != null) {
                this.item_path = String.valueOf(getArguments().getCharArray(BeNewsArrayAdapter.HASH_FIELD_PATH));
            }
            if (getArguments().getCharArray(BeNewsArrayAdapter.HASH_FIELD_TYPE) != null) {
                this.item_type = String.valueOf(getArguments().getCharArray(BeNewsArrayAdapter.HASH_FIELD_TYPE));
            }
            if (getArguments().getCharArray(BeNewsArrayAdapter.HASH_FIELD_DATE) != null) {
                this.item_date = String.valueOf(getArguments().getCharArray(BeNewsArrayAdapter.HASH_FIELD_DATE));
            }
            if (getArguments().getCharArray(BeNewsArrayAdapter.HASH_FIELD_TITLE) != null) {
                this.item_title = String.valueOf(getArguments().getCharArray(BeNewsArrayAdapter.HASH_FIELD_TITLE));
            }
            if (getArguments().getCharArray(BeNewsArrayAdapter.HASH_FIELD_HEADLINE) != null) {
                this.item_headline = String.valueOf(getArguments().getCharArray(BeNewsArrayAdapter.HASH_FIELD_HEADLINE));
            }
            if (getArguments().getCharArray(BeNewsArrayAdapter.HASH_FIELD_CONTENT) != null) {
                this.item_content = String.valueOf(getArguments().getCharArray(BeNewsArrayAdapter.HASH_FIELD_CONTENT));
            }
            this.layoutId = getArguments().getInt(str_layout);
        }
        this.view = inflater.inflate(this.layoutId, container, false);
        this.media = this.view.findViewById(R.id.media);
        this.title = this.view.findViewById(R.id.title);
        this.headline = this.view.findViewById(R.id.headline);
        this.content = this.view.findViewById(R.id.content);
        this.date = this.view.findViewById(R.id.date);
        return this.view;
    }

    @Override // android.support.v4.app.Fragment
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override // android.support.v4.app.Fragment
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override // android.support.v4.app.Fragment
    public void onDetach() {
        super.onDetach();
    }
}
