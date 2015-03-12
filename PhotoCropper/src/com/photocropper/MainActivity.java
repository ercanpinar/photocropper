
package com.photocropper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.edmodo.cropper.CropImageView;

public class MainActivity extends Activity {

	// Static final constants
	private static final int DEFAULT_ASPECT_RATIO_VALUES = 10;
	private static final int ROTATE_NINETY_DEGREES = 90;
	private static final String ASPECT_RATIO_X = "ASPECT_RATIO_X";
	private static final String ASPECT_RATIO_Y = "ASPECT_RATIO_Y";
	private static final int ON_TOUCH = 1;

	// Instance variables
	private int mAspectRatioX = DEFAULT_ASPECT_RATIO_VALUES;
	private int mAspectRatioY = DEFAULT_ASPECT_RATIO_VALUES;

	private final int REQUEST_CAMERA = 1;
	private final int SELECT_FILE = 2;
	
	Bitmap croppedImage;

	CropImageView cropImageView;
	// Saves the state upon rotating the screen/restarting the activity
	@Override
	protected void onSaveInstanceState(Bundle bundle) {
		super.onSaveInstanceState(bundle);
		bundle.putInt(ASPECT_RATIO_X, mAspectRatioX);
		bundle.putInt(ASPECT_RATIO_Y, mAspectRatioY);
	}

	// Restores the state upon rotating the screen/restarting the activity
	@Override
	protected void onRestoreInstanceState(Bundle bundle) {
		super.onRestoreInstanceState(bundle);
		mAspectRatioX = bundle.getInt(ASPECT_RATIO_X);
		mAspectRatioY = bundle.getInt(ASPECT_RATIO_Y);
	}

	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);

		// Sets fonts for all
		Typeface mFont = Typeface.createFromAsset(getAssets(), "Roboto-Thin.ttf");
		ViewGroup root = (ViewGroup) findViewById(R.id.mylayout);
		setFont(root, mFont);

		// Initialize components of the app


		cropImageView = (CropImageView) findViewById(R.id.CropImageView);
		
		final SeekBar aspectRatioXSeek = (SeekBar) findViewById(R.id.aspectRatioXSeek);
		final SeekBar aspectRatioYSeek = (SeekBar) findViewById(R.id.aspectRatioYSeek);
		final ToggleButton fixedAspectRatioToggle = (ToggleButton) findViewById(R.id.fixedAspectRatioToggle);
		Spinner showGuidelinesSpin = (Spinner) findViewById(R.id.showGuidelinesSpin);

		// Sets sliders to be disabled until fixedAspectRatio is set
		aspectRatioXSeek.setEnabled(false);
		aspectRatioYSeek.setEnabled(false);

		// Set initial spinner value
		showGuidelinesSpin.setSelection(ON_TOUCH);

		//Sets the rotate button
		final Button rotateButton = (Button) findViewById(R.id.Button_rotate);
		rotateButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				cropImageView.rotateImage(ROTATE_NINETY_DEGREES);
			}
		});

		// Sets fixedAspectRatio
		fixedAspectRatioToggle.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				cropImageView.setFixedAspectRatio(isChecked);
				if (isChecked) {
					aspectRatioXSeek.setEnabled(true);
					aspectRatioYSeek.setEnabled(true);
				}
				else {
					aspectRatioXSeek.setEnabled(false);
					aspectRatioYSeek.setEnabled(false);
				}
			}
		});

		// Sets initial aspect ratio to 10/10, for demonstration purposes
		cropImageView.setAspectRatio(DEFAULT_ASPECT_RATIO_VALUES, DEFAULT_ASPECT_RATIO_VALUES);

		// Sets aspectRatioX
		final TextView aspectRatioX = (TextView) findViewById(R.id.aspectRatioX);

		aspectRatioXSeek.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar aspectRatioXSeek, int progress, boolean fromUser) {
				try {
					mAspectRatioX = progress;
					cropImageView.setAspectRatio(progress, mAspectRatioY);
					aspectRatioX.setText(" " + progress);
				} catch (IllegalArgumentException e) {
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
		});

		// Sets aspectRatioY
		final TextView aspectRatioY = (TextView) findViewById(R.id.aspectRatioY);

		aspectRatioYSeek.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar aspectRatioYSeek, int progress, boolean fromUser) {
				try {
					mAspectRatioY = progress;
					cropImageView.setAspectRatio(mAspectRatioX, progress);
					aspectRatioY.setText(" " + progress);
				} catch (IllegalArgumentException e) {
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
		});


		// Sets up the Spinner
		showGuidelinesSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
				cropImageView.setGuidelines(i);
			}

			public void onNothingSelected(AdapterView<?> adapterView) {
				return;
			}
		});

		final Button cropButton = (Button) findViewById(R.id.Button_crop);
		cropButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				croppedImage = cropImageView.getCroppedImage();
				ImageView croppedImageView = (ImageView) findViewById(R.id.croppedImageView);
				croppedImageView.setImageBitmap(croppedImage);
				
			}
		});
		final Button selectBtn = (Button) findViewById(R.id.btn_select_photo);
		selectBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				selectImage();
			}
		});

	}

	/*
	 * Sets the font on all TextViews in the ViewGroup. Searches recursively for
	 * all inner ViewGroups as well. Just add a check for any other views you
	 * want to set as well (EditText, etc.)
	 */
	public void setFont(ViewGroup group, Typeface font) {
		int count = group.getChildCount();
		View v;
		for (int i = 0; i < count; i++) {
			v = group.getChildAt(i);
			if (v instanceof TextView || v instanceof EditText || v instanceof Button) {
				((TextView) v).setTypeface(font);
			} else if (v instanceof ViewGroup)
				setFont((ViewGroup) v, font);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	private void selectImage() {
		final CharSequence[] items = { "Take Photo", "Choose from Library",
		"Cancel" };

		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		builder.setTitle("Add Photo!");
		builder.setItems(items, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int item) {
				if (items[item].equals("Take Photo")) {
					Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					File f = new File(android.os.Environment
							.getExternalStorageDirectory(), "temp.jpg");
					intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
					startActivityForResult(intent, REQUEST_CAMERA);
				} else if (items[item].equals("Choose from Library")) {
					Intent intent = new Intent(
							Intent.ACTION_PICK,
							android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
					intent.setType("image/*");
					startActivityForResult(
							Intent.createChooser(intent, "Select File"),
							SELECT_FILE);
				} else if (items[item].equals("Cancel")) {
					dialog.dismiss();
				}
			}
		});
		builder.show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			if (requestCode == REQUEST_CAMERA) {
				File f = new File(Environment.getExternalStorageDirectory()
						.toString());
				for (File temp : f.listFiles()) {
					if (temp.getName().equals("temp.jpg")) {
						f = temp;
						break;
					}
				}
				try {
					Bitmap bm;
					BitmapFactory.Options btmapOptions = new BitmapFactory.Options();

					bm = BitmapFactory.decodeFile(f.getAbsolutePath(),
							btmapOptions);

					cropImageView.setImageBitmap(bm);

					String path = android.os.Environment
							.getExternalStorageDirectory()
							+ File.separator
							+ "Phoenix" + File.separator + "default";
					f.delete();
					OutputStream fOut = null;
					File file = new File(path, String.valueOf(System
							.currentTimeMillis()) + ".jpg");
					try {
						fOut = new FileOutputStream(file);
						bm.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
						fOut.flush();
						fOut.close();
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (requestCode == SELECT_FILE) {
				Uri selectedImageUri = data.getData();

				String tempPath = getPath(selectedImageUri, MainActivity.this);
				Bitmap bm;
				BitmapFactory.Options btmapOptions = new BitmapFactory.Options();
				bm = BitmapFactory.decodeFile(tempPath, btmapOptions);
				cropImageView.setImageBitmap(bm);
			}
		}
	}

	public String getPath(Uri uri, Activity activity) {
		String[] projection = { MediaColumns.DATA };
		Cursor cursor = activity
				.managedQuery(uri, projection, null, null, null);
		int column_index = cursor.getColumnIndexOrThrow(MediaColumns.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}
}
