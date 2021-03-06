package com.example.audioplayer;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadata;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MyViewHolder> {

    private Context mContext;
    static ArrayList<MusicFiles> mFiles;

    MusicAdapter(Context mContext, ArrayList<MusicFiles> mFiles)
    {
        this.mFiles = mFiles;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.music_items, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        holder.file_name.setText(mFiles.get(position).getTitle());
        byte[] image = getAlbumArt(mFiles.get(position).getPath());
        if (image != null){
            Glide.with(mContext).asBitmap()
                    .load(image)
                    .into(holder.album_art);
        } else {
            Glide.with(mContext)
                    .load(R.drawable.download)
                    .into(holder.album_art);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, PlayerActivity.class);
                intent.putExtra("position", position);
                mContext.startActivity(intent);
            }
        });

        holder.menuMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                PopupMenu popupMenu = new PopupMenu(mContext, v);
                popupMenu.getMenuInflater().inflate(R.menu.popup, popupMenu.getMenu());
                popupMenu.show();

                popupMenu.setOnMenuItemClickListener((item) -> {
                    switch (item.getItemId()) {
                        case R.id.delete:
                            //Toast.makeText(mContext, "Delete Clicked", Toast.LENGTH_SHORT).show();
                            deleteFile(position, v);
                            break;
                        case R.id.info:
                            try {
                                infoFile(position, v);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            break;

                    }
                    return true;
                });
            }
        });
    }

    private void infoFile(int position, View v) throws IOException {
        Uri contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                Long.parseLong(mFiles.get(position).getId())); // content://

        File file = new File(mFiles.get(position).getPath());

        RandomAccessFile raf = new RandomAccessFile(file, "rw");

        try {
            byte[] bytes = new byte[(int) raf.length()];
            int bytesRead = raf.read(bytes, 0, bytes.length);

                if (UnicodeFormatter.byteToHex(bytes[0]).equals("49")
                        && UnicodeFormatter.byteToHex(bytes[1]).equals("44")
                        && UnicodeFormatter.byteToHex(bytes[2]).equals("33")){

                    Snackbar.make(v, "ID3 tag Found", Snackbar.LENGTH_LONG).show();
            } else {
                    Snackbar.make(v, "Can`t find ID3 tag", Snackbar.LENGTH_LONG).show();
                }

        } finally {
            raf.close();
        }


    }

    private void deleteFile(int position, View v){

        Uri contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                Long.parseLong(mFiles.get(position).getId())); // content://

        File file = new File(mFiles.get(position).getPath());

        boolean deleted = file.delete(); // will delete the file
        if (deleted) {
            mContext.getContentResolver().delete(contentUri, null, null);
            mFiles.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, mFiles.size());
            Snackbar.make(v, "File Deleted", Snackbar.LENGTH_LONG).show();
        } else { // may occur if the file is stored in sdCard
            Snackbar.make(v, "File Can`t Be Deleted", Snackbar.LENGTH_LONG).show();

        }
    }

    @Override
    public int getItemCount() {
        return mFiles.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        TextView file_name;
        ImageView album_art, menuMore;

        public MyViewHolder(@NotNull View itemView) {
            super(itemView);
            file_name = itemView.findViewById(R.id.music_file_name);
            album_art = itemView.findViewById(R.id.music_img);
            menuMore = itemView.findViewById(R.id.menuMore);


        }
    }

    private byte[] getAlbumArt(String uri){
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art = retriever.getEmbeddedPicture();
        retriever.release();
        return art;
    }
    void updateList(ArrayList<MusicFiles> musicFilesArrayList){
        mFiles = new ArrayList<>();
        mFiles.addAll(musicFilesArrayList);
        notifyDataSetChanged();
    }
}

    class UnicodeFormatter  {

        static public String byteToHex(byte b) {
            // Returns hex String representation of byte b
            char hexDigit[] = {
                    '0', '1', '2', '3', '4', '5', '6', '7',
                    '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
            };
            char[] array = { hexDigit[(b >> 4) & 0x0f], hexDigit[b & 0x0f] };
            return new String(array);
        }

        static public String charToHex(char c) {
            // Returns hex String representation of char c
            byte hi = (byte) (c >>> 8);
            byte lo = (byte) (c & 0xff);
            return byteToHex(hi) + byteToHex(lo);
        }

    } // class

