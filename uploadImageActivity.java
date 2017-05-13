package edu.stanford.cs108.bunnyworld;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
/*

*/
public class uploadImageActivity extends AppCompatActivity {

    private static int RESULT_LOAD_IMG = 1;
    String imgDecodableString;
    SingletonData data;
    private CustomListAdapter adapter;
    protected static List<String> cannotRemove = new ArrayList<String>(Arrays.asList("carrot",
            "carrot2", "death", "duck", "fire", "mystic")); // images that cannot be removed

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_image);
        Intent intent = getIntent();
        data = SingletonData.getInstance(this);
        List<String> imageNameList = data.getImageNameList();
        for (String s : cannotRemove) { imageNameList.remove(s); }
        adapter = new CustomListAdapter(this, R.layout.custom_list, imageNameList);
        ListView imageListView = (ListView)findViewById(R.id.image_list);
        imageListView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        List<String> imageNameList = data.getImageNameList();
        for (String s : cannotRemove) { imageNameList.remove(s); }
        adapter = new CustomListAdapter(this, R.layout.custom_list, imageNameList);
        ListView imageListView = (ListView)findViewById(R.id.image_list);
        imageListView.setAdapter(adapter);
    }

    public void loadImagefromGallery(View view) {
        // Create intent to Open Image applications like Gallery, Google Photos
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        // Start the Intent
        startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        try {
            // When an Image is picked
            if (requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK
                    && null != intent) {
                // Get the Image from data

                Uri selectedImage = intent.getData();
                String[] filePathColumn = { MediaStore.Images.Media.DATA };

                // Get the cursor
                Cursor cursor = getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                // Move to first row
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                imgDecodableString = cursor.getString(columnIndex);
                cursor.close();

                if (data.addNewImage(imgDecodableString)) {
                    Toast.makeText(this, "You have added a new image",
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "This image already exists",
                            Toast.LENGTH_LONG).show();
                }

            } else {
                Toast.makeText(this, "You haven't picked Image",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Image upload failed", Toast.LENGTH_LONG)
                    .show();
        }

    }

    public void onRenameImage(View view) {
        String oldName = (String)view.getTag();
        List<String> imageNameList = data.getImageNameList();
        for (String s : cannotRemove) imageNameList.remove(s);
        int position = imageNameList.indexOf(oldName);
        ListView imageListView = (ListView)findViewById(R.id.image_list);
        View row = imageListView.getChildAt(position - imageListView.getFirstVisiblePosition());
        EditText editText = (EditText) row.findViewById(R.id.edit_image_name);
        String newName = editText.getText().toString();
        if (!imageNameList.contains(newName) && !cannotRemove.contains(newName)) {
            data.renameImage(oldName, newName);
            CustomListAdapter.CustomListHolder holder = new CustomListAdapter.CustomListHolder();
            holder.imageView = (ImageView)row.findViewById(R.id.image_view);
            holder.name = (TextView)row.findViewById(R.id.image_name);
            holder.editText = (EditText)row.findViewById(R.id.edit_image_name);
            holder.renameImage = (Button)row.findViewById(R.id.rename_image);
            holder.deleteImage = (Button)row.findViewById(R.id.delete_image);
            holder.renameImage.setTag(newName);
            holder.deleteImage.setTag(newName);

            Bitmap bitmapStr = data.getBitmap(newName);
            holder.imageView.setImageBitmap(bitmapStr);
            holder.name.setText(newName);

            row.setTag(holder);
        }
    }

    public void onDeleteImage(View view) {
        String imageName = (String)view.getTag();
        data.deleteImage(imageName);
        adapter.remove(imageName);
    }
}
