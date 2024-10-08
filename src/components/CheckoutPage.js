import React, { useState } from 'react';
import './../Checkout.css';
import { useLocation, useNavigate } from "react-router-dom";
import $ from 'jquery';

const CheckoutPage = ({ username }) => {
  const location = useLocation();
  const navigate = useNavigate();
  const cartItems = location.state?.cartItems || [];
  const totalPrice = cartItems.reduce((total, item) => total + item.price, 0);
  const [deliveryOption, setDeliveryOption] = useState(null);
  const [confirmationNumber, setConfirmationNumber] = useState('');
  const [deliveryDate, setDeliveryDate] = useState('');
  const [homeAddress, setHomeAddress] = useState({
    name: '',
    street: '',
    city: '',
    state: '',
    zip: ''
  });
  const [selectedStore, setSelectedStore] = useState('');

  const handleDeliveryOptionChange = (e) => {
    setDeliveryOption(e.target.value);
  };

  const handleHomeAddressChange = (e) => {
    setHomeAddress({ ...homeAddress, [e.target.name]: e.target.value });
  };

  const convertStoreToAddress = (store) => {
    if (!store || typeof store !== 'string') {
      console.error('Invalid store value:', store);
      return {
        name: '',
        street: '',
        city: '',
        state: '',
        zip: ''
      };
    }
  
    const [name, address] = store.split(' - ');
  
    if (!address) {
      console.error('Address part missing in store value:', store);
      return {
        name: name || '',
        street: '',
        city: '',
        state: '',
        zip: ''
      };
    }
  
    const [street, cityStateZip] = address.split(', ');
    const [city, stateZip] = cityStateZip.split(', ');
    const [state, zip] = stateZip ? stateZip.split(' ') : ['', ''];
  
    return {
      name: name || '',
      street: street || '',
      city: city || '',
      state: state || '',
      zip: zip || ''
    };
  };
  

  const handleStoreChange = (e) => {
    setSelectedStore(e.target.value);
  };

  const formatDate = (isoDate) => {
    const date = new Date(isoDate);
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const year = date.getFullYear();
    return `${month}-${day}-${year}`;
  };

  const address = deliveryOption === 'homeDelivery'
    ? homeAddress
    : convertStoreToAddress(selectedStore);

  const formattedOrderPlacedDate = formatDate(new Date().toISOString());

  const completeCheckout = () => {
    console.log(formattedOrderPlacedDate);
    console.log("cartItems", cartItems);
    $.ajax({
      type: "POST",
      url: `http://localhost:8080/backend/checkout?username=${username}`,
      contentType: "application/json",
      data: JSON.stringify({
        cartItems: cartItems,
        deliveryOption: deliveryOption,
        address: address,
        orderPlacedDate: formattedOrderPlacedDate,
        status: "orderPlaced"
      }),
      success: (response) => {
        console.log("checkout method response -> ", response);
        const { confirmationNumber, deliveryDate } = response;
        setConfirmationNumber(confirmationNumber);
        setDeliveryDate(deliveryDate);
        navigate('/checkout-success', {
          state: {
            confirmationNumber,
            deliveryDate,
            deliveryOption,
            address
          }
        });
      },
      error: (xhr, status, error) => {
        console.error("POST request failed:", status, error);
        alert(`Error: ${status} - ${error}`);
      }
    });
  };
  

  return (
    <div className="checkout-page">
      <div className="form-section">
        <CustomerInfo />
        <PaymentInfo />
        <div className="section">
          <h2>Delivery Options</h2>
          <div className="input-group">
            <label>
              <input
                type="radio"
                name="deliveryOption"
                value="storePickup"
                checked={deliveryOption === 'storePickup'}
                onChange={handleDeliveryOptionChange}
              />
              Store Pickup
            </label>
            <label>
              <input
                type="radio"
                name="deliveryOption"
                value="homeDelivery"
                checked={deliveryOption === 'homeDelivery'}
                onChange={handleDeliveryOptionChange}
              />
              Home Delivery
            </label>
          </div>
        </div>

        {deliveryOption === 'storePickup' && (
          <StorePickup selectedStore={selectedStore} onStoreChange={handleStoreChange} />
        )}
        {deliveryOption === 'homeDelivery' && (
          <HomeDelivery address={homeAddress} onAddressChange={handleHomeAddressChange} />
        )}

      </div>

      <div className="cart-summary">
        <CartSummary cartItems={cartItems} totalPrice={totalPrice} />
      </div>

      <div className="checkout-button" onClick={completeCheckout}>
        <button className="checkout-btn">Complete Checkout and Pay</button>
      </div>
    </div>
  );
};

const StorePickup = ({ selectedStore, onStoreChange }) => {
  const stores = [
    'Chicago Downtown - 123 Main St, Chicago, IL',
    'Chicago West - 456 Oak St, Chicago, IL',
    'Chicago North - 789 Pine St, Chicago, IL',
    'Chicago South - 101 Maple St, Chicago, IL',
    'Chicago East - 202 Birch St, Chicago, IL',
    'Chicago Loop - 303 Cedar St, Chicago, IL',
  ];

  return (
    <div className="section">
      <h3>Pick a Store</h3>
      <select value={selectedStore} onChange={onStoreChange}>
        {stores.map((store, index) => (
          <option key={index} value={store}>
            {store}
          </option>
        ))}
      </select>
    </div>
  );
};

const HomeDelivery = ({ address, onAddressChange }) => (
  <div className="section">
    <h3>Shipping Address</h3>
    <input
      type="text"
      name="name"
      placeholder="Name"
      value={address.name}
      onChange={onAddressChange}
    />
    <input
      type="text"
      name="street"
      placeholder="Street Address"
      value={address.street}
      onChange={onAddressChange}
    />
    <div className="input-group">
      <input
        type="text"
        name="city"
        placeholder="City"
        value={address.city}
        onChange={onAddressChange}
      />
      <input
        type="text"
        name="state"
        placeholder="State"
        value={address.state}
        onChange={onAddressChange}
      />
      <input
        type="text"
        name="zip"
        placeholder="Zip Code"
        value={address.zip}
        onChange={onAddressChange}
      />
    </div>
  </div>
);

const CustomerInfo = () => (
  <div className="section">
    <h2>Customer Info</h2>
    <div className="input-group">
      <input type="text" placeholder="First Name" />
      <input type="text" placeholder="Last Name" />
    </div>
    <input type="email" placeholder="Email" />
    <input type="text" placeholder="Address" />
    <div className="input-group">
      <input type="text" placeholder="City" />
      <input type="text" placeholder="State" />
    </div>
  </div>
);

const PaymentInfo = () => {
  const [cardNumber, setCardNumber] = useState('');
  const [month, setMonth] = useState('');
  const [year, setYear] = useState('');
  const [cvc, setCVC] = useState('');

  const handleCardNumberChange = (e) => {
    const value = e.target.value;
    if (value.length <= 16) {
      setCardNumber(value);
    }
  };

  const handleMonthChange = (e) => {
    const value = e.target.value;
    if (value.length <= 2 && Number(value) >= 0 && Number(value) <= 12) {
      setMonth(value);
    }
  };

  const handleYearChange = (e) => {
    const value = e.target.value;
    if (value.length <= 4) {
      setYear(value);
    }
  };

  const handleCVCChange = (e) => {
    const value = e.target.value;
    if (value.length <= 3) {
      setCVC(value);
    }
  };

  return (
    <div className="section">
      <h2>Payment Info</h2>
      <div className="payment-methods">
        <label className="left-align">Credit Card Details</label>
      </div>
      <input
        type="number"
        placeholder="Credit Card Number"
        value={cardNumber}
        onChange={handleCardNumberChange}
        required
      />
      <div className="input-group">
        <input
          type="number"
          placeholder="MM"
          value={month}
          onChange={handleMonthChange}
          required
        />
        <input
          type="number"
          placeholder="YYYY"
          value={year}
          onChange={handleYearChange}
          required
        />
        <input
          type="number"
          placeholder="CVC"
          value={cvc}
          onChange={handleCVCChange}
          required
        />
      </div>
    </div>
  );
};

const CartSummary = ({ cartItems, totalPrice }) => (
  <div className="section">
    <h2>Current Cart</h2>
    <div className="cart-items">
      {cartItems.map((item, index) => (
        <div key={index} className="cart-item">
          <span>{item.name}</span>
          <span>${item.price}</span>
        </div>
      ))}
    </div>
    <div className="total">
      <h3>Cart Totals</h3>
      <span>Total: ${totalPrice}</span>
    </div>
  </div>
);

export default CheckoutPage;
