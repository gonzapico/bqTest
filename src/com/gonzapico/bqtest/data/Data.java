package com.gonzapico.bqtest.data;

import android.content.Context;
import android.os.Handler;

import com.evernote.client.android.EvernoteSession;

/***
 * Class where we are saving all the data needed through the lifecycle of the
 * app
 * 
 * @author gonzapico
 * 
 */
public class Data {

	public static Handler handlerNotas;
	public static EvernoteSession evernoteSession;
	public static Context ctx;
}
