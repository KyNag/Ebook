package ky.god.pdf;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

public class ChoosePDFActivity extends ListActivity {
	static private File  mDirectory;
	static private Map<String, Integer> mPositions = new HashMap<String, Integer>();
	private File         mParent;
	private File []      mDirs;
	private File []      mFiles;
	private Handler	     mHandler;
	private Runnable     mUpdateFiles;
	private ChoosePDFAdapter adapter;
	ConnectionDetector cd;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		String storageState = Environment.getExternalStorageState();

		if (!Environment.MEDIA_MOUNTED.equals(storageState)
				&& !Environment.MEDIA_MOUNTED_READ_ONLY.equals(storageState))
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.no_media_warning);
			builder.setMessage(R.string.no_media_hint);
			AlertDialog alert = builder.create();
			alert.setButton(AlertDialog.BUTTON_POSITIVE,"Dismiss",
					new OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							finish();
						}
					});
			alert.show();
			return;
		}
		 
		DownloadManager.Query query = null;
	    Cursor c = null;
	    DownloadManager downloadManager = null;
	    downloadManager = (DownloadManager)getSystemService(Context.DOWNLOAD_SERVICE);
	    query = new DownloadManager.Query();
	     if(query!=null) {
	                query.setFilterByStatus(DownloadManager.STATUS_FAILED|DownloadManager.STATUS_PAUSED|DownloadManager.STATUS_SUCCESSFUL|
	                        DownloadManager.STATUS_RUNNING|DownloadManager.STATUS_PENDING);
	            } else {
	                return;
	            }
	    c = downloadManager.query(query);
	    
	    
	    if(c.moveToFirst()) { 
	    int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS)); 
	    switch(status) { 
	    case DownloadManager.STATUS_PAUSED: 
	    break; 
	    case DownloadManager.STATUS_PENDING: 
	    	
			internet();
			
			showAlertDialog(this, "Please , Wait",
                    "Downloading Ebook , you can see the progress in notification bar.\nClicking on OK will close the application and you can open the application once the download is completed.\nClicking on cancel will proceed to the application", true);
			
	    break; 
	    case DownloadManager.STATUS_RUNNING: 
	    	
			internet();
			showAlertDialog(this, "Please , Wait",
                    "Downloading Ebook , you can see the progress in notification bar.\nClicking on OK will close the application and you can open the application once the download is completed.\nClicking on cancel will proceed to the application", true);
	    break; 
	    case DownloadManager.STATUS_SUCCESSFUL: 
	    break; 
	    case DownloadManager.STATUS_FAILED: 
	    	
			internet();
			showAlertDialog(this, "No Internet Connection",
                    "You don't have internet connection.", false);
	    break; 
	    }
	}
		
		
			mDirectory = (Environment.getExternalStoragePublicDirectory("/Android/obb/ky.God.pdf/" ) );
			
		// Create a list adapter...
		adapter = new ChoosePDFAdapter(getLayoutInflater());
		setListAdapter(adapter);
		
		
		
		
		
		

		// ...that is updated dynamically when files are scanned
		mHandler = new Handler();
		mUpdateFiles = new Runnable() {
			public void run() {
				Resources res = getResources();
				String appName = res.getString(R.string.app_name);
				String version = res.getString(R.string.version);
				String title = res.getString(R.string.picker_title);
				setTitle(String.format("PLEASE SELECT A COUNTRY FROM THE LIST"));

				//mParent = mDirectory.getParentFile();

				mDirs = mDirectory.listFiles(new FileFilter() {

					public boolean accept(File file) {
						return file.isDirectory();
					}
				});
				if (mDirs == null)
					mDirs = new File[0];

				mFiles = mDirectory.listFiles(new FileFilter() {

					public boolean accept(File file) {
						if (file.isDirectory())
							return false;
						String fname = file.getName().toLowerCase();
//						int pos = fname.lastIndexOf(".");
//						String justName = pos > 0 ? fname.substring(0, pos) : fname;
						if(fname.contains(""))
						
						return true;
						
//						if (fname.endsWith(".pdf"))
//							
//							return true;
//						
//						
//						if (fname.endsWith(".xps"))
//							return true;
//						if (fname.endsWith(".cbz"))
//							return true;
						return false;
					
					}
				});
				if (mFiles == null)
					mFiles = new File[0];

				Arrays.sort(mFiles, new Comparator<File>() {
					public int compare(File arg0, File arg1) {
						return arg0.getName().compareToIgnoreCase(arg1.getName());
					}
				});

				Arrays.sort(mDirs, new Comparator<File>() {
					public int compare(File arg0, File arg1) {
						return arg0.getName().compareToIgnoreCase(arg1.getName());
					}
				});

				adapter.clear();
				adapter.notifyDataSetChanged();
		if (mParent != null)
		//	adapter.add(new ChoosePDFItem(ChoosePDFItem.Type.PARENT, "OPEN OTHER EBOOK"));
				for (File f : mDirs)
					adapter.add(new ChoosePDFItem(ChoosePDFItem.Type.DIR, f.getName()));
				for (File f : mFiles)
					adapter.add(new ChoosePDFItem(ChoosePDFItem.Type.DOC, f.getName()));
				
				lastPosition();
			}
		};

		// Start initial file scan...
		mHandler.post(mUpdateFiles);

		// ...and observe the directory and scan files upon changes.
		FileObserver observer = new FileObserver(mDirectory.getPath(), FileObserver.CREATE | FileObserver.DELETE) {
			public void onEvent(int event, String path) {
				mHandler.post(mUpdateFiles);
			}
		};
		observer.startWatching();
	}

	private void lastPosition() {
		String p = mDirectory.getAbsolutePath();
		if (mPositions.containsKey(p))
			getListView().setSelection(mPositions.get(p));
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		mPositions.put(mDirectory.getAbsolutePath(), getListView().getFirstVisiblePosition());

		if (position < (mParent == null ? 0 : 1)) {
			//mDirectory = mParent;
			mHandler.post(mUpdateFiles);
			return;
		}

		position -= (mParent == null ? 0 : 1);

		if (position < mDirs.length) {
			mDirectory = mDirs[position];
			mHandler.post(mUpdateFiles);
			return;
		}

		position -= mDirs.length;

		Uri uri = Uri.parse(mFiles[position].getAbsolutePath());
		Intent intent = new Intent(this,MuPDFActivity.class);
		intent.setAction(Intent.ACTION_VIEW);
		intent.setData(uri);
		
		startActivity(intent);
	}

	@Override
	protected void onPause() {
		super.onPause();
		mPositions.put(mDirectory.getAbsolutePath(), getListView().getFirstVisiblePosition());
	}
	public void onBackPressed() {
		   Log.i("App", "Finishing & Exit");
		   finish();
		   Intent intent = new Intent(Intent.ACTION_MAIN);
		   intent.addCategory(Intent.CATEGORY_HOME);
		   intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		   startActivity(intent);
		 }
	  public void showAlertDialog(Context context, String title, String message, Boolean status) {
			final AlertDialog alertDialog = new AlertDialog.Builder(context).create();
			 
	        // Setting Dialog Title
	        alertDialog.setTitle(title);
	 
	        // Setting Dialog Message
	        alertDialog.setMessage(message);
	         
	        // Setting alert dialog icon
	        alertDialog.setIcon((status) ? R.drawable.success : R.drawable.fail);
	 
	        // Setting OK Button
	        alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int which) {
	            	alertDialog.dismiss();
	            	finish();
	            	
	            }
	        });
	        
	        alertDialog.setButton2("Cancel", new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int which) {
	            	alertDialog.dismiss();
	            	
	            	
	            }
	        });
	        // Showing Alert Message
	        alertDialog.show();
			
			// TODO Auto-generated method stub
			
		};
		 public void internet() {
			 cd = new ConnectionDetector(getApplicationContext());
			 Boolean isInternetPresent = false;
			 isInternetPresent = cd.isConnectingToInternet();
		
			 if (!isInternetPresent) {
				    showAlertDialog(ChoosePDFActivity.this, "No Internet Connection",
                           "You don't have internet connection.", false);
				   
               }
		 }
}
