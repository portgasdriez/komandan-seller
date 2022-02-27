package wrteam.multivendor.seller.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

import wrteam.multivendor.seller.R;
import wrteam.multivendor.seller.com.darsh.multipleimageselect.models.Image;


@SuppressLint("NotifyDataSetChanged")
public class ImageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final LayoutInflater mInflater;


    private List<Image> mAlbumFiles;

    public ImageAdapter(Context context) {
        this.mInflater = LayoutInflater.from(context);

    }

    public void notifyDataSetChanged(List<Image> imagePathList) {
        this.mAlbumFiles = imagePathList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {

        return position;

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ImageViewHolder(mInflater.inflate(R.layout.item_content_image, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((ImageViewHolder) holder).setData(mAlbumFiles.get(position));
    }

    @Override
    public int getItemCount() {
        return mAlbumFiles == null ? 0 : mAlbumFiles.size();
    }

    private static class ImageViewHolder extends RecyclerView.ViewHolder {

        private final ImageView mIvImage;

        ImageViewHolder(View itemView) {
            super(itemView);
            this.mIvImage = itemView.findViewById(R.id.iv_album_content_image);
        }

        public void setData(Image albumFile) {
            Picasso.get().
                    load(new File(albumFile.path))
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .into(mIvImage);

        }


    }


}
