import React, { useEffect, useState } from 'react';
import $ from 'jquery';
import { useLocation } from 'react-router-dom';
import './../Orders.css';

const OrdersPage = () => {
  const location = useLocation();
  const { username } = location.state || {};
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const currentDate = new Date();

  useEffect(() => {
    if(orders.length > 0) {
    }
  }, [orders]); 

  useEffect(() => {
    $.ajax({
      type: "GET",
      url: `http://localhost:8080/backend/order?username=${username}`,
      contentType: "application/json",
      success: (response) => {
        console.log(response);
        setOrders(response.orders || []);
        setLoading(false);
      },
      error: (xhr, status, error) => {
        console.error("GET request failed:", status, error);
        setOrders([]);
        setLoading(false);
      }
    });
  }, [username]);

  const handleCancelOrder = (confirmationNumber) => {
    $.ajax({
      type: "POST",
      url: 'http://localhost:8080/backend/order?action=cancelOrder',
      contentType: "application/json",
      data: JSON.stringify({ action: "cancelOrder", confirmationNumber,  }),
      success: (response) => {
        if (response.success) {
          setOrders(orders.filter(order => order.confirmationNumber !== confirmationNumber));
          alert('Order canceled successfully.');
        } else {
          alert('Error canceling order.');
        }
      },
      error: (xhr, status, error) => {
        console.error("Error canceling order:", status, error);
        alert(`Error: ${status} - ${error}`);
      }
    });
  };

  

  if (loading) {
    return <p className="loading">Loading...</p>;
  }

  return (
    <div>
      <h1 style={{ textAlign: "center", margin: "20px" }}>Your Orders</h1>
      <div style={{ textAlign: "center", margin: "20px" }}>
        {orders.length ? (
          <table className="order-table">
            <thead>
              <tr>
                <th>Confirmation Number</th>
                <th>Delivery Date</th>
                <th>Order Placed Date</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {orders.map(order => {
                const specificDateString = order.deliveryDate;
                const [month, day, year] = specificDateString.split('-').map(Number);
                const specificDate = new Date(year, month - 1, day);

                const specificDateString1 = order.orderPlacedDate;
                const [month1, day1, year1] = specificDateString1.split('-').map(Number);
                const specificDate1 = new Date(year1, month1 - 1, day1);

                const timeDifference = specificDate - specificDate1;

                const daysDifference = Math.ceil(timeDifference / (1000 * 60 * 60 * 24));

                console.log(`Difference in days: ${daysDifference}`);
                
                return <tr key={order.confirmationNumber}>
                  <td>{order.confirmationNumber}</td>
                  <td>{order.deliveryDate}</td>
                  <td>{order.orderPlacedDate}</td>
                  <td>{order.status === "orderPlaced" ? "Order Placed" : "Order Canceled"}</td>
                  <td>
                    {order.status === "orderPlaced" && daysDifference > 5 && (
                      <button onClick={() => handleCancelOrder(order.confirmationNumber)}>
                        Cancel
                      </button>
                    )}
                  </td>
                </tr>
              })}
            </tbody>
          </table>
        ) : (
          <p>No orders found.</p>
        )}
      </div>
    </div>
  );
};

export default OrdersPage;
