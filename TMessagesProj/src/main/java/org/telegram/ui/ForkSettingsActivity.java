/*
 * Copyright 23rd, 2019.
 */

package org.telegram.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.DataQuery;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.support.widget.LinearLayoutManager;
import org.telegram.messenger.support.widget.RecyclerView;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Cells.CheckBoxCell;
import org.telegram.ui.Cells.EmptyCell;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.NotificationsCheckCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextDetailSettingsCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.NumberPicker;
import org.telegram.ui.Components.RecyclerListView;

public class ForkSettingsActivity extends BaseFragment {

    private RecyclerListView listView;
    private ListAdapter listAdapter;

    private int rowCount;
    private int sectionRow1;
    private int sectionRow2;

    private int squareAvatarsRow;
    private int inappCameraRow;
    private int photoHasStickerRow;
    private int unmutedOnTopRow;
    private int shareAnonymRow;
    private int rearVideoMessages;

    private int syncPinsRow;
    private int pinOrderRow;

    private int emptyRow;

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();

        rowCount = 0;
        
        sectionRow1 = rowCount++;
        squareAvatarsRow = rowCount++;
        inappCameraRow = rowCount++;
        photoHasStickerRow = rowCount++;
        unmutedOnTopRow = rowCount++;
        shareAnonymRow = rowCount++;
        rearVideoMessages = rowCount++;

        emptyRow = rowCount++;
        sectionRow2 = rowCount++;
        syncPinsRow = rowCount++;
        pinOrderRow = rowCount++;

        return true;
    }

    public boolean toggleGlobalMainSetting(String option, View view, boolean byDefault) {
        SharedPreferences preferences = MessagesController.getGlobalMainSettings();
        boolean optionBool = preferences.getBoolean(option, byDefault);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(option, !optionBool);
        editor.commit();
        if (view instanceof TextCheckCell) {
            ((TextCheckCell) view).setChecked(!optionBool);
        }
        return !optionBool;
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setTitle(LocaleController.getString("ForkSettingsTitle", R.string.ForkSettingsTitle));

        if (AndroidUtilities.isTablet()) {
            actionBar.setOccupyStatusBar(false);
        }
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        listAdapter = new ListAdapter(context);

        fragmentView = new FrameLayout(context);
        fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        FrameLayout frameLayout = (FrameLayout) fragmentView;

        listView = new RecyclerListView(context);
        listView.setVerticalScrollBarEnabled(false);
        listView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false) {
            @Override
            public boolean supportsPredictiveItemAnimations() {
                return false;
            }
        });
        listView.setGlowColor(Theme.getColor(Theme.key_avatar_backgroundActionBarBlue));
        listView.setAdapter(listAdapter);
        listView.setItemAnimator(null);
        listView.setLayoutAnimation(null);
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.LEFT));
        listView.setOnItemClickListener((view, position, x, y) -> {
            if (position == squareAvatarsRow) {
                SharedPreferences preferences = MessagesController.getGlobalMainSettings();
                boolean squareAvatars = preferences.getBoolean("squareAvatars", true);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("squareAvatars", !squareAvatars);
                editor.commit();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(!squareAvatars);
                }
            } else if (position == inappCameraRow) {
                SharedConfig.toggleInappCamera();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(SharedConfig.inappCamera);
                }
            } else if (position == photoHasStickerRow) {
                SharedConfig.toggleHasSticker();
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(SharedConfig.hasSticker);
                }
            } else if (position == syncPinsRow) {
                toggleGlobalMainSetting("syncPins", view, true);
            } else if (position == unmutedOnTopRow) {
                toggleGlobalMainSetting("unmutedOnTop", view, false);
                MessagesController.getInstance(currentAccount).sortDialogs(null);
            } else if (position == shareAnonymRow) {
                toggleGlobalMainSetting("shareAlertAsAnonym", view, true);
            } else if (position == rearVideoMessages) {
                toggleGlobalMainSetting("rearVideoMessages", view, false);
            } else if (position == pinOrderRow) {
                presentFragment(new PinsOrderActivity());
            }
        });

        return fragmentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }
    }

    private class ListAdapter extends RecyclerListView.SelectionAdapter {

        private Context mContext;

        public ListAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getItemCount() {
            return rowCount;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            switch (holder.getItemViewType()) {
                case 2: {
                    TextSettingsCell textCell = (TextSettingsCell) holder.itemView;
                    if (position == pinOrderRow) {
                        textCell.setText(LocaleController.getString("PinDialogsOrder", R.string.PinDialogsOrder), true);
                    }
                    break;
                }
                case 3: {
                    TextCheckCell textCell = (TextCheckCell) holder.itemView;
                    SharedPreferences preferences = MessagesController.getGlobalMainSettings();
                    if (position == squareAvatarsRow) {
                        String t = LocaleController.getString("SquareAvatars", R.string.SquareAvatars);
                        String info = LocaleController.getString("SquareAvatarsInfo", R.string.SquareAvatarsInfo);
                        textCell.setTextAndValueAndCheck(t, info, preferences.getBoolean("squareAvatars", true), false, false);
                    } else if (position == inappCameraRow) {
                        String t = LocaleController.getString("InAppCamera", R.string.InAppCamera);
                        String info = LocaleController.getString("InAppCameraInfo", R.string.InAppCameraInfo);
                        textCell.setTextAndValueAndCheck(t, info, preferences.getBoolean("inappCamera", true), false, false);
                    } else if (position == photoHasStickerRow) {
                        String t = LocaleController.getString("PhotoHasSticker", R.string.PhotoHasSticker);
                        String info = LocaleController.getString("PhotoHasStickerInfo", R.string.PhotoHasStickerInfo);
                        textCell.setTextAndValueAndCheck(t, info, preferences.getBoolean("photoHasSticker", false), true, false);
                    } else if (position == syncPinsRow) {
                        String t = LocaleController.getString("SyncPins", R.string.SyncPins);
                        String info = LocaleController.getString("SyncPinsInfo", R.string.SyncPinsInfo);
                        textCell.setTextAndValueAndCheck(t, info, preferences.getBoolean("syncPins", true), true, false);
                    } else if (position == unmutedOnTopRow) {
                        String t = LocaleController.getString("UnmutedOnTop", R.string.UnmutedOnTop);
                        String info = LocaleController.getString("UnmutedOnTopInfo", R.string.UnmutedOnTopInfo);
                        textCell.setTextAndValueAndCheck(t, info, preferences.getBoolean("unmutedOnTop", false), true, false);
                    } else if (position == shareAnonymRow) {
                        String t = LocaleController.getString("ShareAlertAnonym", R.string.ShareAlertAnonym);
                        textCell.setTextAndCheck(t, preferences.getBoolean("shareAlertAsAnonym", true), true);
                    } else if (position == rearVideoMessages) {
                        String t = LocaleController.getString("RearVideoMessages", R.string.RearVideoMessages);
                        textCell.setTextAndCheck(t, preferences.getBoolean("rearVideoMessages", false), false);
                    }
                    break;
                }
                case 4: {
                    HeaderCell headerCell = (HeaderCell) holder.itemView;
                    if (position == sectionRow1) {
                        headerCell.setText(LocaleController.getString("General", R.string.General));
                    } else if (position == sectionRow2) {
                        headerCell.setText(LocaleController.getString("PinSection", R.string.PinSection));
                    }
                    break;
                }
            }
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            int position = holder.getAdapterPosition();
            boolean fork = position == squareAvatarsRow
                        || position == inappCameraRow
                        || position == photoHasStickerRow
                        || position == syncPinsRow
                        || position == unmutedOnTopRow
                        || position == shareAnonymRow
                        || position == pinOrderRow
                        || position == rearVideoMessages;
            return fork;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = null;
            switch (viewType) {
                case 1:
                    view = new ShadowSectionCell(mContext);
                    break;
                case 2:
                    view = new TextSettingsCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 3:
                    view = new TextCheckCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 4:
                    view = new HeaderCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 5:
                    view = new NotificationsCheckCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 6:
                    view = new TextDetailSettingsCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
            }
            view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
            return new RecyclerListView.Holder(view);
        }

        @Override
        public int getItemViewType(int position) {
            if (position == emptyRow) {
                return 1;
            } else if (position == pinOrderRow) {
                return 2;
            } else if (position == squareAvatarsRow
                || position == inappCameraRow
                || position == syncPinsRow
                || position == unmutedOnTopRow
                || position == shareAnonymRow
                || position == rearVideoMessages
                || position == photoHasStickerRow) {
                return 3;
            } else if (position == sectionRow1
                || position == sectionRow2) {
                return 4;
            }
            return 6;
        }
    }

    @Override
    public ThemeDescription[] getThemeDescriptions() {
        return new ThemeDescription[]{
                new ThemeDescription(listView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[]{EmptyCell.class, TextSettingsCell.class, TextCheckCell.class, HeaderCell.class, TextDetailSettingsCell.class, NotificationsCheckCell.class}, null, null, null, Theme.key_windowBackgroundWhite),
                new ThemeDescription(fragmentView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_windowBackgroundGray),

                new ThemeDescription(actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_avatar_backgroundActionBarBlue),
                new ThemeDescription(listView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, Theme.key_avatar_backgroundActionBarBlue),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, Theme.key_avatar_actionBarIconBlue),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, null, null, Theme.key_actionBarDefaultTitle),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, Theme.key_avatar_actionBarSelectorBlue),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SUBMENUBACKGROUND, null, null, null, null, Theme.key_actionBarDefaultSubmenuBackground),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SUBMENUITEM, null, null, null, null, Theme.key_actionBarDefaultSubmenuItem),

                new ThemeDescription(listView, ThemeDescription.FLAG_SELECTOR, null, null, null, null, Theme.key_listSelector),

                new ThemeDescription(listView, 0, new Class[]{View.class}, Theme.dividerPaint, null, null, Theme.key_divider),

                new ThemeDescription(listView, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[]{ShadowSectionCell.class}, null, null, null, Theme.key_windowBackgroundGrayShadow),

                new ThemeDescription(listView, 0, new Class[]{TextSettingsCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText),
                new ThemeDescription(listView, 0, new Class[]{TextSettingsCell.class}, new String[]{"valueTextView"}, null, null, null, Theme.key_windowBackgroundWhiteValueText),

                new ThemeDescription(listView, 0, new Class[]{NotificationsCheckCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText),
                new ThemeDescription(listView, 0, new Class[]{NotificationsCheckCell.class}, new String[]{"valueTextView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText2),
                new ThemeDescription(listView, 0, new Class[]{NotificationsCheckCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_switchTrack),
                new ThemeDescription(listView, 0, new Class[]{NotificationsCheckCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_switchTrackChecked),

                new ThemeDescription(listView, 0, new Class[]{TextCheckCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText),
                new ThemeDescription(listView, 0, new Class[]{TextCheckCell.class}, new String[]{"valueTextView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText2),
                new ThemeDescription(listView, 0, new Class[]{TextCheckCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_switchTrack),
                new ThemeDescription(listView, 0, new Class[]{TextCheckCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_switchTrackChecked),

                new ThemeDescription(listView, 0, new Class[]{HeaderCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlueHeader),

                new ThemeDescription(listView, 0, new Class[]{TextDetailSettingsCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText),
                new ThemeDescription(listView, 0, new Class[]{TextDetailSettingsCell.class}, new String[]{"valueTextView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText2),
        };
    }
}
