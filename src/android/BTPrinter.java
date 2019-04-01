package com.idex.bluetooth;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.Set;
import java.util.UUID;
import java.util.Date;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Bitmap.Config;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.util.Base64;
import android.util.Xml.Encoding;
import java.io.UnsupportedEncodingException;
import java.lang.Math;

public class BTPrinter extends CordovaPlugin {
	private static final String LOG_TAG = "BTPrinter";
	BluetoothAdapter mBluetoothAdapter;
	BluetoothSocket mmSocket;
	BluetoothDevice mmDevice;
	//OutputStream mmOutputStream;
	//InputStream mmInputStream;

  //DataInputStream is = null;
  //   DataOutputStream os = null;
	DataInputStream mmInputStream;
	DataOutputStream mmOutputStream;

	Thread workerThread;
	Thread writerThread;
	byte[] readBuffer;
	int readBufferPosition;
	int counter;
	volatile boolean stopWorker;
	volatile boolean stopWriter;

	Bitmap bitmap;

	private static final int CHUNK_SIZE = 200;

	private static int[][] Floyd16x16 = new int[][]{{0, 128, 32, 160, 8, 136, 40, 168, 2, 130, 34, 162, 10, 138, 42, 170}, {192, 64, 224, 96, 200, 72, 232, 104, 194, 66, 226, 98, 202, 74, 234, 106}, {48, 176, 16, 144, 56, 184, 24, 152, 50, 178, 18, 146, 58, 186, 26, 154}, {240, 112, 208, 80, 248, 120, 216, 88, 242, 114, 210, 82, 250, 122, 218, 90}, {12, 140, 44, 172, 4, 132, 36, 164, 14, 142, 46, 174, 6, 134, 38, 166}, {204, 76, 236, 108, 196, 68, 228, 100, 206, 78, 238, 110, 198, 70, 230, 102}, {60, 188, 28, 156, 52, 180, 20, 148, 62, 190, 30, 158, 54, 182, 22, 150}, {252, 124, 220, 92, 244, 116, 212, 84, 254, 126, 222, 94, 246, 118, 214, 86}, {3, 131, 35, 163, 11, 139, 43, 171, 1, 129, 33, 161, 9, 137, 41, 169}, {195, 67, 227, 99, 203, 75, 235, 107, 193, 65, 225, 97, 201, 73, 233, 105}, {51, 179, 19, 147, 59, 187, 27, 155, 49, 177, 17, 145, 57, 185, 25, 153}, {243, 115, 211, 83, 251, 123, 219, 91, 241, 113, 209, 81, 249, 121, 217, 89}, {15, 143, 47, 175, 7, 135, 39, 167, 13, 141, 45, 173, 5, 133, 37, 165}, {207, 79, 239, 111, 199, 71, 231, 103, 205, 77, 237, 109, 197, 69, 229, 101}, {63, 191, 31, 159, 55, 183, 23, 151, 61, 189, 29, 157, 53, 181, 21, 149}, {254, 127, 223, 95, 247, 119, 215, 87, 253, 125, 221, 93, 245, 117, 213, 85}};
	private static int[][] Floyd8x8 = new int[][]{{0, 32, 8, 40, 2, 34, 10, 42}, {48, 16, 56, 24, 50, 18, 58, 26}, {12, 44, 4, 36, 14, 46, 6, 38}, {60, 28, 52, 20, 62, 30, 54, 22}, {3, 35, 11, 43, 1, 33, 9, 41}, {51, 19, 59, 27, 49, 17, 57, 25}, {15, 47, 7, 39, 13, 45, 5, 37}, {63, 31, 55, 23, 61, 29, 53, 21}};
	private static int[][] Floyd4x4 = new int[][]{{0, 8, 2, 10}, {12, 4, 14, 6}, {3, 11, 1, 9}, {15, 7, 13, 5}};

	public static byte[] GS_P_x_y = new byte[]{29, 80, 0, 0};
	public static byte[] GS_V_m = new byte[]{29, 86, 0};
	public static byte[] GS_V_m_n = new byte[]{29, 86, 66, 0};
	public static byte[] GS_W_nL_nH = new byte[]{29, 87, 118, 2};
	public static byte[] GS_exclamationmark_n = new byte[]{29, 33, 0};
	public static byte[] GS_E_n = new byte[]{27, 69, 0};
	public static byte[] GS_B_n = new byte[]{29, 66, 0};
	public static byte[] GS_backslash_m = new byte[]{29, 47, 0};
	public static byte[] GS_H_n = new byte[]{29, 72, 0};
	public static byte[] GS_f_n = new byte[]{29, 102, 0};
	public static byte[] GS_h_n = new byte[]{29, 104, -94};
	public static byte[] GS_w_n = new byte[]{29, 119, 3};
	public static byte[] GS_k_m_n_ = new byte[]{29, 107, 65, 12};
	public static byte[] GS_k_m_v_r_nL_nH = new byte[]{29, 107, 97, 0, 2, 0, 0};
	public static byte[] GS_dollors_nL_nH = new byte[]{29, 36, 0, 0};
	public static byte[] GS_backslash_nL_nH = new byte[]{29, 92, 0, 0};
	public static byte[] GS_leftbracket_k_pL_pH_cn_67_n = new byte[]{29, 40, 107, 3, 0, 49, 67, 3};
	public static byte[] GS_leftbracket_k_pL_pH_cn_69_n = new byte[]{29, 40, 107, 3, 0, 49, 69, 48};
	public static byte[] GS_leftbracket_k_pL_pH_cn_80_m__d1dk = new byte[]{29, 40, 107, 3, 0, 49, 80, 48};
	public static byte[] GS_leftbracket_k_pL_pH_cn_fn_m = new byte[]{29, 40, 107, 3, 0, 49, 81, 48};

	public BTPrinter() {}

	@Override
	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
		if (action.equals("list")) {
			listBT(callbackContext);
			return true;
		} else if (action.equals("connect")) {
			String name = args.getString(0);

			if (findBT(callbackContext, name)) {
				try {
					connectBT(callbackContext);
				} catch (IOException e) {
					Log.e(LOG_TAG, e.getMessage());
					e.printStackTrace();
				}
			} else {
				callbackContext.error("Bluetooth Device Not Found: " + name);
			}
			return true;
		} else if (action.equals("disconnect")) {
			try {
				disconnectBT(callbackContext);
			} catch (IOException e) {
				Log.e(LOG_TAG, e.getMessage());
				e.printStackTrace();
			}
			return true;
		}
		else if (action.equals("barcode")) {
			try {
				String msg = args.getString(0);
				barcode(callbackContext, msg);
			} catch (IOException e) {
				Log.e(LOG_TAG, e.getMessage());
				e.printStackTrace();
			}
			return true;
		}
		else if (action.equals("print") || action.equals("printImage")) {
			try {
				Log.d("IMAGE", args.toString(0));
				String msg = args.getString(0);
				int inch = Integer.parseInt(args.getString(1));

				if(inch != 3) {
					inch = 2;
				}

				Log.d("IMAGE", "INCH " + inch);
				printImage(callbackContext, msg, inch);
			} catch (IOException e) {
				Log.e(LOG_TAG, e.getMessage());
				e.printStackTrace();
			}
			return true;
		}
		else if (action.equals("printText")) {
			try {
				String msg = args.getString(0);
				printText(callbackContext, msg);
			} catch (IOException e) {
				Log.e(LOG_TAG, e.getMessage());
				e.printStackTrace();
			}
			return true;
		}
		else if (action.equals("printPOSCommand")) {
			try {
				String msg = args.getString(0);
				printPOSCommand(callbackContext, hexStringToBytes(msg));
			} catch (IOException e) {
				Log.e(LOG_TAG, e.getMessage());
				e.printStackTrace();
			}
			return true;
		}
		return false;
	}
    //This will return the array list of paired bluetooth printers
	void listBT(CallbackContext callbackContext) {
		BluetoothAdapter mBluetoothAdapter = null;
		String errMsg = null;
		try {
			mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			if (mBluetoothAdapter == null) {
				errMsg = "No bluetooth adapter available";
				Log.e(LOG_TAG, errMsg);
				callbackContext.error(errMsg);
				return;
			}
			if (!mBluetoothAdapter.isEnabled()) {
				Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				this.cordova.getActivity().startActivityForResult(enableBluetooth, 0);
			}
			Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
			if (pairedDevices.size() > 0) {
				JSONArray json = new JSONArray();
				for (BluetoothDevice device : pairedDevices) {
					/*
					Hashtable map = new Hashtable();
					map.put("type", device.getType());
					map.put("address", device.getAddress());
					map.put("name", device.getName());
					JSONObject jObj = new JSONObject(map);
					*/
					json.put(device.getAddress());
				}
				callbackContext.success(json);
			} else {
				callbackContext.error("No Bluetooth Device Found");
			}
			//Log.d(LOG_TAG, "Bluetooth Device Found: " + mmDevice.getAddress());
		} catch (Exception e) {
			errMsg = e.getMessage();
			Log.e(LOG_TAG, errMsg);
			e.printStackTrace();
			callbackContext.error(errMsg);
		}
	}

	// This will find a bluetooth printer device
	boolean findBT(CallbackContext callbackContext, String name) {
		try {
			mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			if (mBluetoothAdapter == null) {
				Log.e(LOG_TAG, "No bluetooth adapter available");
			}
			if (!mBluetoothAdapter.isEnabled()) {
				Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				this.cordova.getActivity().startActivityForResult(enableBluetooth, 0);
			}
			Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
			if (pairedDevices.size() > 0) {
				for (BluetoothDevice device : pairedDevices) {
					if (device.getAddress().equalsIgnoreCase(name)) {
						mmDevice = device;
						return true;
					}
				}
			}
			Log.d(LOG_TAG, "Bluetooth Device Found: " + mmDevice.getAddress());
		} catch (Exception e) {
			String errMsg = e.getMessage();
			Log.e(LOG_TAG, errMsg);
			e.printStackTrace();
			callbackContext.error(errMsg);
		}
		return false;
	}

	// Tries to open a connection to the bluetooth printer device
	boolean connectBT(CallbackContext callbackContext) throws IOException {
		try {
			// Standard SerialPortService ID
			UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
			mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
			mmSocket.connect();
			//mmOutputStream = mmSocket.getOutputStream();
			//mmOutputStream = mmSocket.getInputStream();
			mmOutputStream = new DataOutputStream(mmSocket.getOutputStream());
			mmInputStream = new DataInputStream(mmSocket.getInputStream());
			beginListenForData();
			//Log.d(LOG_TAG, "Bluetooth Opened: " + mmDevice.getAddress());
			callbackContext.success("Bluetooth Opened: " + mmDevice.getAddress());
			return true;
		} catch (Exception e) {
			String errMsg = e.getMessage();
			Log.e(LOG_TAG, errMsg);
			e.printStackTrace();
			callbackContext.error(errMsg);
		}

		/*
		UUID SERIAL_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); // bluetooth serial port service

		try {
			mmSocket = mmDevice.createRfcommSocketToServiceRecord(SERIAL_UUID);
		} catch (Exception e) {
			Log.e("","Error creating socket");
		}

		try {
			mmSocket.connect();
			mmOutputStream = new DataOutputStream(mmSocket.getOutputStream());
			mmInputStream = new DataInputStream(mmSocket.getInputStream());
			beginListenForData();
			callbackContext.success("Bluetooth Opened: " + mmDevice.getAddress());
		} catch (IOException e) {
			Log.e("",e.getMessage());
			try {
				Log.e("","trying fallback...");

				mmSocket = (BluetoothSocket) mmDevice.getClass().getMethod("createRfcommSocket", new Class[] {int.class}).invoke(mmDevice,1);
				mmSocket.connect();

				mmOutputStream = new DataOutputStream(mmSocket.getOutputStream());
				mmInputStream = new DataInputStream(mmSocket.getInputStream());
				beginListenForData();
					//Log.d(LOG_TAG, "Bluetooth Opened: " + mmDevice.getAddress());
				callbackContext.success("Bluetooth Opened: " + mmDevice.getAddress());
			}
			catch (Exception e2) {
				Log.e("", "Couldn't establish Bluetooth connection!");
				String errMsg = e.getMessage();
				Log.e(LOG_TAG, errMsg);
				e.printStackTrace();
				callbackContext.error(errMsg);
			}
		}
		*/

		return false;
	}

	// After opening a connection to bluetooth printer device,
	// we have to listen and check if a data were sent to be printed.
	void beginListenForData() {
		try {
			final Handler handler = new Handler();
			// This is the ASCII code for a newline character
			final byte delimiter = 10;
			stopWorker = false;
			readBufferPosition = 0;
			readBuffer = new byte[1024];
			workerThread = new Thread(new Runnable() {
				public void run() {
					while (!Thread.currentThread().isInterrupted() && !stopWorker) {
						try {
							int bytesAvailable = mmInputStream.available();
							if (bytesAvailable > 0) {
								byte[] packetBytes = new byte[bytesAvailable];
								mmInputStream.read(packetBytes);
								for (int i = 0; i < bytesAvailable; i++) {
									byte b = packetBytes[i];
									if (b == delimiter) {
										byte[] encodedBytes = new byte[readBufferPosition];
										System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
										/*
										final String data = new String(encodedBytes, "US-ASCII");
										readBufferPosition = 0;
										handler.post(new Runnable() {
											public void run() {
												myLabel.setText(data);
											}
										});
                                        */
									} else {
										readBuffer[readBufferPosition++] = b;
									}
								}
							}
						} catch (IOException ex) {
							stopWorker = true;
						}
					}
				}
			});
			workerThread.start();
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void beginWriteData(byte[] data) {
		Log.d("IMAGE", "stopWriter " + stopWriter);
		final byte[] writeByte = data;
		try {
			final Handler handler = new Handler();
			// This is the ASCII code for a newline character
			final byte delimiter = 10;
			stopWriter = false;
			writerThread = new Thread(new Runnable() {
				public void run() {
					if (!stopWriter) {
					//while (!Thread.currentThread().isInterrupted() && !stopWriter) {
						
							//mmOutputStream.write(writeByte, 0, writeByte.length);
							//mmOutputStream.flush();
							//Log.d("IMAGE", "Length " + writeByte.length);
						int currentIndex = 0;
						int size = writeByte.length; 
						while (currentIndex < size) {
							try {
								int currentLength = Math.min(size - currentIndex, CHUNK_SIZE);
								mmOutputStream.write(writeByte, currentIndex, currentLength);
								currentIndex += currentLength; 
								Log.d("IMAGE", "Index " + currentIndex);
							} catch (IOException ex) {
								stopWriter = true;
								ex.printStackTrace();
							}
						}

						try {
							mmOutputStream.flush();
						} catch (IOException ex) {
							stopWriter = true;
							ex.printStackTrace();
						}
						
						stopWriter = true;
					}
				}
			});
			writerThread.start();
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//This will send data to bluetooth printer
	boolean printText(CallbackContext callbackContext, String msg) throws IOException {
		try {

			mmOutputStream.write(msg.getBytes());

			// tell the user data were sent
			//Log.d(LOG_TAG, "Data Sent");
			callbackContext.success("Data Sent");
			return true;

		} catch (Exception e) {
			String errMsg = e.getMessage();
			Log.e(LOG_TAG, errMsg);
			e.printStackTrace();
			callbackContext.error(errMsg);
		}
		return false;
	}

	boolean barcode(CallbackContext callbackContext, String msg) throws IOException {
		try {
			if(msg.length() > 0) {
				int nWidthX = 6;
				int nVersion = 15;
				int nErrorCorrectionLevel =4;

				Object var5 = null;

				byte[] bCodeData;
				try {
					Log.d("QR", "GBK");
					bCodeData = msg.getBytes("GBK");
				} catch (UnsupportedEncodingException var7) {
					return false;
				}


				GS_w_n[2]           = (byte)nWidthX;
				GS_k_m_v_r_nL_nH[3] = (byte)nVersion;
				GS_k_m_v_r_nL_nH[4] = (byte)nErrorCorrectionLevel;
				GS_k_m_v_r_nL_nH[5] = (byte)(bCodeData.length & 255);
				GS_k_m_v_r_nL_nH[6] = (byte)((bCodeData.length & '\uff00') >> 8);
				byte[] data         = byteArraysToBytes(new byte[][]{GS_w_n, GS_k_m_v_r_nL_nH, bCodeData});

				Log.d("QR", "" + data.length);
				mmOutputStream.write(data, 0, data.length);
	            // tell the user data were sent
	            //Log.d(LOG_TAG, "Data Sent");
				callbackContext.success("Data Sent");
				return true;
			} else {
				callbackContext.error("Exception in barcode printing");
			}
		} catch (Exception e) {
			String errMsg = e.getMessage();
			Log.e(LOG_TAG, errMsg);
			e.printStackTrace();
			callbackContext.error(errMsg);
		}
		return false;
	}

	public static byte[] byteArraysToBytes(byte[][] data) {
		int length = 0;

		for(int i = 0; i < data.length; ++i) {
			length += data[i].length;
		}

		byte[] send = new byte[length];
		int k = 0;

		for(int i = 0; i < data.length; ++i) {
			for(int j = 0; j < data[i].length; ++j) {
				send[k++] = data[i][j];
			}
		}

		return send;
	}

	//This will send data to bluetooth printer
	boolean printImage(CallbackContext callbackContext, String msg, int inch) throws IOException {
		try {

			final String encodedString = msg;
			final String pureBase64Encoded = encodedString.substring(encodedString.indexOf(",")  + 1);

			final byte[] decodedBytes = Base64.decode(pureBase64Encoded, Base64.DEFAULT);

			Bitmap mBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);

			int nWidth = 384;
			
			if(inch == 3) {
				nWidth = 576;
			}

			if (mBitmap != null) {
				Log.d("IMAGE", "Bitmap Created!");

				byte[] bt = POS_PrintPicture(mBitmap, nWidth, 0);

				Log.d("IMAGE", "Length " + bt.length);
				//mmOutputStream.write(bt, 0, bt.length);
				//mmOutputStream.flush();

				int currentIndex = 0;
				int size = bt.length; 
				while (currentIndex < size) {
				    int currentLength = Math.min(size - currentIndex, CHUNK_SIZE);
				    mmOutputStream.write(bt, currentIndex, currentLength);
				    currentIndex += currentLength; 
					Log.d("IMAGE", "Index " + currentIndex);
				}

				
				// Date date = new Date();
				//Log.d("IMAGE", "Flushed " + date.toString());
				//beginWriteData(bt);
				//Thread.sleep(10000);
				callbackContext.success("Image printed!");
				return true;
			}else{
				Log.e("Print Photo error", "the file isn't exists");
				callbackContext.error("Print Photo error");
				return false;
			}
		} catch (Exception e) {
			String errMsg = e.getMessage();
			Log.e(LOG_TAG, errMsg);
			e.printStackTrace();
			callbackContext.error(errMsg);
		}
		return false;
	}

	public static byte[] POS_PrintPicture(Bitmap mBitmap, int nWidth, int nMode) {
		Log.d("IMAGE", "POS PRINT");
		int width         = (nWidth + 7) / 8 * 8;
		int height        = mBitmap.getHeight() * width / mBitmap.getWidth();
		height            = (height + 7) / 8 * 8;
		Bitmap rszBitmap  = resizeImage(mBitmap, width, height);
		Bitmap grayBitmap = toGrayscale(rszBitmap);
		byte[] dithered   = bitmapToBWPix(grayBitmap);
		byte[] data       = eachLinePixToCmd(dithered, width, nMode);

		return data;
	}

	private static byte[] bitmapToBWPix(Bitmap mBitmap) {
		Log.d("IMAGE", "BITMAPTOBWPIX");

		int[] pixels = new int[mBitmap.getWidth() * mBitmap.getHeight()];
		byte[] data = new byte[mBitmap.getWidth() * mBitmap.getHeight()];
		mBitmap.getPixels(pixels, 0, mBitmap.getWidth(), 0, 0, mBitmap.getWidth(), mBitmap.getHeight());
		format_K_dither16x16(pixels, mBitmap.getWidth(), mBitmap.getHeight(), data);
		return data;
	}

	public static void format_K_dither16x16(int[] orgpixels, int xsize, int ysize, byte[] despixels) {
		int k = 0;

		for(int y = 0; y < ysize; ++y) {
			for(int x = 0; x < xsize; ++x) {
				if((orgpixels[k] & 255) > Floyd16x16[x & 15][y & 15]) {
					despixels[k] = 0;
				} else {
					despixels[k] = 1;
				}

				++k;
			}
		}
	}

	private static byte[] eachLinePixToCmd(byte[] src, int nWidth, int nMode) {
		int[] p0 = new int[]{0, 128};
		int[] p1 = new int[]{0, 64};
		int[] p2 = new int[]{0, 32};
		int[] p3 = new int[]{0, 16};
		int[] p4 = new int[]{0, 8};
		int[] p5 = new int[]{0, 4};
		int[] p6 = new int[]{0, 2};
		int nHeight = src.length / nWidth;
		int nBytesPerLine = nWidth / 8;
		byte[] data = new byte[nHeight * (8 + nBytesPerLine)];
		//int offset = 0;
		int k = 0;

		for(int i = 0; i < nHeight; ++i) {
			int offset = i * (8 + nBytesPerLine);
			data[offset + 0] = 29;
			data[offset + 1] = 118;
			data[offset + 2] = 48;
			data[offset + 3] = (byte)(nMode & 1);
			data[offset + 4] = (byte)(nBytesPerLine % 256);
			data[offset + 5] = (byte)(nBytesPerLine / 256);
			data[offset + 6] = 1;
			data[offset + 7] = 0;

			for(int j = 0; j < nBytesPerLine; ++j) {
				data[offset + 8 + j] = (byte)(p0[src[k]] + p1[src[k + 1]] + p2[src[k + 2]] + p3[src[k + 3]] + p4[src[k + 4]] + p5[src[k + 5]] + p6[src[k + 6]] + src[k + 7]);
				k += 8;
			}
		}

		return data;
	}

	public static Bitmap toGrayscale(Bitmap bmpOriginal) {
		Log.d("IMAGE", "TOGRAYSCALE");

		int height = bmpOriginal.getHeight();
		int width = bmpOriginal.getWidth();
		Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		Canvas c = new Canvas(bmpGrayscale);
		Paint paint = new Paint();
		ColorMatrix cm = new ColorMatrix();
		cm.setSaturation(0.0F);
		ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
		paint.setColorFilter(f);
		c.drawBitmap(bmpOriginal, 0.0F, 0.0F, paint);
		return bmpGrayscale;
	}


	public static Bitmap resizeImage(Bitmap bitmap, int w, int h) {
		Log.d("IMAGE", "RESIZE");
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		float scaleWidth = (float)w / (float)width;
		float scaleHeight = (float)h / (float)height;
		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleHeight);
		Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
		return resizedBitmap;
	}


	boolean printPOSCommand(CallbackContext callbackContext, byte[] buffer) throws IOException {
		try {
            //mmOutputStream.write(("Inam").getBytes());
            //mmOutputStream.write((((char)0x0A) + "10 Rehan").getBytes());
			mmOutputStream.write(buffer);
            //mmOutputStream.write(0x0A);

            // tell the user data were sent
			Log.d(LOG_TAG, "Data Sent");
			callbackContext.success("Data Sent");
			return true;
		} catch (Exception e) {
			String errMsg = e.getMessage();
			Log.e(LOG_TAG, errMsg);
			e.printStackTrace();
			callbackContext.error(errMsg);
		}
		return false;
	}

	// disconnect bluetooth printer.
	boolean disconnectBT(CallbackContext callbackContext) throws IOException {
		try {
			stopWorker = true;
			mmOutputStream.close();
			mmInputStream.close();
			mmSocket.close();
			callbackContext.success("Bluetooth Disconnect");
			return true;
		} catch (Exception e) {
			String errMsg = e.getMessage();
			Log.e(LOG_TAG, errMsg);
			e.printStackTrace();
			callbackContext.error(errMsg);
		}
		return false;
	}


	public byte[] getText(String textStr) {
        // TODO Auto-generated method stubbyte[] send;
		byte[] send=null;
		try {
			send = textStr.getBytes("GBK");
		} catch (UnsupportedEncodingException e) {
			send = textStr.getBytes();
		}
		return send;
	}

	public static byte[] hexStringToBytes(String hexString) {
		hexString = hexString.toLowerCase();
		String[] hexStrings = hexString.split(" ");
		byte[] bytes = new byte[hexStrings.length];
		for (int i = 0; i < hexStrings.length; i++) {
			char[] hexChars = hexStrings[i].toCharArray();
			bytes[i] = (byte) (charToByte(hexChars[0]) << 4 | charToByte(hexChars[1]));
		}
		return bytes;
	}

	private static byte charToByte(char c) {
		return (byte) "0123456789abcdef".indexOf(c);
	}


}
