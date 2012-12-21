/*
 * Audio microphone thread.
 */

package org.renpy.android;

import java.lang.Thread;
import android.os.Process;
import android.media.MediaRecorder;
import android.media.MediaRecorder.AudioSource;
import android.media.MediaRecorder.OutputFormat;
import android.media.MediaRecorder.AudioEncoder;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.util.Log;
import java.nio.ByteBuffer;

class AudioIn extends Thread {
	private static String TAG = "AudioIn";
	private boolean stopped = false;
	static AudioIn instance = null;

	public AudioIn() {}

	@Override
	public void run() {
		AudioRecord recorder = null;
		byte[] buffer = null;
		int rate = 22050;
		int channel = AudioFormat.CHANNEL_IN_MONO;
		int fmt = AudioFormat.ENCODING_PCM_16BIT;
		int N = 0;

		Log.d(TAG, "Starting audio recording thread");

		Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);

		try {
			// ... initialize
			int bufsize = AudioRecord.getMinBufferSize(rate, channel, fmt);
			Log.d(TAG, String.format("Minimal bufsize is %d bytes", bufsize));

			buffer = new byte[bufsize];
			recorder = new AudioRecord(AudioSource.MIC,
					rate, channel, fmt, bufsize);

			Log.d(TAG, "Recording started");
			recorder.startRecording();

			// ... loop

			while (!stopped) {
				N = recorder.read(buffer, 0, buffer.length);
				nativeAudioCallback(buffer, N);
			}

		} catch(Throwable x) {
			Log.w(TAG, "Error reading voice audio", x);
		} finally {
			if ( recorder != null )
				recorder.stop();
			recorder = null;
		}
	}

	public void close() {
		stopped = true;
	}

	static public void start_recording() {
		instance = new AudioIn();
		instance.start();
	}

	static public void stop_recording() {
		if ( instance != null ) {
			instance.close();
			instance = null;
		}
	}

	public native void nativeAudioCallback(byte[] buffer, int bufsize);

}
