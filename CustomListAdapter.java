package edu.stanford.cs108.bunnyworld;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by haolunfang on 2017/3/12.
 */

public class CustomListAdapter extends ArrayAdapter<String>  {


    private static SingletonData data;
    private List<String> imageNameList;
    private int resource;
    private Context context;

    public CustomListAdapter(Context context, int resource, List<String> imageNameList) {
        super(context, resource, imageNameList);
        data = SingletonData.getInstance(context);
        this.resource = resource;
        this.context = context;
        this.imageNameList = imageNameList;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View row = convertView;
        CustomListHolder holder = null;

        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        row = inflater.inflate(resource, parent, false);
        String imageName = imageNameList.get(position);
        holder = new CustomListHolder();
        holder.imageView = (ImageView)row.findViewById(R.id.image_view);
        holder.name = (TextView)row.findViewById(R.id.image_name);
        holder.editText = (EditText)row.findViewById(R.id.edit_image_name);
        holder.renameImage = (Button)row.findViewById(R.id.rename_image);
        holder.deleteImage = (Button)row.findViewById(R.id.delete_image);
        holder.renameImage.setTag(imageName);
        holder.deleteImage.setTag(imageName);

        Bitmap bitmapStr = data.getBitmap(imageName);
        holder.imageView.setImageBitmap(bitmapStr);
        holder.name.setText(imageName);

        row.setTag(holder);

        return row;
    }

    public static class CustomListHolder {
        ImageView imageView;
        TextView name;
        EditText editText;
        Button renameImage;
        Button deleteImage;
    }
}
