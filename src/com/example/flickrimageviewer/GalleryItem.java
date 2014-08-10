package com.example.flickrimageviewer;

// http://developer.android.com/training/articles/perf-tips.html
public class GalleryItem {
    public String mCaption;
    public String mId;
    public String mUrl;
    public String mSecret;
    public String mServer;
    public String mFarm;    
    public String mOwner;
    private static final String PHOTO_IMAGE_URL = "http://farm%s.static.flickr.com/%s/%s_%s%s.jpg";
        
    public String getDownloadUrl(){
    	return String.format(PHOTO_IMAGE_URL, mFarm, mServer, mId, mSecret, "_b");
    }
    
    public String toString() {
        return mCaption;
    }

}
