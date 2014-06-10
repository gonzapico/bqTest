/*
 * Copyright 2012 Evernote Corporation
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.gonzapico.bqtest;

import android.app.Activity;
import android.os.Bundle;

import com.evernote.client.android.EvernoteSession;

/**
 * This is the parent activity that all sample activites extend from. This
 * creates the Evernote Session in onCreate and stores the CONSUMER_KEY and
 * CONSUMER_SECRET
 */
public class ParentActivity extends Activity {

	// Your Evernote API key. See http://dev.evernote.com/documentation/cloud/
	// Please obfuscate your code to help keep these values secret.
	private static final String CONSUMER_KEY = "gonzapico-3148";
	private static final String CONSUMER_SECRET = "9fd956916e740c4e";

	// Initial development is done on Evernote's testing service, the sandbox.
	// Change to HOST_PRODUCTION to use the Evernote production service
	// once your code is complete, or HOST_CHINA to use the Yinxiang Biji
	// (Evernote China) production service.
	private static final EvernoteSession.EvernoteService EVERNOTE_SERVICE = EvernoteSession.EvernoteService.SANDBOX;

	// Set this to true if you want to allow linked notebooks for accounts that
	// can only access a single
	// notebook.
	private static final boolean SUPPORT_APP_LINKED_NOTEBOOKS = true;

	protected EvernoteSession mEvernoteSession;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Set up the Evernote Singleton Session
		mEvernoteSession = EvernoteSession
				.getInstance(this, CONSUMER_KEY, CONSUMER_SECRET,
						EVERNOTE_SERVICE, SUPPORT_APP_LINKED_NOTEBOOKS);
	}

}
