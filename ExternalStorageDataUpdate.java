public class ExternalStorageUpdate extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        File src = new File(Environment.getExternalStorageDirectory() + "/ram", "kitna.mp3");
        Uri uri1 = getAudioContentUri(MainActivity.this, src);
        Uri uri = Uri.fromFile(src);
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Audio.Media.ALBUM,"Chetan");
        contentValues.put(MediaStore.Audio.Media.ARTIST,"Ram");
        contentValues.put(MediaStore.Audio.Media.TITLE,"Chetan Ram");
        contentValues.put(MediaStore.Audio.Media.COMPOSER, "CR");
        contentValues.put(MediaStore.Audio.Media.DISPLAY_NAME, "Chetan Ram Song");
        contentValues.put(MediaStore.Audio.Media.YEAR, "2017");
        int i = getContentResolver().update(uri1, contentValues, null, null);
        Cursor c = getContentResolver().query(uri1, null, null, null, null);
        c.moveToFirst();
        Log.e("", "");
/*
        File src = new File(Environment.getExternalStorageDirectory()+"/ram","kitna.mp3");
        MusicMetadataSet src_set = null;
        try {
            src_set = new MyID3().read(src);
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } // read metadata

        if (src_set == null) // perhaps no metadata
        {
            Log.i("NULL", "NULL");
        }
        else
        {
            try{
                IMusicMetadata metadata = src_set.getSimplified();
                String artist = metadata.getArtist();
                String album = metadata.getAlbum();
                String song_title = metadata.getSongTitle();
                Number track_number = metadata.getTrackNumber();
                Log.i("artist", artist);
                Log.i("album", album);
            }catch (Exception e) {
                e.printStackTrace();
            }
            File dst = new File(Environment.getExternalStorageDirectory()+"/ram","kitna.mp3");
            MusicMetadata meta = new MusicMetadata("name");
            meta.setAlbum("Chetan");
            meta.setArtist("ram");
            try {
                new MyID3().write(src, dst, src_set, meta);
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ID3WriteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }  // write updated metadata
        }*/
    }

    public static Uri getAudioContentUri(Context context, File audiFile) {
        String filePath = audiFile.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Media._ID},
                MediaStore.Audio.Media.DATA + "=? ",
                new String[]{filePath}, null);

        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/audio/media");
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            if (audiFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Audio.Media.DATA, filePath);
                return context.getContentResolver().insert(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }
}
