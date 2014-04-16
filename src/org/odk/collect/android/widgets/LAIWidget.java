package org.odk.collect.android.widgets;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.application.Collect;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Widget that allows user to retrieve LAI values and add them to the form.
 * 
 * @author Marco Foi (foimarco@gmail.com)
 */
public class LAIWidget extends QuestionWidget implements IBinaryWidget {
	
	private Button mGetBarcodeButton;
	private TextView mStringAnswer;

	public LAIWidget(Context context, FormEntryPrompt prompt) {
		super(context, prompt);
		setOrientation(LinearLayout.VERTICAL);

		TableLayout.LayoutParams params = new TableLayout.LayoutParams();
		params.setMargins(7, 5, 7, 5);
		
		// set button formatting
		mGetBarcodeButton = new Button(getContext());
		mGetBarcodeButton.setId(QuestionWidget.newUniqueId());
		mGetBarcodeButton.setText(getContext().getString(R.string.get_lai));
		mGetBarcodeButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP,
				mAnswerFontsize);
		mGetBarcodeButton.setPadding(20, 20, 20, 20);
		mGetBarcodeButton.setEnabled(!prompt.isReadOnly());
		mGetBarcodeButton.setLayoutParams(params);
		
		// launch LAI retrival intent on click
		mGetBarcodeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Collect.getInstance()
						.getActivityLogger()
						.logInstanceAction(this, "recordLAI", "click",
								mPrompt.getIndex());
				Intent i = new Intent("eu.marcofoi.android.PocketLAI.ACTION_LAI");
				try {
					Collect.getInstance().getFormController()
							.setIndexWaitingForData(mPrompt.getIndex());
					((Activity) getContext()).startActivityForResult(i,
							FormEntryActivity.LAI_CAPTURE);
				} catch (ActivityNotFoundException e) {
					Toast.makeText(
							getContext(),
							getContext().getString(
									R.string.lai_application_error),
							Toast.LENGTH_SHORT).show();
					Collect.getInstance().getFormController()
							.setIndexWaitingForData(null);
				}
			}
		});
		
		// set text formatting
		mStringAnswer = new TextView(getContext());
		mStringAnswer.setId(QuestionWidget.newUniqueId());
		mStringAnswer.setTextSize(TypedValue.COMPLEX_UNIT_DIP, mAnswerFontsize);
		mStringAnswer.setGravity(Gravity.CENTER);

		String s = prompt.getAnswerText();
		if (s != null) {
			mGetBarcodeButton.setText(getContext().getString(
					R.string.replace_lai));
			mStringAnswer.setText(s);
		}
		// finish complex layout
		addView(mGetBarcodeButton);
		addView(mStringAnswer);
	}
	

	@Override
	public void clearAnswer() {
		mStringAnswer.setText(null);
		mGetBarcodeButton.setText(getContext().getString(R.string.get_lai));
	}
	
	@Override
	public IAnswerData getAnswer() {
		String s = mStringAnswer.getText().toString();
		if (s == null || s.equals("")) {
			return null;
		} else {
			return new StringData(s);
		}
	}

	/**
	 * Allows answer to be set externally in {@Link FormEntryActivity}.
	 */
	@Override
	public void setBinaryData(Object answer) {
		mStringAnswer.setText((String) answer);
		Collect.getInstance().getFormController().setIndexWaitingForData(null);
	}

	@Override
	public void setFocus(Context context) {
		// Hide the soft keyboard if it's showing.
		InputMethodManager inputManager = (InputMethodManager) context
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		inputManager.hideSoftInputFromWindow(this.getWindowToken(), 0);
	}

	@Override
	public boolean isWaitingForBinaryData() {
		return mPrompt.getIndex().equals(
				Collect.getInstance().getFormController()
						.getIndexWaitingForData());
	}

	@Override
	public void cancelWaitingForBinaryData() {
		Collect.getInstance().getFormController().setIndexWaitingForData(null);
	}

	@Override
	public void setOnLongClickListener(OnLongClickListener l) {
		mStringAnswer.setOnLongClickListener(l);
		mGetBarcodeButton.setOnLongClickListener(l);
	}

	@Override
	public void cancelLongPress() {
		super.cancelLongPress();
		mGetBarcodeButton.cancelLongPress();
		mStringAnswer.cancelLongPress();
	}

}
