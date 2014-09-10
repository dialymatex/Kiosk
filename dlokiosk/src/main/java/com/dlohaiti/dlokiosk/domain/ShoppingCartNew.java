package com.dlohaiti.dlokiosk.domain;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class ShoppingCartNew {
    private final List<Product> products = new ArrayList<Product>();
    private final List<Promotion> promotions = new ArrayList<Promotion>();
    private SalesChannel salesChannel;
    private CustomerAccount customerAccount;
    private final RegisterNew register;

    @Inject
    public ShoppingCartNew(RegisterNew register) {
        this.register = register;
    }

    public void addOrProduct(Product newProduct) {
        Integer existingProduct = null;
        for (int index = 0; index < products.size(); index++) {
            if (products.get(index).getId().equals(newProduct.getId())) {
                existingProduct = index;
            }
        }
        if (existingProduct != null) {
            int updatedQuantity = newProduct.getQuantity() + products.get(existingProduct).getQuantity();
            products.set(existingProduct, newProduct.withQuantity(updatedQuantity));
        } else {
            products.add(newProduct);
        }
    }

    public void removeProduct(int position) {
        products.remove(position);
    }

    public List<Product> getProducts() {
        return products;
    }

    public void checkout() {
        register.checkout(this);
        clear();
    }

    public void clear() {
        products.clear();
        promotions.clear();
        salesChannel = null;
        customerAccount = null;
    }

    public boolean isEmpty() {
        return products.isEmpty() && promotions.isEmpty();
    }

    public List<Promotion> getPromotions() {
        return promotions;
    }

    public void addPromotion(Promotion promotion) {
        promotions.add(promotion);
    }

    public void removePromotion(Promotion promotion) {
        promotions.remove(promotion);
    }

    public Money getSubtotal() {
        return register.subtotal(this);
    }

    public String getCurrencyCode() {
        String currencyCode = "";
        if (products.isEmpty()) {
            return currencyCode;
        } else {
            currencyCode = products.get(0).getPrice().getCurrencyCode();
        }
        return currencyCode;
    }

    public BigDecimal getTotal() {
        return register.total(this).getAmount();
    }

    public void removePromotion(int id) {
        promotions.remove(id);
    }

    public void clearPromotions() {
        promotions.clear();
    }

    public SalesChannel salesChannel() {
        return salesChannel;
    }

    public void setSalesChannel(SalesChannel channel) {
        this.salesChannel = channel;
    }

    public CustomerAccount customerAccount() {
        return customerAccount;
    }

    public void setCustomerAccount(CustomerAccount account) {
        this.customerAccount = account;
    }
}