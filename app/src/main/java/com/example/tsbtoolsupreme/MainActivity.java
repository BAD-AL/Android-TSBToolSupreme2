package com.example.tsbtoolsupreme;
// keyPsswd = androidKey2

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.ClipboardManager;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	public static ITecmoTool CurrentTool = null;
	public static InputParser CurrentInputParser = null;
	public static ClipboardManager ClipboardManager;
	
	private static final int LAUNCH_EDIT_SELECTOR = 2;
	private static final int FILE_SELECT_CODE = 6;
	
	private EditText inputFileNameTextBox;
	private Button saveRomButton;
	private Button browseButton;
	private TextView versionTextView;
	
	private String defaultText ="";
	private Uri romUri = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		saveRomButton = (Button)findViewById(R.id.mSaveRomButton);
		browseButton = (Button)findViewById(R.id.mBrowseButton);
		
		saveRomButton.setEnabled(false);
		inputFileNameTextBox = (EditText)findViewById(R.id.mInputFileNameTextBox);
		inputFileNameTextBox.setText(defaultText);
		ClipboardManager = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
		versionTextView = (TextView)findViewById(R.id.textViewVersion);

		try{
			Context context = getApplicationContext();
			String versionName =  context.getPackageManager()
					.getPackageInfo(context.getPackageName(), 0).versionName;
			versionTextView.setText("Version: "+ versionName);
		}
		catch(Exception e){
			versionTextView.setText(e.getMessage());
		}

		saveRomButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				saveButtonClick();
			}
		});
		
		browseButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showFileChooser();
			}
		});
		Button visitForumButton = (Button)findViewById(R.id.mVisitForumsButton);
		visitForumButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String url = "http://tecmobowl.org/forum/94-editorsemulation/";
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(url));
				startActivity(i);
			}
		});
		
		Button getFileBrowserButton = (Button)findViewById(R.id.mGetFileBrowerButton);
		getFileBrowserButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getFileBrowserFromMarket();
			}
		});
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) 
	{
		if ( requestCode == FILE_SELECT_CODE )
		{
			if (resultCode == RESULT_OK) {
				// Get the Uri of the selected file 
				Uri uri = data.getData();
				String path = uri.getPath().toLowerCase();
				if (path.endsWith(".nes") || path.endsWith(".smc")) {
					romUri = uri;
					// get last part of filename and put it in the text box.
					int index = -1;
					for(index= path.length()-4; index > 0; index--)
						if(path.charAt(index) == '/')
							break;
					path = uri.getPath().substring(index+1);
					inputFileNameTextBox.setText(path);
					try {
						this.loadRom(romUri);
					}catch (Exception exc){
						Toast.makeText(this,
								"Error loading ROM file. \n"+exc.toString(), Toast.LENGTH_LONG);
					}
				}
				else {
					Toast.makeText(this,  "You must select a .nes or .smc file.",
							Toast.LENGTH_LONG).show();
				}
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void saveButtonClick()
	{
		if( CurrentTool != null ) {
			String path = inputFileNameTextBox.getText().toString();
			String message = "File "+ path + " saved";
			try{
				OutputStream stream = getContentResolver().openOutputStream(romUri);
				CurrentTool.SaveRom(stream);
			}
			catch (Exception e ) {
				message = "ERROR! "+ e.getMessage();
			}
			Toast.makeText(this,  message, Toast.LENGTH_LONG).show();
		}
	}
	
	private void loadRom(Uri fileUri) throws FileNotFoundException, IOException
	{
		String error = "";
		InputStream stream = getContentResolver().openInputStream(fileUri);

		CurrentTool = TecmoToolFactory.GetToolForRom(stream, fileUri.getPath());
		Vector<String> errors =  CurrentTool.getErrors();
		if( errors.size() > 0 ) {
			CurrentTool = null;
			StringBuilder builder = new StringBuilder();
			for(int i =0; i < errors.size(); i++) {
				builder.append(errors.get(i));
				builder.append("\n");
			}
			error = builder.toString();
		}
		else {
			CurrentInputParser = new InputParser(CurrentTool);
		}
		if( CurrentTool != null ) {
			saveRomButton.setEnabled(true);
			Intent intent = new Intent(MainActivity.this, EditSelector.class); // launch screen2
			//intent.putExtra(name, value);
			startActivityForResult(intent, LAUNCH_EDIT_SELECTOR);
		}
		else {
			error = "ERROR! ROM not supported. Go to tecmobowl.org for awesome ROMS";
		}

		if( error.length() > 0 )
			Toast.makeText(this,  error, Toast.LENGTH_LONG).show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public static int GetIndex(String[] array, String val) {
		int ret = -1;

		for (int i = 0; i < array.length; i++) {
			if (val.equals(array[i])) {
				ret = i;
				break;
			}
		}
		return ret;
	}

	private void showFileChooser() {
	    Intent intent = new Intent();
		if( Build.VERSION.SDK_INT < 19)
			intent.setAction((Intent.ACTION_GET_CONTENT));
		else
			intent.setAction((Intent.ACTION_OPEN_DOCUMENT));

	    intent.setType("*/*");
	    intent.addCategory(Intent.CATEGORY_OPENABLE);

	    try {
	    	startActivityForResult(
	                Intent.createChooser(intent, "Use a file Browser to locate a TSB NES ROM to open"),
	                FILE_SELECT_CODE);
	    } catch (android.content.ActivityNotFoundException ex) 
	    {
	        // Potentially direct the user to the Market with a Dialog
	        Toast.makeText(this, "Please install a File Browser.", 
	                Toast.LENGTH_SHORT).show();
	        
	        getFileBrowserFromMarket();
	    }
	}
	
	private void getFileBrowserFromMarket()
	{
        Intent goToMarket = new Intent(Intent.ACTION_VIEW)
        	//.setData(Uri.parse("market://details?id=com.estrongs.android.pop"));
                .setData(Uri.parse("https://play.google.com/store/apps/details?id=es.fileexplorer.filebrowser.ezfilemanager"));
        startActivity(goToMarket);
	}

}
