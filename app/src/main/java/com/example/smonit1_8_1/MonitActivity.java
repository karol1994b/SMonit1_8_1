package com.example.smonit1_8_1;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidplot.Plot;
import com.androidplot.util.Redrawer;
import com.androidplot.xy.AdvancedLineAndPointRenderer;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.FastLineAndPointRenderer;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static android.os.Environment.DIRECTORY_DOWNLOADS;


public class MonitActivity extends Activity {

    private static final String TAG = "SMonit";
    private int mMaxChars = 50000;
    private UUID mDeviceUUID;
    private BluetoothSocket mmSocket;
    private ReadInput mReadThread = null;

    private TextView mTxtReceive;
    private ScrollView scrollView;

    private boolean mIsBluetoothConnected = false;

    private BluetoothDevice mDevice;

    private ProgressDialog progressDialog;

    private SQLiteDatabase db;
    private Cursor resultsCursor;

    int theLastOne;

    private XYPlot mySimpleXYPlot0;
    private XYPlot mySimpleXYPlot1;
    private XYPlot mySimpleXYPlot2;
    private XYPlot plot;

    private Redrawer my_redrawer;
    private Redrawer redrawer_ultra;
    private Redrawer redrawer;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monit);
        mTxtReceive = findViewById(R.id.txtReceive);
        scrollView = findViewById(R.id.viewScroll);
        mySimpleXYPlot0 = (XYPlot) findViewById(R.id.plot0);
        mySimpleXYPlot1 = (XYPlot) findViewById(R.id.plot1);
        mySimpleXYPlot2 = (XYPlot) findViewById(R.id.plot2);
        plot = (XYPlot) findViewById(R.id.plot);


        Bundle b = getIntent().getExtras();
        assert b != null;
        mDevice = b.getParcelable(MainActivity.DEVICE_EXTRA);
        mDeviceUUID = UUID.fromString(b.getString(MainActivity.DEVICE_UUID));

        mTxtReceive.setMovementMethod(new ScrollingMovementMethod());

        if (mmSocket == null || !mIsBluetoothConnected) {
            new ConnectBT().execute();
        }
        new createDatabaseTask().execute(MonitActivity.this);
    }


    @Override
    protected void onResume() {
        Log.d(TAG, "Resumed");
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (mmSocket != null && mIsBluetoothConnected) {
            new DisConnectBT().execute();
        }
        Log.d(TAG, "Paused");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "Stopped");
        super.onStop();
        redrawer_ultra.finish();
        redrawer.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        resultsCursor.close();
        db.close();
    }





    private class ReadInput extends Thread {

        private boolean bStop = false;
        private Thread t;

        public ReadInput() {
            t = new Thread(this, "Input Thread");
            t.start();
        }


        public boolean isRunning() {
            return t.isAlive();
        }

        @Override
        public void run() {
            InputStream mmInStream;
            byte[] buffer;

            try {
                mmInStream = mmSocket.getInputStream();

                while (true) {

//                    buffer = new byte[1024];
                    buffer = new byte[4096];

//                    Number[] kontener0 = new Number[4096];
                    Number[] kontener0 = new Number[16384];
                    Number[] kontener1 = new Number[4096];
                    Number[] kontener2 = new Number[4096];


                    if (mmInStream.available() > 0) {
                        mmInStream.read(buffer);

                        int i;

                        for (i = 0; i < buffer.length && buffer[i] != 0; i++) {

                        }

                        final String strInput = new String(buffer, 0, i);

                        int k = 0;
                        try {
                            String[] splitted_data = strInput.split("\\r?\\n");
                            String[][] splitted_channels = new String[splitted_data.length][3];
                            for (int z = 0; z < splitted_data.length; z++) {
                                splitted_channels[z] = splitted_data[z].split(",",3);
                            }


                            for (int j = 0; j < splitted_data.length; j++) {

                                if (Integer.parseInt(splitted_channels[j][0]) < 1000 && Integer.parseInt(splitted_channels[j][0]) > 10) {
                                    kontener0[k] = Integer.parseInt(splitted_channels[j][0]);
                                    Log.d(TAG, kontener0[k].toString());
                                    k++;
                                }

//                                if (Integer.parseInt(splitted_channels[j][1]) < 1000 && Integer.parseInt(splitted_channels[j][1]) > 10) {
//                                    kontener1[k] = Integer.parseInt(splitted_channels[j][1]);
//                                    //Log.d(TAG, kontener1[k].toString());
//                                    k++;
//                                }
//
//                                if (Integer.parseInt(splitted_channels[j][2]) < 1000 && Integer.parseInt(splitted_channels[j][2]) > 10) {
//                                    kontener2[k] = Integer.parseInt(splitted_channels[j][2]);
//                                    Log.d(TAG, kontener2[k].toString());
//                                    k++;
//                                }

                            }

                            mySimpleXYPlot0.clear();
                            mySimpleXYPlot1.clear();
                            mySimpleXYPlot2.clear();

                        } catch (NumberFormatException e) {
                            System.err.println("This is not a number!");
                            Log.d(TAG, "wyjątek związany z NFE");
                        }



                        XYSeries series0 = new SimpleXYSeries(
                                Arrays.asList(kontener0),
                                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY,
                                "Dane - Seria0");

                        XYSeries series1 = new SimpleXYSeries(
                                Arrays.asList(kontener0),
                                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY,
                                "Dane - Seria1");

                        XYSeries series2 = new SimpleXYSeries(
                                Arrays.asList(kontener0),
                                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY,
                                "Dane - Seria2");



                        FastLineAndPointRenderer.Formatter series0Format = new FastLineAndPointRenderer.Formatter(
                                Color.rgb(0, 255, 0),
                                null,
                                null);

                        FastLineAndPointRenderer.Formatter series1Format = new FastLineAndPointRenderer.Formatter(
                                Color.rgb(0, 255, 0),
                                null,
                                null);

                        FastLineAndPointRenderer.Formatter series2Format = new FastLineAndPointRenderer.Formatter(
                                Color.rgb(0, 255, 0),
                                null,
                                null);


                        series0Format.setLegendIconEnabled(false);
                        series1Format.setLegendIconEnabled(false);
                        series2Format.setLegendIconEnabled(false);

                        mySimpleXYPlot0.setRangeBoundaries( 0, 700, BoundaryMode.FIXED);
                        mySimpleXYPlot0.addSeries(series0, series0Format);
                        mySimpleXYPlot0.getBackgroundPaint().setAlpha(0);

                        mySimpleXYPlot1.setRangeBoundaries( 0, 700, BoundaryMode.FIXED);
                        mySimpleXYPlot1.addSeries(series1, series1Format);
                        mySimpleXYPlot1.getBackgroundPaint().setAlpha(0);

                        mySimpleXYPlot2.setRangeBoundaries( 0, 700, BoundaryMode.FIXED);
                        mySimpleXYPlot2.addSeries(series2, series2Format);
                        mySimpleXYPlot2.getBackgroundPaint().setAlpha(0);


                        mySimpleXYPlot0.setLinesPerRangeLabel(3);
                        mySimpleXYPlot1.setLinesPerRangeLabel(3);
                        mySimpleXYPlot2.setLinesPerRangeLabel(3);



                        ECGModel ecgSeries = new ECGModel(1000, 200, strInput);

                        MyFadeFormatter formatter = new MyFadeFormatter(1000);
                        formatter.setLegendIconEnabled(false);
                        plot.addSeries(ecgSeries, formatter);

                        plot.setLinesPerRangeLabel(3);
                        plot.setRangeBoundaries( 0, 50, BoundaryMode.FIXED);

                        ecgSeries.start(new WeakReference<>(plot.getRenderer(AdvancedLineAndPointRenderer.class)));

                        redrawer = new Redrawer(plot, 30, true);


                        List<Plot> lista_wykresow = new ArrayList<>(3);

                        lista_wykresow.add(mySimpleXYPlot0);
                        lista_wykresow.add(mySimpleXYPlot1);
                        lista_wykresow.add(mySimpleXYPlot2);


                        redrawer_ultra = new Redrawer(lista_wykresow, 20, true);
                        mySimpleXYPlot0.redraw();
                        mySimpleXYPlot1.redraw();
                        mySimpleXYPlot2.redraw();


                        mTxtReceive.post(new Runnable() {
                            @Override
                            public void run() {
                                mTxtReceive.append(strInput);
                                int txtLength = mTxtReceive.getEditableText().length();

                                if(txtLength > mMaxChars){
                                    mTxtReceive.getEditableText().delete(0, txtLength - mMaxChars);
                                }


                                scrollView.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        scrollView.fullScroll(View.FOCUS_DOWN);
                                    }
                                });

                            }
                        });

                    }
                    //Thread.sleep(500);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        public void stop2() {
            bStop = true;
        }

    }




    private class ConnectBT extends AsyncTask<Void, Void, Void> {

        private boolean mConnectSuccessful = true;

        @Override
        protected void onPreExecute() {

            progressDialog = ProgressDialog.show(MonitActivity.this, "Please wait", "Connecting");
        }
        @Override
        protected Void doInBackground(Void... params) {
            try {
                if (mmSocket == null || !mIsBluetoothConnected) {
                    mmSocket = mDevice.createRfcommSocketToServiceRecord(mDeviceUUID);
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    mmSocket.connect();
                }
            } catch (IOException e) {
                e.printStackTrace();
                mConnectSuccessful = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (!mConnectSuccessful) {
                Toast.makeText(getApplicationContext(), "Could not connect to device. Is it a Serial device? Also check if the UUID is correct in the settings", Toast.LENGTH_LONG).show();
                //finish();
            } else {
                Toast.makeText(getApplicationContext(), "Connected to device", Toast.LENGTH_SHORT).show();
                mIsBluetoothConnected = true;
                mReadThread = new ReadInput();
            }
            progressDialog.dismiss();
        }
    }


    private class DisConnectBT extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            if (mReadThread != null) {
                mReadThread.stop2();
                while (mReadThread.isRunning()) {
                }
                mReadThread = null;
            }

            try {
                mmSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }


        @Override
        protected void onPostExecute(Void result) {
            //super.onPostExecute(result);
            mIsBluetoothConnected = false;
            //finish();
        }
    }


    public void onSaveClicked(View view) {

        String results_db = mTxtReceive.getText().toString();

        new UpdateDatabaseTask().execute(results_db);
        saveToTxtfile();
    }


    void saveToTxtfile() {
        try {
            String root = Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS).toString();
            File myFile = new File(root, "ecgResults.txt");
            myFile.createNewFile();
            FileOutputStream fOut = new FileOutputStream(myFile);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);

            myOutWriter.append(mTxtReceive.getText());

            myOutWriter.close();
            fOut.close();
            Toast.makeText(getApplicationContext(), "Done writing to file 'ecgResults.txt' in the path Phone/Download",
                    Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(),e.getMessage(), Toast.LENGTH_SHORT)
                    .show();
            Log.d(TAG, "Brak uprawnien - brak dostepu");
        }
    }

}
