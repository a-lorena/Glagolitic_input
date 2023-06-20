package com.example.writeglagolitic;

import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.inputmethodservice.InputMethodService;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputConnection;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.PopupWindow;

import com.example.writeglagolitic.ml.Model2;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.TransformToGrayscaleOp;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

public class Keyboard extends InputMethodService implements View.OnClickListener {

	WritingView writingView;
	InputConnection ic;
	View view;

	public static final String GLAGOLITIC_SCRIPT = "glagolitic";
	public static final String LATIN_SCRIPT = "latin";

	SharedPreferences sharedPreferences;
	public static final String SHARED_PREFS = "writeGlagoliticPrefs";
	public static final String SCRIPT_TYPE = GLAGOLITIC_SCRIPT;

	@Override
	public View onCreateInputView() {
		view = getLayoutInflater().inflate(R.layout.keyboard, null);

		Button backspaceButton = view.findViewById(R.id.backspaceButton);
		Button clearButton = view.findViewById(R.id.clearButton);
		Button scriptButton = view.findViewById(R.id.scriptButton);
		Button acceptButton = view.findViewById(R.id.acceptButton);
		Button spaceButton = view.findViewById(R.id.spaceButton);
		Button enterButton = view.findViewById(R.id.enterButton);
		writingView = view.findViewById(R.id.writingView);

		sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
		String script = sharedPreferences.getString(SCRIPT_TYPE, GLAGOLITIC_SCRIPT);

		if (script.equals(GLAGOLITIC_SCRIPT)) scriptButton.setText("Ⰰ");
		else scriptButton.setText("A");

		backspaceButton.setOnClickListener(this);
		clearButton.setOnClickListener(this);
		scriptButton.setOnClickListener(this);
		acceptButton.setOnClickListener(this);
		spaceButton.setOnClickListener(this);
		enterButton.setOnClickListener(this);

		return view;
	}

	@Override
	public void onClick(View view) {
		ic = getCurrentInputConnection();

		if (view instanceof Button) {
			String tag = view.getTag().toString();

			switch (tag) {
				case "backspace":
					ic.deleteSurroundingText(1, 0);
					break;

				case "clear":
					writingView.clear();
					break;

				case "script":
					setScript((Button) view);
					break;

				case "accept":
					Bitmap image = writingView.save();

					image = BitmapUtils.invertColors(image);

					/*
					ContentResolver resolver = getContentResolver();
					BitmapUtils.saveBitmap(image, resolver);
					*/

					predictLetter(image);
					writingView.clear();
					break;

				case "space":
					ic.commitText(" ", 1);
					break;

				case "enter":
					ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
					break;
			}
		}
	}

	private void setScript(Button button) {
		SharedPreferences.Editor editor = sharedPreferences.edit();
		String script = sharedPreferences.getString(SCRIPT_TYPE, GLAGOLITIC_SCRIPT);

		if (script.equals(GLAGOLITIC_SCRIPT)) {
			button.setText("A");
			editor.putString(SCRIPT_TYPE, LATIN_SCRIPT);
		} else {
			button.setText("Ⰰ");
			editor.putString(SCRIPT_TYPE, GLAGOLITIC_SCRIPT);
		}

		editor.apply();
	}

	private void predictLetter(Bitmap image) {
		try {
			Model2 model = Model2.newInstance(getApplicationContext());
			TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 100, 100, 1}, DataType.FLOAT32);

			ImageProcessor imageProcessor = new ImageProcessor.Builder().add(new TransformToGrayscaleOp()).build();
			TensorImage tensorImage = new TensorImage(DataType.FLOAT32);
			tensorImage.load(image);
			tensorImage = imageProcessor.process(tensorImage);

			ByteBuffer byteBuffer = tensorImage.getBuffer();
			inputFeature0.loadBuffer(byteBuffer);

			Model2.Outputs outputs = model.process(inputFeature0);
			TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

			float[] confidence = outputFeature0.getFloatArray();
			int classIndex = 0;
			float maxConfidence = 0;
			for (int i = 0; i < confidence.length; i++) {
				if (confidence[i] > maxConfidence) {
					maxConfidence = confidence[i];
					classIndex = i;
				}
			}

			String letter = getLetter(classIndex);
			String script = sharedPreferences.getString(SCRIPT_TYPE, GLAGOLITIC_SCRIPT);

			if (letter.equals("JAT") && script.equals(LATIN_SCRIPT)) {
				List<String> vowels = Arrays.asList("A", "E", "I", "O", "U", " ", "", "\n");
				String letterBefore = (String) ic.getTextBeforeCursor(1, 0);

				if (vowels.contains(letterBefore)) {
					ic.commitText("JA", 1);
				} else {
					showDialog();
				}
			} else {
				ic.commitText(letter, 1);
			}

			model.close();
		} catch (IOException e) {
			// TODO Handle the exception
		}
	}

	private String getLetter(int index) {
		String letter;

		String[] latinLetters =  {"A", "B", "C", "Č", "Ć", "D", "Đ", "E", "F", "G", "H", "I", "J",
									"JAT", "JU", "K", "L", "M", "N", "O", "P", "R", "S", "Š", "T",
									"U", "V", "Z", "Ž"};

		String[] glagoliticLetters = { "Ⰰ", "Ⰱ", "Ⱌ", "Ⱍ", "Ⱋ", "Ⰴ", "Ⰼ", "Ⰵ", "Ⱇ", "Ⰳ", "Ⱈ", "Ⰻ", "Ⰼ",
										"Ⱑ", "Ⱓ", "Ⰽ", "Ⰾ", "Ⰿ", "Ⱀ", "Ⱁ", "Ⱂ", "Ⱃ", "Ⱄ", "Ⱎ", "Ⱅ",
										"Ⱆ", "Ⰲ", "Ⰸ", "Ⰶ"};

		String script = sharedPreferences.getString(SCRIPT_TYPE, GLAGOLITIC_SCRIPT);

		if (script.equals(GLAGOLITIC_SCRIPT)) {
			letter = glagoliticLetters[index];
		} else {
			letter = latinLetters[index];
		}

		return letter;
	}

	private void showDialog() {
		View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_layout, new FrameLayout(this));

		PopupWindow popup = new PopupWindow(this);
		popup.setContentView(dialogView);
		popup.setBackgroundDrawable(new ColorDrawable());
		popup.showAtLocation(writingView, Gravity.TOP, 0, 20);

		Button ijeButton = dialogView.findViewById(R.id.ije_button);
		ijeButton.setOnClickListener(v -> {
			ic.commitText("IJE", 1);
			popup.dismiss();
		});

		Button jeButton = dialogView.findViewById(R.id.je_button);
		jeButton.setOnClickListener(v -> {
			ic.commitText("JE", 1);
			popup.dismiss();
		});
	}
}
