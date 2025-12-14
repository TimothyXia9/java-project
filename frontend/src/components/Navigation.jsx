import React from 'react';
import { Link } from 'react-router-dom';

function Navigation({ onLogout }) {
  return (
    <nav style={styles.nav}>
      <div style={styles.container}>
        <h1 style={styles.logo}>Nutrition Tracker</h1>
        <div style={styles.links}>
          <Link to="/dashboard" style={styles.link}>Dashboard</Link>
          <Link to="/add-meal" style={styles.link}>Add Meal</Link>
          <Link to="/profile" style={styles.link}>Profile</Link>
          <button onClick={onLogout} style={styles.logoutBtn}>Logout</button>
        </div>
      </div>
    </nav>
  );
}

const styles = {
  nav: {
    backgroundColor: '#333',
    color: 'white',
    padding: '15px 0'
  },
  container: {
    maxWidth: '1200px',
    margin: '0 auto',
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    padding: '0 20px'
  },
  logo: {
    fontSize: '24px',
    fontWeight: 'bold'
  },
  links: {
    display: 'flex',
    gap: '20px',
    alignItems: 'center'
  },
  link: {
    color: 'white',
    textDecoration: 'none',
    fontSize: '16px'
  },
  logoutBtn: {
    backgroundColor: '#f44336'
  }
};

export default Navigation;
