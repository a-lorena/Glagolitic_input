package com.example.writeglagolitic;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;

import java.io.OutputStream;
import java.util.Objects;

public class BitmapUtils {
	public static Bitmap invertColors(Bitmap image) {
		int width = image.getWidth();
		int height = image.getHeight();

		Bitmap output = Bitmap.createBitmap(width, height, image.getConfig());

		int A,R,G,B;
		int pixel;

		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				pixel = image.getPixel(i,j);
				A = Color.alpha(pixel);
				R = Color.red(pixel);
				G = Color.green(pixel);
				B = Color.blue(pixel);

				int gray = (int) (0.2989 * R + 0.5870 * G + 0.1140 * B);

				if (gray > 130) gray = 0;
				else gray = 255;

				output.setPixel(i, j, Color.argb(A, gray, gray, gray));
			}
		}

		return output;
	}

	public static void saveBitmap(Bitmap image, ContentResolver resolver) {
		OutputStream fos;
		try {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
				ContentValues contentValues = new ContentValues();
				contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "drawn_letter.jpg");
				contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg");
				Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
				fos = resolver.openOutputStream(Objects.requireNonNull(imageUri));
				image.compress(Bitmap.CompressFormat.JPEG, 100, fos);
				Objects.requireNonNull(fos);
			}
		} catch (Exception e) {
			Log.d("error", e.toString());
		}
	}
}
