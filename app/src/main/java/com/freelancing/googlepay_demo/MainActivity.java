package com.freelancing.googlepay_demo;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    EditText amount, note, name, upivirtualid;
    Button send;
    String TAG = "main";
    final int UPI_PAYMENT = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        send = (Button) findViewById(R.id.send);
        amount = (EditText) findViewById(R.id.amount_et);
        note = (EditText) findViewById(R.id.note);
        name = (EditText) findViewById(R.id.name);
        upivirtualid = (EditText) findViewById(R.id.upi_id);


        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Getting the values from the EditTexts
                if (TextUtils.isEmpty(name.getText().toString().trim())) {
                    Toast.makeText(MainActivity.this, " Name is invalid", Toast.LENGTH_SHORT).show();

                } else if (TextUtils.isEmpty(upivirtualid.getText().toString().trim())) {
                    Toast.makeText(MainActivity.this, " UPI ID is invalid", Toast.LENGTH_SHORT).show();

                } else if (TextUtils.isEmpty(note.getText().toString().trim())) {
                    Toast.makeText(MainActivity.this, " Note is invalid", Toast.LENGTH_SHORT).show();

                } else if (TextUtils.isEmpty(amount.getText().toString().trim())) {
                    Toast.makeText(MainActivity.this, " Amount is invalid", Toast.LENGTH_SHORT).show();
                } else {

                    payUsingUpi(name.getText().toString(), upivirtualid.getText().toString(),
                            note.getText().toString(), amount.getText().toString());

                }


            }
        });


    }

    private void payUsingUpi(String name, String upiId, String note, String amount) {


        Uri uri = Uri.parse("upi://pay").buildUpon()
                .appendQueryParameter("pa", upiId)
                .appendQueryParameter("pn", name)
                .appendQueryParameter("tn", note)
                .appendQueryParameter("am", amount)
                .appendQueryParameter("cu", "INR")
                // .appendQueryParameter("mc", "your-merchant-code")   OPTIONAL
                // .appendQueryParameter("tr", "your-transaction-ref-id")  OPTIONAL
                // .appendQueryParameter("url", "your-transaction-url")  OPTIONAL
                // .appendQueryParameter("refUrl", "blueapp")  OPTIONAL
                .build();


        Intent upiPayIntent = new Intent(Intent.ACTION_VIEW);
        upiPayIntent.setData(uri);

        // will always show a dialog to user to choose an app
        Intent chooser = Intent.createChooser(upiPayIntent, "Pay with");

        //check if intent  resolves
        if (null != chooser.resolveActivity(getPackageManager())) {
            startActivityForResult(chooser, UPI_PAYMENT);
        } else {
            Toast.makeText(MainActivity.this, "No UPI app found, please install one to continue", Toast.LENGTH_SHORT).show();
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case UPI_PAYMENT:
                if ((RESULT_OK == resultCode) || (resultCode == 11)) {


                    if (data != null) {
                        String trxt = data.getStringExtra("tezResponse");

                        Toast.makeText(MainActivity.this,"onActivityResult: "+data.getStringExtra("tezResponse")+"",Toast.LENGTH_LONG).show();


                        Log.e("UPI", "onActivityResult: " + trxt);
                        ArrayList<String> dataList = new ArrayList<>();
                        dataList.add(trxt);
                        upiPaymentDataOperation(dataList);
                    } else {
                        Log.e("UPI", "onActivityResult: " + "Return data is null");
                        ArrayList<String> dataList = new ArrayList<>();
                        dataList.add("nothing");
                        Toast.makeText(MainActivity.this,"nothing: ",Toast.LENGTH_LONG).show();

                        upiPaymentDataOperation(dataList);
                    }
                } else {
//when user simply back without payment
                    Log.e("UPI", "onActivityResult: " + "Return data is null");
                    ArrayList<String> dataList = new ArrayList<>();
                    dataList.add("nothing");
                    Toast.makeText(MainActivity.this,"nothing: ",Toast.LENGTH_LONG).show();

                    upiPaymentDataOperation(dataList);
                }
                break;


        }


    }

    private void upiPaymentDataOperation(ArrayList<String> data) {

        if(isConnectionAvailable(MainActivity.this)){
          String str=data.get(0);
            Log.e("UPIPAY", "upiPaymentDataOperation: "+str);
            Toast.makeText(MainActivity.this,"UPIPAY: "+"upiPaymentDataOperation: "+str,Toast.LENGTH_LONG).show();

            String paymentCancel = "";
            if(str == null) str = "discard";
            String status = "";
            String approvalRefNo = "";
            String response[] = str.split("&");




            for (int i = 0; i < response.length; i++) {
                String equalStr[] = response[i].split("=");

                Toast.makeText(MainActivity.this,"tezResponse : "+response[i],Toast.LENGTH_LONG).show();


                if(equalStr.length >= 2) {
                    if (equalStr[0].toLowerCase().equals("Status".toLowerCase())) {
                        status = equalStr[1].toLowerCase();
                    }
                    else if (equalStr[0].toLowerCase().equals("ApprovalRefNo".toLowerCase()) || equalStr[0].toLowerCase().equals("txnRef".toLowerCase())) {
                        approvalRefNo = equalStr[1];
                    }
                }
                else {
                    paymentCancel = "Payment cancelled by user.";
                }
            }



            if (status.equals("success")) {
                //Code to handle successful transaction here.
                Toast.makeText(MainActivity.this, "Transaction successful.", Toast.LENGTH_SHORT).show();
                Log.e("UPI", "payment successfull: "+approvalRefNo);
            }
            else if("Payment cancelled by user.".equals(paymentCancel)) {
                Toast.makeText(MainActivity.this, "Payment cancelled by user.", Toast.LENGTH_SHORT).show();
                Log.e("UPI", "Cancelled by user: "+approvalRefNo);

            }
            else {
                Toast.makeText(MainActivity.this, "Transaction failed.Please try again", Toast.LENGTH_SHORT).show();
                Log.e("UPI", "failed payment: "+approvalRefNo);

            }



        } else {
            Log.e("UPI", "Internet issue: ");

            Toast.makeText(MainActivity.this, "Internet connection is not available. Please check and try again", Toast.LENGTH_SHORT).show();
        }




    }


    public static boolean isConnectionAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            NetworkInfo netinfo = connectivityManager.getActiveNetworkInfo();
            if (netinfo != null && netinfo.isAvailable()
                    && netinfo.isConnected()
                    && netinfo.isConnectedOrConnecting()) {
                return true;
            }

        }

        return false;
    }




}
