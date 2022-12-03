package android.support.v7.internal.view.menu;

import android.support.v4.internal.view.SupportMenuItem;
import android.view.MenuItem;
import android.view.SubMenu;
import java.util.HashMap;
import java.util.Iterator;
/* loaded from: classes.dex */
abstract class BaseMenuWrapper<T> extends BaseWrapper<T> {
    private HashMap<MenuItem, SupportMenuItem> mMenuItems;
    private HashMap<SubMenu, SubMenu> mSubMenus;

    /* JADX INFO: Access modifiers changed from: package-private */
    public BaseMenuWrapper(T object) {
        super(object);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public final SupportMenuItem getMenuItemWrapper(MenuItem frameworkItem) {
        if (frameworkItem != null) {
            if (this.mMenuItems == null) {
                this.mMenuItems = new HashMap<>();
            }
            SupportMenuItem compatItem = this.mMenuItems.get(frameworkItem);
            if (compatItem == null) {
                SupportMenuItem compatItem2 = MenuWrapperFactory.createSupportMenuItemWrapper(frameworkItem);
                this.mMenuItems.put(frameworkItem, compatItem2);
                return compatItem2;
            }
            return compatItem;
        }
        return null;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public final SubMenu getSubMenuWrapper(SubMenu frameworkSubMenu) {
        if (frameworkSubMenu != null) {
            if (this.mSubMenus == null) {
                this.mSubMenus = new HashMap<>();
            }
            SubMenu compatSubMenu = this.mSubMenus.get(frameworkSubMenu);
            if (compatSubMenu == null) {
                SubMenu compatSubMenu2 = MenuWrapperFactory.createSupportSubMenuWrapper(frameworkSubMenu);
                this.mSubMenus.put(frameworkSubMenu, compatSubMenu2);
                return compatSubMenu2;
            }
            return compatSubMenu;
        }
        return null;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public final void internalClear() {
        if (this.mMenuItems != null) {
            this.mMenuItems.clear();
        }
        if (this.mSubMenus != null) {
            this.mSubMenus.clear();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public final void internalRemoveGroup(int groupId) {
        if (this.mMenuItems != null) {
            Iterator<MenuItem> iterator = this.mMenuItems.keySet().iterator();
            while (iterator.hasNext()) {
                MenuItem menuItem = iterator.next();
                if (groupId == menuItem.getGroupId()) {
                    iterator.remove();
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public final void internalRemoveItem(int id) {
        if (this.mMenuItems != null) {
            Iterator<MenuItem> iterator = this.mMenuItems.keySet().iterator();
            while (iterator.hasNext()) {
                MenuItem menuItem = iterator.next();
                if (id == menuItem.getItemId()) {
                    iterator.remove();
                    return;
                }
            }
        }
    }
}
