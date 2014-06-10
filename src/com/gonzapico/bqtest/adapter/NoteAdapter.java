package com.gonzapico.bqtest.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.evernote.edam.type.Note;
import com.gonzapico.bqtest.R;

public class NoteAdapter extends ArrayAdapter<Note> {

	ArrayList<Note> values;
	Context ctx;

	public NoteAdapter(Context context, ArrayList<Note> values) {
		super(context, R.layout.note_row, values);
		this.ctx = context;
		this.values = values;
	}

	@Override
	public int getCount() {
		return this.values.size();
	}

	@Override
	public Note getItem(int position) {
		return this.values.get(position);
	}

	@Override
	public long getItemId(int position) {
		return this.values.indexOf(this.values.get(position));
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) ctx
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.note_row, parent, false);
		// UI
		TextView titulo = (TextView) rowView.findViewById(R.id.titulo);
		TextView creacion = (TextView) rowView.findViewById(R.id.creacion);
		TextView modificacion = (TextView) rowView.findViewById(R.id.modificacion);
		// Fill the values
		titulo.setText(values.get(position).getTitle());
		creacion.setText(String.valueOf(values.get(position).getCreated()));
		modificacion.setText(String.valueOf(values.get(position).getUpdated()));
		rowView.setTag(values.get(position).getGuid());

		return rowView;
	}

}
