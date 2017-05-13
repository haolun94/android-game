package edu.stanford.cs108.bunnyworld;

import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

public class uploadActivity extends AppCompatActivity {
    private static int RESULT_LOAD_IMG = 1;
    private static int RESULT_LOAD_AUD = 2;
    SingletonData data;
    private List<String> soundNameFromAssets;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        Intent intent = getIntent();
        data = SingletonData.getInstance(this);
        implementListView();
        soundNameFromAssets = data.getSoundsFromAssets();
    }

    public void redrawListView() {
        // dialog box need to be created before hand
        final ListView lv = (ListView)findViewById(R.id.sound_listview);
        final List<String> soundNameList = data.getSoundNameList();
        String[] items = soundNameList.toArray(new String[soundNameList.size()]);
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.list_item_1, items);
        lv.setAdapter(adapter);
    }
    public void implementListView() {
        // dialog box need to be created before hand
        final ListView lv = (ListView)findViewById(R.id.sound_listview);
        final List<String> soundNameList = data.getSoundNameList();
        String[] items = soundNameList.toArray(new String[soundNameList.size()]);
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.list_item_1, items);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, final int position, long id) {
                view.setSelected(true);
                final String currSoundName = parent.getItemAtPosition(position).toString();
                Button renameButton = (Button) findViewById(R.id.rename_sound_button);
                renameButton.setOnClickListener(new View.OnClickListener(){

                    @Override
                    public void onClick(View v) {// when a list item is selected, listen to the RENAME button and if rename is clicked, open a dialog box
                        System.out.println("You selected item " + position);//todo: this is for debugging. May delete it.
                        if (soundNameFromAssets.contains(currSoundName)) {
                            Toast.makeText(v.getContext(), "Sound name from assets cannot be modified.",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        final Dialog dialog = new Dialog(v.getContext());
                        dialog.setContentView(R.layout.dialog_upload);
                        dialog.setTitle("Rename");
                        dialog.show();

                        // Rename button
                        Button OKButton = (Button) dialog.findViewById(R.id.upload_dialogButtonOK);
                        OKButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                EditText editName = (EditText) dialog.findViewById(R.id.upload_edit_sound);
                                String newSoundName = editName.getText().toString();

                                if (!newSoundName.equals(currSoundName) && !newSoundName.isEmpty()) {
                                    if (soundNameList.contains(newSoundName)) {
                                        //((Activity)getContext()).dialogBox("a");
                                        Toast.makeText(v.getContext(), "This image name is already in use",
                                                Toast.LENGTH_LONG).show();
                                    } else {
                                        data.updateSoundName(newSoundName, currSoundName);
                                        //redraw the listview
                                        final List<String> soundNameList = data.getSoundNameList();
                                        String[] items = soundNameList.toArray(new String[soundNameList.size()]);
                                        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(v.getContext(),
                                                R.layout.list_item_1, items);
                                        lv.setAdapter(adapter);
                                    }

                                }//if (!newSoundName.equals(currSoundName) && !newSoundName.isEmpty())
                                dialog.dismiss();
                            }//onClick
                        });
                    } //onClick
                });

                // Delete button
                Button deleteButton = (Button) findViewById(R.id.delete_sound_button);
                deleteButton.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        if (soundNameFromAssets.contains(currSoundName)) {
                            Toast.makeText(v.getContext(), "Sound name from assets cannot be deleted.",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        data.deleteSoundName(currSoundName);
                        //redraw the listview
                        final List<String> soundNameList = data.getSoundNameList();
                        String[] items = soundNameList.toArray(new String[soundNameList.size()]);
                        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(v.getContext(),
                                R.layout.list_item_1, items);
                        lv.setAdapter(adapter);
                        Toast.makeText(v.getContext(), currSoundName +" is deleted",
                                Toast.LENGTH_LONG).show();
                    }
                });


                // Play button
                Button playButton = (Button) findViewById(R.id.play_sound);
                playButton.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        playSound(view, currSoundName);
                    }
                });

            } //onItemClick
        });//  lv.setOnItemClickListener

    }

    public void loadImagefromGallery(View view) {
        // Create intent to Open Image applications like Gallery, Google Photos
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        // Start the Intent
        startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
    }

    public void loadAudiofromMusic(View view) {
        Intent musicIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
        musicIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
        startActivityForResult(musicIntent, RESULT_LOAD_AUD);
    }


    public void playSound(View view, String soundName) {
        Uri ur = data.getUri(soundName);
        if (ur != null){
            MediaPlayer mp = MediaPlayer.create(view.getContext(), ur);
            mp.start();
        }
    }


    // upload image to the resource
    // -- Sofia
    private void uploadImage(Intent intent) {
        Uri selectedImage = intent.getData();
        String[] filePathColumn = { MediaStore.Images.Media.DATA };

        // Get the cursor
        Cursor cursor = getContentResolver().query(selectedImage,
                filePathColumn, null, null, null);
        // Move to first row
        cursor.moveToFirst();

        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String imgDecodableString = cursor.getString(columnIndex);
        cursor.close();
        // add this image to database
        if (data.addNewImage(imgDecodableString)) {
            Toast.makeText(this, "You have added a new image",
                    Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "This image already exists",
                    Toast.LENGTH_LONG).show();
        }
    }

    //upload sound to the resource
    // --Sofia
    private void uploadSound(Intent intent) {
        Uri selectedAudio = intent.getData();
        String uriString = selectedAudio.toString();
        if (data.addNewSound(uriString)) {
            Toast.makeText(this, "You have added a new sound file",
                    Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "This image already exists",
                    Toast.LENGTH_LONG).show();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        try {
            // When an Image is picked
//            if (requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK
//                    && null != intent) {
//                uploadImage(intent);
//
//                // Get the Image from data
//            }
            // When an Audio file is picked
            if(requestCode == RESULT_LOAD_AUD && resultCode == RESULT_OK
                    && null != intent){
                uploadSound(intent);
                redrawListView();
            } else {
                Toast.makeText(this, "You haven't picked any file.",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "File uploading failed", Toast.LENGTH_LONG)
                    .show();
        }
    }


}
