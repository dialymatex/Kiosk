package com.dlohaiti.dlokiosk.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import com.dlohaiti.dlokiosk.R;
import com.dlohaiti.dlokiosk.domain.Money;
import com.dlohaiti.dlokiosk.domain.Product;
import com.google.inject.Inject;
import org.springframework.util.support.Base64;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.dlohaiti.dlokiosk.db.KioskDatabaseUtils.matches;
import static com.dlohaiti.dlokiosk.db.KioskDatabaseUtils.where;

public class ProductRepository {
    private final static String TAG = ProductRepository.class.getSimpleName();
    private final KioskDatabase db;
    private final Context context;
    private final static String[] columns = new String[]{
            KioskDatabase.ProductsTable.ID,
            KioskDatabase.ProductsTable.SKU,
            KioskDatabase.ProductsTable.ICON,
            KioskDatabase.ProductsTable.REQUIRES_QUANTITY,
            KioskDatabase.ProductsTable.MINIMUM_QUANTITY,
            KioskDatabase.ProductsTable.MAXIMUM_QUANTITY,
            KioskDatabase.ProductsTable.PRICE,
            KioskDatabase.ProductsTable.CURRENCY,
            KioskDatabase.ProductsTable.DESCRIPTION,
            KioskDatabase.ProductsTable.GALLONS
    };

    @Inject
    public ProductRepository(Context context, KioskDatabase db) {
        this.context = context;
        this.db = db;
    }

    public List<Product> list() {
        List<Product> products = new ArrayList<Product>();
        SQLiteDatabase readableDatabase = db.getReadableDatabase();
        readableDatabase.beginTransaction();
        try {
            Cursor cursor = readableDatabase.query(KioskDatabase.ProductsTable.TABLE_NAME, columns, null, null, null, null, null);
            cursor.moveToFirst();
            //TODO: guard against/fail gracefully when running out of memory
            while (!cursor.isAfterLast()) {
                products.add(buildProduct(cursor));
                cursor.moveToNext();
            }
            cursor.close();
            readableDatabase.setTransactionSuccessful();
            return products;
        } catch(Exception e) {
            Log.e(TAG, "Failed to load all products from the database.", e);
            return new ArrayList<Product>();
        } finally {
            readableDatabase.endTransaction();
        }
    }

    private Product buildProduct(Cursor cursor) {
        String sku = cursor.getString(1);
        boolean requiresQuantity = Boolean.parseBoolean(cursor.getString(3));
        Integer minimum = cursor.getInt(4);
        Integer maximum = cursor.getInt(5);
        Money price = new Money(new BigDecimal(cursor.getDouble(6)));
        String description = cursor.getString(8);
        Integer gallons = cursor.getInt(9);
        Bitmap resource; //TODO: how can we make this show the unknown image when it doesn't decode properly?
        try {
            byte[] imageData = Base64.decode(cursor.getString(2));
            resource = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
        } catch (IOException e) {
            Log.e(TAG, String.format("image for product(%s) could not be decoded", sku), e);
            resource = BitmapFactory.decodeResource(context.getResources(), R.drawable.unknown);
        }
        long id = cursor.getLong(0);
        return new Product(id, sku, resource, requiresQuantity, 1, minimum, maximum, price, description, gallons);
    }

    public Product findById(Long id) {
        SQLiteDatabase readableDatabase = db.getReadableDatabase();
        readableDatabase.beginTransaction();
        try {
            Cursor cursor = readableDatabase.query(KioskDatabase.ProductsTable.TABLE_NAME, columns, where(KioskDatabase.ProductsTable.ID), matches(id), null, null, null);
            if (cursor.getCount() != 1) {
                return new Product(null, null, null, false, null, null, null, null, null, null);
            }
            cursor.moveToFirst();
            Product product = buildProduct(cursor);
            cursor.close();
            readableDatabase.setTransactionSuccessful();
            return product;
        } catch(Exception e) {
            Log.e(TAG, String.format("Failed to find product with id %d in the database.", id), e);
            return new Product(null, null, null, false, null, null, null, null, null, null);
        } finally {
            readableDatabase.endTransaction();
        }
    }

    public boolean replaceAll(List<Product> products) {
        SQLiteDatabase wdb = db.getWritableDatabase();
        wdb.beginTransaction();
        try {
            wdb.delete(KioskDatabase.ProductsTable.TABLE_NAME, null, null);
            for(Product p : products) {
                ContentValues values = new ContentValues();
                values.put(KioskDatabase.ProductsTable.SKU, p.getSku());
                values.put(KioskDatabase.ProductsTable.PRICE, p.getPrice().getAmount().toString());
                values.put(KioskDatabase.ProductsTable.DESCRIPTION, p.getDescription());
                values.put(KioskDatabase.ProductsTable.GALLONS, p.getGallons());
//                values.put(KioskDatabase.ProductsTable.ICON, null)
                values.put(KioskDatabase.ProductsTable.MINIMUM_QUANTITY, p.getMinimumQuantity());
                values.put(KioskDatabase.ProductsTable.MAXIMUM_QUANTITY, p.getMaximumQuantity());
                values.put(KioskDatabase.ProductsTable.REQUIRES_QUANTITY, p.requiresQuantity());
                values.put(KioskDatabase.ProductsTable.CURRENCY, p.getPrice().getCurrencyCode());
                wdb.insert(KioskDatabase.ProductsTable.TABLE_NAME, null, values);
            }
            wdb.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Failed to replace all products.", e);
            return false;
        } finally {
            wdb.endTransaction();
        }
    }
}
