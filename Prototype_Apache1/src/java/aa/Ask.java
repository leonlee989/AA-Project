package aa;

import java.util.Date;

// represents an Ask (in a sell order)
public class Ask {
  private int askID;
  private String stock;
  private int price; // ask price
  private String userId; // user who made this sell order
  private Date date;
  
  public Ask(int id, String stock,int price,String userId,Date date){
      this.askID = id;
      this.stock = stock;
      this.price = price;
      this.userId = userId;
      this.date = date;
  }
  
  // constructor
  public Ask(String stock, int price, String userId) {
    this.stock = stock;
    this.price = price;
    this.userId = userId;
    this.date = new Date();
  }
  
  public Ask(String stock,int price,String userId,Date date){
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
  
  public int getID(){
      return askID;
  }
  
  public void setID(int i){
      this.askID = i;
  }

  // toString
  public String toString() {
    return "id: " + askID + ", stock: " + stock + ", price: " + price + ", userId: " + userId + ", date: " + date;
  }
}