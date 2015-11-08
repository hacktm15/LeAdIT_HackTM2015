package abbyy.ocrsdk.android;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SyncStatusObserver;
import android.os.Bundle;
import android.view.Menu;
import android.widget.TextView;

public class ResultsActivity extends Activity {

	String outputPath;
	TextView tv;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		tv = new TextView(this);
		setContentView(tv);
		
		String imageUrl = "unknown";
		
		Bundle extras = getIntent().getExtras();
		if( extras != null) {
			imageUrl = extras.getString("IMAGE_PATH" );
			outputPath = extras.getString( "RESULT_PATH" );
		}
		
		// Starting recognition process
		new AsyncProcessTask(this).execute(imageUrl, outputPath);
	}

	public void updateResults(Boolean success) {
		if (!success)
			return;
		try {
			StringBuffer contents = new StringBuffer();

			FileInputStream fis = openFileInput(outputPath);
			try {
				Reader reader = new InputStreamReader(fis, "UTF-8");
				BufferedReader bufReader = new BufferedReader(reader);
				String text = null;
				while ((text = bufReader.readLine()) != null) {
					contents.append(text).append(System.getProperty("line.separator"));
				}
			} finally {
				fis.close();
			}

			displayMessage(contents.toString());
		} catch (Exception e) {
			displayMessage("Error: " + e.getMessage());
		}
	}
	
	public void displayMessage( String text )
	{
		tv.post( new MessagePoster( text ) );
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_results, menu);
		return true;
	}

	class MessagePoster implements Runnable {
		private final Pattern DATE_REGEX = Pattern.compile(".+(\\d{2}/\\d{2}/2\\d{3}|\\d{2}-\\d{2}-2\\d{3}).+");
		private final Pattern TOTAL_REGEX = Pattern.compile(".TOTAL\\s+(\\d+,\\d+|\\d+.?\\d+).");
		private String escapeMessage;

		public MessagePoster( String message )
		{
			_message = message;
		}

		@SuppressLint("NewApi")
		public void run() {
			if (_message.contains("Error:")) {
				escapeMessage = "S-a produs o eroare de conexiune!";
				System.out.println(escapeMessage);
				setTextViewContent(escapeMessage);
				return;
			}

			// we have a possible good result
			Matcher mDate = DATE_REGEX.matcher(_message);
			boolean bDate = mDate.find();
			Matcher mTotal = TOTAL_REGEX.matcher(_message);
			boolean bTotal = mTotal.find();

			if (_message.isEmpty() || !bDate || !bTotal) {
				escapeMessage = "Nu s-au putut extrage informațiile utile! Vă rugăm să reîncercați.";
				System.out.println(escapeMessage);
				setTextViewContent(escapeMessage);
				return;
			}

			setTextViewContent("În data de " + mDate.group(1) + " aveți un bon în valoare de " + mTotal.group(1) + ".");

		}

		private void setTextViewContent(String mess) {
			tv.append(mess + "\n" );
			setContentView( tv );
		}

		private final String _message;
	}
}
