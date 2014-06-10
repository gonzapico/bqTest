package com.gonzapico.bqtest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;

import com.evernote.client.android.InvalidAuthenticationException;
import com.evernote.edam.error.EDAMNotFoundException;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;
import com.evernote.edam.notestore.NoteFilter;
import com.evernote.edam.notestore.NoteMetadata;
import com.evernote.edam.notestore.NoteStore;
import com.evernote.edam.notestore.NotesMetadataList;
import com.evernote.edam.notestore.NotesMetadataResultSpec;
import com.evernote.edam.type.Note;
import com.evernote.thrift.TException;
import com.evernote.thrift.protocol.TBinaryProtocol;
import com.evernote.thrift.transport.THttpClient;
import com.evernote.thrift.transport.TTransportException;
import com.gonzapico.bqtest.adapter.NoteAdapter;
import com.gonzapico.bqtest.data.Data;

/***
 * Class to show an ordered list of the notes of the user on Evernote depends on
 * the choice of the spinner.
 * 
 * @author gonzapico
 * 
 */
public class NoteListActivity extends Activity{

	public static final int LIST_LOADED = 0;
	public static final int ORDER_BY_NAME = 1;
	public static final int ORDER_BY_DATE = 2;

	// UI
	LinearLayout loading;
	ListView notesList;

	// Notes arrayList
	ArrayList<Note> listOfNotes;

	ArrayAdapter<Note> notesAdapter;

	public boolean isFirstLoaded = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_note_list);

		listOfNotes = new ArrayList<Note>();

		loading = (LinearLayout) this.findViewById(R.id.status);
		notesList = (ListView) this.findViewById(R.id.lisfOfNotes);

		isFirstLoaded = false;

		// Populating the spinner
		Spinner spinnerOrder = (Spinner) this.findViewById(R.id.spinnerOrder);
		// Create an ArrayAdapter using the string array and a default spinner
		// layout
		ArrayAdapter<CharSequence> adapter = ArrayAdapter
				.createFromResource(this, R.array.order_array,
						android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		spinnerOrder.setAdapter(adapter);

		spinnerOrder.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				if (isFirstLoaded)
					if (arg2 == 0) {
						showProgress(true);
						showList(false);
						Collections.sort(listOfNotes, new Comparator<Note>() {

							@Override
							public int compare(Note lhs, Note rhs) {

								return lhs
										.getTitle()
										.toLowerCase()
										.compareTo(rhs.getTitle().toLowerCase());
							}

						});

						Message msg = new Message();
						msg.arg1 = ORDER_BY_NAME;
						Data.handlerNotas.sendMessage(msg);

					} else if (arg2 == 1) {
						showProgress(true);
						showList(false);
						Collections.sort(listOfNotes, new Comparator<Note>() {

							@Override
							public int compare(Note lhs, Note rhs) {
								long timeToCompareL = 0;
								long timeToCompareR = 0;
								if (lhs.getCreated() == lhs.getUpdated()) {
									timeToCompareL = lhs.getCreated();
								} else if (lhs.getUpdated() > lhs.getCreated()) {
									timeToCompareL = lhs.getUpdated();
								}
								if (rhs.getCreated() == rhs.getUpdated()) {
									timeToCompareR = rhs.getCreated();
								} else if (rhs.getUpdated() > rhs.getCreated()) {
									timeToCompareR = rhs.getUpdated();
								}
								return (timeToCompareL > timeToCompareR ? 1
										: (timeToCompareL == timeToCompareR ? 0
												: -1));
							}

						});
					}

			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {

			}
		});

		/*
		 * Defining the handler that will receive the message when the data is
		 * loaded
		 */
		Data.handlerNotas = new Handler() {
			@Override
			public void handleMessage(Message inputMessage) {
				switch (inputMessage.arg1) {
				case LIST_LOADED:
					notesAdapter = new NoteAdapter(getApplicationContext(),
							listOfNotes);
					notesList.setAdapter(notesAdapter);
					notesList
							.setOnItemClickListener(new AdapterView.OnItemClickListener() {

								@Override
								public void onItemClick(AdapterView<?> parent,
										final View view, int position, long id) {

									// Showing the content
									Intent i = new Intent(
											getApplicationContext(),
											NoteActivity.class);
									i.putExtra("Content",
											listOfNotes.get(position)
													.getContent().toString());
									startActivity(i);
								}

							});
					showProgress(false);
					showList(true);
					isFirstLoaded = true;
					break;
				case ORDER_BY_NAME:
					notesAdapter.notifyDataSetChanged();
					showProgress(false);
					showList(true);
					break;
				case ORDER_BY_DATE:
					notesAdapter.notifyDataSetChanged();
					showProgress(false);
					showList(true);
					break;

				default:
					break;
				}
			}
		};

		GetNotesTask g2t = new GetNotesTask();
		g2t.execute();
	}

	/**
	 * AsyncTask to get the notes of the user who has logged in on the app
	 * @author gonzapico
	 *
	 */
	public class GetNotesTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {

			listOfNotes.clear();

			String authToken = Data.evernoteSession.getAuthenticationResult()
					.getAuthToken();
			String noteStoreUrl = Data.evernoteSession
					.getAuthenticationResult().getNoteStoreUrl();


			THttpClient noteStoreTrans = null;
			try {
				noteStoreTrans = new THttpClient(noteStoreUrl);
			} catch (TTransportException e) {
				e.printStackTrace();
			}
			TBinaryProtocol noteStoreProt = new TBinaryProtocol(noteStoreTrans);
			NoteStore.Client noteStore = new NoteStore.Client(noteStoreProt,
					noteStoreProt);


			NoteFilter filter = new NoteFilter();

			NotesMetadataResultSpec spec = new NotesMetadataResultSpec();
			spec.setIncludeTitle(true);
			NotesMetadataList notes = new NotesMetadataList();

			try {
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

			for (NoteMetadata note : notes.getNotes()) {
				try {
					if (!noteStore
							.getNote(authToken, note.getGuid(), true, false,
									false, false).getContent().toString()
							.equals(null))
						listOfNotes.add(noteStore.getNote(authToken,
								note.getGuid(), true, false, false, false));

				} catch (EDAMUserException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (EDAMSystemException e) {
					// Going back to the login to authenticate again
					Intent i = new Intent(getApplicationContext(), LoginActivity.class);
					startActivity(i);
					finish();
					e.printStackTrace();
				} catch (EDAMNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (TException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			return null;

		}

		@Override
		protected void onPostExecute(Void arg) {

			Message msg = new Message();
			msg.arg1 = LIST_LOADED;
			Data.handlerNotas.sendMessage(msg);

		}

		@Override
		protected void onCancelled() {
			// showProgress(false);

		}

		@Override
		protected void onPreExecute() {
			showProgress(true);
			showList(false);
		}
	}

	/**
	 * Method to show the progress loading or hide it
	 * 
	 * @param show
	 */
	public void showProgress(boolean show) {
		if (show)
			loading.setVisibility(View.VISIBLE);
		else
			loading.setVisibility(View.INVISIBLE);

	}

	/**
	 * Method to show or hide the list of the notes
	 * 
	 * @param show
	 */
	public void showList(boolean show) {
		if (show)
			notesList.setVisibility(View.VISIBLE);
		else
			notesList.setVisibility(View.INVISIBLE);
	}

	public void newNote(View v) {
		Intent i = new Intent(getApplicationContext(), CreateNoteActivity.class);
		startActivityForResult(i, 1);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == 1) {
			if (resultCode == RESULT_OK) {
				boolean result = data.getBooleanExtra("result", true);
				if (result) {
					GetNotesTask g2t = new GetNotesTask();
					g2t.execute();
				}

			}
			if (resultCode == RESULT_CANCELED) {
				// Write your code if there's no result
			}
		}
	}// onActivityResult

	@Override 
	public void onBackPressed() {
		// Every time you go back from here, you're logging out
		try {
			Data.evernoteSession.logOut(getApplicationContext());
		} catch (InvalidAuthenticationException e) {
			e.printStackTrace();
		}
		// Otherwise defer to system default behavior.
		super.onBackPressed();
	}

}
