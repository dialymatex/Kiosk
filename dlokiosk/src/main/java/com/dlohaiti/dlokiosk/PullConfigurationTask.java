package com.dlohaiti.dlokiosk;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import com.dlohaiti.dlokiosk.client.Configuration;
import com.dlohaiti.dlokiosk.client.ConfigurationClient;
import com.dlohaiti.dlokiosk.client.ProductJson;
import com.dlohaiti.dlokiosk.db.ProductRepository;
import com.dlohaiti.dlokiosk.domain.Money;
import com.dlohaiti.dlokiosk.domain.Product;
import com.google.inject.Inject;
import roboguice.inject.InjectResource;
import roboguice.util.RoboAsyncTask;

import java.util.ArrayList;
import java.util.List;

public class PullConfigurationTask extends RoboAsyncTask<String> {
    private static final String TAG = PullConfigurationTask.class.getSimpleName();
    private ProgressDialog dialog;
    @Inject private ConfigurationClient client;
    @Inject private ProductRepository productRepository;
    @InjectResource(R.string.fetch_configuration_failed) private String fetchConfigurationFailedMessage;
    @InjectResource(R.string.fetch_configuration_succeeded) private String fetchConfigurationSucceededMessage;
    private Context context;

    public PullConfigurationTask(Context context) {
        super(context);
        this.context = context;
        this.dialog = new ProgressDialog(context);
    }

    @Override protected void onPreExecute() throws Exception {
        dialog.setMessage("Loading Configuration From Server...");
        dialog.show();
    }

    @Override public String call() throws Exception {
        Configuration c = client.fetch();
        List<Product> products = new ArrayList<Product>();
        for(ProductJson p : c.getProducts()) {
            Money price = new Money(p.getPrice().getAmount());
            products.add(new Product(null, p.getSku(), null, p.isRequiresQuantity(), 1, p.getMinimumQuantity(), p.getMaximumQuantity(), price, p.getDescription(), p.getGallons()));
        }
        if(productRepository.replaceAll(products)) {
            Log.i(TAG, "products successfully replaced");
        }
        return "";
    }

    @Override protected void onSuccess(String s) throws Exception {
        Toast.makeText(context, fetchConfigurationSucceededMessage, Toast.LENGTH_LONG).show();
    }

    @Override protected void onException(Exception e) throws RuntimeException {
        Log.e(TAG, "Error fetching configuration from server", e);
        Toast.makeText(context, fetchConfigurationFailedMessage, Toast.LENGTH_LONG).show();
    }

    @Override protected void onFinally() throws RuntimeException {
        if(dialog.isShowing()) {
            dialog.dismiss();
        }
    }
}
