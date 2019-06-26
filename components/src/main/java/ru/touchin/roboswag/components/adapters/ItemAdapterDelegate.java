package ru.touchin.roboswag.components.adapters;

import android.support.annotation.NonNull;
import android.view.ViewGroup;

import java.util.List;

import ru.touchin.roboswag.components.utils.LifecycleBindable;

/**
 * Objects of such class controls creation and binding of specific type of RecyclerView's ViewHolders.
 * Such delegates are creating and binding ViewHolders for specific items.
 * Default {@link #getItemViewType} is generating on construction of object.
 *
 * @param <TViewHolder> Type of {@link BindableViewHolder} of delegate;
 * @param <TItem>       Type of items to bind to {@link BindableViewHolder}s.
 */
public abstract class ItemAdapterDelegate<TViewHolder extends BindableViewHolder, TItem> extends AdapterDelegate<TViewHolder> {

    public ItemAdapterDelegate(@NonNull final LifecycleBindable parentLifecycleBindable) {
        super(parentLifecycleBindable);
    }

    /**
     * Returns if object is processable by this delegate.
     * This item will be casted to {@link TItem} and passes to {@link #onBindViewHolder(TViewHolder, TItem, int, int)}.
     *
     * @param item                   Item to check;
     * @param positionInAdapter      Position of item in adapter;
     * @param itemCollectionPosition Position of item in collection that contains item;
     * @return True if item is processable by this delegate.
     */
    public abstract boolean isForViewType(@NonNull final Object item, final int positionInAdapter, final int itemCollectionPosition);

    /**
     * Returns unique ID of item to support stable ID's logic of RecyclerView's adapter.
     *
     * @param item                 Item to check;
     * @param positionInAdapter    Position of item in adapter;
     * @param positionInCollection Position of item in collection that contains item;
     * @return Unique item ID.
     */
    public long getItemId(@NonNull final TItem item, final int positionInAdapter, final int positionInCollection) {
        return 0;
    }

    /**
     * Creates ViewHolder to bind item to it later.
     *
     * @param parent Container of ViewHolder's view.
     * @return New ViewHolder.
     */
    @NonNull
    public abstract TViewHolder onCreateViewHolder(@NonNull final ViewGroup parent);

    /**
     * Binds item to created by this object ViewHolder.
     *
     * @param holder               ViewHolder to bind item to;
     * @param item                 Item to check;
     * @param positionInAdapter    Position of item in adapter;
     * @param positionInCollection Position of item in collection that contains item;
     */
    public abstract void onBindViewHolder(@NonNull final TViewHolder holder, @NonNull final TItem item,
                                          final int positionInAdapter, final int positionInCollection);

    /**
     * Binds item with payloads to created by this object ViewHolder.
     *
     * @param holder               ViewHolder to bind item to;
     * @param item                 Item to check;
     * @param payloads             Payloads;
     * @param positionInAdapter    Position of item in adapter;
     * @param positionInCollection Position of item in collection that contains item;
     */
    public void onBindViewHolder(@NonNull final TViewHolder holder, @NonNull final TItem item, @NonNull final List<Object> payloads,
                                 final int positionInAdapter, final int positionInCollection) {
        //do nothing by default
    }

}
