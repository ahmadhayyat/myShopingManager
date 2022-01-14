package com.example.myShopingManager;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ItemDataAdapter extends ListAdapter<Items, ItemDataAdapter.ViewHolder> {
    Context context;
    ItemAdapterInteraction interaction;
    ArrayList<Integer> purchasedItems;

    protected ItemDataAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<Items> DIFF_CALLBACK = new DiffUtil.ItemCallback<Items>() {
        @Override
        public boolean areItemsTheSame(@NonNull Items oldItem, @NonNull Items newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Items oldItem, @NonNull Items newItem) {
            return oldItem.getName().equals(newItem.getName()) &&
                    oldItem.getDate().equals(newItem.getDate()) &&
                    oldItem.getPrice().equals(newItem.getPrice());
        }
    };

    @NonNull
    @Override
    public ItemDataAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_layout, parent, false);
        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull final ItemDataAdapter.ViewHolder holder, final int position) {
        final Items item = getItemAt(position);
        String prevMsgTime;
        if (position < getItemCount() - 1)
            prevMsgTime = getItemAt(position + 1).getDate();
        else prevMsgTime = getItemAt(position).getDate();
        String currentMsgTime = item.getDate();
        if (position == 0) {
            holder.itemsDate.setText(currentMsgTime.toUpperCase());
            holder.itemsDate.setVisibility(View.VISIBLE);
        } else if (currentMsgTime.equalsIgnoreCase(prevMsgTime)) {
            holder.itemsDate.setVisibility(View.GONE);
        } else {
            holder.itemsDate.setText(currentMsgTime.toUpperCase());
            holder.itemsDate.setVisibility(View.VISIBLE);
        }
        if (item.getStatus() == Constants.STATUS_PURCHASED)
            setPurchasedUI(holder);
        else if (item.getStatus() == Constants.STATUS_NOT_AVAILABLE)
            setNotAvailableUI(holder);
        else setNotNeutralUI(holder);
        holder.itemName.setText(item.getName());
        if (item.getPrice() != 0 || item.getPrice() != null) {
            holder.itemPrice.setText(String.valueOf(item.getPrice()));
        } else {
            holder.itemPrice.setText("");
        }
        holder.cb_save.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                item.setPrice(Integer.parseInt(holder.itemPrice.getText().toString().trim()));
                interaction.onPriceUpdate(item);
                new Handler().postDelayed(() -> {
                    holder.cb_save.setChecked(false);
                    this.notifyItemChanged(position);
                }, 1000);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (item.getStatus() == Constants.STATUS_PURCHASED ||
                    item.getStatus() == Constants.STATUS_NOT_AVAILABLE) {
                item.setStatus(Constants.STATUS_NEUTRAL);
                interaction.onStatusChange(item);
                setNotNeutralUI(holder);
            } else {
                item.setStatus(Constants.STATUS_PURCHASED);
                interaction.onStatusChange(item);
                setPurchasedUI(holder);
            }
        });

        holder.itemView.setOnLongClickListener(view -> {
            item.setStatus(Constants.STATUS_NOT_AVAILABLE);
            interaction.onStatusChange(item);
            setNotAvailableUI(holder);
            return true;
        });
    }

    void setPurchasedUI(ViewHolder holder) {
        holder.itemsStatus.setVisibility(View.VISIBLE);
        holder.itemsStatus.setText(context.getString(R.string.purchased));
        holder.itemsStatus.setBackground(AppCompatResources.getDrawable(context, R.drawable.purchased_bg));
        holder.llMain.setBackground(AppCompatResources.getDrawable(context, R.drawable.rounded_bg_light_green));
    }

    void setNotAvailableUI(ViewHolder holder) {
        holder.itemsStatus.setVisibility(View.VISIBLE);
        holder.itemsStatus.setText(context.getString(R.string.not_available));
        holder.itemsStatus.setBackground(AppCompatResources.getDrawable(context, R.drawable.not_available_bg));
        holder.llMain.setBackground(AppCompatResources.getDrawable(context, R.drawable.rounded_bg_light_orange));
    }

    void setNotNeutralUI(ViewHolder holder) {
        holder.itemsStatus.setVisibility(View.GONE);
        holder.llMain.setBackground(AppCompatResources.getDrawable(context, R.drawable.rounded_bg_white));
    }

    public Items getItemAt(int position) {
        return getItem(position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView itemName, itemsDate, itemsStatus;
        EditText itemPrice;
        CheckBox cb_save;
        LinearLayout llMain;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.itemName);
            itemPrice = itemView.findViewById(R.id.itemPrice);
            itemsDate = itemView.findViewById(R.id.itemsDate);
            cb_save = itemView.findViewById(R.id.cb_save);
            llMain = itemView.findViewById(R.id.llMain);
            itemsStatus = itemView.findViewById(R.id.itemStatus);
        }
    }

    public void setInteraction(ItemAdapterInteraction interaction) {
        this.interaction = interaction;
    }

    interface ItemAdapterInteraction {
        void onPriceUpdate(Items item);

        void onStatusChange(Items item);
    }
}
