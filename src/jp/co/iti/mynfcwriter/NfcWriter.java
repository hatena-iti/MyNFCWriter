/**
 * 
 */
package jp.co.iti.mynfcwriter;

import java.io.IOException;
import java.nio.charset.Charset;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.TagLostException;
import android.nfc.tech.Ndef;

/**
 * @author jumpei
 *
 */
public class NfcWriter {

	private NfcAdapter nfcAdapter = null;
	private String errorMessage = null;

	public NfcWriter(Context context) {
		// Get default NFC adapter
		this.nfcAdapter = NfcAdapter.getDefaultAdapter(context);
	}

	public void enable(Activity activity, PendingIntent pendingIntent) {
		this.nfcAdapter.enableForegroundDispatch(activity, pendingIntent, null, null);
	}

	public void disable(Activity activity) {
		this.nfcAdapter.disableForegroundDispatch(activity);
	}

	public boolean write(Context context, Intent intent, String targetUrl) {
		boolean result = false;

		this.errorMessage = null;

		Tag tag = (Tag)intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		if (tag == null) {
			this.errorMessage = "Failed: Tag is null.";
			return result;
		}

		Ndef ndef = Ndef.get(tag);
		if (ndef == null) {
			this.errorMessage = "Failed: Wrong tag format.";
			return result;
		}

		NdefMessage ndefMessage = this.createMessage(targetUrl);
		if (ndefMessage == null) {
			this.errorMessage = "Failed: NdefMessage is null.";
			return result;
		}

		if (this.writeMessage(ndef, ndefMessage)) {
			result = true;
		}

		return result;
	}

	public String getErrorMessage() {
		return this.errorMessage;
	}


    private NdefMessage createMessage(String targetUrl) {
        NdefRecord[] rs = new NdefRecord[] {
        	createUriRecord(targetUrl),
            createActionRecord() 
        };
        NdefMessage spPayload = new NdefMessage(rs);

        NdefRecord spRecord = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,
        									NdefRecord.RTD_SMART_POSTER,
        									new byte[0],
        									spPayload.toByteArray());
        return new NdefMessage(new NdefRecord[]{spRecord});
    }

    private NdefRecord createUriRecord(String url) {
        return NdefRecord.createUri(url);
    }

    private NdefRecord createActionRecord() {
        byte[] typeField = "act".getBytes(Charset.forName("US-ASCII"));
        byte[] payload = {(byte) 0x00};
        return new NdefRecord(NdefRecord.TNF_WELL_KNOWN, 
                              typeField,
                              new byte[0],
                              payload); // y2z
    }

    private boolean writeMessage(Ndef ndef, NdefMessage ndefMessage) {
    	boolean result = false;
   
        if (!ndef.isWritable()) {
        	this.errorMessage = "Failed: Readonly.";
            return result;
        }

        int messageSize = ndefMessage.toByteArray().length;
        if (messageSize > ndef.getMaxSize()) {
        	this.errorMessage = "Failed: Overflow.";
            return false;

        }

        try {
            if (!ndef.isConnected()) {
            	ndef.connect();
            }
            ndef.writeNdefMessage(ndefMessage);
            result = true;

        } catch (TagLostException e) {
        	this.errorMessage = "Failed: " + e.getLocalizedMessage();

        } catch (IOException e) {
        	this.errorMessage = "Failed: " + e.getLocalizedMessage();

        } catch (FormatException e) {
        	this.errorMessage = "Failed: " + e.getLocalizedMessage();

        } finally {
            try {
                ndef.close();
            } catch (IOException e) {
                // ignore.
            }
        }

        return result;
    }
}
