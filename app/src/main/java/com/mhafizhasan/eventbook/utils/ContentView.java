package com.mhafizhasan.eventbook.utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;

/**
 * Created by Azmi on 20/1/2016.
 */
public class ContentView<T> {

    private final int layoutId;
    private View view = null;
    private T content = null;

    public View getView() {
        return view;
    }

    public T getContent() {
        return content;
    }

    public ContentView(int layoutId) {
        this.layoutId = layoutId;
    }

    public ContentView<T> instantiate() {
        throw new UnsupportedOperationException("instantiate() not implemented for " + getClass().getName());
    }


    public void show(T content) {
        if(view == null)
            throw new IllegalStateException("prepareView() must be called before showing");
        // Unbind previous content
        if (this.content != null)
            unbind(this.content);
        // Bind new content
        if(content != null)
            bind(content);
        this.content = content;
    }

    public void clear() {
        if(content == null)
            return;
        unbind(content);
        content = null;
    }

    public void prepareView(ViewGroup parent) {
        // Prepare layout
        if (view != null)
            return;
        view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
        view.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {
                onAttachedToWindow();
            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                onDetachedFromWindow();
            }
        });
        ButterKnife.bind(this, view);
    }

    public void releaseView() {
        if(view == null)
            return;
        clear();
        view = null;
        ButterKnife.unbind(this);
    }



    protected void bind(T content) {
        // nothing
    }

    protected void unbind(T content) {
        // nothing
    }

    protected void onAttachedToWindow() {
        // nothing
    }

    protected void onDetachedFromWindow() {
        // nothing
    }
}
