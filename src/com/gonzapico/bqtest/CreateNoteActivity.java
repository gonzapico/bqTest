package com.gonzapico.bqtest;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.evernote.client.android.EvernoteUtil;
import com.evernote.client.android.OnClientCallback;
import com.evernote.edam.type.Note;
import com.evernote.thrift.transport.TTransportException;
import com.gonzapico.bqtest.data.Data;

/***
 * Activity to create a new note and save it on Evernote. Every time the user
 * saves a new one, the app goes back to the activity of the list of the notes
 * and you can see what you have added on the list.
 * 
 * @author gonzapico
 * 
 */
public class CreateNoteActivity extends ParentActivity {

	private EditText mEditTextTitle;
	private EditText mEditTextContent;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_note);

		mEditTextTitle = (EditText) this.findViewById(R.id.title);
		mEditTextContent = (EditText) this.findViewById(R.id.content);
	}

	// Callback used as a result of creating a note in a normal notebook or a
	// linked notebook
	private OnClientCallback<Note> mNoteCreateCallback = new OnClientCallback<Note>() {
		@Override
		public void onSuccess(Note note) {
			Toast.makeText(getApplicationContext(), R.string.note_saved,
					Toast.LENGTH_LONG).show();

			Intent returnIntent = new Intent();
			returnIntent.putExtra("result", true);
			setResult(RESULT_OK, returnIntent);
			finish();
		}

		@Override
		public void onException(Exception exception) {
			Toast.makeText(getApplicationContext(), R.string.error_saving_note,
					Toast.LENGTH_LONG).show();
		}
	};

	/**
	 * Saves text field content as note to selected notebook, or default
	 * notebook if no notebook select
	 */
	public void saveNote(View view) {
		String title = mEditTextTitle.getText().toString();
		String content = mEditTextContent.getText().toString();
		if (TextUtils.isEmpty(title) || TextUtils.isEmpty(content)) {
			Toast.makeText(getApplicationContext(),
					R.string.empty_content_error, Toast.LENGTH_LONG).show();
			return;
		}

		Note note = new Note();
		note.setTitle(title);

		// TODO: line breaks need to be converted to render in ENML
		note.setContent(EvernoteUtil.NOTE_PREFIX + content
				+ EvernoteUtil.NOTE_SUFFIX);

		if (!Data.evernoteSession.getAuthenticationResult()
				.isAppLinkedNotebook()) {
			try {
				Data.evernoteSession.getClientFactory().createNoteStoreClient()
						.createNote(note, mNoteCreateCallback);
			} catch (TTransportException exception) {
				Toast.makeText(getApplicationContext(),
						R.string.error_creating_notestore, Toast.LENGTH_LONG)
						.show();
			}
		} else {
			super.createNoteInAppLinkedNotebook(note, mNoteCreateCallback);
		}

	}

}
