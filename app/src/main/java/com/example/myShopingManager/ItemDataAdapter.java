package com.example.myShopingManager;

import android.content.Context;
import android.os.Handler;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ItemDataAdapter extends ListAdapter<Items, ItemDataAdapter.ViewHolder> {
    Context context;
    ItemAdapterInteraction interaction;

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
        /*if (Constants.getInstance().getDate().equals(item.getDate())) {
            holder.itemsDate.setVisibility(View.VISIBLE);
            holder.itemsDate.setText(item.getDate());
        } else holder.itemsDate.setVisibility(View.GONE);*/

        if (position != 0) {
            processDate(holder.itemsDate, item.getDate()
                    , getItemAt(position - 1).getDate()
                    , false)
            ;
        } else {
            processDate(holder.itemsDate, item.getDate()
                    , null
                    , true)
            ;
        }


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
        holder.itemView.setOnClickListener(v -> holder.itemView.setBackgroundColor(context.getResources().getColor(R.color.lightGreen)));
    }

    public Items getItemAt(int position) {
        return getItem(position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView itemName, itemsDate;
        EditText itemPrice;
        CheckBox cb_save;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.itemName);
            itemPrice = itemView.findViewById(R.id.itemPrice);
            itemsDate = itemView.findViewById(R.id.itemsDate);
            cb_save = itemView.findViewById(R.id.cb_save);

        }
    }

    private void processDate(@NonNull TextView tv, String dateAPIStr
            , String dateAPICompareStr
            , boolean isFirstItem) {

        SimpleDateFormat f = new SimpleDateFormat("dd/MM/yyyy");
        if (isFirstItem) {
            //first item always got date/today to shows
            //and overkill to compare with next item flow
            Date dateFromAPI = null;
            try {
                dateFromAPI = f.parse(dateAPIStr);
                if (DateUtils.isToday(dateFromAPI.getTime())) tv.setText("today");
                else if (DateUtils.isToday(dateFromAPI.getTime() + DateUtils.DAY_IN_MILLIS))
                    tv.setText("yesterday");
                else tv.setText(dateAPIStr);
                tv.setIncludeFontPadding(false);
                tv.setVisibility(View.VISIBLE);
            } catch (ParseException e) {
                e.printStackTrace();
                tv.setVisibility(View.GONE);
            }
        } else {
            if (!dateAPIStr.equalsIgnoreCase(dateAPICompareStr)) {
                try {
                    Date dateFromAPI = f.parse(dateAPIStr);
                    if (DateUtils.isToday(dateFromAPI.getTime())) tv.setText("today");
                    else if (DateUtils.isToday(dateFromAPI.getTime() + DateUtils.DAY_IN_MILLIS))
                        tv.setText("yesterday");
                    else tv.setText(dateAPIStr);
                    tv.setIncludeFontPadding(false);
                    tv.setVisibility(View.VISIBLE);
                } catch (ParseException e) {
                    e.printStackTrace();
                    tv.setVisibility(View.GONE);
                }
            } else {
                tv.setVisibility(View.GONE);
            }
        }
    }

    public void setInteraction(ItemAdapterInteraction interaction) {
        this.interaction = interaction;
    }

    interface ItemAdapterInteraction {
        void onPriceUpdate(Items item);
    }
}
