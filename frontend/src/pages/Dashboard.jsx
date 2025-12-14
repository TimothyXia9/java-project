import React, { useState, useEffect } from 'react';
import { mealService } from '../services/api';

function Dashboard() {
  const [meals, setMeals] = useState([]);
  const [selectedDate, setSelectedDate] = useState(new Date().toISOString().split('T')[0]);
  const [loading, setLoading] = useState(false);
  const [totalCalories, setTotalCalories] = useState(0);

  useEffect(() => {
    loadMeals();
  }, [selectedDate]);

  const loadMeals = async () => {
    setLoading(true);
    try {
      const response = await mealService.getMealsByDate(selectedDate);
      setMeals(response.data);
      calculateTotalCalories(response.data);
    } catch (error) {
      console.error('Failed to load meals:', error);
    } finally {
      setLoading(false);
    }
  };

  const calculateTotalCalories = (mealList) => {
    let total = 0;
    mealList.forEach(meal => {
      meal.mealFoods.forEach(mf => {
        const servings = mf.servings || 1;
        total += mf.food.calories * servings;
      });
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

      {loading ? (
        <p>Loading...</p>
      ) : meals.length === 0 ? (
        <div className="card">
          <p>No meals logged for this date.</p>
        </div>
      ) : (
        meals.map(meal => (
          <div key={meal.id} className="card">
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <h3>{meal.mealType}</h3>
              <button onClick={() => handleDeleteMeal(meal.id)} style={{ backgroundColor: '#f44336' }}>
                Delete
              </button>
            </div>
            {meal.notes && <p><em>{meal.notes}</em></p>}
            <ul>
              {meal.mealFoods.map(mf => (
                <li key={mf.id}>
                  {mf.food.name} - {mf.servings || 1} serving(s)
                  ({Math.round(mf.food.calories * (mf.servings || 1))} kcal)
                </li>
              ))}
            </ul>
          </div>
        ))
      )}
    </div>
  );
}

export default Dashboard;
