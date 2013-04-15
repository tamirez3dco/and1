package com.example.mushmush3;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends Activity {
	
	public int chosenPosition = -1;
	public String chsonImagePath = "";
	private static final int SELECT_PICTURE = 1;
	
	File sdcard_pictures = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/MUSHMUSH/");
	String tempSoundPath = sdcard_pictures + "/tmp_sound.mp3";
	MediaPlayer mPlayer;
	MediaRecorder mRecorder;
	
    public static Bitmap get_Bitmap_From_JPG_avoid_outofmemory_exception(String path, int reqWidth,int reqHeight)
    {
    	BitmapFactory.Options options = new BitmapFactory.Options();
    	options.inJustDecodeBounds = true;
    	BitmapFactory.decodeFile(path,options);
    	
    	int importedHeight = options.outHeight;
    	int importedWidth = options.outWidth;
    	String importedImageType = options.outMimeType;

    	Bitmap ret;
    	// scale down picture to avoif out of memory exception....
    	if (importedWidth > 0 && importedHeight > 0)
    	{
        	float ratioWidth = (float)importedWidth / (float) reqWidth;
        	float ratioHeight = (float)importedHeight / (float) reqHeight;
        	float maxRation = Math.max(ratioWidth, ratioHeight);
        	double pow_of_2_ratio = Math.pow(2.0, Math.ceil(Math.log(maxRation)/Math.log(2.0)));
        	
        	options.inSampleSize = (int)pow_of_2_ratio;
        	options.inJustDecodeBounds = false;
        	ret = BitmapFactory.decodeFile(path,options); 
    	}
    	else 
    	{
    		Log.e("BALAGAN in pictures",path);
    		ret = BitmapFactory.decodeFile(path);
    	}
    	return ret;

    }

	   public void onActivityResult(int requestCode, int resultCode, Intent data) {
	        if (resultCode == RESULT_OK) {
	            if (requestCode == SELECT_PICTURE) {
	                Uri selectedImageUri = data.getData();
	                chsonImagePath = getPath(selectedImageUri);
	                
	                Bitmap bigBMP = get_Bitmap_From_JPG_avoid_outofmemory_exception(chsonImagePath, 400, 600);
	                if (bigBMP != null)
	                {
		        		Log.v("bigBMP","bigBMP.width="+bigBMP.getWidth()+" ,bigBMP.height="+bigBMP.getHeight());
		        		ImageView mainImage = (ImageView)findViewById(R.id.mainimage);
		        		mainImage.setImageBitmap(bigBMP);
		        		mainImage.invalidate();
		        		
		        		// enable setImage
	                }
	                else
	                {
	                	Toast.makeText(com.example.mushmush3.MainActivity.this, "could not load image ?!?!", Toast.LENGTH_SHORT).show();
		        		// disable setImage
	                }
	            }
	        }
	    }
	   
	   public String getPath(Uri uri) {
		   String res = null;
		    String[] proj = { MediaStore.Images.Media.DATA };
		    Cursor cursor = getContentResolver().query(uri, proj, null, null, null);
		    if(cursor.moveToFirst()){;
		       int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		       res = cursor.getString(column_index);
		    }
		    cursor.close();
		    return res;
	    }
	   
	   static public void notifyMediaScannerService(Context context, String path) {
		    MediaScannerConnection.scanFile(context,
		            new String[] { path }, null,
		            new MediaScannerConnection.OnScanCompletedListener() {
		        public void onScanCompleted(String path, Uri uri) {
		            Log.i("ExternalStorage", "Scanned " + path + ":");
		            Log.i("ExternalStorage", "-> uri=" + uri);
		        }
		    });
		}
	    
    /** Called when the user touches the button */
    public void setImageClicked(View view) {
    	
    	Log.v("setImageClicked", ""+chosenPosition);
    	
    	ImageView mainImage = (ImageView)findViewById(R.id.mainimage);
    	Bitmap bmp = mainImage.getDrawingCache();

		String desiredImagePath = sdcard_pictures + "/sample_" + chosenPosition + ".jpg";
/*        
        try {
            FileOutputStream out = new FileOutputStream(desiredImagePath);
            bmp.compress(Bitmap.CompressFormat.JPEG, 90, out);
     } catch (Exception e) {
            e.printStackTrace();
     }
*/     
        
        try {
            FileInputStream in = new FileInputStream(chsonImagePath);
            FileOutputStream out = new FileOutputStream(desiredImagePath);
            byte[] buf = new byte[1024];
            int i = 0;
            while ((i = in.read(buf)) != -1) {
                out.write(buf, 0, i);
            }
            in.close();
            out.close();
        } catch(IOException e) {
            System.out.println("Error copying file");
        }

        GridView gridview = (GridView) findViewById(R.id.thegrid);
        ViewGroup gv = gridview;
        LinearLayout cell = (LinearLayout)gv.getChildAt(chosenPosition);
        ImageView imageThumb = (ImageView)cell.getChildAt(0);
        imageThumb.setImageBitmap(this.get_Bitmap_From_JPG_avoid_outofmemory_exception(desiredImagePath, 300, 360));
        imageThumb.invalidate();
        
        
		sendBroadcast (
			    new Intent(Intent.ACTION_MEDIA_MOUNTED, 
			        Uri.parse("file://" + Environment.getExternalStorageDirectory())));

        
        
        // Do something in response to button click
    }            
    public void setTextClicked(View view) {
    	Log.v("setTextClicked", ""+chosenPosition);


        EditText mainText = (EditText)findViewById(R.id.maintext);
    	String textToWrite = mainText.getText().toString();
    	String pathToWrite = sdcard_pictures + "/sample_" + chosenPosition + ".txt";
    	
    	try {
			File myFile = new File(pathToWrite);
			myFile.createNewFile();
			FileOutputStream fOut = new FileOutputStream(myFile);
			OutputStreamWriter myOutWriter = 
									new OutputStreamWriter(fOut,"windows-1255");
			myOutWriter.append(textToWrite);
			myOutWriter.close();
			fOut.close();
			Toast.makeText(getBaseContext(),
					"Done writing SD 'mysdfile.txt'",
					Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			Toast.makeText(getBaseContext(), e.getMessage(),
					Toast.LENGTH_SHORT).show();
		}

    }            
    public void setCloseClicked(View view) {
    	Log.v("setCloseClicked", ""+chosenPosition);
    	LinearLayout mainLayout = (LinearLayout)findViewById(R.id.mainvertical);
    	View theGrid = findViewById(R.id.thegrid);
		theGrid.setVisibility(View.VISIBLE);
    	mainLayout.setVisibility(View.INVISIBLE);
    }            

    public void recordClicked(View view) {
    	ToggleButton tb = (ToggleButton)view;
    	if (tb.isChecked()) 
    	{
    		startRecording();
    		Log.v("start record Clicked", ""+chosenPosition);
    		findViewById(R.id.button_play).setEnabled(false);
    		findViewById(R.id.button_save).setEnabled(false);
    		
    	}
    	else
    	{
    		stopRecording();
    		Log.v("start record Clicked", ""+chosenPosition);
    		findViewById(R.id.button_play).setEnabled(true);
    		findViewById(R.id.button_save).setEnabled(true);
    	}
    	
    }            
    public void playClicked(View view) {
    	Log.v("playClicked", ""+chosenPosition);
    	startPlaying();
    }            

       
    public void saveClicked(View view) {
    	Log.v("saveClicked", ""+chosenPosition);
    	File tmpfile = new File(tempSoundPath);
    	String desiredPath = sdcard_pictures + "/sample_" + chosenPosition + ".3gpp";
    	File desiredFile = new File(desiredPath);
    	tmpfile.renameTo(desiredFile);
    	Toast.makeText(com.example.mushmush3.MainActivity.this, "Sound file save for position " + chosenPosition, Toast.LENGTH_SHORT).show();
    }            

    private void startPlaying() {
    	mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(tempSoundPath);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e("startPlaying", "prepare() failed");
        }
    }

    private void stopPlaying() {
        mPlayer.release();
        mPlayer = null;
    }

    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC );
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(tempSoundPath);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e("startRecording", "prepare() failed");
        }

        mRecorder.start();
    }

    private void stopRecording() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
    }

    public void newClicked(View view) {
    	Log.v("newClicked", "new clicked");
    	File files[] = sdcard_pictures.listFiles(new img_filter());
    	int newPosition = files.length; 
    	
    	Resources res = getBaseContext().getResources();
    	int id = R.drawable.image_new; 
    	Bitmap b = BitmapFactory.decodeResource(res, id);
    	
    	String new_jpg_path = sdcard_pictures + "/sample_" + newPosition + ".jpg";
    	File file = new File(new_jpg_path);
    	try
    	{
        	FileOutputStream fos = new FileOutputStream(file);
        	b.compress(CompressFormat.JPEG, 90, fos);  		
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    	}
    	
       	String textToWrite = "beynatayim";
       	String pathToWrite = sdcard_pictures + "/sample_" + newPosition + ".txt";
       	
       	try {
   			File myFile = new File(pathToWrite);
   			myFile.createNewFile();
   			FileOutputStream fOut = new FileOutputStream(myFile);
   			OutputStreamWriter myOutWriter = 
   									new OutputStreamWriter(fOut,"windows-1255");
   			myOutWriter.append(textToWrite);
   			myOutWriter.close();
   			fOut.close();
   			Toast.makeText(getBaseContext(),
   					"Done writing SD 'mysdfile.txt'",
   					Toast.LENGTH_SHORT).show();
   		} catch (Exception e) {
   			Toast.makeText(getBaseContext(), e.getMessage(),
   					Toast.LENGTH_SHORT).show();
   		}
    }            
    
    
    @Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GridView gridview = (GridView) findViewById(R.id.thegrid);
        gridview.setAdapter(new ImageAdapter(this));

        gridview.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
            	chosenPosition = position;
                Log.v("v1","v.x=" +v.getX() + " ,v.y=" + v.getY() + " ,v.width="+v.getWidth()+" ,v.height="+v.getHeight()+", scaleX="+v.getScaleX() + ", scaleY=" + v.getScaleY());

                DisplayMetrics displaymetrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
                int screen_height = displaymetrics.heightPixels;
                int screen_width = displaymetrics.widthPixels;

                
                LinearLayout mainLayout = (LinearLayout)findViewById(R.id.mainvertical);
                ImageView mainImage = (ImageView)findViewById(R.id.mainimage);
                EditText mainText = (EditText)findViewById(R.id.maintext);
                
                
                
                mainImage.setScaleType(ScaleType.FIT_XY);
                //popupView.setX(v.getX());
                //popupView.setY(v.getY());
                mainLayout.setX(0);
                mainLayout.setY(0);
                
                mainLayout.getLayoutParams().height = screen_height; 
                mainLayout.getLayoutParams().width = screen_width; 
                mainLayout.setPivotX(0);
                mainLayout.setPivotY(0);
                mainLayout.setScaleX((float) 1);
                mainLayout.setScaleY((float) 1);
                
                mainImage.getLayoutParams().height = (int)(screen_height * 3.0 / 8.0); 
                mainImage.getLayoutParams().width = screen_width; 

                mainText.getLayoutParams().height = (int)(screen_height * 1.0 / 8.0); 
                mainText.getLayoutParams().width = screen_width; 

                
                
                String file_name = "sample_" + position;
                Resources r = getResources();
                int drawableId = r.getIdentifier(file_name, "drawable", "com.example.mushmush3");
                
                String jpg_path = sdcard_pictures + "/sample_" + position + ".jpg";
                Bitmap bigBMP = MainActivity.get_Bitmap_From_JPG_avoid_outofmemory_exception(jpg_path, screen_width, screen_height);
                
                
        		Log.v("bigBMP","bigBMP.width="+bigBMP.getWidth()+" ,bigBMP.height="+bigBMP.getHeight());
        		mainImage.setImageBitmap(bigBMP);

//        		Log.v("popupView2","v.x=" + popupView.getX() + " ,v.y=" + popupView.getY() + " ,v.width="+popupView.getWidth()+" ,v.height="+popupView.getHeight()+", scaleX="+popupView.getScaleX() + ", scaleY=" + popupView.getScaleY());

                String strLine = null;
                FileInputStream fis;
                final StringBuffer storedString = new StringBuffer();

                try {
                	String str = sdcard_pictures + "/sample_" + position + ".txt";
                	File f = new File(str);
                	if (f.exists())
                	{
                        fis = new FileInputStream(str);
                        BufferedReader d = new BufferedReader(new InputStreamReader(fis,"windows-1255"));
                        //BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(pathname), "UTF-16LE"));

                        if ((strLine = d.readLine()) != null) {
                            storedString.append(strLine);
                        }

                        d.close();
                        fis.close();
                	}
                	else strLine = "Klum";
                		
                }
                catch  (Exception e) {  
                	e.printStackTrace();
                }
                
                mainText.setText(strLine);
        		
        		
        		mainLayout.setVisibility(View.VISIBLE);
        		View theGrid = findViewById(R.id.thegrid);
        		theGrid.setVisibility(View.INVISIBLE);
                
        		mainText.setOnClickListener(new Button.OnClickListener(){

                	@Override
                	public void onClick(View v) 
                	{
                		Log.v("click mainText","v.x=" + v.getX() + " ,v.y=" + v.getY() + " ,v.width="+v.getWidth()+" ,v.height="+v.getHeight()+", scaleX="+v.getScaleX() + ", scaleY=" + v.getScaleY());
                		//v.setVisibility(View.INVISIBLE);
                	}
                });
                       
        		
        		mainImage.setOnClickListener(new Button.OnClickListener(){

                	@Override
                	public void onClick(View v) 
                	{
                			    
                		Log.v("click mainImage","v.x=" + v.getX() + " ,v.y=" + v.getY() + " ,v.width="+v.getWidth()+" ,v.height="+v.getHeight()+", scaleX="+v.getScaleX() + ", scaleY=" + v.getScaleY());
                		//v.setVisibility(View.INVISIBLE);
            	    	Log.v("imageClicked", ""+chosenPosition);
            	        // Do something in response to button click
            	    	  // in onCreate or any event where your want the user to
                        // select a file
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(Intent.createChooser(intent,
                                "Select Picture"), SELECT_PICTURE);
                        

                	}
                });

            }
        });
        
        
   }

	public class img_filter implements FilenameFilter
	{

		@Override
		public boolean accept(File dir, String filename) {
			// TODO Auto-generated method stub
			return filename.endsWith(".jpg");
		}
		
	}
	
	public class ImageAdapter extends BaseAdapter {
        private Context mContext;

        public ImageAdapter(Context c) {
            mContext = c;
        }

        public int getCount() {
            File files[] = sdcard_pictures.listFiles(new img_filter());
            return files.length;
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        // create a new ImageView for each item referenced by the Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
        	LinearLayout ll;
        	
            ImageView imageView;
            
            TextView et;
            
            DisplayMetrics displaymetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
            int screen_width = displaymetrics.widthPixels;
            
            
            
            GridView gridView = (GridView)findViewById(R.id.thegrid);
            int thumbWidth = (screen_width - (gridView.getNumColumns() - 1) * gridView.getHorizontalSpacing()) / gridView.getNumColumns();
            
            if (convertView == null) {  // if it's not recycled, initialize some attributes
                ll = new LinearLayout(parent.getContext());
                ll.setOrientation(LinearLayout.VERTICAL);

                imageView = new ImageView(parent.getContext());
                imageView.setLayoutParams(new GridView.LayoutParams(thumbWidth, thumbWidth*2/3));
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                imageView.setPadding(8, 8, 8, 8);

                et = new TextView(parent.getContext());
                //et.setLayoutParams(new GridView.LayoutParams(thumbWidth, thumbWidth*1/3));
            } else {
                //imageView = (ImageView) convertView;
            	ll = (LinearLayout) convertView;
            	imageView = (ImageView) ll.getChildAt(0);
            	et = (TextView)ll.getChildAt(1);
            }

            
        	String jpg_path = sdcard_pictures + "/sample_" + position + ".jpg";
        	Bitmap thumbBMP = get_Bitmap_From_JPG_avoid_outofmemory_exception(jpg_path, 300, 360);

            
            
            imageView.setImageBitmap(thumbBMP);
            
            String strLine = null;
            FileInputStream fis;
            final StringBuffer storedString = new StringBuffer();

            try {
            	String str = sdcard_pictures + "/sample_" + position + ".txt";
            	File f = new File(str);
            	if (f.exists())
            	{
                    fis = new FileInputStream(str);
                    BufferedReader d = new BufferedReader(new InputStreamReader(fis,"windows-1255"));
                    //BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(pathname), "UTF-16LE"));

                    if ((strLine = d.readLine()) != null) {
                        storedString.append(strLine);
                    }

                    d.close();
                    fis.close();
            	}
            	else strLine = "Klum";
            		
            }
            catch  (Exception e) {  
            	e.printStackTrace();
            }
            
            et.setText(strLine);

            
            ll.removeAllViews();
            
            ll.addView(imageView, 0);
            ll.addView(et, 1);
            //return imageView;
            return ll;
        }
    }
}



