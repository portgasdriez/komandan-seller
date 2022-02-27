package wrteam.multivendor.seller.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import wrteam.multivendor.seller.R;
import wrteam.multivendor.seller.activity.ProductDetailActivity;
import wrteam.multivendor.seller.com.darsh.multipleimageselect.helpers.Constants;
import wrteam.multivendor.seller.com.darsh.multipleimageselect.models.Image;
import wrteam.multivendor.seller.helper.ApiConfig;
import wrteam.multivendor.seller.helper.Constant;
import wrteam.multivendor.seller.helper.OnIntentReceived;
import wrteam.multivendor.seller.helper.Session;
import wrteam.multivendor.seller.model.PriceVariation;
import wrteam.multivendor.seller.model.Unit;

@SuppressLint("NotifyDataSetChanged")
public class ProductItemAdapter extends RecyclerView.Adapter<ProductItemAdapter.ProductItemHolder> implements OnIntentReceived {

    final Activity activity;
    final ArrayList<PriceVariation> variants;
    final Session session;
    ArrayList<String> arrayListStockStatus;
    ArrayList<Unit> arrayListUnit;
    ProductItemHolder holder;
    int variant_position = -1;
    String from;

    public ProductItemAdapter(Activity activity, ArrayList<PriceVariation> variants, ArrayList<Unit> arrayListUnit, ArrayList<String> arrayListStockStatus, String from) {
        this.activity = activity;
        this.variants = variants;
        this.arrayListUnit = arrayListUnit;
        this.arrayListStockStatus = arrayListStockStatus;
        this.from = from;
        session = new Session(activity);
    }

    @NotNull
    @Override
    public ProductItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        @SuppressLint("InflateParams") View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.lyt_product_items, null);
        return new ProductItemHolder(v);
    }

    public void addItem(PriceVariation priceVariation) {
        variants.add(priceVariation);
        notifyItemInserted(getItemCount());
    }

    public void removeItem(PriceVariation priceVariation) {
        variants.remove(priceVariation);
        notifyDataSetChanged();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NotNull final ProductItemHolder holder, @SuppressLint("RecyclerView") int position) {
        try {

            this.holder = holder;
            PriceVariation priceVariation = variants.get(position);

            if (getItemCount() == 0) {
                holder.imgDelete.setVisibility(View.INVISIBLE);
            } else {
                holder.imgDelete.setVisibility(View.VISIBLE);
            }

            holder.recyclerViewImageGallery.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false));
            holder.recyclerViewImageGallery.setNestedScrollingEnabled(false);

            holder.btnOtherImages.setOnClickListener(v -> {
                variant_position = position;
                ProductDetailActivity.SelectImage("variant", activity);
            });

            if (from.equalsIgnoreCase("new") || priceVariation.getImages() == null) {
                priceVariation.setImages(new ArrayList<>());
            }

            ProductImagesAdapter productImagesAdapter = new ProductImagesAdapter(activity, priceVariation.getImages() == null ? new ArrayList<>() : priceVariation.getImages(), "variant", priceVariation.getProduct_id());
            holder.recyclerViewImageGallery.setAdapter(productImagesAdapter);

            holder.stockLyt.setVisibility(ProductDetailActivity.productType.equals("loose") ? View.GONE : View.VISIBLE);
            holder.edtMeasurement.addTextChangedListener(new GeneralTextWatcher(priceVariation, "measure"));
            holder.edtOriginalPrice.addTextChangedListener(new GeneralTextWatcher(priceVariation, "price"));
            holder.edtDiscountedPrice.addTextChangedListener(new GeneralTextWatcher(priceVariation, "dis_price"));
            holder.edtStock.addTextChangedListener(new GeneralTextWatcher(priceVariation, "stock"));
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item, arrayListStockStatus);
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            holder.spinnerStatus.setAdapter(arrayAdapter);
            holder.spinnerStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    priceVariation.setServe_for(arrayListStockStatus.get(position));
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            UnitAdapter unitAdapter = new UnitAdapter(activity, arrayListUnit, holder, position);
            holder.spinnerUnit.setAdapter(unitAdapter);
            holder.spinnerUnit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    priceVariation.setMeasurement_unit_id(arrayListUnit.get(position).getId());
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            StockUnitAdapter unitStockAdapter = new StockUnitAdapter(activity, arrayListUnit, holder, position);
            holder.spinnerUnitStock.setAdapter(unitStockAdapter);
            holder.spinnerUnitStock.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    priceVariation.setStock_unit_id(arrayListUnit.get(position).getId());
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            if (priceVariation.getServe_for() != null && !priceVariation.getServe_for().equals("Sold Out")) {
                holder.spinnerStatus.setSelection(1);
            } else {
                holder.spinnerStatus.setSelection(0);
            }

            holder.imgAdd.setOnClickListener(v -> {
                String measurement, measurementId, price, discountPrice, serveFor, stock, stockId;
                measurement = holder.edtMeasurement.getText().toString();
                measurementId = String.valueOf(holder.spinnerUnit.getSelectedItemId());
                price = holder.edtOriginalPrice.getText().toString();
                discountPrice = holder.edtDiscountedPrice.getText().toString();
                serveFor = String.valueOf(holder.spinnerStatus.getSelectedItem());
                stock = holder.edtStock.getText().toString();
                stockId = String.valueOf(holder.spinnerUnitStock.getSelectedItemId());
                priceVariation.setImages(new ArrayList<>());
                if (measurement.isEmpty() || price.isEmpty() || discountPrice.isEmpty()) {
                    Toast.makeText(activity, "Field should not be empty", Toast.LENGTH_SHORT).show();
                } else {
                    if (ProductDetailActivity.productType.equals("packet") && stock.isEmpty()) {
                        Toast.makeText(activity, "Enter stocks!!", Toast.LENGTH_SHORT).show();
                    } else {
                        PriceVariation pv = variants.get(position);
                        pv.setMeasurement(measurement);
                        pv.setMeasurement_unit_id(measurementId);
                        pv.setPrice(price);
                        pv.setDiscounted_price(discountPrice);
                        pv.setServe_for(serveFor);
                        pv.setStock_unit_id(stockId);
                        addItem(new PriceVariation());
                    }
                }

            });

            if (variants.size() > 1) {
                holder.imgDelete.setOnClickListener(v -> {
                    holder.edtMeasurement.clearFocus();
                    holder.edtDiscountedPrice.clearFocus();
                    holder.edtOriginalPrice.clearFocus();
                    holder.edtStock.clearFocus();

                    if (ProductDetailActivity.priceVariations.get(position).getId() != null && !ProductDetailActivity.priceVariations.get(position).getId().equals("0")) {
                        removeVariant(activity, priceVariation);
                    } else {
                        removeItem(priceVariation);
                    }
                });
            }

            holder.edtMeasurement.setText(priceVariation.getMeasurement());
            holder.edtOriginalPrice.setText(priceVariation.getPrice());
            holder.edtDiscountedPrice.setText(priceVariation.getDiscounted_price());
            holder.edtStock.setText(priceVariation.getStock());
            unitAdapter.setItem(priceVariation.getMeasurement_unit_id());
            unitStockAdapter.setItem(priceVariation.getStock_unit_id());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onIntent(Intent data, int resultCode) {
        ImageAdapter mAdapter = new ImageAdapter(activity);
        holder.recyclerViewImageGallery.setAdapter(mAdapter);
        List<Image> mAlbumFiles = data.getParcelableArrayListExtra(Constants.INTENT_EXTRA_IMAGES);
        ArrayList<String> images = new ArrayList<>();
        for (int i = 0; i < mAlbumFiles.size(); i++) {
            images.add(mAlbumFiles.get(i).path);
        }
        variants.get(variant_position).setImages(images);
        mAdapter.notifyDataSetChanged(mAlbumFiles);
        notifyDataSetChanged();
        variant_position = -1;
    }

    public void removeVariant(final Activity activity, PriceVariation priceVariation) {

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);
        // Setting Dialog Message
        alertDialog.setTitle(R.string.remove_variant);
        alertDialog.setMessage(R.string.remove_variant_msg);
        alertDialog.setCancelable(false);
        final AlertDialog alertDialog1 = alertDialog.create();

        // Setting OK Button
        alertDialog.setPositiveButton(R.string.yes, (dialog, which) -> {
            Map<String, String> params = new HashMap<>();
            params.put(Constant.ID, session.getData(Constant.ID));
            params.put(Constant.DELETE_VARIANT, Constant.GetVal);
            params.put(Constant.VARIANT_ID, priceVariation.getId());
            ApiConfig.RequestToVolley((result, response) -> {
                if (result) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (!jsonObject.getBoolean(Constant.ERROR)) {
                            removeItem(priceVariation);
                        } else {
                            dialog.dismiss();
                        }
                    } catch (JSONException e) {
                        dialog.dismiss();
                        e.printStackTrace();
                    }
                }
            }, activity, Constant.MAIN_URL, params, false);
        });
        alertDialog.setNegativeButton(R.string.no, (dialog, which) -> alertDialog1.dismiss());
        // Showing Alert Message
        alertDialog.show();

    }

    @Override
    public int getItemCount() {
        return variants.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private static class GeneralTextWatcher implements TextWatcher {

        PriceVariation priceVariation;
        String type;

        public GeneralTextWatcher(PriceVariation priceVariation, String type) {
            this.priceVariation = priceVariation;
            this.type = type;
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.length() != 0)
                if (type.equals("measure")) {
                    priceVariation.setMeasurement(s.toString());
                } else if (type.equals("price")) {
                    priceVariation.setPrice(s.toString());
                } else if (type.equals("dis_price")) {
                    priceVariation.setDiscounted_price(s.toString());
                } else if (type.equals("stock")) {
                    priceVariation.setStock(s.toString());
                }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }


    }

    public static class ProductItemHolder extends RecyclerView.ViewHolder {
        TextView edtMeasurement, edtOriginalPrice, edtDiscountedPrice, edtStock, btnOtherImages;
        Spinner spinnerUnit, spinnerUnitStock, spinnerStatus;
        ImageView imgDelete, imgAdd;
        LinearLayout stockLyt;
        RecyclerView recyclerViewImageGallery;

        public ProductItemHolder(View itemView) {
            super(itemView);

            edtMeasurement = itemView.findViewById(R.id.edtMeasurement);
            edtOriginalPrice = itemView.findViewById(R.id.edtOriginalPrice);
            edtDiscountedPrice = itemView.findViewById(R.id.edtDiscountedPrice);
            edtStock = itemView.findViewById(R.id.edtStock);
            spinnerUnit = itemView.findViewById(R.id.spinnerUnit);
            spinnerUnitStock = itemView.findViewById(R.id.spinnerUnitStock);
            spinnerStatus = itemView.findViewById(R.id.spinnerStatus);
            stockLyt = itemView.findViewById(R.id.stockLyt);
            imgDelete = itemView.findViewById(R.id.imgDelete);
            imgAdd = itemView.findViewById(R.id.imgAdd);
            btnOtherImages = itemView.findViewById(R.id.btnOtherImages);
            recyclerViewImageGallery = itemView.findViewById(R.id.recyclerViewImageGallery);
            stockLyt.setVisibility(ProductDetailActivity.productType.equals("loose") ? View.GONE : View.VISIBLE);
        }
    }

    public static class UnitAdapter extends BaseAdapter {
        final Context context;
        final ArrayList<Unit> units;
        final LayoutInflater inflter;
        ProductItemHolder holder;
        int position;


        public UnitAdapter(Context applicationContext, ArrayList<Unit> units, ProductItemHolder holder, int position) {
            this.context = applicationContext;
            this.units = units;
            this.holder = holder;
            this.position = position;
            inflter = (LayoutInflater.from(applicationContext));
        }

        @Override
        public int getCount() {
            return units.size();
        }

        @Override
        public Unit getItem(int i) {
            return units.get(i);
        }

        @Override
        public long getItemId(int i) {
            return Long.parseLong(units.get(i).getId());
        }

        public void setItem(String id) {
            for (int i = 0; i < getCount(); i++) {
                if (id != null && id.equals(units.get(i).getId())) {
                    holder.spinnerUnit.setSelection(i);
                    break;
                }
            }
        }

        @SuppressLint({"SetTextI18n", "ViewHolder", "InflateParams"})
        @Override
        public View getView(int pos, View view, ViewGroup viewGroup) {
            view = inflter.inflate(R.layout.lyt_spinner_item, null);
            TextView measurement = view.findViewById(R.id.txtmeasurement);

            Unit unit = units.get(pos);
            measurement.setText(unit.getName());

            holder.spinnerUnit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                    ProductDetailActivity.priceVariations.get(position).setMeasurement_unit_id(units.get(pos).getId());

                } // to close the onItemSelected

                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            return view;
        }
    }

    public static class StockUnitAdapter extends BaseAdapter {
        final Context context;
        final ArrayList<Unit> units;
        final LayoutInflater inflter;
        ProductItemHolder holder;
        int position;


        public StockUnitAdapter(Context applicationContext, ArrayList<Unit> units, ProductItemHolder holder, int position) {
            this.context = applicationContext;
            this.units = units;
            this.holder = holder;
            this.position = position;
            inflter = (LayoutInflater.from(applicationContext));
        }

        @Override
        public int getCount() {
            return units.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        public void setItem(String id) {
            for (int i = 0; i < getCount(); i++) {
                if (id != null && id.equals(units.get(i).getId())) {
                    holder.spinnerUnitStock.setSelection(i);
                    break;
                }
            }
        }

        @SuppressLint({"SetTextI18n", "ViewHolder", "InflateParams"})
        @Override
        public View getView(int pos, View view, ViewGroup viewGroup) {
            view = inflter.inflate(R.layout.lyt_spinner_item, null);
            TextView measurement = view.findViewById(R.id.txtmeasurement);

            Unit unit = units.get(pos);
            measurement.setText(unit.getName());

            holder.spinnerUnitStock.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

                    ProductDetailActivity.priceVariations.get(position).setStock_unit_id(units.get(pos).getId());

                } // to close the onItemSelected

                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            return view;
        }
    }

}