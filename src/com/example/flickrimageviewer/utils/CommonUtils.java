package com.example.flickrimageviewer.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class CommonUtils {

	private static final String TAG = "CommonUtils";
	
	public static int getNumberOfCores() {
		return Runtime.getRuntime().availableProcessors();
	}

	// Check if the Network is Online
	public static boolean isNetworkOnline(Context context) {
		boolean status = false;
		try {
			ConnectivityManager cm = (ConnectivityManager) context
					.getSystemService(context.CONNECTIVITY_SERVICE);
			NetworkInfo netInfo = cm.getNetworkInfo(0);
			if (netInfo != null
					&& netInfo.getState() == NetworkInfo.State.CONNECTED) {
				status = true;
			} else {
				netInfo = cm.getNetworkInfo(1);
				if (netInfo != null
						&& netInfo.getState() == NetworkInfo.State.CONNECTED)
					status = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return status;

	}

	public static final String md5(final String s) {
		final String MD5 = "MD5";
		try {
			// Create MD5 Hash
			MessageDigest digest = java.security.MessageDigest.getInstance(MD5);
			digest.update(s.getBytes());
			byte messageDigest[] = digest.digest();

			// Create Hex String
			StringBuilder hexString = new StringBuilder();
			for (byte aMessageDigest : messageDigest) {
				String h = Integer.toHexString(0xFF & aMessageDigest);
				while (h.length() < 2)
					h = "0" + h;
				hexString.append(h);
			}
			return hexString.toString();

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return "";
	}

	public static void writeToFile(byte[] bitmapBytes, String strFilePath) {
		try {
			FileOutputStream fos = new FileOutputStream(strFilePath);
			fos.write(bitmapBytes);
			fos.close();

		} catch (FileNotFoundException ex) {
			Log.w(TAG, "FileNotFoundException : " + ex);
		} catch (IOException ioe) {
			Log.w(TAG, "IOException : " + ioe);
		}

	}
	
	public static final String getApplicationDirectory(Context context){
		PackageManager m = context.getPackageManager();
		String s = context.getPackageName();
		try {
		    PackageInfo p = m.getPackageInfo(s, 0);
		    s = p.applicationInfo.dataDir;
		} catch (NameNotFoundException e) {
		    Log.w(TAG, "Error Package name not found ", e);
		}
		return s;
	}
	
	public static final boolean checkIfFileExists(String absFilePath){
		boolean fileExists = false;
		File myFile = new File(absFilePath);
		if(myFile.exists()){
			fileExists = true;
		}
		return fileExists;
	}
	
	public static final byte[] readFileBytes(String absFilePath){
		File file = new File(absFilePath);
	    int size = (int) file.length();
	    byte[] bytes = new byte[size];
	    try {
	        BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
	        buf.read(bytes, 0, bytes.length);
	        buf.close();
	    } catch (FileNotFoundException e) {
	        e.printStackTrace();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	    return bytes;
	}
	
	public static final boolean checkIfDirectoryExists(String absDirPath) {
		boolean dirExists = false;
		File myDir= new File(absDirPath);
		if(myDir.exists() && myDir.isDirectory()){
			dirExists = true;
		}
		else {
			myDir.mkdir();
		}
		return dirExists;
	}
	
}
