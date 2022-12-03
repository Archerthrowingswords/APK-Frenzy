package org.benews;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.TextView;
/* loaded from: classes.dex */
public class BeNewsFragList extends Fragment implements AdapterView.OnItemClickListener {
    private ListAdapter mAdapter;
    private AbsListView mListView;
    private OnFragmentInteractionListener mListener;

    /* loaded from: classes.dex */
    public interface OnFragmentInteractionListener {
        void onItemPress(int i);
    }

    public void setListAdapter(ListAdapter mAdapter) {
        this.mAdapter = mAdapter;
        if (mAdapter != null && this.mListView != null) {
            this.mListView.setAdapter((AbsListView) this.mAdapter);
            this.mListView.setOnItemClickListener(this);
        }
    }

    @Override // android.support.v4.app.Fragment
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override // android.support.v4.app.Fragment
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item_list, container, false);
        this.mListView = (AbsListView) view.findViewById(16908298);
        if (this.mAdapter != null && this.mListView.getAdapter() != this.mAdapter) {
            this.mListView.setAdapter((AbsListView) this.mAdapter);
            this.mListView.setOnItemClickListener(this);
        }
        return view;
    }

    @Override // android.support.v4.app.Fragment
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override // android.support.v4.app.Fragment
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            this.mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override // android.support.v4.app.Fragment
    public void onDetach() {
        super.onDetach();
        this.mListener = null;
    }

    @Override // android.widget.AdapterView.OnItemClickListener
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (this.mListener != null) {
            this.mListener.onItemPress(position);
        }
    }

    public void setEmptyText(CharSequence emptyText) {
        View emptyView = this.mListView.getEmptyView();
        if (emptyText instanceof TextView) {
            ((TextView) emptyView).setText(emptyText);
        }
    }
}
