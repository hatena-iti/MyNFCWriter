package jp.co.iti.mynfcwriter;

import android.os.Bundle;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.view.Menu;
import android.widget.Toast;

public class MainActivity extends Activity {

	// Target URL
	private static final String TARGET_URL = "http://bit.ly/1cBnsuk";

	private NfcWriter nfcWriter = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		this.nfcWriter = new NfcWriter(this);
	}

	@Override
	protected void onResume() {
		super.onResume();

		PendingIntent pendingIntent = this.createPendingIntent();

		// Enable NFC adapter
		this.nfcWriter.enable(this, pendingIntent);
	}

	@Override
	protected void onPause() {
		super.onPause();

		// Disable NFC adapter
		this.nfcWriter.disable(this);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		if (this.nfcWriter.write(this, intent, TARGET_URL)) {
			Toast.makeText(this, "Completed.", Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(this, this.nfcWriter.getErrorMessage(), Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		this.nfcWriter = null;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

    private PendingIntent createPendingIntent() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
                		| Intent.FLAG_ACTIVITY_NEW_TASK);
        return PendingIntent.getActivity(this, 0, intent, 0);
    }
}
