package com.dlohaiti.dlokiosk;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.widget.Toast;

import com.dlohaiti.dlokiosk.client.CustomerAccountClient;
import com.dlohaiti.dlokiosk.client.DeliveriesClient;
import com.dlohaiti.dlokiosk.client.PostResponse;
import com.dlohaiti.dlokiosk.client.ReadingsClient;
import com.dlohaiti.dlokiosk.client.ReceiptsClient;
import com.dlohaiti.dlokiosk.client.SponsorClient;
import com.dlohaiti.dlokiosk.db.CustomerAccountRepository;
import com.dlohaiti.dlokiosk.db.DeliveryRepository;
import com.dlohaiti.dlokiosk.db.ReadingsRepository;
import com.dlohaiti.dlokiosk.db.ReceiptsRepository;
import com.dlohaiti.dlokiosk.db.SponsorRepository;
import com.dlohaiti.dlokiosk.domain.CustomerAccount;
import com.dlohaiti.dlokiosk.domain.Delivery;
import com.dlohaiti.dlokiosk.domain.Reading;
import com.dlohaiti.dlokiosk.domain.Receipt;
import com.dlohaiti.dlokiosk.domain.Sponsor;
import com.google.inject.Inject;
import roboguice.util.RoboAsyncTask;

import java.util.Collection;
import java.util.List;

public class ManualSyncReadingsTask extends RoboAsyncTask<String> {

    @Inject
    private ReceiptsClient receiptsClient;
    @Inject
    private DeliveriesClient deliveriesClient;
    @Inject
    private ReadingsClient readingsClient;
    @Inject
    private SponsorClient sponsorClient;
    @Inject
    private CustomerAccountClient accountClient;
    @Inject
    private ReceiptsRepository receiptsRepository;
    @Inject
    private DeliveryRepository deliveriesRepository;
    @Inject
    private ReadingsRepository readingsRepository;
    @Inject
    private CustomerAccountRepository customerAccountRepository;
    @Inject
    private SponsorRepository sponsorRepository;

    private Activity activity;
    private ProgressDialog progressDialog;

    public ManualSyncReadingsTask(Activity activity) {
        super(activity.getApplicationContext());
        this.progressDialog = new ProgressDialog(activity);
        this.activity = activity;
    }

    @Override
    protected void onPreExecute() {
        if (this.progressDialog == null) {
            this.progressDialog = new ProgressDialog(activity);
            this.progressDialog.setIndeterminate(true);
        }

        this.progressDialog.setMessage(activity.getString(R.string.sending_readings_message));
        this.progressDialog.show();
    }

    @Override
    public String call() throws Exception {
        Collection<Receipt> receipts = receiptsRepository.list();
        Collection<Reading> readings = readingsRepository.list();
        List<CustomerAccount> accounts = customerAccountRepository.getNonSyncAccounts();
        List<Sponsor> sponsors = sponsorRepository.getNonSyncSponsors();

        if (sponsors.isEmpty() && accounts.isEmpty() && receipts.isEmpty() && readings.isEmpty()) {
            return activity.getString(R.string.no_readings_msg);
        }

        Failures failures = new Failures();

        for (Sponsor sponsor : sponsors) {
            sponsor.withAccounts(customerAccountRepository.findBySponsorId(sponsor.getId()));
            PostResponse response = sponsorClient.send(sponsor);
            if (response.isSuccess()) {
                sponsorRepository.synced(sponsor);
            } else {
                failures.add(new Failure(FailureKind.SPONSOR, response.getErrors()));
            }
        }

        for (CustomerAccount account : accounts) {
            PostResponse response = accountClient.send(account);
            if (response.isSuccess()) {
                customerAccountRepository.synced(account);
            } else {
                failures.add(new Failure(FailureKind.ACCOUNT, response.getErrors()));
            }
        }

        for (Reading reading : readings) {
            PostResponse response = readingsClient.send(reading);
            if (response.isSuccess()) {
                readingsRepository.remove(reading);
            } else {
                failures.add(new Failure(FailureKind.READING, response.getErrors()));
            }
        }

        for (Receipt receipt : receipts) {
            PostResponse response = receiptsClient.send(receipt);
            if (response.isSuccess()) {
                receiptsRepository.remove(receipt);
            } else {
                failures.add(new Failure(FailureKind.RECEIPT, response.getErrors()));
            }
        }

        if (failures.isNotEmpty()) {
            Integer readingCount = failures.countFor(FailureKind.READING);
            Integer receiptCount = failures.countFor(FailureKind.RECEIPT);
            Integer accountCount = failures.countFor(FailureKind.ACCOUNT);
            Integer sponsorCount = failures.countFor(FailureKind.SPONSOR);
            return activity.getString(R.string.send_error_msg, readingCount, receiptCount,accountCount,sponsorCount);
        }
        return activity.getString(R.string.send_success_msg, readings.size(), receipts.size(), accounts.size(),sponsors.size());
    }

    private void showMessage(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(message);
        builder.setPositiveButton(R.string.ok, null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onSuccess(String message) {
        showMessage(message);
    }

    @Override
    protected void onException(Exception e) throws RuntimeException {
        Toast.makeText(getContext(), "PROBLEM: " + e.getMessage(), Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onFinally() throws RuntimeException {
        if (this.progressDialog != null) {
            this.progressDialog.dismiss();
        }
    }

}
