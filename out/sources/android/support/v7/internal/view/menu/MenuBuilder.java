package android.support.v7.internal.view.menu;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.internal.view.SupportMenu;
import android.support.v4.view.ActionProvider;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.appcompat.R;
import android.util.SparseArray;
import android.view.ContextMenu;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
/* loaded from: classes.dex */
public class MenuBuilder implements SupportMenu {
    private static final String ACTION_VIEW_STATES_KEY = "android:menu:actionviewstates";
    private static final String EXPANDED_ACTION_VIEW_ID = "android:menu:expandedactionview";
    private static final String PRESENTER_KEY = "android:menu:presenters";
    private static final String TAG = "MenuBuilder";
    private static final int[] sCategoryToOrder = {1, 4, 5, 3, 2, 0};
    private Callback mCallback;
    private final Context mContext;
    private ContextMenu.ContextMenuInfo mCurrentMenuInfo;
    private MenuItemImpl mExpandedItem;
    Drawable mHeaderIcon;
    CharSequence mHeaderTitle;
    View mHeaderView;
    private boolean mQwertyMode;
    private final Resources mResources;
    private boolean mShortcutsVisible;
    private int mDefaultShowAsAction = 0;
    private boolean mPreventDispatchingItemsChanged = false;
    private boolean mItemsChangedWhileDispatchPrevented = false;
    private boolean mOptionalIconsVisible = false;
    private boolean mIsClosing = false;
    private ArrayList<MenuItemImpl> mTempShortcutItemList = new ArrayList<>();
    private CopyOnWriteArrayList<WeakReference<MenuPresenter>> mPresenters = new CopyOnWriteArrayList<>();
    private ArrayList<MenuItemImpl> mItems = new ArrayList<>();
    private ArrayList<MenuItemImpl> mVisibleItems = new ArrayList<>();
    private boolean mIsVisibleItemsStale = true;
    private ArrayList<MenuItemImpl> mActionItems = new ArrayList<>();
    private ArrayList<MenuItemImpl> mNonActionItems = new ArrayList<>();
    private boolean mIsActionItemsStale = true;

    /* loaded from: classes.dex */
    public interface Callback {
        boolean onMenuItemSelected(MenuBuilder menuBuilder, MenuItem menuItem);

        void onMenuModeChange(MenuBuilder menuBuilder);
    }

    /* loaded from: classes.dex */
    public interface ItemInvoker {
        boolean invokeItem(MenuItemImpl menuItemImpl);
    }

    public MenuBuilder(Context context) {
        this.mContext = context;
        this.mResources = context.getResources();
        setShortcutsVisibleInner(true);
    }

    public MenuBuilder setDefaultShowAsAction(int defaultShowAsAction) {
        this.mDefaultShowAsAction = defaultShowAsAction;
        return this;
    }

    public void addMenuPresenter(MenuPresenter presenter) {
        this.mPresenters.add(new WeakReference<>(presenter));
        presenter.initForMenu(this.mContext, this);
        this.mIsActionItemsStale = true;
    }

    public void removeMenuPresenter(MenuPresenter presenter) {
        Iterator i$ = this.mPresenters.iterator();
        while (i$.hasNext()) {
            WeakReference<MenuPresenter> ref = i$.next();
            MenuPresenter item = ref.get();
            if (item == null || item == presenter) {
                this.mPresenters.remove(ref);
            }
        }
    }

    private void dispatchPresenterUpdate(boolean cleared) {
        if (!this.mPresenters.isEmpty()) {
            stopDispatchingItemsChanged();
            Iterator i$ = this.mPresenters.iterator();
            while (i$.hasNext()) {
                WeakReference<MenuPresenter> ref = i$.next();
                MenuPresenter presenter = ref.get();
                if (presenter == null) {
                    this.mPresenters.remove(ref);
                } else {
                    presenter.updateMenuView(cleared);
                }
            }
            startDispatchingItemsChanged();
        }
    }

    private boolean dispatchSubMenuSelected(SubMenuBuilder subMenu) {
        if (this.mPresenters.isEmpty()) {
            return false;
        }
        boolean result = false;
        Iterator i$ = this.mPresenters.iterator();
        while (i$.hasNext()) {
            WeakReference<MenuPresenter> ref = i$.next();
            MenuPresenter presenter = ref.get();
            if (presenter == null) {
                this.mPresenters.remove(ref);
            } else if (!result) {
                result = presenter.onSubMenuSelected(subMenu);
            }
        }
        return result;
    }

    private void dispatchSaveInstanceState(Bundle outState) {
        Parcelable state;
        if (!this.mPresenters.isEmpty()) {
            SparseArray<Parcelable> presenterStates = new SparseArray<>();
            Iterator i$ = this.mPresenters.iterator();
            while (i$.hasNext()) {
                WeakReference<MenuPresenter> ref = i$.next();
                MenuPresenter presenter = ref.get();
                if (presenter == null) {
                    this.mPresenters.remove(ref);
                } else {
                    int id = presenter.getId();
                    if (id > 0 && (state = presenter.onSaveInstanceState()) != null) {
                        presenterStates.put(id, state);
                    }
                }
            }
            outState.putSparseParcelableArray(PRESENTER_KEY, presenterStates);
        }
    }

    private void dispatchRestoreInstanceState(Bundle state) {
        Parcelable parcel;
        SparseArray<Parcelable> presenterStates = state.getSparseParcelableArray(PRESENTER_KEY);
        if (presenterStates != null && !this.mPresenters.isEmpty()) {
            Iterator i$ = this.mPresenters.iterator();
            while (i$.hasNext()) {
                WeakReference<MenuPresenter> ref = i$.next();
                MenuPresenter presenter = ref.get();
                if (presenter == null) {
                    this.mPresenters.remove(ref);
                } else {
                    int id = presenter.getId();
                    if (id > 0 && (parcel = presenterStates.get(id)) != null) {
                        presenter.onRestoreInstanceState(parcel);
                    }
                }
            }
        }
    }

    public void savePresenterStates(Bundle outState) {
        dispatchSaveInstanceState(outState);
    }

    public void restorePresenterStates(Bundle state) {
        dispatchRestoreInstanceState(state);
    }

    public void saveActionViewStates(Bundle outStates) {
        SparseArray<Parcelable> viewStates = null;
        int itemCount = size();
        for (int i = 0; i < itemCount; i++) {
            MenuItem item = getItem(i);
            View v = MenuItemCompat.getActionView(item);
            if (v != null && v.getId() != -1) {
                if (viewStates == null) {
                    viewStates = new SparseArray<>();
                }
                v.saveHierarchyState(viewStates);
                if (MenuItemCompat.isActionViewExpanded(item)) {
                    outStates.putInt(EXPANDED_ACTION_VIEW_ID, item.getItemId());
                }
            }
            if (item.hasSubMenu()) {
                SubMenuBuilder subMenu = (SubMenuBuilder) item.getSubMenu();
                subMenu.saveActionViewStates(outStates);
            }
        }
        if (viewStates != null) {
            outStates.putSparseParcelableArray(getActionViewStatesKey(), viewStates);
        }
    }

    public void restoreActionViewStates(Bundle states) {
        MenuItem itemToExpand;
        if (states != null) {
            SparseArray<Parcelable> viewStates = states.getSparseParcelableArray(getActionViewStatesKey());
            int itemCount = size();
            for (int i = 0; i < itemCount; i++) {
                MenuItem item = getItem(i);
                View v = MenuItemCompat.getActionView(item);
                if (v != null && v.getId() != -1) {
                    v.restoreHierarchyState(viewStates);
                }
                if (item.hasSubMenu()) {
                    SubMenuBuilder subMenu = (SubMenuBuilder) item.getSubMenu();
                    subMenu.restoreActionViewStates(states);
                }
            }
            int expandedId = states.getInt(EXPANDED_ACTION_VIEW_ID);
            if (expandedId > 0 && (itemToExpand = findItem(expandedId)) != null) {
                MenuItemCompat.expandActionView(itemToExpand);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public String getActionViewStatesKey() {
        return ACTION_VIEW_STATES_KEY;
    }

    public void setCallback(Callback cb) {
        this.mCallback = cb;
    }

    private MenuItem addInternal(int group, int id, int categoryOrder, CharSequence title) {
        int ordering = getOrdering(categoryOrder);
        MenuItemImpl item = new MenuItemImpl(this, group, id, categoryOrder, ordering, title, this.mDefaultShowAsAction);
        if (this.mCurrentMenuInfo != null) {
            item.setMenuInfo(this.mCurrentMenuInfo);
        }
        this.mItems.add(findInsertIndex(this.mItems, ordering), item);
        onItemsChanged(true);
        return item;
    }

    @Override // android.view.Menu
    public MenuItem add(CharSequence title) {
        return addInternal(0, 0, 0, title);
    }

    @Override // android.view.Menu
    public MenuItem add(int titleRes) {
        return addInternal(0, 0, 0, this.mResources.getString(titleRes));
    }

    @Override // android.view.Menu
    public MenuItem add(int group, int id, int categoryOrder, CharSequence title) {
        return addInternal(group, id, categoryOrder, title);
    }

    @Override // android.view.Menu
    public MenuItem add(int group, int id, int categoryOrder, int title) {
        return addInternal(group, id, categoryOrder, this.mResources.getString(title));
    }

    @Override // android.view.Menu
    public SubMenu addSubMenu(CharSequence title) {
        return addSubMenu(0, 0, 0, title);
    }

    @Override // android.view.Menu
    public SubMenu addSubMenu(int titleRes) {
        return addSubMenu(0, 0, 0, this.mResources.getString(titleRes));
    }

    @Override // android.view.Menu
    public SubMenu addSubMenu(int group, int id, int categoryOrder, CharSequence title) {
        MenuItemImpl item = (MenuItemImpl) addInternal(group, id, categoryOrder, title);
        SubMenuBuilder subMenu = new SubMenuBuilder(this.mContext, this, item);
        item.setSubMenu(subMenu);
        return subMenu;
    }

    @Override // android.view.Menu
    public SubMenu addSubMenu(int group, int id, int categoryOrder, int title) {
        return addSubMenu(group, id, categoryOrder, this.mResources.getString(title));
    }

    @Override // android.view.Menu
    public int addIntentOptions(int group, int id, int categoryOrder, ComponentName caller, Intent[] specifics, Intent intent, int flags, MenuItem[] outSpecificItems) {
        PackageManager pm = this.mContext.getPackageManager();
        List<ResolveInfo> lri = pm.queryIntentActivityOptions(caller, specifics, intent, 0);
        int N = lri != null ? lri.size() : 0;
        if ((flags & 1) == 0) {
            removeGroup(group);
        }
        for (int i = 0; i < N; i++) {
            ResolveInfo ri = lri.get(i);
            Intent rintent = new Intent(ri.specificIndex < 0 ? intent : specifics[ri.specificIndex]);
            rintent.setComponent(new ComponentName(ri.activityInfo.applicationInfo.packageName, ri.activityInfo.name));
            MenuItem item = add(group, id, categoryOrder, ri.loadLabel(pm)).setIcon(ri.loadIcon(pm)).setIntent(rintent);
            if (outSpecificItems != null && ri.specificIndex >= 0) {
                outSpecificItems[ri.specificIndex] = item;
            }
        }
        return N;
    }

    @Override // android.view.Menu
    public void removeItem(int id) {
        removeItemAtInt(findItemIndex(id), true);
    }

    @Override // android.view.Menu
    public void removeGroup(int group) {
        int i = findGroupIndex(group);
        if (i >= 0) {
            int maxRemovable = this.mItems.size() - i;
            int numRemoved = 0;
            while (true) {
                int numRemoved2 = numRemoved;
                numRemoved = numRemoved2 + 1;
                if (numRemoved2 >= maxRemovable || this.mItems.get(i).getGroupId() != group) {
                    break;
                }
                removeItemAtInt(i, false);
            }
            onItemsChanged(true);
        }
    }

    private void removeItemAtInt(int index, boolean updateChildrenOnMenuViews) {
        if (index >= 0 && index < this.mItems.size()) {
            this.mItems.remove(index);
            if (updateChildrenOnMenuViews) {
                onItemsChanged(true);
            }
        }
    }

    public void removeItemAt(int index) {
        removeItemAtInt(index, true);
    }

    public void clearAll() {
        this.mPreventDispatchingItemsChanged = true;
        clear();
        clearHeader();
        this.mPreventDispatchingItemsChanged = false;
        this.mItemsChangedWhileDispatchPrevented = false;
        onItemsChanged(true);
    }

    @Override // android.view.Menu
    public void clear() {
        if (this.mExpandedItem != null) {
            collapseItemActionView(this.mExpandedItem);
        }
        this.mItems.clear();
        onItemsChanged(true);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setExclusiveItemChecked(MenuItem item) {
        int group = item.getGroupId();
        int N = this.mItems.size();
        for (int i = 0; i < N; i++) {
            MenuItemImpl curItem = this.mItems.get(i);
            if (curItem.getGroupId() == group && curItem.isExclusiveCheckable() && curItem.isCheckable()) {
                curItem.setCheckedInt(curItem == item);
            }
        }
    }

    @Override // android.view.Menu
    public void setGroupCheckable(int group, boolean checkable, boolean exclusive) {
        int N = this.mItems.size();
        for (int i = 0; i < N; i++) {
            MenuItemImpl item = this.mItems.get(i);
            if (item.getGroupId() == group) {
                item.setExclusiveCheckable(exclusive);
                item.setCheckable(checkable);
            }
        }
    }

    @Override // android.view.Menu
    public void setGroupVisible(int group, boolean visible) {
        int N = this.mItems.size();
        boolean changedAtLeastOneItem = false;
        for (int i = 0; i < N; i++) {
            MenuItemImpl item = this.mItems.get(i);
            if (item.getGroupId() == group && item.setVisibleInt(visible)) {
                changedAtLeastOneItem = true;
            }
        }
        if (changedAtLeastOneItem) {
            onItemsChanged(true);
        }
    }

    @Override // android.view.Menu
    public void setGroupEnabled(int group, boolean enabled) {
        int N = this.mItems.size();
        for (int i = 0; i < N; i++) {
            MenuItemImpl item = this.mItems.get(i);
            if (item.getGroupId() == group) {
                item.setEnabled(enabled);
            }
        }
    }

    @Override // android.view.Menu
    public boolean hasVisibleItems() {
        int size = size();
        for (int i = 0; i < size; i++) {
            MenuItemImpl item = this.mItems.get(i);
            if (item.isVisible()) {
                return true;
            }
        }
        return false;
    }

    @Override // android.view.Menu
    public MenuItem findItem(int id) {
        MenuItem possibleItem;
        int size = size();
        for (int i = 0; i < size; i++) {
            MenuItemImpl item = this.mItems.get(i);
            if (item.getItemId() != id) {
                if (item.hasSubMenu() && (possibleItem = item.getSubMenu().findItem(id)) != null) {
                    return possibleItem;
                }
            } else {
                return item;
            }
        }
        return null;
    }

    public int findItemIndex(int id) {
        int size = size();
        for (int i = 0; i < size; i++) {
            MenuItemImpl item = this.mItems.get(i);
            if (item.getItemId() == id) {
                return i;
            }
        }
        return -1;
    }

    public int findGroupIndex(int group) {
        return findGroupIndex(group, 0);
    }

    public int findGroupIndex(int group, int start) {
        int size = size();
        if (start < 0) {
            start = 0;
        }
        for (int i = start; i < size; i++) {
            MenuItemImpl item = this.mItems.get(i);
            if (item.getGroupId() == group) {
                return i;
            }
        }
        return -1;
    }

    @Override // android.view.Menu
    public int size() {
        return this.mItems.size();
    }

    @Override // android.view.Menu
    public MenuItem getItem(int index) {
        return this.mItems.get(index);
    }

    @Override // android.view.Menu
    public boolean isShortcutKey(int keyCode, KeyEvent event) {
        return findItemWithShortcutForKey(keyCode, event) != null;
    }

    @Override // android.view.Menu
    public void setQwertyMode(boolean isQwerty) {
        this.mQwertyMode = isQwerty;
        onItemsChanged(false);
    }

    private static int getOrdering(int categoryOrder) {
        int index = ((-65536) & categoryOrder) >> 16;
        if (index < 0 || index >= sCategoryToOrder.length) {
            throw new IllegalArgumentException("order does not contain a valid category.");
        }
        return (sCategoryToOrder[index] << 16) | (65535 & categoryOrder);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean isQwertyMode() {
        return this.mQwertyMode;
    }

    public void setShortcutsVisible(boolean shortcutsVisible) {
        if (this.mShortcutsVisible != shortcutsVisible) {
            setShortcutsVisibleInner(shortcutsVisible);
            onItemsChanged(false);
        }
    }

    private void setShortcutsVisibleInner(boolean shortcutsVisible) {
        boolean z = true;
        this.mShortcutsVisible = (shortcutsVisible && this.mResources.getConfiguration().keyboard != 1 && this.mResources.getBoolean(R.bool.abc_config_showMenuShortcutsWhenKeyboardPresent)) ? false : false;
    }

    public boolean isShortcutsVisible() {
        return this.mShortcutsVisible;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public Resources getResources() {
        return this.mResources;
    }

    public Context getContext() {
        return this.mContext;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean dispatchMenuItemSelected(MenuBuilder menu, MenuItem item) {
        return this.mCallback != null && this.mCallback.onMenuItemSelected(menu, item);
    }

    public void changeMenuMode() {
        if (this.mCallback != null) {
            this.mCallback.onMenuModeChange(this);
        }
    }

    private static int findInsertIndex(ArrayList<MenuItemImpl> items, int ordering) {
        for (int i = items.size() - 1; i >= 0; i--) {
            MenuItemImpl item = items.get(i);
            if (item.getOrdering() <= ordering) {
                return i + 1;
            }
        }
        return 0;
    }

    @Override // android.view.Menu
    public boolean performShortcut(int keyCode, KeyEvent event, int flags) {
        MenuItemImpl item = findItemWithShortcutForKey(keyCode, event);
        boolean handled = false;
        if (item != null) {
            handled = performItemAction(item, flags);
        }
        if ((flags & 2) != 0) {
            close(true);
        }
        return handled;
    }

    void findItemsWithShortcutForKey(List<MenuItemImpl> items, int keyCode, KeyEvent event) {
        boolean qwerty = isQwertyMode();
        int metaState = event.getMetaState();
        KeyCharacterMap.KeyData possibleChars = new KeyCharacterMap.KeyData();
        boolean isKeyCodeMapped = event.getKeyData(possibleChars);
        if (isKeyCodeMapped || keyCode == 67) {
            int N = this.mItems.size();
            for (int i = 0; i < N; i++) {
                MenuItemImpl item = this.mItems.get(i);
                if (item.hasSubMenu()) {
                    ((MenuBuilder) item.getSubMenu()).findItemsWithShortcutForKey(items, keyCode, event);
                }
                char shortcutChar = qwerty ? item.getAlphabeticShortcut() : item.getNumericShortcut();
                if ((metaState & 5) == 0 && shortcutChar != 0 && ((shortcutChar == possibleChars.meta[0] || shortcutChar == possibleChars.meta[2] || (qwerty && shortcutChar == '\b' && keyCode == 67)) && item.isEnabled())) {
                    items.add(item);
                }
            }
        }
    }

    MenuItemImpl findItemWithShortcutForKey(int keyCode, KeyEvent event) {
        ArrayList<MenuItemImpl> items = this.mTempShortcutItemList;
        items.clear();
        findItemsWithShortcutForKey(items, keyCode, event);
        if (items.isEmpty()) {
            return null;
        }
        int metaState = event.getMetaState();
        KeyCharacterMap.KeyData possibleChars = new KeyCharacterMap.KeyData();
        event.getKeyData(possibleChars);
        int size = items.size();
        if (size == 1) {
            return items.get(0);
        }
        boolean qwerty = isQwertyMode();
        for (int i = 0; i < size; i++) {
            MenuItemImpl item = items.get(i);
            char shortcutChar = qwerty ? item.getAlphabeticShortcut() : item.getNumericShortcut();
            if ((shortcutChar == possibleChars.meta[0] && (metaState & 2) == 0) || ((shortcutChar == possibleChars.meta[2] && (metaState & 2) != 0) || (qwerty && shortcutChar == '\b' && keyCode == 67))) {
                return item;
            }
        }
        return null;
    }

    @Override // android.view.Menu
    public boolean performIdentifierAction(int id, int flags) {
        return performItemAction(findItem(id), flags);
    }

    public boolean performItemAction(MenuItem item, int flags) {
        MenuItemImpl itemImpl = (MenuItemImpl) item;
        if (itemImpl == null || !itemImpl.isEnabled()) {
            return false;
        }
        boolean invoked = itemImpl.invoke();
        ActionProvider provider = itemImpl.getSupportActionProvider();
        boolean providerHasSubMenu = provider != null && provider.hasSubMenu();
        if (itemImpl.hasCollapsibleActionView()) {
            boolean invoked2 = invoked | itemImpl.expandActionView();
            if (invoked2) {
                close(true);
                return invoked2;
            }
            return invoked2;
        } else if (itemImpl.hasSubMenu() || providerHasSubMenu) {
            close(false);
            if (!itemImpl.hasSubMenu()) {
                itemImpl.setSubMenu(new SubMenuBuilder(getContext(), this, itemImpl));
            }
            SubMenuBuilder subMenu = (SubMenuBuilder) itemImpl.getSubMenu();
            if (providerHasSubMenu) {
                provider.onPrepareSubMenu(subMenu);
            }
            boolean invoked3 = invoked | dispatchSubMenuSelected(subMenu);
            if (!invoked3) {
                close(true);
                return invoked3;
            }
            return invoked3;
        } else if ((flags & 1) == 0) {
            close(true);
            return invoked;
        } else {
            return invoked;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public final void close(boolean allMenusAreClosing) {
        if (!this.mIsClosing) {
            this.mIsClosing = true;
            Iterator i$ = this.mPresenters.iterator();
            while (i$.hasNext()) {
                WeakReference<MenuPresenter> ref = i$.next();
                MenuPresenter presenter = ref.get();
                if (presenter == null) {
                    this.mPresenters.remove(ref);
                } else {
                    presenter.onCloseMenu(this, allMenusAreClosing);
                }
            }
            this.mIsClosing = false;
        }
    }

    @Override // android.view.Menu
    public void close() {
        close(true);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void onItemsChanged(boolean structureChanged) {
        if (!this.mPreventDispatchingItemsChanged) {
            if (structureChanged) {
                this.mIsVisibleItemsStale = true;
                this.mIsActionItemsStale = true;
            }
            dispatchPresenterUpdate(structureChanged);
            return;
        }
        this.mItemsChangedWhileDispatchPrevented = true;
    }

    public void stopDispatchingItemsChanged() {
        if (!this.mPreventDispatchingItemsChanged) {
            this.mPreventDispatchingItemsChanged = true;
            this.mItemsChangedWhileDispatchPrevented = false;
        }
    }

    public void startDispatchingItemsChanged() {
        this.mPreventDispatchingItemsChanged = false;
        if (this.mItemsChangedWhileDispatchPrevented) {
            this.mItemsChangedWhileDispatchPrevented = false;
            onItemsChanged(true);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void onItemVisibleChanged(MenuItemImpl item) {
        this.mIsVisibleItemsStale = true;
        onItemsChanged(true);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void onItemActionRequestChanged(MenuItemImpl item) {
        this.mIsActionItemsStale = true;
        onItemsChanged(true);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public ArrayList<MenuItemImpl> getVisibleItems() {
        if (!this.mIsVisibleItemsStale) {
            return this.mVisibleItems;
        }
        this.mVisibleItems.clear();
        int itemsSize = this.mItems.size();
        for (int i = 0; i < itemsSize; i++) {
            MenuItemImpl item = this.mItems.get(i);
            if (item.isVisible()) {
                this.mVisibleItems.add(item);
            }
        }
        this.mIsVisibleItemsStale = false;
        this.mIsActionItemsStale = true;
        return this.mVisibleItems;
    }

    public void flagActionItems() {
        if (this.mIsActionItemsStale) {
            boolean flagged = false;
            Iterator i$ = this.mPresenters.iterator();
            while (i$.hasNext()) {
                WeakReference<MenuPresenter> ref = i$.next();
                MenuPresenter presenter = ref.get();
                if (presenter == null) {
                    this.mPresenters.remove(ref);
                } else {
                    flagged |= presenter.flagActionItems();
                }
            }
            if (flagged) {
                this.mActionItems.clear();
                this.mNonActionItems.clear();
                ArrayList<MenuItemImpl> visibleItems = getVisibleItems();
                int itemsSize = visibleItems.size();
                for (int i = 0; i < itemsSize; i++) {
                    MenuItemImpl item = visibleItems.get(i);
                    if (item.isActionButton()) {
                        this.mActionItems.add(item);
                    } else {
                        this.mNonActionItems.add(item);
                    }
                }
            } else {
                this.mActionItems.clear();
                this.mNonActionItems.clear();
                this.mNonActionItems.addAll(getVisibleItems());
            }
            this.mIsActionItemsStale = false;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public ArrayList<MenuItemImpl> getActionItems() {
        flagActionItems();
        return this.mActionItems;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public ArrayList<MenuItemImpl> getNonActionItems() {
        flagActionItems();
        return this.mNonActionItems;
    }

    public void clearHeader() {
        this.mHeaderIcon = null;
        this.mHeaderTitle = null;
        this.mHeaderView = null;
        onItemsChanged(false);
    }

    private void setHeaderInternal(int titleRes, CharSequence title, int iconRes, Drawable icon, View view) {
        Resources r = getResources();
        if (view != null) {
            this.mHeaderView = view;
            this.mHeaderTitle = null;
            this.mHeaderIcon = null;
        } else {
            if (titleRes > 0) {
                this.mHeaderTitle = r.getText(titleRes);
            } else if (title != null) {
                this.mHeaderTitle = title;
            }
            if (iconRes > 0) {
                this.mHeaderIcon = r.getDrawable(iconRes);
            } else if (icon != null) {
                this.mHeaderIcon = icon;
            }
            this.mHeaderView = null;
        }
        onItemsChanged(false);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public MenuBuilder setHeaderTitleInt(CharSequence title) {
        setHeaderInternal(0, title, 0, null, null);
        return this;
    }

    protected MenuBuilder setHeaderTitleInt(int titleRes) {
        setHeaderInternal(titleRes, null, 0, null, null);
        return this;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public MenuBuilder setHeaderIconInt(Drawable icon) {
        setHeaderInternal(0, null, 0, icon, null);
        return this;
    }

    protected MenuBuilder setHeaderIconInt(int iconRes) {
        setHeaderInternal(0, null, iconRes, null, null);
        return this;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public MenuBuilder setHeaderViewInt(View view) {
        setHeaderInternal(0, null, 0, null, view);
        return this;
    }

    public CharSequence getHeaderTitle() {
        return this.mHeaderTitle;
    }

    public Drawable getHeaderIcon() {
        return this.mHeaderIcon;
    }

    public View getHeaderView() {
        return this.mHeaderView;
    }

    public MenuBuilder getRootMenu() {
        return this;
    }

    public void setCurrentMenuInfo(ContextMenu.ContextMenuInfo menuInfo) {
        this.mCurrentMenuInfo = menuInfo;
    }

    void setOptionalIconsVisible(boolean visible) {
        this.mOptionalIconsVisible = visible;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean getOptionalIconsVisible() {
        return this.mOptionalIconsVisible;
    }

    public boolean expandItemActionView(MenuItemImpl item) {
        if (this.mPresenters.isEmpty()) {
            return false;
        }
        boolean expanded = false;
        stopDispatchingItemsChanged();
        Iterator i$ = this.mPresenters.iterator();
        while (i$.hasNext()) {
            WeakReference<MenuPresenter> ref = i$.next();
            MenuPresenter presenter = ref.get();
            if (presenter == null) {
                this.mPresenters.remove(ref);
            } else {
                expanded = presenter.expandItemActionView(this, item);
                if (expanded) {
                    break;
                }
            }
        }
        startDispatchingItemsChanged();
        if (expanded) {
            this.mExpandedItem = item;
            return expanded;
        }
        return expanded;
    }

    public boolean collapseItemActionView(MenuItemImpl item) {
        if (this.mPresenters.isEmpty() || this.mExpandedItem != item) {
            return false;
        }
        boolean collapsed = false;
        stopDispatchingItemsChanged();
        Iterator i$ = this.mPresenters.iterator();
        while (i$.hasNext()) {
            WeakReference<MenuPresenter> ref = i$.next();
            MenuPresenter presenter = ref.get();
            if (presenter == null) {
                this.mPresenters.remove(ref);
            } else {
                collapsed = presenter.collapseItemActionView(this, item);
                if (collapsed) {
                    break;
                }
            }
        }
        startDispatchingItemsChanged();
        if (collapsed) {
            this.mExpandedItem = null;
            return collapsed;
        }
        return collapsed;
    }

    public MenuItemImpl getExpandedItem() {
        return this.mExpandedItem;
    }
}
