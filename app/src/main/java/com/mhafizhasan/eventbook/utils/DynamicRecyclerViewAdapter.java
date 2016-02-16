package com.mhafizhasan.eventbook.utils;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;

/**
 * Created by someguy233 on 03-Nov-15.
 */
public class DynamicRecyclerViewAdapter extends RecyclerView.Adapter<DynamicRecyclerViewAdapter.ViewHolder> {

    class ViewHolder extends RecyclerView.ViewHolder {

        final ContentView contentView;

        ViewHolder(ContentView contentView) {
            super(contentView.getView());
            this.contentView = contentView;
        }
    }

    public static class Cursor {
        private final DynamicRecyclerViewAdapter adapter;
        private int index = 0;

        public Cursor(DynamicRecyclerViewAdapter adapter) {
            this.adapter = adapter;
            adapter.cursors.add(this);
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            if(index < -1 || index > adapter.getItemCount())
                throw new ArrayIndexOutOfBoundsException("index (" + index + ") must be >= -1 && <= " + adapter.getItemCount());
            this.index = index;
        }

        public void detach() {
            adapter.cursors.remove(this);
        }
    }

    private final RecyclerView recyclerView;
    private final List<Object> contents = new ArrayList<>();
    private final List<Cursor> cursors = new ArrayList<>();
    private final Map<String, Object> lookup = new HashMap<>();
    private final ArrayList<Class<?>> contentTypes = new ArrayList<>();
    private final ArrayList<ContentView<?>> contentViews = new ArrayList<>();
    private boolean isCommitting = true;

    public void stopCommits() {
        isCommitting = false;
    }

    public void startCommits() {
        if(isCommitting)
            return;
        isCommitting = true;
        notifyDataSetChanged();         // notify all changed
    }

    public boolean isCommitting() {
        return isCommitting;
    }

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

    public Object lookup(String name) {
        return lookup.get(name);
    }

    public int indexOf(Object content) {
        return contents.indexOf(content);
    }

    public Object getItem(int position) {
        return contents.get(position);
    }

    ViewHolder getViewHolder(int position) {
        return (ViewHolder) recyclerView.findViewHolderForAdapterPosition(position);
    }

    public <T extends ContentView<?>> T getContentView(int position) {
        ViewHolder viewHolder = (ViewHolder) recyclerView.findViewHolderForAdapterPosition(position);
        return (T) viewHolder.contentView;
    }

    public <T> void register(Class<T> type, ContentView<? super T> contentView) {
        int index = contentTypes.indexOf(type);
        if(index != -1) {
            // Existing type
            contentTypes.set(index, type);
            contentViews.set(index, contentView);
            return;
        }

        // Else new type
        contentTypes.add(type);
        contentViews.add(contentView);
    }


    public boolean remove(Object content) {
        int index = contents.indexOf(content);
        if(index != -1) {
            remove(index);
            return true;
        }
        return false;
    }

    public void remove(int index) {
        if(index < 0 || index >= contents.size())
            throw new ArrayIndexOutOfBoundsException("index (" + index + ") must be >= 0 && < " + contents.size());
        lookup.remove(contents.remove(index).toString());
        if(isCommitting)
            notifyItemRemoved(index);
        // Update cursors
        for(int c = 0, size = cursors.size(); c < size; c++) {
            Cursor cursor = cursors.get(c);
            if(cursor.index > 0 && cursor.index >= index)
                cursor.index--;
        }
    }

    public void remove(int start, int end) {
        if(start < 0 || start >= contents.size())
            throw new ArrayIndexOutOfBoundsException("start (" + start + ") must be >= 0 && < " + contents.size());
        if(end < 0 || end > contents.size())
            throw new ArrayIndexOutOfBoundsException("end (" + end + ") must be >= 0 && <= " + contents.size());
        if(end < start)
            throw new ArrayIndexOutOfBoundsException("end (" + end + ") must be >= start (" + start + ") ");
        if(start == end)
            return;
        // Remove items
        for (int c = start; c < end; c++)
            lookup.remove(contents.get(c).toString());      // remove lookups
        contents.subList(start, end).clear();
        if(isCommitting)
            notifyItemRangeRemoved(start, end - start);
        // Update cursors
        for(int c = 0, size = cursors.size(); c < size; c++) {
            Cursor cursor = cursors.get(c);
            if(cursor.index > 0 && cursor.index >= start) {
                if (cursor.index < end)
                    cursor.index = start;
                else
                    cursor.index -= (end - start);
            }
        }
    }

    public void clear() {
        if(getItemCount() > 0)
            remove(0, getItemCount());
    }

    public boolean offer(Object content) {
        return offer(contents.size(), content);
    }

    public boolean offer(int position, Object content) {
        if(position < 0 || position > contents.size())
            throw new ArrayIndexOutOfBoundsException("position (" + position + ") must be >= 0 && <= " + contents.size());
        String name = content.toString();
        Object prev = lookup.get(name);
        if(prev != null)
            return false;
        // If updating first item and current item is visible, automatic scroll to top, do the same for last item
        int itemCount = getItemCount();
        if(itemCount > 0 && ((position == 0 && getViewHolder(0) != null) || (position == itemCount && getViewHolder(itemCount - 1) != null && getViewHolder(0) == null)))
            recyclerView.smoothScrollToPosition(position);
        // Else no old item, insert new item
        contents.add(position, content);
        if(isCommitting)
            notifyItemInserted(position);
        // Update cursors
        for(int c = 0, size = cursors.size(); c < size; c++) {
            Cursor cursor = cursors.get(c);
            if(cursor.index >= position)
                cursor.index++;
        }
        return true;
    }

    public void put(Object content) {
        put(content, contents.size());
    }

    public void put(Object content, int position) {
        String name = content.toString();
        Object prev = lookup.get(name);
        if(prev != null) {
            int index = contents.indexOf(prev);
            if(index != position) {
                if(index < position) {
                    position--;
                    if(index == position)
                        return;         // no point moving
                }
                contents.remove(index);
                contents.add(position, prev);
                if(isCommitting)
                    notifyItemMoved(index, position);
                // Update cursors
                for(int c = 0, size = cursors.size(); c < size; c++) {
                    Cursor cursor = cursors.get(c);
                    if(cursor.index > 0 && cursor.index >= index)
                        cursor.index--;
                    if(cursor.index >= position)
                        cursor.index++;
                }
            }
            // Else already at the same position, check if need to update
            if(content.equals(prev))
                return;     // no point updating
            // Else update to new one
            contents.set(position, content);
            if(isCommitting)
                notifyItemChanged(position);
        }
        else {
            // If updating first item and current item is visible, automatic scroll to top, do the same for last item
            int itemCount = getItemCount();
            if(itemCount > 0 && ((position == 0 && getViewHolder(0) != null) || (position == itemCount && getViewHolder(itemCount - 1) != null && getViewHolder(0) == null)))
                recyclerView.smoothScrollToPosition(position);
            // Else new content
            contents.add(position, content);
            if(isCommitting)
                notifyItemInserted(position);
            // Update cursors
            for(int c = 0, size = cursors.size(); c < size; c++) {
                Cursor cursor = cursors.get(c);
                if(cursor.index >= position)
                    cursor.index++;
            }
        }
        lookup.put(name, content);
    }

    public boolean refresh(Object content) {
        int index = contents.indexOf(content);
        if(index == -1)
            return false;
        if(isCommitting)
            notifyItemChanged(index);
        return true;
    }



    public boolean patch(Object content) {
        return update(getItemCount(), content, true);
    }

    public boolean patch(int position, Object content) {
        return update(position, content, true);
    }

    public boolean update(Object content) {
        return update(getItemCount(), content, false);
    }

    public boolean update(int position, Object content) {
        return update(position, content, false);
    }

    public boolean update(int position, Object content, boolean patch) {
        if(position < 0 || position > contents.size())
            throw new ArrayIndexOutOfBoundsException("position (" + position + ") must be >= 0 && <= " + contents.size());
        String name = content.toString();
        Object prev = lookup.get(name);
        if(prev != null) {
            int index = contents.indexOf(prev);
            if(index < position) {
                Log.d("DD", "Prev item more updated: " + position + " " + index + " " + name);      // TODO
                return false;       // previous item is more updated
            }
            else if(index > position) {
                if(patch) {
                    Log.d("DD", "Patching items: " + position + " " + index + " " + name);      // TODO
                    // Remove items
                    for (int c = position; c < index; c++)
                        lookup.remove(contents.get(c).toString());      // remove lookups
                    contents.subList(position, index).clear();
                    if(isCommitting)
                        notifyItemRangeRemoved(position, index - position);
                    // Update cursors
                    for(int c = 0, size = cursors.size(); c < size; c++) {
                        Cursor cursor = cursors.get(c);
                        if(cursor.index > 0 && cursor.index >= position) {
                            if (cursor.index < index)
                                cursor.index = position;
                            else
                                cursor.index -= (index - position);
                        }
                    }
               }
                else {
                    Log.d("DD", "Moving item: " + position + " " + index + " " + name);      // TODO
                    // Just move item
                    for (int c = index; c > position; c--)
                        contents.set(c, contents.get(c - 1));
                    contents.set(position, prev);
                    if(isCommitting)
                        notifyItemMoved(index, position);
                    // Update cursors
                    for(int c = 0, size = cursors.size(); c < size; c++) {
                        Cursor cursor = cursors.get(c);
                        if(cursor.index > 0 && cursor.index >= index)
                            cursor.index--;
                        if(cursor.index >= position)
                            cursor.index++;
                    }
                }
            }
            // Update item
            contents.set(position, content);
            if(!content.equals(prev)) {
                Log.d("DD", "Updating: " + position + " " + name);      // TODO
                if(isCommitting)
                    notifyItemChanged(position);
            }
            else
                Log.d("DD", "Ignoring same item: " + position + " " + name);      // TODO
        }
        else {
            // Else no old item, insert new item
            Log.d("DD", "New item: " + position + "  " + name);
            // If updating first item and current item is visible, automatic scroll to top, do the same for last item
            int itemCount = getItemCount();
            if(itemCount > 0 && ((position == 0 && getViewHolder(0) != null) || (position == itemCount && getViewHolder(itemCount - 1) != null && getViewHolder(0) == null)))
                recyclerView.smoothScrollToPosition(position);
            contents.add(position, content);
            if(isCommitting)
                notifyItemInserted(position);
            // Update cursors
            for(int c = 0, size = cursors.size(); c < size; c++) {
                Cursor cursor = cursors.get(c);
                if(cursor.index >= position)
                    cursor.index++;
            }
        }
        lookup.put(name, content);
        return true;
    }

    public DynamicRecyclerViewAdapter(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
        recyclerView.setAdapter(this);
    }

    @Override
    public int getItemViewType(int position) {
        return getItemViewType(contents.get(position).getClass());
    }

    public int getItemViewType(Class<?> type) {
        int index = contentTypes.indexOf(type);
        if(index == -1)
            throw new IllegalArgumentException(type.getSimpleName() + " not registered");
        return index;
    }
/*
    public <VH extends ViewHolder> VH bindViewHolder(ViewGroup parent, Object content) {
        int viewType = getItemViewType(content.getClass());
        VH vh = (VH) recyclerView.getRecycledViewPool().getRecycledView(viewType);
        if(vh == null)
            vh = (VH) createViewHolder(parent, viewType);
        else
            Log.e("DD", "Re-using viewholder: " + vh);
        vh.content = content;
        vh.adapter = this;
        vh.bind(content);
        return vh;
    }

    public void unbindViewHolder(ViewHolder<?> vh) {
        if(vh.content == null)
            throw new IllegalArgumentException("vh already unbound");
        onViewRecycled(vh);
        recyclerView.getRecycledViewPool().putRecycledView(vh);
    }
*/
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Prepare content view
        ContentView<?> contentView = contentViews.get(viewType);
        if(contentView.getView() != null)
            contentView = contentView.instantiate();        // Instantiate as first view is being used
        contentView.prepareView(parent);
        // Attach to RecyclerView.ViewHolder
        return new ViewHolder(contentView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.contentView.show(contents.get(position));
    }

    @Override
    public void onViewRecycled(ViewHolder holder) {
        holder.contentView.clear();
    }

    /*
    @Override
    public void onViewAttachedToWindow(ViewHolder<?> holder) {
        holder.onAttachedToWindow();
    }

    @Override
    public void onViewDetachedFromWindow(ViewHolder<?> holder) {
        holder.onDetachedFromWindow();
    }
*/
    @Override
    public int getItemCount() {
        return contents.size();
    }
}
