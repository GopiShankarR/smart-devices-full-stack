import React from "react";
import { HashLink as Link } from 'react-router-hash-link';
import { useNavigate } from 'react-router-dom';
import './../Header.css';

const Header = ({ isLoggedIn, userType, username, onLogout }) => {
  const navigate = useNavigate();

  const handleCart = () => {
    console.log(username);
    navigate("/cart", { state: { username } });
  }

  const handleViewOrders = () => {
    navigate(`/orders/${username}`, { state: { username }});
  };

  const logoutFunction = () => {
    onLogout();
    navigate("/");
  }

  const handleTrending = () => {
    navigate("/trending");
  }

  return (
    <header className="header">
       <div className="left-section">
        <Link smooth to="/home" className="logo">SmartHomes</Link>
        {isLoggedIn && (
          <nav className="nav-links">
            <div className="nav-item">
              <Link smooth to="/doorbells">Doorbells</Link>
            </div>

            <div className="nav-item">
              <Link smooth to="/door-locks">Door Locks</Link>
            </div>

            <div className="nav-item">
              <Link smooth to="/lightings">Lightings</Link>
            </div>
            
            <div className="nav-item">
              <Link smooth to="/speakers">Speakers</Link>
            </div>

            <div className="nav-item">
              <Link smooth to="/climate-control">Thermostats</Link>
            </div>

            <div className="nav-item trending">
              <Link smooth to="/trending">Trending</Link>
            </div>
          </nav>
        )}
      </div>
      {isLoggedIn ? (
        <div className="header-actions">
          {userType === "Customer" && (
            <>
              <button onClick={handleCart} className="cart-button">Cart</button>
              <button onClick={handleViewOrders} className="view-orders-button">View Orders</button>
            </>
          )}
          <button className="login-link" onClick={logoutFunction}>Logout</button>
        </div>
      ) : (
        <Link to="/" className="login-link">Login</Link>
      )}
    </header>
  )
};

export default Header;