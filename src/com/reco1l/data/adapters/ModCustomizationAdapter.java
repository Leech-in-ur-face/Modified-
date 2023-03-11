package com.reco1l.data.adapters;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;

import com.reco1l.data.BaseAdapter;
import com.reco1l.data.BaseViewHolder;
import com.reco1l.ui.base.Identifiers;
import com.reco1l.management.modding.mods.ModWrapper;
import com.rimu.R;

import java.util.ArrayList;

public class ModCustomizationAdapter extends BaseAdapter<ModCustomizationAdapter.VH, ModWrapper> {

    private int holderCount;

    //----------------------------------------------------------------------------------------//

    public ModCustomizationAdapter(ArrayList<ModWrapper> list) {
        super(list);
    }

    //----------------------------------------------------------------------------------------//

    @Override
    protected int getItemLayout() {
        return R.layout.item_mod_customization;
    }

    @Override
    protected VH getViewHolder(View rootView) {
        holderCount++;

        VH holder = new VH(rootView);

        holder.frame = new FrameLayout(context());
        holder.frame.setId(Identifiers.ModMenu_CustomizationFrames + holderCount);
        holder.body.addView(holder.frame);

        return holder;
    }

    //----------------------------------------------------------------------------------------//

    public static class VH extends BaseViewHolder<ModWrapper> {

        private final TextView name;
        private final LinearLayout body;

        private FrameLayout frame;

        //------------------------------------------------------------------------------------//

        public VH(@NonNull View root) {
            super(root);
            name = root.findViewById(R.id.mm_customName);
            body = root.findViewById(R.id.mm_customBody);
        }

        //------------------------------------------------------------------------------------//

        @Override
        protected void onBind(ModWrapper mod, int position) {
            name.setText(mod.getName());

            ModWrapper.Properties fragment = mod.getProperties();
            fragment.replace(frame);
         }
    }
}
