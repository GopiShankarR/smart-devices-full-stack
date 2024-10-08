import java.io.Serializable;

public class ProductInfo implements Serializable{
	private String id;
	private String name;
	private double price;	
	private String image;
	private String retailer;
	private String productType;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public String getRetailer() {
		return retailer;
	}

	public void setRetailer(String retailer) {
		this.retailer = retailer;
	}

	public ProductInfo(String id, String name, double price, String image, String retailer, String productType) {
		super();
		this.id = id;
		this.name = name;
		this.price = price;
		this.retailer = retailer;
		this.image = image;
		this.productType = productType;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getProductType() {
		return productType;
	}
	public void setProductType(String productType) {
		this.productType = productType;
	}

	@Override
	public String toString() {
		return "ProductInfo [id=" + id + ", name=" + name + ", price=" + price + ", image=" + image + ", retailer="
				+ retailer + ", productType=" + productType + "]";
	}
}
