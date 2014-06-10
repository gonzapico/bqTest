package com.gonzapico.bqtest;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

/***
 * Activity to show the content of the selected note in a web view to show it
 * well (content of Evernote notes are saven in HTML)
 * 
 * @author gonzapico
 * 
 */
public class NoteActivity extends Activity {

	WebView noteContent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_note);

		noteContent = (WebView) this.findViewById(R.id.notaContent);

		noteContent.loadData(getIntent().getExtras().getString("Content"),
				"text/html", "utf-8");

	}

}
