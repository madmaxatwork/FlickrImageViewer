package com.example.flickrimageviewer;

import com.example.flickrimageviewer.utils.FlickrDefines;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class FullImageActivity extends Activity {
	  private ImageView mImg;
	  private TextView mTextView;
	  private Bitmap mBitmap;
	  private ProgressDialog mPDialog;
	  private String mUrl;
	  private LoadImage mCurrentTask;
	  
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		Bundle imgData = getIntent().getBundleExtra(FlickrDefines.PHOTO_DATA_KEY);	
		
		setContentView(R.layout.image_viewer);
	    mImg = (ImageView)findViewById(R.id.image_photo);
	    mTextView = (TextView)findViewById(R.id.caption_title);
		
	    String caption = imgData.getString(FlickrDefines.PHOTO_TITLE_KEY, "Boom");
	    mUrl = imgData.getString(FlickrDefines.PHOTO_DOWNLOAD_URL_KEY);
	    	    
	    mTextView.setText(caption);
	    
	    mCurrentTask = (LoadImage) new LoadImage().execute(mUrl);
		
	}

	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mCurrentTask.cancel(true);
	}


	private class LoadImage extends AsyncTask<String, String, Bitmap> {
	    @Override
	        protected void onPreExecute() {
	            super.onPreExecute();
	            mPDialog = new ProgressDialog(FullImageActivity.this);
	            mPDialog.setMessage(getResources().getString(R.string.loading_image));
	            mPDialog.show();
	    }
	       protected Bitmap doInBackground(String... args) {
	         try {
	        	 byte[] bitmapBytes = new FlickrImageFetch().getUrlBytes(args[0]);
	              mBitmap = BitmapFactory
	                     .decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
	             
	        } catch (Exception e) {
	              e.printStackTrace();
	        }
	      return mBitmap;
	       }
	       protected void onPostExecute(Bitmap image) {
	         if(image != null){
	           mImg.setImageBitmap(image);
	           mPDialog.dismiss();
	         }else{
	           mPDialog.dismiss();
	           Toast.makeText(FullImageActivity.this, "Image Does Not exist or Network Error", Toast.LENGTH_SHORT).show();
	         }
	       }
	   }	
	
}
