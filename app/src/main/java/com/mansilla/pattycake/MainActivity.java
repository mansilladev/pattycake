package com.mansilla.pattycake;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import java.text.NumberFormat;
import java.util.List;
//import java.util.UUID;
import java.util.Collections;

import com.google.gson.Gson;
//import com.google.gson.GsonBuilder;
//import com.google.gson.reflect.TypeToken;

//import co.poynt.api.model.Card;
//import co.poynt.api.model.CardType;
//import co.poynt.api.model.Customer;
//import co.poynt.api.model.FundingSource;
//import co.poynt.api.model.FundingSourceAccountType;
//import co.poynt.api.model.FundingSourceType;
import co.poynt.api.model.Order;
import co.poynt.api.model.TransactionReference;
import co.poynt.api.model.TransactionReferenceType;

//import co.poynt.api.model.OrderAmounts;
//import co.poynt.api.model.OrderItem;
//import co.poynt.api.model.Product;
import co.poynt.api.model.Transaction;
//import co.poynt.api.model.TransactionAction;
//import co.poynt.api.model.TransactionAmounts;
//import co.poynt.api.model.TransactionStatus;
import co.poynt.os.model.Intents;
import co.poynt.os.model.Payment;
//import co.poynt.os.model.PaymentStatus;
//import co.poynt.os.model.PoyntError;
//import co.poynt.os.services.v1.IPoyntBusinessActivationListener;
//import co.poynt.os.services.v1.IPoyntBusinessCustomersListListener;
//import co.poynt.os.services.v1.IPoyntCustomerReadListener;
//import co.poynt.os.services.v1.IPoyntCustomerService;
import co.poynt.os.services.v1.IPoyntOrderService;
//import co.poynt.os.services.v1.IPoyntOrderServiceListener;
import co.poynt.os.services.v1.IPoyntTransactionService;
//import co.poynt.os.services.v1.IPoyntTransactionServiceListener;

public class MainActivity extends AppCompatActivity {
    private static final int COLLECT_PAYMENT_REQUEST = 13132;
    private static final String TAG = "Poynt at the Table";
//    private Gson gson;
//    private IPoyntTransactionService mTransactionService;

//    private IPoyntOrderService mOrderService;

//    private Transaction transaction;

    Button collectButton;
    EditText serverText;
    EditText tableText;
    EditText amountText;
    EditText ticketText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        collectButton = (Button) findViewById(R.id.collectButton);
        collectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String amountCharge;
                amountText = (EditText) findViewById(R.id.amount);
                amountCharge = amountText.getText().toString().replaceAll("[^\\d]", "");
                if (amountCharge.length() == 0) {
                    amountCharge = "0";
                }

                if (Integer.parseInt(amountCharge)> 0) {
                    launchPoyntPayment(Long.parseLong(amountCharge), null);
                }
                else {
                    Toast.makeText(MainActivity.this, "Amount required.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void launchPoyntPayment(Long amount, Order order) {
        String currencyCode = NumberFormat.getCurrencyInstance().getCurrency().getCurrencyCode();

        serverText = (EditText) findViewById(R.id.serverNumber);
        tableText = (EditText) findViewById(R.id.tableNumber);
        ticketText = (EditText) findViewById(R.id.ticketNumber);
        Payment payment = new Payment();
        String referenceId = "Server " + serverText.getText().toString() + ":Table " +
                tableText.getText().toString() + ":Ticket "+ticketText.getText().toString();

        TransactionReference ref = new TransactionReference();
        ref.setType(TransactionReferenceType.CUSTOM);
        ref.setCustomType("posReferenceId");
        ref.setId(referenceId);

        payment.setReferences(Collections.singletonList(ref));
        payment.setCurrency(currencyCode);
        payment.setAmount(amount);
        payment.setMultiTender(true);

        // Start payment activity
        try {
            Intent collectPaymentIntent = new Intent(Intents.ACTION_COLLECT_PAYMENT);
            collectPaymentIntent.putExtra(Intents.INTENT_EXTRAS_PAYMENT, payment);
            startActivityForResult(collectPaymentIntent, COLLECT_PAYMENT_REQUEST);
        } catch (ActivityNotFoundException ex) {
            Log.e(TAG, "Poynt Payment Activity not found - did you install PoyntServices?", ex);
        }
        co.poynt.os.model.PrintedReceipt receipt;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == COLLECT_PAYMENT_REQUEST) {
            Payment payment = data.getParcelableExtra("payment");
            if (payment != null){
                List<Transaction> txnList = payment.getTransactions();
                for (Transaction t :txnList){
                    Log.d(TAG, "onActivityResult: " + t);
                }
            }
        }
    }
}
