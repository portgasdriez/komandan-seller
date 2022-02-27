package wrteam.multivendor.seller.adapter;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
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
import wrteam.multivendor.seller.helper.Session;

public class ProductImagesAdapter extends RecyclerView.Adapter<ProductImagesAdapter.ImageHolder> {

    final Activity activity;
    final ArrayList<String> images;
    final Session session;
    String from;
    String id;

    public ProductImagesAdapter(Activity activity, ArrayList<String> images, String from, String id) {
        this.activity = activity;
        this.images = images;
        this.from = from;
        this.id = id;
        session = new Session(activity);
    }

    @NonNull
    @Override
    public ImageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.lyt_image_list, parent, false);
        return new ImageHolder(v);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NotNull final ImageHolder holder, int position) {

        final String image = images.get(position);
        if (from.equals("api") || from.equals("variant")) {
            Picasso.get().
                    load(image)
                    .fit()
                    .centerInside()
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .into(holder.imgProductImage);
        } else {
            holder.imgProductImage.setImageBitmap(BitmapFactory.decodeFile(image));
        }

        holder.imgProductImageDelete.setOnClickListener(v -> {
            if (id.equals("0")) {
                images.remove(image);
                notifyDataSetChanged();
            } else {
                removeImage(activity, "" + position, image);
            }
        });
    }

    public void removeImage(final Activity activity, String position, String image) {

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);
        // Setting Dialog Message
        alertDialog.setTitle(R.string.remove_image);
        alertDialog.setMessage(R.string.remove_image_msg);
        alertDialog.setCancelable(false);
        final AlertDialog alertDialog1 = alertDialog.create();

        // Setting OK Button
        alertDialog.setPositiveButton(R.string.yes, (dialog, which) -> {
            Map<String, String> params = new HashMap<>();
            if (from.equals("api")) {
                params.put(Constant.DELETE_OTHER_IMAGES, Constant.GetVal);
                params.put(Constant.PRODUCT_ID, id);
            }
            if (from.equals("variant")) {
                params.put(Constant.DELETE_VARIANT_IMAGES, Constant.GetVal);
                params.put(Constant.VARIANT_ID, id);
            }
            params.put(Constant.IMAGE, position);
            ApiConfig.RequestToVolley((result, response) -> {
                if (result) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (!jsonObject.getBoolean(Constant.ERROR)) {
                            images.remove(image);
                            notifyDataSetChanged();
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
        return images.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public static class ImageHolder extends RecyclerView.ViewHolder {
        final ImageView imgProductImage, imgProductImageDelete;

        public ImageHolder(View itemView) {
            super(itemView);
            imgProductImage = itemView.findViewById(R.id.imgProductImage);
            imgProductImageDelete = itemView.findViewById(R.id.imgProductImageDelete);
        }
    }
}