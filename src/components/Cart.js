import React, { useEffect, useState } from "react";
import $ from 'jquery';
import { FaTrashAlt } from 'react-icons/fa';
import './../Cart.css';
import { useNavigate } from 'react-router-dom';

const Cart = ({ username }) => {
  const [cartItems, setCartItems] = useState([]);
  const [totalPrice, setTotalPrice] = useState(0);
  const navigate = useNavigate();

  const calculateTotalPrice = (items) => {
    const total = items.reduce((sum, item) => sum + item.price, 0);
    setTotalPrice(total);
  };

  useEffect(() => {
    console.log("Fetching cart items for user:", username);
    const urlEndPoint = `http://localhost:8080/backend/cart?username=${username}`;
    $.ajax({
      type: "GET",
      url: urlEndPoint,
      contentType: "application/json",
      success: (response) => {
        console.log(response);
        if (Array.isArray(response)) {
          setCartItems(response);
          calculateTotalPrice(response);
        } else if (response && Array.isArray(response.cartItems)) {
          setCartItems(response.cartItems);
          calculateTotalPrice(response);
        } else {
          console.error('Unexpected response format:', response);
          alert('Failed to load cart items.');
        }
      },
      error: (xhr, status, error) => {
        console.error("GET request failed:", status, error);
        alert(`Error: ${status} - ${error}`);
      }
    });
  }, [username]);

  const deleteItem = (itemId, itemName) => {
    $.ajax({
      type: "POST",
      url: `http://localhost:8080/backend/cart?username=${username}`,
      contentType: "application/json",
      data: JSON.stringify({
        username: username,
        name: itemName, 
        action: "delete"
      }),
      success: (response) => {
        if (response.success) {
          const updatedItems = cartItems.filter(item => item.name !== itemName);
          setCartItems(updatedItems);
          calculateTotalPrice(updatedItems);
        } else {
          alert('Error deleting the item.');
        }
      },
      error: (xhr, status, error) => {
        console.error("Error deleting cart item:", status, error);
        alert(`Error: ${status} - ${error}`);
      }
    });
  };

  const handleCheckout = () => {
    navigate("/checkout", { state: { cartItems }});
  }

  return (
    <div className="cart-container">
      <h1>Cart Info</h1>
      {cartItems.length === 0 ? (
        <p>No items in your cart</p>
      ) : (
        <>
          <table className="product-table">
            <thead>
              <tr>
                <th>#</th>
                <th>Product Name</th>
                <th>Product Image</th>
                <th>Price</th>
                <th>Delete</th>
              </tr>
            </thead>
            <tbody>
              {cartItems.map((item) => (
                <tr key={item.id}>
                  <td>{item.id}</td>
                  <td>{item.name}</td>
                  <td><img src={item.image} alt={item.name} width="50" height="50" /></td>
                  <td>${item.price}</td>
                  <td>
                    <button className="delete-btn" onClick={() => deleteItem(item.id, item.name)}>
                      <FaTrashAlt />
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          <div className="total-price">
            <h2>Total Price: ${totalPrice.toFixed(2)}</h2>
          </div>
          <button className="checkout-btn" onClick={handleCheckout}>Checkout</button>
        </>
      )}
    </div>
  );
};

export default Cart;
