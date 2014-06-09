package com.gonzapico.bqtest;

import java.util.ArrayList;
import java.util.List;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.evernote.client.android.EvernoteSession;
import com.evernote.edam.error.EDAMNotFoundException;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;
import com.evernote.edam.notestore.NoteFilter;
import com.evernote.edam.notestore.NoteMetadata;
import com.evernote.edam.notestore.NoteStore;
import com.evernote.edam.notestore.NotesMetadataList;
import com.evernote.edam.notestore.NotesMetadataResultSpec;
import com.evernote.edam.type.Notebook;
import com.evernote.thrift.TException;
import com.evernote.thrift.protocol.TBinaryProtocol;
import com.evernote.thrift.transport.THttpClient;
import com.evernote.thrift.transport.TTransportException;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 * 
 * I have used the Login Activity that Android provide us and I did the little
 * changes that I needed
 */
public class LoginActivity extends ParentActivity {

	// UI references.
	private View mLoginFormView;
	private View mLoginStatusView;
	private TextView mLoginStatusMessageView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_login);

		mLoginFormView = findViewById(R.id.login_form);
		mLoginStatusView = findViewById(R.id.login_status);
		mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);

		findViewById(R.id.sign_in_button).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						attemptLogin();
					}
				});
	}

	/**
	 * Attempts to sign in on Evernote.
	 */
	public void attemptLogin() {

		// Show a progress spinner, and kick off a background task to
		// perform the user login attempt.
		mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
		showProgress(true);

		/*
		 * If the user isn't already logged in, then we authenticate him/her on
		 * Evernote. Otherwise, we go to the next activity to show his/her notes
		 */
		if (!mEvernoteSession.isLoggedIn())
			mEvernoteSession.authenticate(this);
		else
			goToNoteList();

	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			mLoginStatusView.setVisibility(View.VISIBLE);
			mLoginStatusView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginStatusView.setVisibility(show ? View.VISIBLE
									: View.GONE);
						}
					});

			mLoginFormView.setVisibility(View.VISIBLE);
			mLoginFormView.animate().setDuration(shortAnimTime)
					.alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginFormView.setVisibility(show ? View.GONE
									: View.VISIBLE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

	/**
	 * Called when the control returns from an activity that we launched.
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		// Update UI when oauth activity returns result
		case EvernoteSession.REQUEST_CODE_OAUTH:
			if (resultCode == Activity.RESULT_OK) {
				showProgress(false);

				// We launch a new activity to show a list of all the notes that
				// the user have saved in Evernote
				ListTask lTask = new ListTask();
				lTask.execute((Void) null);

			}
			break;
		}
	}

	/**
	 * Represents an asynchronous login/registration task used to authenticate
	 * the user.
	 */
	public class ListTask extends AsyncTask<Void, Void, ArrayList<Notebook>> {
		@Override
		protected ArrayList<Notebook> doInBackground(Void... params) {

			String authToken = mEvernoteSession.getAuthenticationResult()
					.getAuthToken();
			String noteStoreUrl = mEvernoteSession.getAuthenticationResult()
					.getNoteStoreUrl();

			String userAgent = "ALTEN" + " " + "bqTest" + "/" + "1.0";

			THttpClient noteStoreTrans = null;
			try {
				noteStoreTrans = new THttpClient(noteStoreUrl);
			} catch (TTransportException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// userStoreTrans.setCustomHeader("User-Agent", userAgent);
			TBinaryProtocol noteStoreProt = new TBinaryProtocol(noteStoreTrans);
			NoteStore.Client noteStore = new NoteStore.Client(noteStoreProt,
					noteStoreProt);
			List<Notebook> notebooks = new ArrayList<Notebook>();
			try {
				notebooks = noteStore.listNotebooks(authToken);
			} catch (EDAMUserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (EDAMSystemException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			int pageSize = 10;

			return (ArrayList<Notebook>) notebooks;
		}

		@Override
		protected void onPostExecute(final ArrayList<Notebook> notas) {
			String guid = "";
			for (Notebook notebook : notas) {

				Log.d("NOTAS", "Notebook: " + notebook.getName() + " y "
						+ notebook.getSharedNotebooksSize());
				guid = notebook.getGuid();

			}

			List2Task l2t = new List2Task();
			l2t.execute(guid);
		}

		@Override
		protected void onCancelled() {
			showProgress(false);
		}
	}

	public class List2Task extends AsyncTask<String, Void, NotesMetadataList> {
		@Override
		protected NotesMetadataList doInBackground(String... params) {

			String authToken = mEvernoteSession.getAuthenticationResult()
					.getAuthToken();
			String noteStoreUrl = mEvernoteSession.getAuthenticationResult()
					.getNoteStoreUrl();

			String userAgent = "ALTEN" + " " + "bqTest" + "/" + "1.0";

			THttpClient noteStoreTrans = null;
			try {
				noteStoreTrans = new THttpClient(noteStoreUrl);
			} catch (TTransportException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// userStoreTrans.setCustomHeader("User-Agent", userAgent);
			TBinaryProtocol noteStoreProt = new TBinaryProtocol(noteStoreTrans);
			NoteStore.Client noteStore = new NoteStore.Client(noteStoreProt,
					noteStoreProt);

			int pageSize = 10;

			NoteFilter filter = new NoteFilter();
			// filter.setOrder(NoteSortOrder.CREATED.getValue());
			// filter.setNotebookGuid(params[0]);

			NotesMetadataResultSpec spec = new NotesMetadataResultSpec();
			spec.setIncludeTitle(true);
			NotesMetadataList notes = new NotesMetadataList();

			try {
				/*
				 * notes = noteStore.findNotesMetadata(authToken, filter, 0,
				 * pageSize, spec);
				 */
				notes = noteStore.findNotesMetadata(authToken, filter, 0, 100,
						spec);
				int matchingNotes = notes.getTotalNotes();
				Log.d("notas", "numero notas --> " + matchingNotes);
				int notesThisPage = notes.getNotes().size();
				Log.d("notas", "numero notas --> " + notesThisPage);

			} catch (EDAMUserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (EDAMSystemException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (EDAMNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// findNotesByQuery("");
			for (NoteMetadata note : notes.getNotes()) {
				try {
					Log.d("Notas copon",
							note.getTitle()
									+ " & "
									+ noteStore
											.getNote(authToken, note.getGuid(),
													true, false, false, false)
											.getContent().toString());
				} catch (EDAMUserException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (EDAMSystemException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (EDAMNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (TException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

			return notes;
		}

		@Override
		protected void onPostExecute(final NotesMetadataList notes) {

			for (NoteMetadata note : notes.getNotes()) {
				Log.d("Notas copon",
						note.getTitle() + " & " + note.getNotebookGuid());

			}
		}

		@Override
		protected void onCancelled() {
			showProgress(false);
		}
	}

	/***
	 * Method to go the activity where we will show the list of the notes of the
	 * user on Evernote
	 */
	public void goToNoteList() {
		Intent intent = new Intent(this, NoteListActivity.class);
		startActivity(intent);
		showProgress(false);
		finish();
	}

}
