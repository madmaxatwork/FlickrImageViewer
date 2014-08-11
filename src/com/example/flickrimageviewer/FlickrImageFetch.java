package com.example.flickrimageviewer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import com.example.flickrimageviewer.utils.FlickrDefines;

import android.net.Uri;
import android.util.Log;

public class FlickrImageFetch {
    public static final String TAG = "FlickrImageFetch";
    
    public ArrayList<GalleryItem> fetchItemsQuery() {
        String url = Uri.parse(FlickrDefines.ENDPOINT).buildUpon()
                .appendQueryParameter(FlickrDefines.METHOD_CONST, FlickrDefines.METHOD_GET_RECENT)
                .appendQueryParameter(FlickrDefines.API_KEY_CONST, FlickrDefines.API_KEY)
                .appendQueryParameter(FlickrDefines.PARAM_EXTRAS, FlickrDefines.EXTRA_SMALL_URL)               
                .build().toString();
        return downloadGalleryItems(url);
    }
    
    public ArrayList<GalleryItem> downloadGalleryItems(String  url) {
        ArrayList<GalleryItem> items = new ArrayList<GalleryItem>();
        
        try {
            String xmlString = getUrl(url);
            Log.i(TAG, "Received xml: " + xmlString);
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new StringReader(xmlString));
            
            parseItems(items, parser);
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch items", ioe);
        } catch (XmlPullParserException xppe) {
            Log.e(TAG, "Failed to parse items", xppe);
        }
        return items;
    }

    public ArrayList<GalleryItem> searchQuery(String query) {
        String url = Uri.parse(FlickrDefines.ENDPOINT).buildUpon()
                .appendQueryParameter(FlickrDefines.METHOD_CONST, FlickrDefines.METHOD_SEARCH)
                .appendQueryParameter(FlickrDefines.API_KEY_CONST, FlickrDefines.API_KEY)
                .appendQueryParameter(FlickrDefines.PARAM_EXTRAS, FlickrDefines.EXTRA_SMALL_URL)
                .appendQueryParameter(FlickrDefines.PARAM_TEXT, query)
                .build().toString();
        return downloadGalleryItems(url);
    }

    void parseItems(ArrayList<GalleryItem> items, XmlPullParser parser) throws XmlPullParserException, IOException {
        int eventType = parser.next();

        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG &&
            	FlickrDefines.XML_PHOTO.equals(parser.getName())) {
                String id = parser.getAttributeValue(null, "id");
                String caption = parser.getAttributeValue(null, "title");
                String smallUrl = parser.getAttributeValue(null, FlickrDefines.EXTRA_SMALL_URL);
                String owner = parser.getAttributeValue(null, "owner");
                String secret = parser.getAttributeValue(null, "secret");
                String server = parser.getAttributeValue(null, "server");
                String farm = parser.getAttributeValue(null, "farm");

                if(smallUrl != null) {
                    GalleryItem item = new GalleryItem();
                    item.mId = id;
                    item.mCaption = caption;
                    item.mUrl = smallUrl;
                    item.mFarm = farm;
                    item.mSecret = secret;
                    item.mServer = server;
                    item.mOwner = owner;
                    items.add(item);                	
                }
            }

            eventType = parser.next();
        }
    }
    
    //Open connection and Start the download
    public byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();
            
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return null;
            }

            int bytesRead = 0;
            byte[] buffer = new byte[4 * 1024];
            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
    }
  
   
    String getUrl(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }    
    
}
