import React, { useEffect, useState } from "react";
import Carousel from "react-multi-carousel";
import './../ProductList.css';
import $ from 'jquery';
import { useNavigate } from 'react-router-dom';
import 'react-multi-carousel/lib/styles.css';

const ProductCategory = ({ category, username }) => {
  const [productList, setProductList] = useState([]);
  const [cartQuantities, setCartQuantities] = useState({});
  const navigate = useNavigate();

  const responsive = {
    desktop: {
      breakpoint: { max: 3000, min: 1024 },
      items: 1,
      partialVisibilityGutter: 40
    },
    tablet: {
      breakpoint: { max: 1024, min: 464 },
      items: 1,
      partialVisibilityGutter: 30
    },
    mobile: {
      breakpoint: { max: 464, min: 0 },
      items: 1,
      partialVisibilityGutter: 20
    }
  };

  useEffect(() => {
    $.ajax({
      type: "GET",
      url: `http://localhost:8080/backend/product?category=${category}`,
      dataType: "json",
      success: (data) => {
        console.log(data);
        setProductList(data);
      },
      error: (xhr, status, error) => {
        console.error("Error fetching product data:", status, error);
      }
    });
  }, [category]);

  const handleKnowAboutMeClick = (id) => {
    navigate(`/product/${id}`, { state: { productList } });
  };

  const addToCart = (product) => {
    const currentQuantity = cartQuantities[product.id] || 0; 
    const newQuantity = currentQuantity + 1;

    const cartItem = {
      id: product.id,
      name: product.name,
      image: product.imageUrl,
      price: product.price,
      quantity: newQuantity
    };
  
    $.ajax({
      type: "POST",
      url: `http://localhost:8080/backend/cart?username=${username}`,
      contentType: "application/json",
      data: JSON.stringify({ ...cartItem, action: "add" }),
      success: (response) => {
        if (response.success) {
          alert("Item added to cart successfully!");
        } else {
          alert("Failed to add item to cart.");
        }
      },
      error: (xhr, status, error) => {
        console.error("Error adding to cart:", status, error);
        alert(`Error: ${status} - ${error}`);
      }
    });
  };

  return (
    <div className="product-list">
      {productList.map((product, index) => (
        
        <div key={product.id} className="product-card">
          <div className="image-slider">
            <Carousel infinite showDots={false} responsive={responsive}>
              <img style={{ height: "210px", width: "100%" }} src={product.imageUrl} alt={product.name} />
              <img style={{ height: "210px", width: "100%" }} src={product.imageUrl2} alt={product.name} />
            </Carousel>
          </div>
          <h2>{product.name}</h2>
          <p>{product.description}</p>
          {index != 0 && <p>${product.price}</p>}
          {index == 0 && <s>${Number(product.price) + 10}</s>}
          {index == 0 && 
            <p>Discount: ${Number(product.price)}</p>
          }
          <button
            className="know-about-me-button"
            onClick={() => handleKnowAboutMeClick(product.id)}
          >
            Know About Me
          </button>
          <br />
          <button className="add-to-cart-button" onClick={() => addToCart(product)}>Add to Cart (Quantity: {cartQuantities[product.id] || 0})</button>
        </div>
      ))}
    </div>
  );
};

export default ProductCategory;
