package com.reco1l.data.adapters;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static com.reco1l.data.adapters.ContextMenuAdapter.*;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.reco1l.Game;
import com.reco1l.data.BaseAdapter;
import com.reco1l.data.BaseViewHolder;
import com.reco1l.ui.custom.ContextMenu;
import com.reco1l.framework.Animation;
import com.reco1l.framework.input.TouchHandler;

import java.util.ArrayList;

import com.reco1l.ui.custom.ContextMenu.Item;
import com.rimu.R;

public class ContextMenuAdapter extends BaseAdapter<ItemHolder, Item> {

    private ContextMenu mParent;

    //--------------------------------------------------------------------------------------------//

    public ContextMenuAdapter(ArrayList<Item> items, ContextMenu parent) {
        super(items);
        mParent = parent;
    }

    //--------------------------------------------------------------------------------------------//

    @Override
    protected ItemHolder getViewHolder(View rootView) {
        TextView textView = new TextView(new ContextThemeWrapper(Game.activity, R.style.text));

        Drawable drawable = new ColorDrawable(color(R.color.accent));
        drawable.setAlpha(0);

        textView.setLayoutParams(new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
        textView.setBackground(drawable);
        textView.setSingleLine(true);

        int m = dimen(R.dimen.M);
        int s = dimen(R.dimen.S);
        textView.setPadding(m, s, m, s);

        return new ItemHolder(textView, mParent);
    }

    //--------------------------------------------------------------------------------------------//

    public static class ItemHolder extends BaseViewHolder<Item> {

        private final TextView mText;

        //----------------------------------------------------------------------------------------//

        public ItemHolder(@NonNull View root, ContextMenu menu) {
            super(root);
            mText = (TextView) root;

            TouchHandler.of(mText, () -> {
                if (item.closeOnClick()) {
                    menu.close();
                }
                item.onClick(mText);
            });
        }

        //----------------------------------------------------------------------------------------//

        @Override
        protected void onBind(Item item, int position) {
            mText.setText(item.getText());
        }

        @Override
        public void onSelect() {
            Drawable background = root.getBackground();

            Animation.ofInt(background.getAlpha(), 60)
                    .runOnUpdate(value -> {
                        background.setAlpha((int) value);
                        root.setBackground(background);
                    })
                    .play(200);

            Animation.ofColor(Color.WHITE, color(R.color.accent))
                    .runOnUpdate(value -> ((TextView) root).setTextColor((int) value))
                    .play(200);
        }

        @Override
        public void onDeselect() {
            Drawable background = root.getBackground();

            Animation.ofInt(background.getAlpha(), 0)
                    .runOnUpdate(value -> {
                        background.setAlpha((int) value);
                        root.setBackground(background);
                    })
                    .play(200);

            Animation.ofColor(color(R.color.accent), Color.WHITE)
                    .runOnUpdate(value -> ((TextView) root).setTextColor((int) value))
                    .play(200);
        }
    }
}
