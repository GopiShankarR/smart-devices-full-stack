import React, { useState } from 'react';
import $ from 'jquery';
import { useNavigate } from 'react-router-dom';

const AddOrder = () => {
  const [newOrder, setNewOrder] = useState({ username: '', confirmationNumber: '', deliveryDate: '', items: '' });
  const navigate = useNavigate();

  const handleAddOrder = () => {
    $.ajax({
      type: "POST",
      url: 'http://localhost:8080/backend/salesman',
      contentType: "application/json",
      data: JSON.stringify({ action: "add", ...newOrder }),
      success: (response) => {
        if (response.success) {
          alert('Order added successfully.');
          navigate('/salesman');
        } else {
          alert('Error adding order.');
        }
      },
      error: (xhr, status, error) => {
        console.error("Error adding order:", status, error);
        alert(`Error: ${status} - ${error}`);
      }
    });
  };

  return (
    <div style={{ textAlign: "center", margin: "20px" }}>
      <h2>Add Order</h2>
      <input
        type="text"
        placeholder="Customer Username"
        value={newOrder.username}
        onChange={(e) => setNewOrder({ ...newOrder, username: e.target.value })}
      />
      <input
        type="text"
        placeholder="Confirmation Number"
        value={newOrder.confirmationNumber}
        onChange={(e) => setNewOrder({ ...newOrder, confirmationNumber: e.target.value })}
      />
      <input
        type="text"
        placeholder="Delivery Date"
        value={newOrder.deliveryDate}
        onChange={(e) => setNewOrder({ ...newOrder, deliveryDate: e.target.value })}
      />
      <input
        type="text"
        placeholder="Items (comma separated)"
        value={newOrder.items}
        onChange={(e) => setNewOrder({ ...newOrder, items: e.target.value })}
      />
      <button onClick={handleAddOrder}>Add Order</button>
    </div>
  );
};

export default AddOrder;
