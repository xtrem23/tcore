package ru.touchin.roboswag.components.adapters;

import android.support.annotation.NonNull;
import android.view.ViewGroup;

import java.util.List;

import ru.touchin.roboswag.components.utils.LifecycleBindable;

/**
 * Objects of such class controls creation and binding of specific type of RecyclerView's ViewHolders.
 * Such delegates are creating and binding ViewHolders by position in adapter.
 * Default {@link #getItemViewType} is generating on construction of object.
 *
 * @param <TViewHolder> Type of {@link BindableViewHolder} of delegate.
 */
public abstract class PositionAdapterDelegate<TViewHolder extends BindableViewHolder> extends AdapterDelegate<TViewHolder> {

    public PositionAdapterDelegate(@NonNull final LifecycleBindable parentLifecycleBindable) {
        super(parentLifecycleBindable);
    }

    /**
     * Returns if object is processable by this delegate.
     *
     * @param positionInAdapter Position of item in adapter;
     * @return True if item is processable by this delegate.
     */
    public abstract boolean isForViewType(final int positionInAdapter);

    /**
     * Returns unique ID of item to support stable ID's logic of RecyclerView's adapter.
     *
     * @param positionInAdapter Position of item in adapter;
     * @return Unique item ID.
     */
    public long getItemId(final int positionInAdapter) {
        return 0;
    }

    /**
     * Creates ViewHolder to bind position to it later.
     *
     * @param parent Container of ViewHolder's view.
     * @return New ViewHolder.
     */
    @NonNull
    public abstract TViewHolder onCreateViewHolder(@NonNull final ViewGroup parent);

    /**
     * Binds position to ViewHolder.
     *
     * @param holder            ViewHolder to bind position to;
     * @param positionInAdapter Position of item in adapter.
     */
    public abstract void onBindViewHolder(@NonNull final TViewHolder holder, final int positionInAdapter);

    /**
     * Binds position with payloads to ViewHolder.
     *
     * @param holder            ViewHolder to bind position to;
     * @param payloads          Payloads;
     * @param positionInAdapter Position of item in adapter.
     */
    public void onBindViewHolder(@NonNull final TViewHolder holder, @NonNull final List<Object> payloads, final int positionInAdapter) {
        //do nothing by default
    }

}
