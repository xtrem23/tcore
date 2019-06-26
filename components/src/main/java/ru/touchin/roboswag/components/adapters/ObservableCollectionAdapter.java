/*
 *  Copyright (c) 2015 RoboSwag (Gavriil Sitnikov, Vsevolod Ivanov)
 *
 *  This file is part of RoboSwag library.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package ru.touchin.roboswag.components.adapters;

import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.BehaviorSubject;
import ru.touchin.roboswag.components.utils.LifecycleBindable;
import ru.touchin.roboswag.components.utils.UiUtils;
import ru.touchin.roboswag.core.log.Lc;
import ru.touchin.roboswag.core.observables.collections.ObservableCollection;
import ru.touchin.roboswag.core.observables.collections.ObservableList;
import ru.touchin.roboswag.core.observables.collections.changes.Change;
import ru.touchin.roboswag.core.observables.collections.changes.ChangePayloadProducer;
import ru.touchin.roboswag.core.observables.collections.changes.CollectionChanges;
import ru.touchin.roboswag.core.observables.collections.changes.SameItemsPredicate;
import ru.touchin.roboswag.core.observables.collections.loadable.LoadingMoreList;
import ru.touchin.roboswag.core.utils.Optional;
import ru.touchin.roboswag.core.utils.ShouldNotHappenException;

/**
 * Created by Gavriil Sitnikov on 20/11/2015.
 * Adapter based on {@link ObservableCollection} and providing some useful features like:
 * - item-based binding method;
 * - delegates by {@link AdapterDelegate} over itemViewType logic;
 * - item click listener setup by {@link #setOnItemClickListener(OnItemClickListener)};
 * - allows to inform about footers/headers by overriding base create/bind methods and {@link #getHeadersCount()} plus {@link #getFootersCount()};
 * - by default it is pre-loading items for collections like {@link ru.touchin.roboswag.core.observables.collections.loadable.LoadingMoreList}.
 *
 * @param <TItem>           Type of items to bind to ViewHolders;
 * @param <TItemViewHolder> Type of ViewHolders to show items.
 */
@SuppressWarnings({"unchecked", "PMD.TooManyMethods"})
//TooManyMethods: it's ok
public abstract class ObservableCollectionAdapter<TItem, TItemViewHolder extends BindableViewHolder>
        extends RecyclerView.Adapter<BindableViewHolder> {

    private static final int PRE_LOADING_COUNT = 20;

    private static boolean inDebugMode;

    /**
     * Enables debugging features like checking concurrent delegates.
     */
    public static void setInDebugMode() {
        inDebugMode = true;
    }

    @NonNull
    private final BehaviorSubject<Optional<ObservableCollection<TItem>>> observableCollectionSubject
            = BehaviorSubject.createDefault(new Optional<>(null));
    @NonNull
    private final BehaviorSubject<Boolean> moreAutoLoadingRequested = BehaviorSubject.create();
    @NonNull
    private final LifecycleBindable lifecycleBindable;
    @Nullable
    private Object onItemClickListener;
    private int lastUpdatedChangeNumber = -1;

    @NonNull
    private final ObservableList<TItem> innerCollection = new ObservableList<>();
    private boolean anyChangeApplied;
    private long itemClickDelayMillis;
    @NonNull
    private final List<RecyclerView> attachedRecyclerViews = new LinkedList<>();
    @NonNull
    private final List<AdapterDelegate<? extends BindableViewHolder>> delegates = new ArrayList<>();

    public ObservableCollectionAdapter(@NonNull final LifecycleBindable lifecycleBindable) {
        super();
        this.lifecycleBindable = lifecycleBindable;
        lifecycleBindable.untilDestroy(innerCollection.observeChanges(), this::onItemsChanged);
        lifecycleBindable.untilDestroy(observableCollectionSubject
                .switchMap(optional -> {
                    final ObservableCollection<TItem> collection = optional.get();
                    if (collection instanceof ObservableList) {
                        innerCollection.setDiffUtilsSource((ObservableList<TItem>) collection);
                    } else {
                        innerCollection.setDiffUtilsSource(null);
                    }
                    return collection != null ? collection.observeItems() : Observable.just(Collections.emptyList());
                }), innerCollection::set);
        lifecycleBindable.untilDestroy(createMoreAutoLoadingObservable());
    }

    @NonNull
    private Observable createMoreAutoLoadingObservable() {
        return observableCollectionSubject
                .switchMap(collectionOptional -> {
                    final ObservableCollection<TItem> collection = collectionOptional.get();
                    if (!(collection instanceof LoadingMoreList)) {
                        return Observable.empty();
                    }
                    return moreAutoLoadingRequested
                            .distinctUntilChanged()
                            .switchMap(requested -> {
                                if (!requested) {
                                    return Observable.empty();
                                }
                                final int size = collection.size();
                                return ((LoadingMoreList<?, ?, ?>) collection)
                                        .loadRange(size, size + PRE_LOADING_COUNT)
                                        .onErrorReturnItem(new ArrayList<>())
                                        .toObservable()
                                        .doOnComplete(() -> moreAutoLoadingRequested.onNext(false));
                            });
                });
    }

    /**
     * Returns if any change of source collection applied to adapter.
     * It's important to not show some footers or headers before first change have applied.
     *
     * @return True id any change applied.
     */
    public boolean isAnyChangeApplied() {
        return anyChangeApplied;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull final RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        attachedRecyclerViews.add(recyclerView);
    }

    private boolean anyRecyclerViewShown() {
        for (final RecyclerView recyclerView : attachedRecyclerViews) {
            if (recyclerView.isShown()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull final RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        attachedRecyclerViews.remove(recyclerView);
    }

    /**
     * Returns parent {@link LifecycleBindable} (Activity/ViewController etc.).
     *
     * @return Parent {@link LifecycleBindable}.
     */
    @NonNull
    public LifecycleBindable getLifecycleBindable() {
        return lifecycleBindable;
    }

    /**
     * Returns {@link ObservableCollection} which provides items and it's changes.
     *
     * @return Inner {@link ObservableCollection}.
     */
    @Nullable
    public ObservableCollection<TItem> getObservableCollection() {
        return observableCollectionSubject.getValue().get();
    }

    /**
     * Method to observe {@link ObservableCollection} which provides items and it's changes.
     *
     * @return Observable of inner {@link ObservableCollection}.
     */
    @NonNull
    public Observable<Optional<ObservableCollection<TItem>>> observeObservableCollection() {
        return observableCollectionSubject;
    }

    /**
     * Sets {@link ObservableCollection} which will provide items and it's changes.
     *
     * @param observableCollection Inner {@link ObservableCollection}.
     */
    public void setObservableCollection(@Nullable final ObservableCollection<TItem> observableCollection) {
        this.observableCollectionSubject.onNext(new Optional<>(observableCollection));
    }

    /**
     * Simply sets items.
     *
     * @param items Items to set.
     */
    public void setItems(@NonNull final Collection<TItem> items) {
        setObservableCollection(new ObservableList<>(items));
    }

    /**
     * Calls when collection changes.
     *
     * @param collectionChanges Changes of collection.
     */
    protected void onItemsChanged(@NonNull final CollectionChanges<TItem> collectionChanges) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            Lc.assertion("Items changes called on not main thread");
            return;
        }
        if (!anyChangeApplied || !anyRecyclerViewShown()) {
            anyChangeApplied = true;
            refreshUpdate();
            return;
        }
        if (collectionChanges.getNumber() != innerCollection.getChangesCount()
                || collectionChanges.getNumber() != lastUpdatedChangeNumber + 1) {
            if (lastUpdatedChangeNumber < collectionChanges.getNumber()) {
                refreshUpdate();
            }
            return;
        }
        notifyAboutChanges(collectionChanges.getChanges());
        lastUpdatedChangeNumber = innerCollection.getChangesCount();
    }

    private void refreshUpdate() {
        notifyDataSetChanged();
        lastUpdatedChangeNumber = innerCollection.getChangesCount();
    }

    private void notifyAboutChanges(@NonNull final Collection<Change> changes) {
        for (final Change change : changes) {
            if (change instanceof Change.Inserted) {
                final Change.Inserted castedChange = (Change.Inserted) change;
                notifyItemRangeInserted(castedChange.getPosition() + getHeadersCount(), castedChange.getCount());
            } else if (change instanceof Change.Removed) {
                if (getItemCount() - getHeadersCount() == 0) {
                    //TODO: bug of recyclerview?
                    notifyDataSetChanged();
                } else {
                    final Change.Removed castedChange = (Change.Removed) change;
                    notifyItemRangeRemoved(castedChange.getPosition() + getHeadersCount(), castedChange.getCount());
                }
            } else if (change instanceof Change.Moved) {
                final Change.Moved castedChange = (Change.Moved) change;
                notifyItemMoved(castedChange.getFromPosition() + getHeadersCount(), castedChange.getToPosition() + getHeadersCount());
            } else if (change instanceof Change.Changed) {
                final Change.Changed castedChange = (Change.Changed) change;
                notifyItemRangeChanged(
                        castedChange.getPosition() + getHeadersCount(),
                        castedChange.getCount(),
                        castedChange.getPayload());
            } else {
                Lc.assertion("Not supported " + change);
            }
        }
    }

    /**
     * Returns headers count goes before items.
     *
     * @return Headers count.
     */
    protected int getHeadersCount() {
        return 0;
    }

    /**
     * Returns footers count goes after items and headers.
     *
     * @return Footers count.
     */
    protected int getFootersCount() {
        return 0;
    }

    /**
     * Returns list of added delegates.
     *
     * @return List of {@link AdapterDelegate}.
     */
    @NonNull
    public List<AdapterDelegate<? extends BindableViewHolder>> getDelegates() {
        return Collections.unmodifiableList(delegates);
    }

    /**
     * Adds {@link ItemAdapterDelegate} to adapter.
     *
     * @param delegate Delegate to add.
     */
    public void addDelegate(@NonNull final ItemAdapterDelegate<? extends TItemViewHolder, ? extends TItem> delegate) {
        addDelegateInternal(delegate);
    }

    /**
     * Adds {@link PositionAdapterDelegate} to adapter.
     *
     * @param delegate Delegate to add.
     */
    public void addDelegate(@NonNull final PositionAdapterDelegate<? extends BindableViewHolder> delegate) {
        addDelegateInternal(delegate);
    }

    private void addDelegateInternal(@NonNull final AdapterDelegate<? extends BindableViewHolder> delegate) {
        if (inDebugMode) {
            for (final AdapterDelegate addedDelegate : delegates) {
                if (addedDelegate.getItemViewType() == delegate.getItemViewType()) {
                    Lc.assertion("AdapterDelegate with viewType=" + delegate.getItemViewType() + " already added");
                    return;
                }
            }
        }
        delegates.add(delegate);
        notifyDataSetChanged();
    }

    /**
     * Removes {@link AdapterDelegate} from adapter.
     *
     * @param delegate Delegate to remove.
     */
    public void removeDelegate(@NonNull final AdapterDelegate<? extends BindableViewHolder> delegate) {
        delegates.remove(delegate);
        notifyDataSetChanged();
    }

    private void checkDelegates(@Nullable final AdapterDelegate alreadyPickedDelegate, @NonNull final AdapterDelegate currentDelegate) {
        if (alreadyPickedDelegate != null) {
            throw new ShouldNotHappenException("Concurrent delegates: " + currentDelegate + " and " + alreadyPickedDelegate);
        }
    }

    private int getItemPositionInCollection(final int positionInAdapter) {
        final int shiftedPosition = positionInAdapter - getHeadersCount();
        return shiftedPosition >= 0 && shiftedPosition < innerCollection.size() ? shiftedPosition : -1;
    }

    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.ModifiedCyclomaticComplexity", "PMD.StdCyclomaticComplexity", "PMD.NPathComplexity"})
    //Complexity: because of debug code
    @Override
    public int getItemViewType(final int positionInAdapter) {
        AdapterDelegate delegateOfViewType = null;
        final int positionInCollection = getItemPositionInCollection(positionInAdapter);
        final TItem item = positionInCollection >= 0 ? innerCollection.get(positionInCollection) : null;
        for (final AdapterDelegate<?> delegate : delegates) {
            if (delegate instanceof ItemAdapterDelegate) {
                if (item != null && ((ItemAdapterDelegate) delegate).isForViewType(item, positionInAdapter, positionInCollection)) {
                    checkDelegates(delegateOfViewType, delegate);
                    delegateOfViewType = delegate;
                    if (!inDebugMode) {
                        break;
                    }
                }
            } else if (delegate instanceof PositionAdapterDelegate) {
                if (((PositionAdapterDelegate) delegate).isForViewType(positionInAdapter)) {
                    checkDelegates(delegateOfViewType, delegate);
                    delegateOfViewType = delegate;
                    if (!inDebugMode) {
                        break;
                    }
                }
            } else {
                Lc.assertion("Delegate of type " + delegate.getClass());
            }
        }

        return delegateOfViewType != null ? delegateOfViewType.getItemViewType() : super.getItemViewType(positionInAdapter);
    }

    @Override
    public long getItemId(final int positionInAdapter) {
        final LongContainer result = new LongContainer();
        tryDelegateAction(positionInAdapter,
                (itemAdapterDelegate, positionInCollection) ->
                        result.value = itemAdapterDelegate.getItemId(innerCollection.get(positionInCollection),
                                positionInAdapter, positionInCollection),
                positionAdapterDelegate -> result.value = positionAdapterDelegate.getItemId(positionInAdapter),
                (positionInCollection) -> result.value = super.getItemId(positionInAdapter));
        return result.value;
    }

    @SuppressWarnings("PMD.CyclomaticComplexity")
    private void tryDelegateAction(final int positionInAdapter,
                                   @NonNull final BiConsumer<ItemAdapterDelegate, Integer> itemAdapterDelegateAction,
                                   @NonNull final Consumer<PositionAdapterDelegate> positionAdapterDelegateAction,
                                   @NonNull final Consumer<Integer> defaultAction) {
        final int viewType = getItemViewType(positionInAdapter);
        final int positionInCollection = getItemPositionInCollection(positionInAdapter);
        for (final AdapterDelegate<?> delegate : delegates) {
            if (delegate instanceof ItemAdapterDelegate) {
                if (positionInCollection >= 0 && viewType == delegate.getItemViewType()) {
                    try {
                        itemAdapterDelegateAction.accept((ItemAdapterDelegate) delegate, positionInCollection);
                    } catch (final Exception exception) {
                        Lc.assertion(exception);
                    }
                    return;
                }
            } else if (delegate instanceof PositionAdapterDelegate) {
                if (viewType == delegate.getItemViewType()) {
                    try {
                        positionAdapterDelegateAction.accept((PositionAdapterDelegate) delegate);
                    } catch (final Exception exception) {
                        Lc.assertion(exception);
                    }
                    return;
                }
            } else {
                Lc.assertion("Delegate of type " + delegate.getClass());
            }
        }
        try {
            defaultAction.accept(positionInCollection);
        } catch (final Exception exception) {
            Lc.assertion(exception);
        }
    }

    @Override
    public int getItemCount() {
        return getHeadersCount() + innerCollection.size() + getFootersCount();
    }

    @NonNull
    @Override
    public BindableViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        for (final AdapterDelegate<?> delegate : delegates) {
            if (delegate.getItemViewType() == viewType) {
                return delegate.onCreateViewHolder(parent);
            }
        }
        throw new ShouldNotHappenException("Add some AdapterDelegate or override this method");
    }

    @Override
    public void onBindViewHolder(@NonNull final BindableViewHolder holder, final int positionInAdapter) {
        lastUpdatedChangeNumber = innerCollection.getChangesCount();

        tryDelegateAction(positionInAdapter,
                (itemAdapterDelegate, positionInCollection) -> {
                    bindItemViewHolder(itemAdapterDelegate, holder, innerCollection.get(positionInCollection),
                            null, positionInAdapter, positionInCollection);
                    updateMoreAutoLoadingRequest(positionInCollection);
                },
                positionAdapterDelegate -> positionAdapterDelegate.onBindViewHolder(holder, positionInAdapter),
                (positionInCollection) -> {
                    if (positionInCollection >= 0) {
                        bindItemViewHolder(null, holder, innerCollection.get(positionInCollection), null, positionInAdapter, positionInCollection);
                    }
                });
    }

    @Override
    public void onBindViewHolder(@NonNull final BindableViewHolder holder, final int positionInAdapter, @NonNull final List<Object> payloads) {
        super.onBindViewHolder(holder, positionInAdapter, payloads);
        tryDelegateAction(positionInAdapter,
                (itemAdapterDelegate, positionInCollection) -> {
                    bindItemViewHolder(itemAdapterDelegate, holder, innerCollection.get(positionInCollection),
                            payloads, positionInAdapter, positionInCollection);
                    updateMoreAutoLoadingRequest(positionInCollection);
                },
                positionAdapterDelegate -> positionAdapterDelegate.onBindViewHolder(holder, positionInAdapter),
                (positionInCollection) -> {
                    if (positionInCollection >= 0) {
                        bindItemViewHolder(null, holder, innerCollection.get(positionInCollection),
                                payloads, positionInAdapter, positionInCollection);
                    }
                });
    }

    private void bindItemViewHolder(@Nullable final ItemAdapterDelegate<TItemViewHolder, TItem> itemAdapterDelegate,
                                    @NonNull final BindableViewHolder holder, @NonNull final TItem item, @Nullable final List<Object> payloads,
                                    final int positionInAdapter, final int positionInCollection) {
        final TItemViewHolder itemViewHolder;
        try {
            itemViewHolder = (TItemViewHolder) holder;
        } catch (final ClassCastException exception) {
            Lc.assertion(exception);
            return;
        }
        updateClickListener(holder, item, positionInAdapter, positionInCollection);
        if (itemAdapterDelegate != null) {
            if (payloads == null) {
                itemAdapterDelegate.onBindViewHolder(itemViewHolder, item, positionInAdapter, positionInCollection);
            } else {
                itemAdapterDelegate.onBindViewHolder(itemViewHolder, item, payloads, positionInAdapter, positionInCollection);
            }
        } else {
            if (payloads == null) {
                onBindItemToViewHolder(itemViewHolder, positionInAdapter, item);
            } else {
                onBindItemToViewHolder(itemViewHolder, positionInAdapter, item, payloads);
            }
        }
    }

    private void updateClickListener(@NonNull final BindableViewHolder holder, @NonNull final TItem item,
                                     final int positionInAdapter, final int positionInCollection) {
        if (onItemClickListener != null && !isOnClickListenerDisabled(item, positionInAdapter, positionInCollection)) {
            UiUtils.setOnRippleClickListener(holder.itemView,
                    () -> {
                        if (onItemClickListener instanceof OnItemClickListener) {
                            ((OnItemClickListener) onItemClickListener).onItemClicked(item);
                        } else if (onItemClickListener instanceof OnItemWithPositionClickListener) {
                            ((OnItemWithPositionClickListener) onItemClickListener).onItemClicked(item, positionInAdapter, positionInCollection);
                        } else {
                            Lc.assertion("Unexpected onItemClickListener type " + onItemClickListener);
                        }
                    },
                    itemClickDelayMillis);
        }
    }

    private void updateMoreAutoLoadingRequest(final int positionInCollection) {
        if (positionInCollection > innerCollection.size() - PRE_LOADING_COUNT) {
            return;
        }
        moreAutoLoadingRequested.onNext(true);
    }

    /**
     * Method to bind item (from {@link #getObservableCollection()}) to item-specific ViewHolder.
     * It is not calling for headers and footer which counts are returned by {@link #getHeadersCount()} and @link #getFootersCount()}.
     * You don't need to override this method if you have delegates for every view type.
     *
     * @param holder            ViewHolder to bind item to;
     * @param positionInAdapter Position of ViewHolder (NOT item!);
     * @param item              Item returned by position (WITH HEADER OFFSET!).
     */
    protected void onBindItemToViewHolder(@NonNull final TItemViewHolder holder, final int positionInAdapter, @NonNull final TItem item) {
        // do nothing by default - let delegates do it
    }

    /**
     * Method to bind item (from {@link #getObservableCollection()}) to item-specific ViewHolder with payloads.
     * It is not calling for headers and footer which counts are returned by {@link #getHeadersCount()} and @link #getFootersCount()}.
     *
     * @param holder            ViewHolder to bind item to;
     * @param positionInAdapter Position of ViewHolder in adapter (NOT item!);
     * @param item              Item returned by position (WITH HEADER OFFSET!);
     * @param payloads          Payloads.
     */
    protected void onBindItemToViewHolder(@NonNull final TItemViewHolder holder, final int positionInAdapter, @NonNull final TItem item,
                                          @NonNull final List<Object> payloads) {
        // do nothing by default - let delegates do it
    }

    @Nullable
    public TItem getItem(final int positionInAdapter) {
        final int positionInCollection = getItemPositionInCollection(positionInAdapter);
        return positionInCollection >= 0 ? innerCollection.get(positionInCollection) : null;
    }

    /**
     * Sets item click listener.
     *
     * @param onItemClickListener Item click listener.
     */
    public void setOnItemClickListener(@Nullable final OnItemClickListener<TItem> onItemClickListener) {
        this.setOnItemClickListener(onItemClickListener, UiUtils.RIPPLE_EFFECT_DELAY);
    }

    /**
     * Sets item click listener.
     *
     * @param onItemClickListener  Item click listener;
     * @param itemClickDelayMillis Delay of calling click listener.
     */
    public void setOnItemClickListener(@Nullable final OnItemClickListener<TItem> onItemClickListener, final long itemClickDelayMillis) {
        this.onItemClickListener = onItemClickListener;
        this.itemClickDelayMillis = itemClickDelayMillis;
        refreshUpdate();
    }

    /**
     * Sets item click listener.
     *
     * @param onItemClickListener Item click listener.
     */
    public void setOnItemClickListener(@Nullable final OnItemWithPositionClickListener<TItem> onItemClickListener) {
        this.setOnItemClickListener(onItemClickListener, UiUtils.RIPPLE_EFFECT_DELAY);
    }

    /**
     * Sets item click listener.
     *
     * @param onItemClickListener  Item click listener;
     * @param itemClickDelayMillis Delay of calling click listener.
     */
    public void setOnItemClickListener(@Nullable final OnItemWithPositionClickListener<TItem> onItemClickListener, final long itemClickDelayMillis) {
        this.onItemClickListener = onItemClickListener;
        this.itemClickDelayMillis = itemClickDelayMillis;
        refreshUpdate();
    }

    /**
     * Returns if click listening disabled or not for specific item.
     *
     * @param item                 Item to check click availability;
     * @param positionInAdapter    Position of clicked item in adapter (with headers);
     * @param positionInCollection Position of clicked item in inner collection;
     * @return True if click listener enabled for such item.
     */
    public boolean isOnClickListenerDisabled(@NonNull final TItem item, final int positionInAdapter, final int positionInCollection) {
        return false;
    }

    /**
     * Enable diff utils algorithm in collection changes.
     *
     * @param detectMoves           The flag that determines whether the {@link Change.Moved} changes will be generated or not;
     * @param sameItemsPredicate    Predicate for the determination of the same elements;
     * @param changePayloadProducer Function that calculate change payload when items the same but contents are different.
     */
    public void enableDiffUtils(final boolean detectMoves,
                                @NonNull final SameItemsPredicate<TItem> sameItemsPredicate,
                                @Nullable final ChangePayloadProducer<TItem> changePayloadProducer) {
        innerCollection.enableDiffUtils(detectMoves, sameItemsPredicate, changePayloadProducer);
    }

    /**
     * Disable diff utils algorithm.
     */
    public void disableDiffUtils() {
        innerCollection.disableDiffUtils();
    }

    /**
     * Returns enabled flag of diff utils.
     *
     * @return true if diff utils is enabled.
     */
    public boolean diffUtilsIsEnabled() {
        return innerCollection.diffUtilsIsEnabled();
    }

    /**
     * Interface to simply add item click listener.
     *
     * @param <TItem> Type of item
     */
    public interface OnItemClickListener<TItem> {

        /**
         * Calls when item have clicked.
         *
         * @param item Clicked item.
         */
        void onItemClicked(@NonNull TItem item);

    }

    /**
     * Interface to simply add item click listener based on item position in adapter and collection.
     *
     * @param <TItem> Type of item
     */
    public interface OnItemWithPositionClickListener<TItem> {

        /**
         * Calls when item have clicked.
         *
         * @param item                 Clicked item;
         * @param positionInAdapter    Position of clicked item in adapter (with headers);
         * @param positionInCollection Position of clicked item in inner collection.
         */
        void onItemClicked(@NonNull TItem item, final int positionInAdapter, final int positionInCollection);

    }

    private class LongContainer {

        private long value;

    }

}
