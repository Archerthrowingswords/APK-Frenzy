package android.support.v7.internal.view.menu;

import android.support.v4.view.ActionProvider;
import android.support.v7.internal.view.menu.MenuItemWrapperICS;
import android.view.ActionProvider;
import android.view.MenuItem;
import android.view.View;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public class MenuItemWrapperJB extends MenuItemWrapperICS {
    /* JADX INFO: Access modifiers changed from: package-private */
    public MenuItemWrapperJB(MenuItem object) {
        super(object, false);
    }

    @Override // android.support.v7.internal.view.menu.MenuItemWrapperICS
    MenuItemWrapperICS.ActionProviderWrapper createActionProviderWrapper(ActionProvider provider) {
        return new ActionProviderWrapperJB(provider);
    }

    /* loaded from: classes.dex */
    class ActionProviderWrapperJB extends MenuItemWrapperICS.ActionProviderWrapper implements ActionProvider.VisibilityListener {
        ActionProvider.VisibilityListener mListener;

        public ActionProviderWrapperJB(android.support.v4.view.ActionProvider inner) {
            super(inner);
        }

        @Override // android.view.ActionProvider
        public View onCreateActionView(MenuItem forItem) {
            return this.mInner.onCreateActionView(forItem);
        }

        @Override // android.view.ActionProvider
        public boolean overridesItemVisibility() {
            return this.mInner.overridesItemVisibility();
        }

        @Override // android.view.ActionProvider
        public boolean isVisible() {
            return this.mInner.isVisible();
        }

        @Override // android.view.ActionProvider
        public void refreshVisibility() {
            this.mInner.refreshVisibility();
        }

        @Override // android.view.ActionProvider
        public void setVisibilityListener(ActionProvider.VisibilityListener listener) {
            this.mListener = listener;
            android.support.v4.view.ActionProvider actionProvider = this.mInner;
            if (listener == null) {
                this = null;
            }
            actionProvider.setVisibilityListener(this);
        }

        @Override // android.support.v4.view.ActionProvider.VisibilityListener
        public void onActionProviderVisibilityChanged(boolean isVisible) {
            if (this.mListener != null) {
                this.mListener.onActionProviderVisibilityChanged(isVisible);
            }
        }
    }
}
