import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { mealService } from '../services/api';

function Dashboard() {
  const [meals, setMeals] = useState([]);
  const [selectedDate, setSelectedDate] = useState(new Date().toISOString().split('T')[0]);
  const [loading, setLoading] = useState(false);
  const [totalCalories, setTotalCalories] = useState(0);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    // Check if user is logged in
    const token = localStorage.getItem('token');
    if (!token) {
      navigate('/login');
      return;
    }
    loadMeals();
  }, [selectedDate, navigate]);

  const loadMeals = async () => {
    setLoading(true);
    setError('');
    try {
      const response = await mealService.getMealsByDate(selectedDate);
      console.log('API Response:', response.data); // Debug log

      // Ensure response.data is an array
      const mealData = Array.isArray(response.data) ? response.data : [];
      setMeals(mealData);
      calculateTotalCalories(mealData);
    } catch (error) {
      console.error('Failed to load meals:', error);

      // Handle authentication errors
      if (error.response?.status === 401 || error.response?.status === 403) {
        localStorage.removeItem('token');
        navigate('/login');
        return;
      }

      // Set error message for user
      const errorMsg = error.response?.data?.message || error.message || 'Failed to load meals';
      setError(errorMsg);

      // Set empty array on error
      setMeals([]);
      setTotalCalories(0);
    } finally {
      setLoading(false);
    }
  };

  const calculateTotalCalories = (mealList) => {
    // Validate input is an array
    if (!Array.isArray(mealList)) {
      setTotalCalories(0);
      return;
    }

    let total = 0;
    mealList.forEach(meal => {
      // Check if meal has mealFoods array
      if (meal.mealFoods && Array.isArray(meal.mealFoods)) {
        meal.mealFoods.forEach(mf => {
          if (mf.food && mf.food.calories) {
            const servings = mf.servings || 1;
            total += mf.food.calories * servings;
          }
        });
      }
    });
    setTotalCalories(Math.round(total));
  };

  const handleDeleteMeal = async (id) => {
    if (window.confirm('Are you sure you want to delete this meal?')) {
      try {
        await mealService.deleteMeal(id);
        loadMeals();
      } catch (error) {
        console.error('Failed to delete meal:', error);
      }
    }
  };

  return (
    <div className="container">
      <h1>Dashboard</h1>

      <div className="card">
        <h2>Daily Summary</h2>
        <div>
          <label>Date: </label>
          <input
            type="date"
            value={selectedDate}
            onChange={(e) => setSelectedDate(e.target.value)}
          />
        </div>
        <h3 style={{ marginTop: '20px' }}>Total Calories: {totalCalories} kcal</h3>
      </div>

      {error && (
        <div className="error" style={{ marginBottom: '15px' }}>
          {error}
        </div>
      )}

      {loading ? (
        <p>Loading...</p>
      ) : meals.length === 0 ? (
        <div className="card">
          <p>No meals logged for this date.</p>
        </div>
      ) : (
        meals.map(meal => {
          // Calculate meal totals
          let mealCalories = 0;
          let mealProtein = 0;
          let mealCarbs = 0;
          let mealFat = 0;
          let mealFiber = 0;

          if (meal.mealFoods && Array.isArray(meal.mealFoods)) {
            meal.mealFoods.forEach(mf => {
              const servings = mf.servings || 1;
              mealCalories += (mf.food?.calories || 0) * servings;
              mealProtein += (mf.food?.protein || 0) * servings;
              mealCarbs += (mf.food?.carbohydrates || 0) * servings;
              mealFat += (mf.food?.fat || 0) * servings;
              mealFiber += (mf.food?.fiber || 0) * servings;
            });
          }

          return (
            <div key={meal.id} className="card" style={{
              borderLeft: '4px solid #4CAF50',
              backgroundColor: '#f9f9f9'
            }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '15px' }}>
                <div>
                  <h3 style={{ margin: 0, color: '#2c3e50' }}>{meal.mealType}</h3>
                  {meal.notes && <p style={{ margin: '5px 0 0 0', fontSize: '14px', color: '#7f8c8d' }}><em>{meal.notes}</em></p>}
                </div>
                <button onClick={() => handleDeleteMeal(meal.id)} style={{
                  backgroundColor: '#f44336',
                  padding: '8px 16px',
                  fontSize: '14px'
                }}>
                  Delete
                </button>
              </div>

              {/* Meal Nutrition Summary */}
              <div style={{
                display: 'grid',
                gridTemplateColumns: 'repeat(auto-fit, minmax(100px, 1fr))',
                gap: '10px',
                marginBottom: '15px',
                padding: '15px',
                backgroundColor: 'white',
                borderRadius: '8px',
                border: '1px solid #e0e0e0'
              }}>
                <div style={{ textAlign: 'center' }}>
                  <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#ff6b6b' }}>
                    {Math.round(mealCalories)}
                  </div>
                  <div style={{ fontSize: '12px', color: '#666', textTransform: 'uppercase' }}>
                    Calories
                  </div>
                </div>
                {mealProtein > 0 && (
                  <div style={{ textAlign: 'center' }}>
                    <div style={{ fontSize: '20px', fontWeight: 'bold', color: '#28a745' }}>
                      {mealProtein.toFixed(1)}g
                    </div>
                    <div style={{ fontSize: '12px', color: '#666', textTransform: 'uppercase' }}>
                      Protein
                    </div>
                  </div>
                )}
                {mealCarbs > 0 && (
                  <div style={{ textAlign: 'center' }}>
                    <div style={{ fontSize: '20px', fontWeight: 'bold', color: '#0d6efd' }}>
                      {mealCarbs.toFixed(1)}g
                    </div>
                    <div style={{ fontSize: '12px', color: '#666', textTransform: 'uppercase' }}>
                      Carbs
                    </div>
                  </div>
                )}
                {mealFat > 0 && (
                  <div style={{ textAlign: 'center' }}>
                    <div style={{ fontSize: '20px', fontWeight: 'bold', color: '#ff9800' }}>
                      {mealFat.toFixed(1)}g
                    </div>
                    <div style={{ fontSize: '12px', color: '#666', textTransform: 'uppercase' }}>
                      Fat
                    </div>
                  </div>
                )}
                {mealFiber > 0 && (
                  <div style={{ textAlign: 'center' }}>
                    <div style={{ fontSize: '20px', fontWeight: 'bold', color: '#dc3545' }}>
                      {mealFiber.toFixed(1)}g
                    </div>
                    <div style={{ fontSize: '12px', color: '#666', textTransform: 'uppercase' }}>
                      Fiber
                    </div>
                  </div>
                )}
              </div>

              {/* Food Items List */}
              <div>
                <h4 style={{ marginTop: 0, marginBottom: '10px', color: '#555', fontSize: '16px' }}>
                  Foods in this meal:
                </h4>
                {meal.mealFoods && Array.isArray(meal.mealFoods) && meal.mealFoods.map(mf => {
                  const servings = mf.servings || 1;
                  return (
                    <div key={mf.id} style={{
                      padding: '12px',
                      marginBottom: '8px',
                      backgroundColor: 'white',
                      borderRadius: '6px',
                      border: '1px solid #e8e8e8',
                      display: 'flex',
                      justifyContent: 'space-between',
                      alignItems: 'center'
                    }}>
                      <div style={{ flex: 1 }}>
                        <div style={{ fontWeight: 'bold', color: '#333', marginBottom: '4px' }}>
                          {mf.food?.name || 'Unknown food'}
                        </div>
                        <div style={{ fontSize: '14px', color: '#666' }}>
                          {mf.quantity || 100}g
                          {mf.food?.brand && (
                            <span style={{
                              marginLeft: '8px',
                              fontSize: '12px',
                              backgroundColor: '#e3f2fd',
                              color: '#1976d2',
                              padding: '2px 6px',
                              borderRadius: '10px'
                            }}>
                              {mf.food.brand}
                            </span>
                          )}
                        </div>
                      </div>
                      <div style={{
                        textAlign: 'right',
                        display: 'flex',
                        gap: '15px',
                        fontSize: '13px',
                        color: '#666'
                      }}>
                        <div>
                          <strong style={{ color: '#ff6b6b' }}>
                            {Math.round((mf.food?.calories || 0) * servings)}
                          </strong> kcal
                        </div>
                        {mf.food?.protein > 0 && (
                          <div>
                            P: <strong>{((mf.food?.protein || 0) * servings).toFixed(1)}g</strong>
                          </div>
                        )}
                        {mf.food?.carbohydrates > 0 && (
                          <div>
                            C: <strong>{((mf.food?.carbohydrates || 0) * servings).toFixed(1)}g</strong>
                          </div>
                        )}
                        {mf.food?.fat > 0 && (
                          <div>
                            F: <strong>{((mf.food?.fat || 0) * servings).toFixed(1)}g</strong>
                          </div>
                        )}
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>
          );
        })
      )}
    </div>
  );
}

export default Dashboard;
