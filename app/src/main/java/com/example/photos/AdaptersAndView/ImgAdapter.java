package com.example.photos.AdaptersAndView;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.photos.R;

import java.util.ArrayList;

public class ImgAdapter extends RecyclerView.Adapter<ImgAdapter.myViewHolder>
{
    private Context context;
    private ArrayList<Bitmap> imgs;
    private View inflater;

    public interface OnItemClickListener {
        void onClick(int position);
    }

    public void insertBitmap(Bitmap bitmap)
    {
        imgs.add(bitmap);
    }

    public void deleteBitmap(int pos) {imgs.remove(pos);}

    private OnItemClickListener listener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public ImgAdapter(Context c,ArrayList<Bitmap> i)
    {
        this.context = c;
        this.imgs = i;
    }

    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        inflater = LayoutInflater.from(context).inflate(R.layout.img_cell,parent,false);
        myViewHolder viewHolder = new myViewHolder(inflater);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull myViewHolder holder, final int position) {
        holder.imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        holder.imageView.setImageBitmap(imgs.get(position));
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onClick(position);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return imgs.size();
    }

    class myViewHolder extends RecyclerView.ViewHolder
    {
        ImageView imageView;
        public myViewHolder(@NonNull View itemView)
        {
            super(itemView);
            imageView = itemView.findViewById(R.id.thumbnail);
        }
    }

}
