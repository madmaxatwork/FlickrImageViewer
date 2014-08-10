package com.example.flickrimageviewer;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.example.flickrimageviewer.utils.CommonUtils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.util.LruCache;
import android.util.Log;

public class ThumbnailDownloader {
    private static final String TAG = "ThumbnailDownloader";
	private LruCache<String, Bitmap> mMemoryCache;
	private static ExecutorService mExecutor;
	private String mDataDir;
    
	static {
		mExecutor = Executors.newFixedThreadPool(CommonUtils.getNumberOfCores() + 1);
		if(BuildConfig.DEBUG){
			Log.d(TAG, "Number of Cores" + CommonUtils.getNumberOfCores() + 1);
		}
	}
	
	public ThumbnailDownloader(int maxSize, String dirPath) {
		mMemoryCache = new LruCache<String, Bitmap>(maxSize);
		mDataDir = dirPath + "/" + "cache";
		CommonUtils.checkIfDirectoryExists(mDataDir);
	}
	
    public interface ThumbnailObserver {
    	public void onThumbnailAvailable(String url, Bitmap bmp);
    }

    class Downloader implements Runnable {
    	private ThumbnailObserver mObserver;
    	private String mURL;
    	
    	public Downloader(final ThumbnailObserver observer, final String url) {
    		mObserver = observer;
    		mURL = url;
		}
    	
		@Override
		public void run() {
			byte[] bitmapBytes = null;
			try {				
				//Generate hash
				String fileName = CommonUtils.md5(mURL);
				//Check whether it is in Disk
				if(CommonUtils.checkIfFileExists(mDataDir + "/" + fileName)) {
					bitmapBytes = CommonUtils.readFileBytes(mDataDir + "/" + fileName);
				}
				else {
					bitmapBytes = new FlickrImageFetch().getUrlBytes(mURL);	
					CommonUtils.writeToFile(bitmapBytes, mDataDir + "/" + fileName);
				}
			} catch (IOException e) {
				e.printStackTrace();
				return; // TODO : review this after wards
			}
			final Bitmap bitmap = BitmapFactory
		        .decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
	                    addBitmapToMemoryCache(mURL, bitmap);
	                    
	        if (null != mObserver) {
	        	mObserver.onThumbnailAvailable(mURL, bitmap);
	        }
		}    	
    }
    
    
    public void queueThumbnail(ThumbnailObserver observer, final String url) {
		if(BuildConfig.DEBUG){
			Log.d(TAG, "Download Url" + url);
		}
        mExecutor.execute(new Downloader(observer, url));
    }
    
    public void clearQueue() {
    }
    
    private void addBitmapToMemoryCache(String key, Bitmap bitmap) {
    	  if (getBitmapFromMemCache(key) == null) {
    	   mMemoryCache.put(key, bitmap); 
    	  } 
    }

	 public Bitmap getBitmapFromMemCache(String key) {
	  return (Bitmap) mMemoryCache.get(key); 
	 }
	 
	 public boolean existsInCache(String key) {
		  return ((key != null) && (Bitmap) mMemoryCache.get(key) != null); 
	 }
}
