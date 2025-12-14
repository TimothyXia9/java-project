import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { mealService, foodService, imageService, barcodeService } from '../services/api';

function AddMeal() {
  const [mealData, setMealData] = useState({
    mealType: 'BREAKFAST',
    mealDate: new Date().toISOString().split('T')[0],
    notes: '',
    foods: []
  });
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState([]);
  const [barcode, setBarcode] = useState('');
  const [imageFile, setImageFile] = useState(null);
  const [recognizedFoods, setRecognizedFoods] = useState([]);
  const [message, setMessage] = useState('');
  const [isAnalyzing, setIsAnalyzing] = useState(false);
  const navigate = useNavigate();

  const handleMealChange = (e) => {
    setMealData({
      ...mealData,
      [e.target.name]: e.target.value
    });
  };

  const searchFoods = async () => {
    if (!searchQuery) return;
    try {
      const response = await foodService.searchFoods(searchQuery);
      setSearchResults(response.data);
    } catch (error) {
      console.error('Search failed:', error);
    }
  };

  const searchRecognizedFood = async (foodName) => {
    setSearchQuery(foodName);
    try {
      const response = await foodService.searchFoods(foodName);
      setSearchResults(response.data);
      setMessage(`Found ${response.data.length} results for "${foodName}"`);
    } catch (error) {
      setMessage(`Failed to search for "${foodName}"`);
    }
  };

  const scanBarcode = async () => {
    if (!barcode) return;
    try {
      const response = await barcodeService.getFoodByBarcode(barcode);
      if (response.data) {
        setSearchResults([response.data]);
      } else {
        setMessage('Food not found for this barcode');
      }
    } catch (error) {
      setMessage('Failed to scan barcode');
    }
  };

  const handleImageUpload = async () => {
    if (!imageFile) return;
    setIsAnalyzing(true);
    setMessage('');
    try {
      const response = await imageService.analyzeImage(imageFile);
      // Parse JSON response
      try {
        const parsedFoods = JSON.parse(response.data);
        setRecognizedFoods(parsedFoods);
        setMessage(`Successfully recognized ${parsedFoods.length} food items!`);
      } catch (parseError) {
        // If not JSON, show raw text
        setMessage('Image analyzed, but response format is unexpected.');
        console.log('Response:', response.data);
      }
    } catch (error) {
      const errorMsg = error.response?.data || error.message;
      setMessage(typeof errorMsg === 'string' ? errorMsg : 'Failed to analyze image');
    } finally {
      setIsAnalyzing(false);
    }
  };

  const addFoodToMeal = (food) => {
    const existingFood = mealData.foods.find(f => f.foodId === food.id);
    if (existingFood) {
      setMessage('This food is already added to the meal');
      return;
    }

    setMealData({
      ...mealData,
      foods: [...mealData.foods, {
        foodId: food.id,
        foodName: food.name,
        quantity: 1,
        quantityUnit: 'serving',
        servings: 1
      }]
    });
    setMessage('Food added to meal');
  };

  const updateFoodQuantity = (index, field, value) => {
    const updatedFoods = [...mealData.foods];
    updatedFoods[index][field] = parseFloat(value) || 0;
    setMealData({ ...mealData, foods: updatedFoods });
  };

  const removeFoodFromMeal = (index) => {
    const updatedFoods = mealData.foods.filter((_, i) => i !== index);
    setMealData({ ...mealData, foods: updatedFoods });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (mealData.foods.length === 0) {
      setMessage('Please add at least one food item');
      return;
    }

    try {
      await mealService.createMeal(mealData);
      setMessage('Meal created successfully!');
      setTimeout(() => navigate('/dashboard'), 1500);
    } catch (error) {
      setMessage('Failed to create meal');
    }
  };

  return (
    <div className="container">
      <h1>Add Meal</h1>

      <div className="card">
        <h3>Meal Details</h3>
        <div>
          <label>Meal Type:</label>
          <select name="mealType" value={mealData.mealType} onChange={handleMealChange}>
            <option value="BREAKFAST">Breakfast</option>
            <option value="LUNCH">Lunch</option>
            <option value="DINNER">Dinner</option>
            <option value="SNACK">Snack</option>
          </select>
        </div>

        <div>
          <label>Date:</label>
          <input
            type="date"
            name="mealDate"
            value={mealData.mealDate}
            onChange={handleMealChange}
          />
        </div>

        <div>
          <label>Notes:</label>
          <input
            type="text"
            name="notes"
            value={mealData.notes}
            onChange={handleMealChange}
            placeholder="Optional notes"
          />
        </div>
      </div>

      {/* Image Recognition Section */}
      <div className="card" style={{ backgroundColor: '#fff8e1', borderLeft: '4px solid #FF9800' }}>
        <h3 style={{ color: '#FF9800', marginTop: 0 }}>AI Image Recognition</h3>
        <div style={{ marginBottom: '15px' }}>
          <label style={{ fontWeight: 'bold' }}>Upload Food Image:</label>
          <input
            type="file"
            accept="image/*"
            onChange={(e) => setImageFile(e.target.files[0])}
            style={{ marginTop: '8px' }}
          />
          <button
            onClick={handleImageUpload}
            disabled={!imageFile || isAnalyzing}
            style={{
              backgroundColor: isAnalyzing ? '#ccc' : '#FF9800',
              marginTop: '10px',
              cursor: isAnalyzing ? 'not-allowed' : 'pointer'
            }}
          >
            {isAnalyzing ? 'Analyzing...' : 'Analyze Image with AI'}
          </button>
        </div>

        {recognizedFoods.length > 0 && (
          <div>
            <h4 style={{ color: '#333', marginBottom: '10px' }}>Recognized Foods:</h4>
            <div style={{ display: 'grid', gap: '10px' }}>
              {recognizedFoods.map((food, index) => (
                <div
                  key={index}
                  style={{
                    padding: '15px',
                    backgroundColor: 'white',
                    border: '1px solid #FFB74D',
                    borderRadius: '8px',
                    boxShadow: '0 2px 4px rgba(0,0,0,0.1)'
                  }}
                >
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                    <div style={{ flex: 1 }}>
                      <div style={{ fontSize: '18px', fontWeight: 'bold', color: '#333', marginBottom: '8px' }}>
                        {food.foodName}
                      </div>
                      <div style={{ fontSize: '14px', color: '#666', display: 'flex', alignItems: 'center', gap: '5px' }}>
                        <span style={{
                          backgroundColor: '#FF9800',
                          color: 'white',
                          padding: '2px 8px',
                          borderRadius: '12px',
                          fontSize: '13px',
                          fontWeight: 'bold'
                        }}>
                          {food.estimatedPortion} {food.portionUnit}
                        </span>
                        <span>estimated portion</span>
                      </div>
                    </div>
                    <button
                      onClick={() => searchRecognizedFood(food.foodName)}
                      style={{
                        backgroundColor: '#4CAF50',
                        padding: '8px 16px',
                        fontSize: '14px',
                        whiteSpace: 'nowrap'
                      }}
                    >
                      Search & Add
                    </button>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>

      {/* Search Section */}
      <div className="card">
        <h3>Search Foods</h3>
        <div style={{ display: 'flex', gap: '10px', marginBottom: '15px' }}>
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder="Search food name"
            onKeyPress={(e) => e.key === 'Enter' && searchFoods()}
          />
          <button onClick={searchFoods}>Search</button>
        </div>

        <div style={{ display: 'flex', gap: '10px', marginBottom: '15px' }}>
          <input
            type="text"
            value={barcode}
            onChange={(e) => setBarcode(e.target.value)}
            placeholder="Enter barcode"
            onKeyPress={(e) => e.key === 'Enter' && scanBarcode()}
          />
          <button onClick={scanBarcode} style={{ backgroundColor: '#2196F3' }}>Scan Barcode</button>
        </div>

        {searchResults.length > 0 && (
          <div>
            <h4>Search Results:</h4>
            {searchResults.map(food => (
              <div key={food.id} style={{
                padding: '12px',
                border: '1px solid #ddd',
                marginBottom: '10px',
                borderRadius: '4px',
                backgroundColor: '#fafafa'
              }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <div>
                    <strong style={{ fontSize: '16px' }}>{food.name}</strong>
                    <div style={{ fontSize: '14px', color: '#666', marginTop: '4px' }}>
                      {food.calories} kcal per {food.servingSize}{food.servingUnit}
                    </div>
                  </div>
                  <button onClick={() => addFoodToMeal(food)} style={{ backgroundColor: '#4CAF50' }}>
                    Add to Meal
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Foods in Meal */}
      <div className="card">
        <h3>Foods in This Meal</h3>
        {mealData.foods.length === 0 ? (
          <p style={{ color: '#999', fontStyle: 'italic' }}>No foods added yet</p>
        ) : (
          mealData.foods.map((food, index) => (
            <div key={index} style={{
              padding: '12px',
              border: '1px solid #4CAF50',
              marginBottom: '10px',
              borderRadius: '4px',
              backgroundColor: '#f1f8f4'
            }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <strong style={{ fontSize: '16px' }}>{food.foodName}</strong>
                <button onClick={() => removeFoodFromMeal(index)} style={{ backgroundColor: '#f44336' }}>
                  Remove
                </button>
              </div>
              <div style={{ display: 'flex', gap: '10px', marginTop: '10px', alignItems: 'center' }}>
                <input
                  type="number"
                  step="0.1"
                  value={food.servings}
                  onChange={(e) => updateFoodQuantity(index, 'servings', e.target.value)}
                  placeholder="Servings"
                  style={{ width: '100px' }}
                />
                <span>servings</span>
              </div>
            </div>
          ))
        )}
      </div>

      {message && (
        <div
          className={message.includes('success') || message.includes('Successfully') ? 'success' : 'error'}
          style={{ marginBottom: '15px' }}
        >
          {message}
        </div>
      )}

      <button onClick={handleSubmit} style={{ width: '100%', padding: '15px', fontSize: '18px' }}>
        Create Meal
      </button>
    </div>
  );
}

export default AddMeal;
