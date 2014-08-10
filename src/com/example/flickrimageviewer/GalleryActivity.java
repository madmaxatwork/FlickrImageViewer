package com.example.flickrimageviewer;

import java.util.ArrayList;
import java.util.HashMap;

import com.example.flickrimageviewer.ThumbnailDownloader.ThumbnailObserver;
import com.example.flickrimageviewer.utils.CommonUtils;
import com.example.flickrimageviewer.utils.FlickrDefines;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.SearchView.OnCloseListener;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;


public class GalleryActivity extends Activity implements OnQueryTextListener,OnCloseListener{
	
	private String mQuery;	
	private GridView mGridView;
	private SearchView mSearchView;
	private ProgressDialog mPDialog;
	private ArrayList<GalleryItem> mItems;
	private GridViewAdapter mCustomGridAdapter;	
	private ThumbnailDownloader mThumbnailThread;
	private static boolean FROM_SETTING = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if(CommonUtils.isNetworkOnline(getApplicationContext())){
			//Fetch The URLs for Images
			updateItems();		
			mThumbnailThread = new ThumbnailDownloader(getCacheSize(),CommonUtils.getApplicationDirectory(this));
			mGridView = (GridView) findViewById(R.id.gridView);
			setupAdapter();		
		}
		else {
			showNoConnectionDialog(this);
		}
		
	}
	
	
	@Override
	protected void onResume() {
		super.onResume();
		if(FROM_SETTING) {
			FROM_SETTING = false;
			if(CommonUtils.isNetworkOnline(getApplicationContext())){
				//Fetch The URLs for Images
				updateItems();		
				mThumbnailThread = new ThumbnailDownloader(getCacheSize(),CommonUtils.getApplicationDirectory(this));
				mGridView = (GridView) findViewById(R.id.gridView);
				setupAdapter();		
			}
			else {
				showNoConnectionDialog(this);
			}	
		}
	}



	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    // Inflate the menu items for use in the action bar
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main, menu);
        mSearchView = (SearchView) menu.findItem(R.id.menu_item_search).getActionView();
        mSearchView.setOnQueryTextListener(this);
        mSearchView.setOnCloseListener(this);
	    return super.onCreateOptionsMenu(menu);
	}
	
    @Override
    public void onDestroy() {
        super.onDestroy();
    } 
       
    //Dialog if not Internet connection
    private void showNoConnectionDialog(Context ctx1) {
        final Context ctx = ctx1;
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setCancelable(true);
        builder.setMessage("No Connection");
        builder.setTitle("No Connection");
        builder.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	FROM_SETTING = true;
                ctx.startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	GalleryActivity.this.finish();
                return;
            }
        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
            	GalleryActivity.this.finish();
                return;
            }
        });

        builder.show();
    }
    
    
	//Retrieve all the items which are recent on Flickr
    public void updateItems() {
        new FetchItemsTask().execute();
    }
		
    void setupAdapter() {
        if (mGridView == null) return;
        
        if (mItems != null) {
    		mCustomGridAdapter = new GridViewAdapter(this, R.layout.row_grid, mItems);
    		mGridView.setAdapter(mCustomGridAdapter);
        } else {
        	mGridView.setAdapter(null);
        }
    }
    
	private class FetchItemsTask extends AsyncTask<Void,Void,ArrayList<GalleryItem>> {
        @Override
        protected ArrayList<GalleryItem> doInBackground(Void... params) {
                if (mQuery != null) {
                    return new FlickrImageFetch().searchQuery(mQuery);                    
                } else {
                    return new FlickrImageFetch().fetchItemsQuery();
                }
        }
        
        protected void onPreExecute() {
            super.onPreExecute();
            mPDialog = new ProgressDialog(GalleryActivity.this);
            mPDialog.setMessage(getResources().getString(R.string.loading_images));
            mPDialog.show();
        }

        @Override
        protected void onPostExecute(ArrayList<GalleryItem> items) {
            mItems = items;
            mPDialog.dismiss();
            setupAdapter();
        }
    }

	// Define the ViewHolder for the Grid
	static class ViewHolder {
		TextView imageTitle;
		ImageView image;
		String url;
	}
	
	private class GridViewAdapter extends BaseAdapter implements ThumbnailObserver {
		private Context context;
		private int layoutResourceId;
		private ArrayList<GalleryItem> flickrItems = new ArrayList<GalleryItem>();
		private HashMap<String, ViewHolder> mURLToHolderMap = new HashMap<String, ViewHolder>();

		public GridViewAdapter(Context context, int layoutResourceId , ArrayList<GalleryItem> items) {
			this.layoutResourceId = layoutResourceId;
			this.context = context;
			this.flickrItems = items;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View row = convertView;
			ViewHolder holder = null;

			if (row == null) {
				LayoutInflater inflater = ((Activity) context).getLayoutInflater();
				row = inflater.inflate(layoutResourceId, parent, false);
				holder = new ViewHolder();
				holder.imageTitle = (TextView) row.findViewById(R.id.text);
				holder.image = (ImageView) row.findViewById(R.id.image);
				row.setTag(holder);
			} else {
				ViewHolder prevHolder = (ViewHolder) convertView.getTag();
				if (null != prevHolder) {
					mURLToHolderMap.remove(prevHolder.url);
				}
				holder = (ViewHolder) row.getTag();
			}

			holder.imageTitle.setText("");
			holder.url = flickrItems.get(position).mUrl;
			mURLToHolderMap.put(holder.url, holder);

			if(mThumbnailThread.existsInCache(holder.url)) {
				holder.image.setImageBitmap(mThumbnailThread.getBitmapFromMemCache(holder.url));
            }
			else{
				holder.image.setImageResource(R.drawable.images);
				//Queue the Thumb-nail download
				mThumbnailThread.queueThumbnail(this, holder.url);
			}
			
		    row.setId(position);
		    row.setOnClickListener(new OnClickListener() {

		        @Override
		        public void onClick(View v) {
	                GalleryItem item = mItems.get(v.getId());
	                
	                Uri photoPageUri = Uri.parse(item.getDownloadUrl());
	                Intent i = new Intent(getApplicationContext(), FullImageActivity.class);
	                Bundle data = new Bundle();
	                data.putString(FlickrDefines.PHOTO_DOWNLOAD_URL_KEY, photoPageUri.toString());
	                data.putString(FlickrDefines.PHOTO_TITLE_KEY, Uri.parse(item.mCaption).toString());
	                i.putExtra(FlickrDefines.PHOTO_DATA_KEY, data);
	                startActivity(i);
		        }
		    });
		    return row;
		}

		//Callback once the thumb nail is downloaded
		@Override
		public void onThumbnailAvailable(final String url, final Bitmap bmp) {
			GalleryActivity.this.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					ViewHolder vh = mURLToHolderMap.get(url);
					if (null != vh) {
						vh.image.setImageBitmap(bmp);
					}
				}
			});
		}

		@Override
		public int getCount() {
			return flickrItems.size();
		}

		@Override
		public GalleryItem getItem(int position) {
			return flickrItems.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}		
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		return false;
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		mQuery = query;
		updateItems();
		mSearchView.clearFocus();
		return false;
	}

	@Override
	public boolean onClose() {
		if(mQuery != null){
			mQuery = null;
			updateItems();
		}
		return false;
	}			
	
	private Integer getCacheSize(){
		//Get the total size of memory to initialize the cache
		ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        final int memClass = am.getMemoryClass();
        // Use 1/8th of the available memory for this memory cache.
        return 1024 * 1024 * memClass / 8;
	}
	
}
