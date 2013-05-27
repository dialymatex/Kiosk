package com.dlohaiti.dlokiosk.domain;

import android.graphics.Bitmap;
import com.dlohaiti.dlokiosk.VisibleGridItem;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class Promotion implements VisibleGridItem, Orderable, Comparable<Promotion> {
    private final Long id;
    private final String sku;
    private final PromotionApplicationType appliesTo;
    private final String productSku;
    private final Date startDate;
    private final Date endDate;
    private final BigDecimal amount;
    private final PromotionType type;
    private final Bitmap resource;
    private boolean used = false;

    public Promotion(Long id,
                     String sku,
                     PromotionApplicationType appliesTo,
                     String productSku,
                     Date startDate,
                     Date endDate,
                     String amount,
                     PromotionType percent,
                     Bitmap resource) {
        this.id = id;
        this.sku = sku;
        this.appliesTo = appliesTo;
        this.productSku = productSku;
        this.startDate = startDate;
        this.endDate = endDate;
        this.amount = new BigDecimal(amount);
        this.type = percent;
        this.resource = resource;
    }

    public boolean appliesTo(List<Product> products) {
        Date now = new Date();
        if (now.before(startDate) || now.after(endDate)) {
            return false;
        }
        if (appliesTo == PromotionApplicationType.BASKET) {
            return true;
        }
        for (Product product : products) {
            if (productSku.equals(product.getSku())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Long getId() {
        return id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public PromotionType getType() {
        return type;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    @Override
    public Bitmap getImageResource() {
        return resource;
    }

    @Override public String getSku() {
        return sku;
    }

    @Override public Integer getQuantity() {
        return 1;
    }

    public boolean appliesToBasket() {
        return appliesTo == PromotionApplicationType.BASKET;
    }

    public boolean isPercentOff() {
        return type == PromotionType.PERCENT;
    }

    public boolean isAmountOff() {
        return type == PromotionType.AMOUNT;
    }

    public boolean isFor(Product product) {
        return appliesTo == PromotionApplicationType.SKU && productSku.equals(product.getSku());
    }

    public void use() {
        used = true;
    }

    public boolean isNotUsed() {
        return !used;
    }

    @Override public int compareTo(Promotion another) {
        if(type == another.type) {
            return another.amount.compareTo(amount);
        }
        if(type == PromotionType.PERCENT) {
            return -1;
        }
        return 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Promotion promotion = (Promotion) o;

        if (used != promotion.used) return false;
        if (amount != null ? !amount.equals(promotion.amount) : promotion.amount != null) return false;
        if (appliesTo != promotion.appliesTo) return false;
        if (endDate != null ? !endDate.equals(promotion.endDate) : promotion.endDate != null) return false;
        if (id != null ? !id.equals(promotion.id) : promotion.id != null) return false;
        if (productSku != null ? !productSku.equals(promotion.productSku) : promotion.productSku != null) return false;
        if (resource != null ? !resource.equals(promotion.resource) : promotion.resource != null) return false;
        if (sku != null ? !sku.equals(promotion.sku) : promotion.sku != null) return false;
        if (startDate != null ? !startDate.equals(promotion.startDate) : promotion.startDate != null) return false;
        if (type != promotion.type) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (sku != null ? sku.hashCode() : 0);
        result = 31 * result + (appliesTo != null ? appliesTo.hashCode() : 0);
        result = 31 * result + (productSku != null ? productSku.hashCode() : 0);
        result = 31 * result + (startDate != null ? startDate.hashCode() : 0);
        result = 31 * result + (endDate != null ? endDate.hashCode() : 0);
        result = 31 * result + (amount != null ? amount.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (resource != null ? resource.hashCode() : 0);
        result = 31 * result + (used ? 1 : 0);
        return result;
    }
}
