import React, { useState, useEffect } from 'react';
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
  const [imagePreview, setImagePreview] = useState(null);
  const [recognizedFoods, setRecognizedFoods] = useState([]);
  const [currentRecognizedFood, setCurrentRecognizedFood] = useState(null);
  const [message, setMessage] = useState('');
  const [isAnalyzing, setIsAnalyzing] = useState(false);
  const navigate = useNavigate();

  // Cleanup preview URL on component unmount
  useEffect(() => {
    return () => {
      if (imagePreview) {
        URL.revokeObjectURL(imagePreview);
      }
    };
  }, [imagePreview]);

  const handleMealChange = (e) => {
    setMealData({
      ...mealData,
      [e.target.name]: e.target.value
    });
  };

  const searchFoods = async () => {
    if (!searchQuery) return;
    setCurrentRecognizedFood(null); // Clear recognized food when doing manual search
    try {
      const response = await foodService.searchFoods(searchQuery);
      setSearchResults(response.data);
    } catch (error) {
      console.error('Search failed:', error);
    }
  };

  const searchRecognizedFood = async (recognizedFood) => {
    setSearchQuery(recognizedFood.foodName);
    setCurrentRecognizedFood(recognizedFood); // Save the recognized food with its weight
    try {
      const response = await foodService.searchFoods(recognizedFood.foodName);
      setSearchResults(response.data);
      setMessage(`Found ${response.data.length} results for "${recognizedFood.foodName}" (${recognizedFood.estimatedPortion}g from image)`);
    } catch (error) {
      setMessage(`Failed to search for "${recognizedFood.foodName}"`);
    }
  };

  const scanBarcode = async () => {
    if (!barcode) return;
    setCurrentRecognizedFood(null); // Clear recognized food when scanning barcode
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

  const handleImageFileChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      setImageFile(file);
      // Create preview URL
      const previewUrl = URL.createObjectURL(file);
      setImagePreview(previewUrl);
      // Clear previous results
      setRecognizedFoods([]);
      setMessage('');
    }
  };

  const handleImageUpload = async () => {
    if (!imageFile) return;
    setIsAnalyzing(true);
    setMessage('');
    try {
      const response = await imageService.analyzeImage(imageFile);
      // Check if response.data is already an object (axios auto-parses JSON)
      let parsedFoods;
      if (typeof response.data === 'string') {
        // If it's a string, parse it
        parsedFoods = JSON.parse(response.data);
      } else if (Array.isArray(response.data)) {
        // If it's already an array, use it directly
        parsedFoods = response.data;
      } else {
        throw new Error('Unexpected response format');
      }

      setRecognizedFoods(parsedFoods);
      setMessage(`Successfully recognized ${parsedFoods.length} food items!`);
    } catch (error) {
      const errorMsg = error.response?.data || error.message;
      setMessage(typeof errorMsg === 'string' ? errorMsg : 'Failed to analyze image');
      console.error('Image analysis error:', error);
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

    // Calculate servings based on recognized weight (if available)
    // All foods are normalized to 100g, so if recognized portion is 150g, servings = 1.5
    let servings = 1;
    let weightInfo = '';

    if (currentRecognizedFood && currentRecognizedFood.estimatedPortion) {
      servings = currentRecognizedFood.estimatedPortion / 100;
      weightInfo = ` (${currentRecognizedFood.estimatedPortion}g from image)`;
    }

    setMealData({
      ...mealData,
      foods: [...mealData.foods, {
        foodId: food.id,
        foodName: food.name,
        quantity: currentRecognizedFood ? currentRecognizedFood.estimatedPortion : 100,
        quantityUnit: 'g',
        servings: servings
      }]
    });
    setMessage(`Food added to meal${weightInfo}`);

    // Clear current recognized food after adding
    setCurrentRecognizedFood(null);
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
            onChange={handleImageFileChange}
            style={{ marginTop: '8px', display: 'block' }}
          />

          {/* Image Preview */}
          {imagePreview && (
            <div style={{
              marginTop: '15px',
              textAlign: 'center',
              border: '2px solid #FF9800',
              borderRadius: '8px',
              padding: '10px',
              backgroundColor: 'white'
            }}>
              <img
                src={imagePreview}
                alt="Food preview"
                style={{
                  maxWidth: '100%',
                  maxHeight: '400px',
                  borderRadius: '8px',
                  boxShadow: '0 4px 8px rgba(0,0,0,0.1)'
                }}
              />
            </div>
          )}

          <button
            onClick={handleImageUpload}
            disabled={!imageFile || isAnalyzing}
            style={{
              backgroundColor: isAnalyzing ? '#ccc' : '#FF9800',
              marginTop: '10px',
              cursor: isAnalyzing ? 'not-allowed' : 'pointer',
              width: '100%',
              padding: '12px',
              fontSize: '16px',
              fontWeight: 'bold'
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
                          {food.estimatedPortion}g
                        </span>
                        <span>estimated weight</span>
                      </div>
                    </div>
                    <button
                      onClick={() => searchRecognizedFood(food)}
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
            <h4 style={{ marginBottom: '15px', color: '#333' }}>
              Search Results ({searchResults.length} items)
            </h4>
            {searchResults.map(food => (
              <div key={food.id} style={{
                padding: '16px',
                border: '1px solid #e0e0e0',
                marginBottom: '12px',
                borderRadius: '8px',
                backgroundColor: '#ffffff',
                boxShadow: '0 2px 4px rgba(0,0,0,0.08)',
                transition: 'box-shadow 0.2s',
                cursor: 'pointer'
              }}
              onMouseEnter={(e) => e.currentTarget.style.boxShadow = '0 4px 8px rgba(0,0,0,0.15)'}
              onMouseLeave={(e) => e.currentTarget.style.boxShadow = '0 2px 4px rgba(0,0,0,0.08)'}
              >
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                  <div style={{ flex: 1 }}>
                    {/* Food Name */}
                    <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '8px' }}>
                      <strong style={{ fontSize: '18px', color: '#2c3e50' }}>{food.name}</strong>
                      {food.brand && (
                        <span style={{
                          fontSize: '12px',
                          backgroundColor: '#3498db',
                          color: 'white',
                          padding: '2px 8px',
                          borderRadius: '12px',
                          fontWeight: 'bold'
                        }}>
                          {food.brand}
                        </span>
                      )}
                      <span style={{
                        fontSize: '11px',
                        backgroundColor: '#95a5a6',
                        color: 'white',
                        padding: '2px 6px',
                        borderRadius: '10px',
                        fontWeight: 'bold'
                      }}>
                        {food.source}
                      </span>
                    </div>

                    {/* Serving Size */}
                    <div style={{
                      fontSize: '14px',
                      color: '#7f8c8d',
                      marginBottom: '12px',
                      fontWeight: '500'
                    }}>
                      Nutrition per 100g
                    </div>

                    {/* Nutrition Info Grid */}
                    <div style={{
                      display: 'grid',
                      gridTemplateColumns: 'repeat(auto-fit, minmax(100px, 1fr))',
                      gap: '8px',
                      marginTop: '8px'
                    }}>
                      <div style={{
                        backgroundColor: '#fff3cd',
                        padding: '8px',
                        borderRadius: '6px',
                        textAlign: 'center',
                        border: '1px solid #ffc107'
                      }}>
                        <div style={{ fontSize: '18px', fontWeight: 'bold', color: '#ff6b6b' }}>
                          {Math.round(food.calories)}
                        </div>
                        <div style={{ fontSize: '11px', color: '#666', textTransform: 'uppercase' }}>
                          Calories
                        </div>
                      </div>

                      {food.protein > 0 && (
                        <div style={{
                          backgroundColor: '#d4edda',
                          padding: '8px',
                          borderRadius: '6px',
                          textAlign: 'center',
                          border: '1px solid #28a745'
                        }}>
                          <div style={{ fontSize: '16px', fontWeight: 'bold', color: '#28a745' }}>
                            {food.protein.toFixed(1)}g
                          </div>
                          <div style={{ fontSize: '11px', color: '#666', textTransform: 'uppercase' }}>
                            Protein
                          </div>
                        </div>
                      )}

                      {food.carbohydrates > 0 && (
                        <div style={{
                          backgroundColor: '#cfe2ff',
                          padding: '8px',
                          borderRadius: '6px',
                          textAlign: 'center',
                          border: '1px solid #0d6efd'
                        }}>
                          <div style={{ fontSize: '16px', fontWeight: 'bold', color: '#0d6efd' }}>
                            {food.carbohydrates.toFixed(1)}g
                          </div>
                          <div style={{ fontSize: '11px', color: '#666', textTransform: 'uppercase' }}>
                            Carbs
                          </div>
                        </div>
                      )}

                      {food.fat > 0 && (
                        <div style={{
                          backgroundColor: '#fff3e0',
                          padding: '8px',
                          borderRadius: '6px',
                          textAlign: 'center',
                          border: '1px solid #ff9800'
                        }}>
                          <div style={{ fontSize: '16px', fontWeight: 'bold', color: '#ff9800' }}>
                            {food.fat.toFixed(1)}g
                          </div>
                          <div style={{ fontSize: '11px', color: '#666', textTransform: 'uppercase' }}>
                            Fat
                          </div>
                        </div>
                      )}

                      {food.fiber > 0 && (
                        <div style={{
                          backgroundColor: '#f8d7da',
                          padding: '8px',
                          borderRadius: '6px',
                          textAlign: 'center',
                          border: '1px solid #dc3545'
                        }}>
                          <div style={{ fontSize: '16px', fontWeight: 'bold', color: '#dc3545' }}>
                            {food.fiber.toFixed(1)}g
                          </div>
                          <div style={{ fontSize: '11px', color: '#666', textTransform: 'uppercase' }}>
                            Fiber
                          </div>
                        </div>
                      )}

                      {food.sodium > 0 && (
                        <div style={{
                          backgroundColor: '#e7e7e7',
                          padding: '8px',
                          borderRadius: '6px',
                          textAlign: 'center',
                          border: '1px solid #6c757d'
                        }}>
                          <div style={{ fontSize: '16px', fontWeight: 'bold', color: '#6c757d' }}>
                            {Math.round(food.sodium)}mg
                          </div>
                          <div style={{ fontSize: '11px', color: '#666', textTransform: 'uppercase' }}>
                            Sodium
                          </div>
                        </div>
                      )}
                    </div>
                  </div>

                  {/* Add Button */}
                  <button
                    onClick={() => addFoodToMeal(food)}
                    style={{
                      backgroundColor: '#4CAF50',
                      padding: '10px 20px',
                      fontSize: '14px',
                      fontWeight: 'bold',
                      whiteSpace: 'nowrap',
                      marginLeft: '16px',
                      border: 'none',
                      borderRadius: '6px',
                      color: 'white',
                      cursor: 'pointer',
                      transition: 'all 0.2s'
                    }}
                    onMouseEnter={(e) => {
                      e.currentTarget.style.backgroundColor = '#45a049';
                      e.currentTarget.style.transform = 'scale(1.05)';
                    }}
                    onMouseLeave={(e) => {
                      e.currentTarget.style.backgroundColor = '#4CAF50';
                      e.currentTarget.style.transform = 'scale(1)';
                    }}
                  >
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
                  step="1"
                  value={food.quantity}
                  onChange={(e) => {
                    const newQuantity = parseFloat(e.target.value) || 0;
                    updateFoodQuantity(index, 'quantity', newQuantity);
                    updateFoodQuantity(index, 'servings', newQuantity / 100);
                  }}
                  placeholder="Weight in grams"
                  style={{ width: '120px' }}
                />
                <span>grams (= {food.servings.toFixed(2)} servings)</span>
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
