package aa;

import java.util.Date;

// represents a bid (in a buy order)
public class Bid {
  private int bidID;
  private String stock;
  private int price; // bid price
  private String userId; // user who made this buy order
  private Date date;
  
  public Bid(int bigID, String stock, int price, String userId,Date date) {
    this.bidID = bidID;
    this.stock = stock;
    this.price = price;
    this.userId = userId;
    this.date = date;
  }
  
  // constructor
  public Bid(String stock, int price, String userId) {
    this.stock = stock;
    this.price = price;
    this.userId = userId;
    this.date = new Date();
  }
  
  public Bid(String stock, int price, String userId,Date date) {
    this.stock = stock;
    this.price = price;
    this.userId = userId;
    this.date = date;
  }
  
  // getters
  public String getStock() {
    return stock;
  }

  public int getPrice() {
    return price;
  }

  public String getUserId() {
    return userId;
  }

  public Date getDate() {
    return date;
  }

  // toString
  public String toString() {
    return "id: " + bidID + "stock: " + stock + ", price: " + price + ", userId: " + userId + ", date: " + date;
  }
}
