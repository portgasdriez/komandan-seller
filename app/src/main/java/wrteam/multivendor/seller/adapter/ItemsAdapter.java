package wrteam.multivendor.seller.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import wrteam.multivendor.seller.R;
import wrteam.multivendor.seller.helper.ApiConfig;
import wrteam.multivendor.seller.helper.Constant;
import wrteam.multivendor.seller.model.DeliveryBoy;
import wrteam.multivendor.seller.helper.Session;
import wrteam.multivendor.seller.helper.VolleyCallback;
import wrteam.multivendor.seller.model.Items;

public class ItemsAdapter extends RecyclerView.Adapter<ItemsAdapter.OrderItemHolder> {

    final Activity activity;
    final ArrayList<Items> items;
    final ArrayList<DeliveryBoy> deliveryBoys;
    final ArrayList<String> deliveryBoysName;
    final Session session;
    int position_ = 0;

    public ItemsAdapter(Activity activity, ArrayList<Items> items, ArrayList<DeliveryBoy> deliveryBoys, ArrayList<String> deliveryBoysName) {
        this.activity = activity;
        this.items = items;
        this.deliveryBoys = deliveryBoys;
        this.deliveryBoysName = deliveryBoysName;
        session = new Session(activity);
    }

    @Override
    public OrderItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.lyt_items, null);
        return new OrderItemHolder(v);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NotNull final OrderItemHolder holder, int position) {

        final Items item = items.get(position);

        final String[] activeStatus = {item.getActive_status()};

        holder.tvProductName.setText(item.getName());
        holder.tvUnit.setText(activity.getString(R.string.unit_) + item.getMeasurement() + " " + item.getUnit());
        holder.tvQuantity.setText(activity.getString(R.string.qty) + item.getQuantity());
        holder.tvPrice.setText(activity.getString(R.string.price) + Constant.SETTING_CURRENCY_SYMBOL + "): " + item.getPrice());
        holder.tvDiscountPrice.setText(activity.getString(R.string.discount_) + Constant.SETTING_CURRENCY_SYMBOL + "): " + item.getDiscounted_price());
        holder.tvTaxPercentage.setText(activity.getString(R.string.tax_) + Constant.SETTING_CURRENCY_SYMBOL + "): " + item.getTax_amount());
        holder.tvTax.setText(activity.getString(R.string.tax__) + item.getTax_percentage());
        holder.tvSubTotal.setText(activity.getString(R.string.subtotal) + Constant.SETTING_CURRENCY_SYMBOL + "): " + item.getSub_total());

        Picasso.get().
                load(item.getImage())
                .fit()
                .centerInside()
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .into(holder.imgProduct);

        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add(activity.getString(R.string.awaiting_payment));
        arrayList.add(activity.getString(R.string.received));
        arrayList.add(activity.getString(R.string.processed));
        arrayList.add(activity.getString(R.string.shipped));
        arrayList.add(activity.getString(R.string.delivered));
        arrayList.add(activity.getString(R.string.cancelled));
        arrayList.add(activity.getString(R.string.returned));
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(activity, android.R.layout.simple_spinner_item, arrayList);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.spinnerStatus.setAdapter(arrayAdapter);

        ArrayList<String> arrayList_ = new ArrayList<>();
        arrayList_.add(Constant.AWAITING_PAYMENT);
        arrayList_.add(Constant.RECEIVED);
        arrayList_.add(Constant.PROCESSED);
        arrayList_.add(Constant.SHIPPED);
        arrayList_.add(Constant.DELIVERED);
        arrayList_.add(Constant.CANCELLED);
        arrayList_.add(Constant.RETURNED);

        for (int i = 0; i < arrayList_.size(); i++) {
            if (item.getActive_status().equals(arrayList_.get(i))) {
                holder.spinnerStatus.setSelection(i);
                position_ = i;
            }
        }

        holder.spinnerStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!activeStatus[0].equals(arrayList_.get(position))) {
                    activeStatus[0] = arrayList_.get(position);
                    ChangeOrderStatus(arrayList_.get(position), item);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        if (session.getData(Constant.ASSIGN_DELIVERY_BOY).equals("0")) {
            holder.lytDeliveryBoy.setVisibility(View.GONE);
        } else {
            holder.lytDeliveryBoy.setVisibility(View.VISIBLE);
        }

        holder.tvDeliveryBoy.setText(item.getDelivery_boy_name().equals("") ? activity.getString(R.string.select_delivery_boy) : item.getDelivery_boy_name());

        holder.lytDeliveryBoy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(activity);
                dialog.setContentView(R.layout.dialog_searchable_dropdown);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                dialog.show();

                EditText edtTextView = dialog.findViewById(R.id.edtTextView);
                ListView listView = dialog.findViewById(R.id.listView);

                final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1, deliveryBoysName);
                listView.setAdapter(arrayAdapter);

                edtTextView.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                        arrayAdapter.getFilter().filter(charSequence);
                    }

                    @Override
                    public void afterTextChanged(Editable charSequence) {

                    }
                });

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @SuppressLint("UseCompatLoadingForDrawables")
                    @SuppressWarnings("deprecation")
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        holder.tvDeliveryBoy.setText(deliveryBoys.get(position).getName());
                        UpdateOrder("", item, "delivery_boy", deliveryBoys.get(position).getId(), deliveryBoys.get(position).getName());
                        dialog.dismiss();
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void ChangeOrderStatus(String status, Items item) {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);
        // Setting Dialog Message
        alertDialog.setTitle(R.string.title_update_order);
        alertDialog.setMessage(R.string.msg_update_order);
        alertDialog.setCancelable(false);
        final AlertDialog alertDialog1 = alertDialog.create();

        // Setting OK Button
        alertDialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                UpdateOrder(status, item, "status", "", "");
            }
        });
        alertDialog.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                alertDialog1.dismiss();
            }
        });
        // Showing Alert Message
        alertDialog.show();
    }

    private void UpdateOrder(String status, Items item, String from, String id, String name) {
        Map<String, String> params = new HashMap<>();
        params.put(Constant.UPDATE_ORDER_STATUS, Constant.GetVal);
        params.put(Constant.SELLER_ID, session.getData(Constant.ID));
        params.put(Constant.ORDER_ID, item.getOrder_id());
        params.put(Constant.ORDER_ITEM_ID, item.getId());
        if (from.equals("status")) {
            params.put(Constant.STATUS, status);
        } else {
            params.put(Constant.DELIVERY_BOY_ID, id);
        }

        ApiConfig.RequestToVolley(new VolleyCallback() {
            @Override
            public void onSuccess(boolean result, String response) {

                if (result) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (!jsonObject.getBoolean(Constant.ERROR)) {
                            if (from.equals("status")) {
                                item.setActive_status(status);
                            } else {
                                item.setDelivery_boy_name(name);
                            }
                        }
                        notifyDataSetChanged();
                        Toast.makeText(activity, jsonObject.getString(Constant.MESSAGE), Toast.LENGTH_LONG).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, activity, Constant.MAIN_URL, params, true);
    }

    public static class OrderItemHolder extends RecyclerView.ViewHolder {
        final TextView tvProductName, tvUnit, tvQuantity, tvPrice, tvDiscountPrice, tvSubTotal, tvTaxPercentage, tvTax, tvDeliveryBoy;
        final ImageView imgProduct;
        Spinner spinnerStatus;
        RelativeLayout lytDeliveryBoy;

        public OrderItemHolder(View itemView) {
            super(itemView);

            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvUnit = itemView.findViewById(R.id.tvUnit);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvDiscountPrice = itemView.findViewById(R.id.tvDiscountPrice);
            tvSubTotal = itemView.findViewById(R.id.tvSubTotal);
            tvTaxPercentage = itemView.findViewById(R.id.tvTaxPercentage);
            tvTax = itemView.findViewById(R.id.tvTax);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            spinnerStatus = itemView.findViewById(R.id.spinnerStatus);
            tvDeliveryBoy = itemView.findViewById(R.id.tvDeliveryBoy);
            lytDeliveryBoy = itemView.findViewById(R.id.lytDeliveryBoy);
        }
    }
}