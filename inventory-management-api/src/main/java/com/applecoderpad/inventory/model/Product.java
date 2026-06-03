package com.applecoderpad.inventory.model;

import com.applecoderpad.inventory.exception.ConflictException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "products")
public class Product {
  @Id private String sku;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private int reorderThreshold;

  @Column(nullable = false)
  private int onHand;

  @Column(nullable = false)
  private int reserved;

  @Column(name = "entity_version", nullable = false)
  private long version;

  @Column(nullable = false)
  private Instant updatedAt;

  protected Product() {}

  private Product(String sku, String name, int onHand, int reorderThreshold) {
    this.sku = sku;
    this.name = name;
    this.onHand = onHand;
    this.reorderThreshold = reorderThreshold;
    this.updatedAt = Instant.now();
  }

  public static Product create(String sku, String name, int onHand, int reorderThreshold) {
    return new Product(sku, name, onHand, reorderThreshold);
  }

  public synchronized void adjust(int delta) {
    if (onHand + delta < reserved)
      throw new ConflictException("adjustment would put on-hand below reserved stock");
    onHand += delta;
    touch();
  }

  public synchronized void reserve(int qty) {
    if (available() < qty) throw new ConflictException("insufficient available stock");
    reserved += qty;
    touch();
  }

  public synchronized void release(int qty) {
    reserved = Math.max(0, reserved - qty);
    touch();
  }

  public synchronized void touch() {
    version++;
    updatedAt = Instant.now();
  }

  public synchronized int available() {
    return onHand - reserved;
  }

  public String sku() {
    return sku;
  }

  public String name() {
    return name;
  }

  public synchronized int onHand() {
    return onHand;
  }

  public synchronized int reserved() {
    return reserved;
  }

  public int reorderThreshold() {
    return reorderThreshold;
  }

  public synchronized long version() {
    return version;
  }

  public synchronized Instant updatedAt() {
    return updatedAt;
  }
}
